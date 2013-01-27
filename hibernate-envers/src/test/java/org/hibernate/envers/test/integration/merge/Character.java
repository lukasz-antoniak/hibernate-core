package org.hibernate.envers.test.integration.merge;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import org.hibernate.envers.Audited;

/**
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
@Entity
@Audited
public class Character implements Serializable {
	private Integer id;
	private String name;
	private List<Alias> aliases = new ArrayList<Alias>();

	public Character() {
	}

	public Character(Integer id, String name) {
		this.id = id;
		this.name = name;
	}

	@Id
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ManyToMany( cascade= CascadeType.ALL, mappedBy="characters" )
	public List<Alias> getAliases() {
		return aliases;
	}

	public void setAliases(List<Alias> aliases) {
		this.aliases = aliases;
	}

	public void associateAlias(Alias alias) {
		alias.getCharacters().add( this );
		getAliases().add( alias );
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) return true;
		if ( !( o instanceof Character) ) return false;

		Character character = (Character) o;

		if ( id != null ? !id.equals( character.id ) : character.id != null ) return false;
		if ( name != null ? !name.equals( character.name ) : character.name != null ) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + ( name != null ? name.hashCode() : 0 );
		return result;
	}

	@Override
	public String toString() {
		return "Character(id = " + id + ", name = " + name + ")";
	}
}