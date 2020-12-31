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
public class XmlPipelineWithDeadLetterUnitTest {

    @Rule
    public final TestPipeline p = TestPipeline.create();

    /*
     Example test for a PTransform
    */
    @org.junit.Test
    public void testPipelineDataReader() {

        // Arrange

        List<Address> lukeAddresses = new ArrayList<>();
        lukeAddresses.add(new Address("Tatooine", "home"));
        lukeAddresses.add(new Address("Lars Farm", "work"));

        List<Person> expectedResults =
                ImmutableList.of(
                        new Person("Luke Skywalker", 1, lukeAddresses),
                        new Person("Leia Organa", 2, null)
                );

        XmlPipelineWithDeadLetter.Options options =
                PipelineOptionsFactory.create().as(XmlPipelineWithDeadLetter.Options.class);

        options.setSourcePath("src/test/resources/people.xml");

        // Act
        PCollection<Person> output = p.apply(new XmlPipelineWithDeadLetter.PipelineDataReader(options));

        // Assert
        PAssert.that(output).containsInAnyOrder(expectedResults);

        p.run().waitUntilFinish();
    }

    /*
     Example test for a DoFn XmlPipelineWithDeadLetter.ValidatePerson()
     */
    @org.junit.Test
    public void testValidatePerson() {

        // Arrange
        List<Address> lukeAddresses = new ArrayList<>();
        lukeAddresses.add(new Address("Tatooine", "home"));
        lukeAddresses.add(new Address("Lars Farm", "work"));

        Person luke = new Person("Luke Skywalker", 1, ImmutableList.of(
                new Address("Tatooine", "home"),
                new Address("Lars Farm", "work"))
        );

        Person leia = new Person("Leia Organa", 2, null);

        List<Person> input = ImmutableList.of(luke, leia);

        // records without addresses are filtered out
        List<Person> expectedMainOutput = ImmutableList.of(luke);

        List<PersonValidationError> expectedDeadLetterOutput = ImmutableList.of(
                new PersonValidationError(leia, "Person has no addresses. Added to error output"));

        p.getCoderRegistry().registerCoderForClass(Person.class, AvroCoder.of(Person.class));
        p.getCoderRegistry().registerCoderForClass(PersonValidationError.class, AvroCoder.of(PersonValidationError.class));

        // Act
        PCollectionTuple output = p.apply(Create.of(input))
                .apply(ParDo.of(new XmlPipelineWithDeadLetter.ValidatePerson())
                        .withOutputTags(XmlPipelineWithDeadLetter.MAIN_OUT,
                                TupleTagList.of(XmlPipelineWithDeadLetter.DEADLETTER_OUT))
                );

        // Assert
        PAssert.that(output.get(XmlPipelineWithDeadLetter.MAIN_OUT))
                .containsInAnyOrder(expectedMainOutput);
        PAssert.that(output.get(XmlPipelineWithDeadLetter.DEADLETTER_OUT))
                .containsInAnyOrder(expectedDeadLetterOutput);

        p.run().waitUntilFinish();
    }
}
