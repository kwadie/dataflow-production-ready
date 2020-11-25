package pipeline;

import avro.shaded.com.google.common.collect.ImmutableList;
import model.Address;
import model.Person;
import model.PersonValidationError;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.apache.beam.sdk.values.TupleTag;
import org.apache.beam.sdk.values.TupleTagList;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class XmlPipelineWithDeadLetterTransformIntegrationTest {

    @Rule
    public final TestPipeline p = TestPipeline.create();

    // main pipeline output to write Person objects to BQ
    public static final TupleTag<Person> MAIN_OUT = new TupleTag<Person>() {
    };

    // dead-letter pipeline output to write Person objects to BQ along with an error message i.e. Tuple<Person, String>
    public static final TupleTag<PersonValidationError> DEADLETTER_OUT = new TupleTag<PersonValidationError>() {
    };


    /*
    Example test for full PTransform consisting of multiple DoFn (Transform integration testing)
     */
    @org.junit.Test
    public void testPipelineDataTransformer() {

        // Arrange
        List<Address> lukeAddresses = new ArrayList<>();
        lukeAddresses.add(new Address("Tatooine", "home"));
        lukeAddresses.add(new Address("Lars Farm", "work"));

        Person luke = new Person("Luke Skywalker", 1, lukeAddresses);

        Person leia = new Person("Leia Organa", 2, null);

        List<Person> input = ImmutableList.of(luke, leia);

        // records without addresses are filtered out
        List<Person> expectedMainOutput = ImmutableList.of(
                new Person("LUKE SKYWALKER", 1, lukeAddresses)
        );

        List<PersonValidationError> expectedDeadLetterOutput = ImmutableList.of(
                new PersonValidationError(new Person("LEIA ORGANA", 2, null),
                        "Person has no addresses. Added to error output"));

        p.getCoderRegistry().registerCoderForClass(Person.class, AvroCoder.of(Person.class));
        p.getCoderRegistry().registerCoderForClass(PersonValidationError.class, AvroCoder.of(PersonValidationError.class));

        // Act
        PCollectionTuple output = p.apply(Create.of(input))
                .apply(new XmlPipelineWithDeadLetter.PipelineDataTransformer(null));

        // Assert
        PAssert.that(output.get(XmlPipelineWithDeadLetter.MAIN_OUT))
                .containsInAnyOrder(expectedMainOutput);
        PAssert.that(output.get(XmlPipelineWithDeadLetter.DEADLETTER_OUT))
                .containsInAnyOrder(expectedDeadLetterOutput);

        p.run().waitUntilFinish();
    }

}
