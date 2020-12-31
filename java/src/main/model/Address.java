package model;

import org.apache.avro.reflect.Nullable;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;
import javax.xml.bind.annotation.XmlAttribute;
import java.util.Objects;

@DefaultCoder(AvroCoder.class)
public class Address {

    @XmlAttribute(name = "address")
    @Nullable
    public String address = null;

    @XmlAttribute(name = "type")
    @Nullable
    public String type = null;

    public Address(String address, String type) {
        this.address = address;
        this.type = type;
    }

    public Address(){

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address1 = (Address) o;
        return Objects.equals(address, address1.address) &&
                Objects.equals(type, address1.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, type);
    }

    @Override
    public String toString() {
        return "Address{" +
                "address='" + address + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}