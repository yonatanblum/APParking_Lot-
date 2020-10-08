package acs.rest.boundaries.user;
/*
	{
		"domain":"2020b.tamir.reznik",
		"email":"demo@us.er"
	}
 
 */

public class UserIdBoundary {
	private String domain;
	private String email;

	public UserIdBoundary() {
	}

	public UserIdBoundary(String domain, String email) {
		super();
		this.domain = domain;
		this.email = email;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String toString() {

		return this.domain + "#" + this.email;

	}

}
