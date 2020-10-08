package acs.rest.boundaries.element;

import javax.persistence.Embeddable;

/*
{
	"domain":"User",
	"id": "456"
}

*/
@Embeddable
public class ElementIdBoundary {
	private String domain;
	private String id;

	public ElementIdBoundary() {

	}

	public ElementIdBoundary(String domain, String id) {

		this.domain = domain;
		this.id = id;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return this.getDomain() + "#" + this.getId();
	}
}
