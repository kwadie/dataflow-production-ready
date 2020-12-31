package model;

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
public class AddressTest {

    @Rule
    public final TestPipeline p = TestPipeline.create();

    @org.junit.Test
    public void testCoder() {

        // Arrange
        List<Address> lukeAddresses = new ArrayList<>();
        lukeAddresses.add(new Address("Tatooine", "home"));
        lukeAddresses.add(new Address("Lars Farm", "work"));
        lukeAddresses.add(new Address("Far Far Away", null));
        lukeAddresses.add(new Address(null, null));

        p.getCoderRegistry().registerCoderForClass(Address.class, AvroCoder.of(Address.class));

        // Act
        PCollection<Address> output = p.apply(Create.of(lukeAddresses));

        // Assert
        PAssert.that(output).containsInAnyOrder(lukeAddresses);

        p.run().waitUntilFinish();
    }

}

