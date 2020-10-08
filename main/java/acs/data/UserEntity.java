package acs.data;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Entity
@Table(name = "USERS")
public class UserEntity {

	private UserIdEntity userId;
	private UserRoleEntityEnum role;
	private String username;
	private String avatar;

	public UserEntity() {
	}

	public UserEntity(UserIdEntity userId, UserRoleEntityEnum role, String username, String avatar) {
		super();
		this.userId = userId;
		this.role = role;
		this.username = username;
		this.avatar = avatar;
	}

	@EmbeddedId
	public UserIdEntity getUserId() {
		return userId;
	}

	public void setUserId(UserIdEntity userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Enumerated(EnumType.STRING)
	public UserRoleEntityEnum getRole() {
		return role;
	}

	public void setRole(UserRoleEntityEnum role) {
		this.role = role;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

}
