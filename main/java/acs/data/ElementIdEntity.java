package acs.data;

import java.io.Serializable;
import javax.persistence.Embeddable;

@Embeddable
public class ElementIdEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String elementDomain;
	private String id;

	public ElementIdEntity() {
		super();
	}

	public ElementIdEntity(String domain, String id) {
		super();
		this.elementDomain = domain;
		this.id = id;
	}

	public String getElementDomain() {
		return elementDomain;
	}

	public void setElementDomain(String domain) {
		this.elementDomain = domain;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((elementDomain == null) ? 0 : elementDomain.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ElementIdEntity other = (ElementIdEntity) obj;
		if (elementDomain == null) {
			if (other.elementDomain != null)
				return false;
		} else if (!elementDomain.equals(other.elementDomain))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return this.getElementDomain() + "#" + this.getId();
	}
}
