package model;

import org.apache.avro.reflect.Nullable;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This JAXB annotated class matches an XML file in the following format
 * <people>
 *     <person id="1">
 *         <name>..</name>
 *         <addresses>
 *             <address type=".." address=".." />
 *             <address type=".." address=".." />
 *         </addresses>
 *     </person>
 *     <person id="..">
 *         <name>..</name>
 *     </person>
 *     ... etc
 * </people>
 */

@XmlRootElement
@DefaultCoder(AvroCoder.class)
public class Person {

    @Nullable
    public String name = null;

    @XmlAttribute(name = "id")
    @Nullable
    public int id = -1;

    // XmLElementWrapper generates a wrapper element around XML representation
    @XmlElementWrapper(name = "addresses")
    // XmlElement sets the name of the entities
    @XmlElement(name = "address")
    @Nullable
    public List<Address> addressList;


    // empty constructor used by JAXB marshaling
    public Person() {
        addressList = new ArrayList<>();
    }

    public Person(String name, int id, List<Address> addressList) {
        this.name = name;
        this.id = id;
        this.addressList = addressList;
    }

    // override and implement equals, hashCode and toString for unit testing to work
    // could be generated by the IDE

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return Objects.equals(name, person.name) &&
                Objects.equals(id, person.id) &&
                Objects.equals(addressList, person.addressList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id, addressList);
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", addressList=" + addressList +
                '}';
    }
}