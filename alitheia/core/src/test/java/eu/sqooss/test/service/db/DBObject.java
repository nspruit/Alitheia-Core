package eu.sqooss.test.service.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import eu.sqooss.service.db.DAObject;

@Entity
@Table(name="DBOBJECT")
public class DBObject extends DAObject {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="OBJECT_ID")
	private long id; 

	@Column(name="OBJECT_NAME")
	private String name;

	// Required for Hibernate
	public DBObject() { }
	
	public DBObject(String name) {
		this.name = name;
	}
	
	@Override
	public long getId() {
		return id;
	}

	@Override
	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DBObject))
			return false;
		
		DBObject other = (DBObject)obj;
		
		if (other.getId() != getId())
			return false;
		if (other.getName() == getName())
			return true;
		return other.getName() != null && other.getName().equals(this.getName());
	}
	
}
