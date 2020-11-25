package model;

import avro.shaded.com.google.common.collect.ImmutableList;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.values.PCollection;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class PersonTest {

    @Rule
    public final TestPipeline p = TestPipeline.create();

    @org.junit.Test
    public void testCoder() {

        // Arrange
        List<Address> lukeAddresses = new ArrayList<>();
        lukeAddresses.add(new Address("Tatooine", "home"));
        lukeAddresses.add(new Address("Lars Farm", "work"));

        List<Person> peopleList =
                ImmutableList.of(
                        new Person("Luke Skywalker",1, lukeAddresses),
                        new Person("Leia Organa",2, null)
                );

        p.getCoderRegistry().registerCoderForClass(Person.class, AvroCoder.of(Person.class));

        // Act
        PCollection<Person> output = p.apply(Create.of(peopleList));

        // Assert
        PAssert.that(output).containsInAnyOrder(peopleList);

        p.run().waitUntilFinish();
    }

}
