package org.hibernate.test.annotations.formula;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Formula;

/**
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
@Entity
@Table(name = "PEOPLE")
public class Person implements Serializable {
	@Id
	@GeneratedValue
	private long id;

	@Column(name = "FIRST_NAME")
	private String firstName;

	@Column(name = "LAST_NAME")
	private String lastName;

	@Formula("FIRST_NAME || ' ' || LAST_NAME")
	private String fullName;

	public Person() {
	}

	public Person(String firstName, String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public Person(long id, String firstName, String lastName) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) return true;
		if ( !( o instanceof Person ) ) return false;

		Person person = (Person) o;

		if ( id != person.id ) return false;
		if ( firstName != null ? !firstName.equals( person.firstName ) : person.firstName != null ) return false;
		if ( lastName != null ? !lastName.equals( person.lastName ) : person.lastName != null ) return false;
		if ( fullName != null ? !fullName.equals( person.fullName ) : person.fullName != null ) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = (int) ( id ^ ( id >>> 32 ) );
		result = 31 * result + ( firstName != null ? firstName.hashCode() : 0 );
		result = 31 * result + ( lastName != null ? lastName.hashCode() : 0 );
		result = 31 * result + ( fullName != null ? fullName.hashCode() : 0 );
		return result;
	}

	@Override
	public String toString() {
		return "Person(id = " + id + ", firstName = " + firstName + ", lastName = " + lastName + ", fullName = " + fullName + ")";
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
}
