package model;


import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;
import java.util.Objects;

@DefaultCoder(AvroCoder.class)
public class PersonValidationError {

    private Person person;
    private String error;


    // for avro coder
    public PersonValidationError() {
    }

    public PersonValidationError(Person person, String error) {
        this.person = person;
        this.error = error;
    }


    public Person getPerson() {
        return person;
    }

    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        return "PersonValidationError{" +
                "person=" + person +
                ", error='" + error + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonValidationError that = (PersonValidationError) o;
        return Objects.equals(person, that.person) &&
                Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(person, error);
    }
}
