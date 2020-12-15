package pipeline;

import com.google.api.services.bigquery.model.TableRow;
import model.Person;
import model.PersonValidationError;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.WriteResult;
import org.apache.beam.sdk.io.xml.XmlIO;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The batch pipeline reads a nested Xml Schema, validate the records,
 * converts to BigQuery schema and load it to BigQuery. Failed records are also saved in BigQuery
 * in a dead letter table
 * <p>
 * usage:
 * mvn compile exec:java \
 * -Dexec.mainClass=pipeline.XmlPipelineWithDeadLetter \
 * -Dexec.args=" \
 * --sourcePath=$xmlPath \
 * --outputTableSpec=$project:dataset.table \
 * --deadLetterTableSpec=$project:dataset.table \
 * --project=$projectToRunDataflow \
 * --tempLocation=$gs://bucket \
 * --runner=$DirectRunnerOrDataflowRunner"
 */


public class XmlPipelineWithDeadLetter {

    private static final Logger LOG = LoggerFactory.getLogger(XmlPipelineWithDeadLetter.class);

    // main pipeline output to write Person objects to BQ
    public static final TupleTag<Person> MAIN_OUT = new TupleTag<Person>() {
    };

    // dead-letter pipeline output to write Person objects to BQ along with an error message i.e. Tuple<Person, String>
    public static final TupleTag<PersonValidationError> DEADLETTER_OUT = new TupleTag<PersonValidationError>() {
    };


    public interface Options extends DataflowPipelineOptions {
        @Description("XML source data path")
        @Validation.Required
        String getSourcePath();

        void setSourcePath(String sourcePath);

        @Description("Table spec to write the output to in the format project:dataset.table")
        String getOutputTableSpec();

        void setOutputTableSpec(String value);

        @Description("Table spec to write the dead letter output to in the format project:dataset.table")
        String getDeadLetterTableSpec();

        void setDeadLetterTableSpec(String value);
    }

    public static void main(String[] args) {

        PipelineOptionsFactory.register(Options.class);

        Options options =
                PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);

        run(options);
    }

    // encapsulate the pipeline logic here for easier unit testing
    public static PipelineResult run(Options options) {

        /**
         * 1) Read and Parse XML
         * 2) Validate that every Person has at least one address
         * 3) Write main output to BigQuery
         * 4) Write dead letter to BigQuery
         */

        Pipeline p = Pipeline.create(options);

        // 1&2 "Extract and Transform"
        PCollectionTuple results = p
                .apply("Extract", new PipelineDataReader(options))
                .apply("Transform", new PipelineDataTransformer(options));

        // 3&4 "Load"
        // write the main output to BQ
        WriteResult mainOutWriteResult = results.get(MAIN_OUT)
                .apply("Write MainOutput to BQ",
                        BigQueryIO.<Person>write()
                                .to(options.getOutputTableSpec())
                                .withFormatFunction(new FormatPersonAsTableRow())
                                .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_TRUNCATE)
                                .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_NEVER)
                                .withExtendedErrorInfo()
                );

        // write the main output to BQ
        WriteResult deadLetterWriteResult = results.get(DEADLETTER_OUT)
                .apply("Write DeadLetter to BQ",
                        BigQueryIO.<PersonValidationError>write()
                                .to(options.getDeadLetterTableSpec())
                                .withFormatFunction(new FormatFailedPersonAsTableRow())
                                .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_TRUNCATE)
                                .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_NEVER)
                                .withExtendedErrorInfo()
                );

        return p.run();
    }

    public static class PipelineDataReader
            extends PTransform<PBegin, PCollection<Person>> {

        private final Options options;

        public PipelineDataReader(Options options) {
            this.options = options;
        }

        @Override
        public PCollection<Person> expand(PBegin input) {

            return input.apply("Read XML",
                    XmlIO.<Person>read()
                            .from(options.getSourcePath())
                            .withRootElement("people")
                            .withRecordElement("person")
                            .withRecordClass(Person.class));
        }
    }

    /**
     * Encapsulate all pipeline transformations except for source and sink in
     * one unit to be able to unit test it
     */
    public static class PipelineDataTransformer
            extends PTransform<PCollection<Person>, PCollectionTuple> {

        private final Options options;

        public PipelineDataTransformer(Options options) {
            this.options = options;
        }

        @Override
        public PCollectionTuple expand(PCollection<Person> input) {

            return input
                    .apply("To UpperCase",
                            MapElements.via(new SimpleFunction<Person, Person>() {
                                @Override
                                public Person apply(Person input) {
                                    return new Person(input.name.toUpperCase(), input.id, input.addressList);
                                }
                            }))
                    .apply("Validate Records",
                            ParDo.of(new ValidatePerson())
                                    .withOutputTags(MAIN_OUT, TupleTagList.of(DEADLETTER_OUT)))
                    ;

            // add more transformations here and update the return type if needed
            // use return PCollectionTuple.of(..) to add multiple outputs
        }
    }

    public static class FormatPersonAsTableRow extends SimpleFunction<Person, TableRow> {
        @Override
        public TableRow apply(Person p) {
            return new TableRow()
                    .set("name", p.name)
                    .set("id", p.id)
                    .set("addresses", p.addressList);
        }
    }

    public static class FormatFailedPersonAsTableRow extends SimpleFunction<PersonValidationError, TableRow> {
        @Override
        public TableRow apply(PersonValidationError failedPerson) {
            return new TableRow()
                    .set("record", failedPerson.getPerson().toString())
                    .set("error", failedPerson.getError());
        }
    }

    static class ValidatePerson extends DoFn<Person, Person> {
        @ProcessElement
        public void processElement(ProcessContext context) {

            Person person = context.element();

            if (person.addressList == null || person.addressList.isEmpty()) {
                String errorMsg = "Person has no addresses. Added to error output";
                context.output(DEADLETTER_OUT, new PersonValidationError(person, errorMsg));
                LOG.info(errorMsg);
            } else {
                context.output(MAIN_OUT, person);
            }
        }
    }
}