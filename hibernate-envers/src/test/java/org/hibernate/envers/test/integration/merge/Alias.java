package org.hibernate.envers.test.integration.merge;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.hibernate.envers.Audited;

/**
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
@Entity
@Audited
public class Alias implements Serializable {
	private Integer id;
	private String alias;
	private List<Character> characters = new ArrayList<Character>();

	public Alias() {
	}

	public Alias(Integer id, String alias) {
		this.id = id;
		this.alias = alias;
	}

	@Id
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	@ManyToMany( cascade = CascadeType.ALL )
	@JoinTable( name = "CHARACTER_ALIAS" )
	public List<Character> getCharacters() {
		return characters;
	}

	public void setCharacters(List<Character> characters) {
		this.characters = characters;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) return true;
		if ( !( o instanceof Alias) ) return false;

		Alias alias1 = (Alias) o;

		if ( alias != null ? !alias.equals( alias1.alias ) : alias1.alias != null ) return false;
		if ( id != null ? !id.equals( alias1.id ) : alias1.id != null ) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + ( alias != null ? alias.hashCode() : 0 );
		return result;
	}

	@Override
	public String toString() {
		return "Alias(id = " + id + ", alias = " + alias + ")";
	}
}