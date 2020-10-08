package acs;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.PostConstruct;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.web.client.RestTemplate;
import acs.data.UserRole;
import acs.rest.boundaries.user.NewUserDetailsBoundary;
import acs.rest.boundaries.user.UserBoundary;
import acs.rest.boundaries.user.UserIdBoundary;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class UserTests {
	private RestTemplate restTemplate;
	private String url;
	private int port;

	@LocalServerPort
	public void setPort(int port) {
		this.port = port;
	}

	@PostConstruct
	public void init() {
		this.restTemplate = new RestTemplate();
		this.url = "http://localhost:" + this.port + "/acs";
	}

	@AfterEach
	public void teardown() {
		UserBoundary userAdmin = this.restTemplate.postForObject(this.url + "/users",
				new NewUserDetailsBoundary("sapir@gmail.com", UserRole.ADMIN, "sapir", "???"), UserBoundary.class);

		this.restTemplate.delete(this.url + "/admin/users/{adminDomain}/{adminEmail}",
				userAdmin.getUserId().getDomain(), userAdmin.getUserId().getEmail());
	}

	@Test
	public void testContext() {

	}

	@Test
	public void test_Post_Create_New_User_Then_The_Database_Contains_Same_User_With_Same_Id() throws Exception {
		// GIVEN the server is up
		// do nothing

		// WHEN I POST new user
		UserIdBoundary postedUserId = this.restTemplate.postForObject(this.url + "/users",
				new NewUserDetailsBoundary("Sapir@gmail.com", UserRole.PLAYER, "sapir", ":-)"), UserBoundary.class)
				.getUserId();

		// split the userId to Domain and Email
		String userDomain = postedUserId.getDomain();
		String userEmail = postedUserId.getEmail();

		// THEN the database contains a single user with same userId as posted
		UserIdBoundary actualUserId = this.restTemplate.getForObject(this.url + "/users/login/{userDomain}/{userEmail}",
				UserBoundary.class, userDomain, userEmail).getUserId();

		assertThat(actualUserId).extracting("domain", "email").usingRecursiveFieldByFieldElementComparator()
				.containsExactly(userDomain, userEmail);

		// assertThat(actualUserId).isNotNull().isEqualTo(postedUserId);

	}

	@Test
	public void test_Post_New_User_Then_The_Database_Contains_A_Single_User() throws Exception {
		// GIVEN the server is up
		// do nothing

		// WHEN I POST new user
		UserBoundary actualUserBoundary = this.restTemplate.postForObject(this.url + "/users",
				new NewUserDetailsBoundary("Sapir@gmail.com", UserRole.ADMIN, "sapir", ":-)"), UserBoundary.class);

		// THEN the database contains a single message
		assertThat(this.restTemplate.getForObject(
				this.url + "/admin/users/" + actualUserBoundary.getUserId().getDomain() + "/"
						+ actualUserBoundary.getUserId().getEmail(),
				UserBoundary[].class, actualUserBoundary.getUserId().getDomain(),
				actualUserBoundary.getUserId().getEmail())).hasSize(1);
	}

	@Test
	public void test_Post_New_User_Then_The_Database_Contains_User_With_The_Same_User_Attribute_Role()
			throws Exception {
		// GIVEN the server is up
		// do nothing

		// WHEN I POST new user with user role attribute: "PLAYER"

		UserBoundary actualUserBoundary = this.restTemplate.postForObject(this.url + "/users",
				new NewUserDetailsBoundary("sapir@gmail.com", UserRole.ADMIN, "???", "???"), UserBoundary.class);

		// THEN the database contains a user with user role attribute "ADMIN"
		UserBoundary[] allUsers = this.restTemplate.getForObject(this.url + "/admin/users/{adminDomain}/{adminEmail}",
				UserBoundary[].class, actualUserBoundary.getUserId().getDomain(),
				actualUserBoundary.getUserId().getEmail());
		boolean containsPlayerRole = false;
		for (UserBoundary m : allUsers) {
			if (m.getRole().equals(UserRole.ADMIN)) {
				containsPlayerRole = true;
			}
		}

		if (!containsPlayerRole) {
			throw new Exception("failed to locate user with proper attribute role");
		}

	}

	@Test
	public void test_Post_New_User_Then_The_Database_Contains_User_With_The_Same_User_Attribute_UserName()
			throws Exception {
		// GIVEN the server is up
		// do nothing

		// WHEN I POST new user with userName attribute: "anna"

		this.restTemplate.postForObject(this.url + "/users",
				new NewUserDetailsBoundary("anna@gmail.com", UserRole.PLAYER, "anna", "???"), UserBoundary.class);

		// crate user ADMIN
		UserBoundary userAdmin = this.restTemplate.postForObject(this.url + "/users",
				new NewUserDetailsBoundary("sapir@gmail.com", UserRole.ADMIN, "sapir", "???"), UserBoundary.class);

		// THEN the database contains a user with userName attribute "anna"
		UserBoundary[] allUsers = this.restTemplate.getForObject(this.url + "/admin/users/{adminDomain}/{adminEmail}",
				UserBoundary[].class, userAdmin.getUserId().getDomain(), userAdmin.getUserId().getEmail());

		boolean containsUserNameAnna = false;
		for (UserBoundary m : allUsers) {
			if (m.getUsername().equals("anna")) {
				containsUserNameAnna = true;
			}
		}

		if (!containsUserNameAnna) {
			throw new Exception("failed to locate user with proper attribute user name");
		}

	}

	@Test
	public void test_Post_New_User_Then_The_Database_Contains_User_With_The_Same_User_Attribute_Avatar()
			throws Exception {
		// GIVEN the server is up
		// do nothing

		// WHEN I POST new user with avatar attribute: ":-))"

		UserBoundary actualUser = this.restTemplate.postForObject(this.url + "/users",
				new NewUserDetailsBoundary("sapir@gmail.com", UserRole.ADMIN, "???", ":-))"), UserBoundary.class);

		// THEN the database contains a user with user avatar attribute ":-))"
		UserBoundary[] allUsers = this.restTemplate.getForObject(this.url + "/admin/users/{adminDomain}/{adminEmail}",
				UserBoundary[].class, actualUser.getUserId().getDomain(), actualUser.getUserId().getEmail());
		boolean containsAvatarAttribute = false;
		for (UserBoundary m : allUsers) {
			if (m.getAvatar().equals(":-))")) {
				containsAvatarAttribute = true;
			}
		}

		if (!containsAvatarAttribute) {
			throw new Exception("failed to locate user with proper attribute avatar");
		}

	}

	@Test
	public void test_Put_Update_User_Attribute_Role_To_MANAGER_Then_Role_Is_Updated_In_The_DataBase() throws Exception {
		// GIVEN the server is up
		// do nothing

		// AND the database contains a single user with role: PLAYER
		UserBoundary boundaryOnServer = this.restTemplate.postForObject(this.url + "/users",
				new NewUserDetailsBoundary("sapir@gmail.com", UserRole.PLAYER, "sapir", ":-))"), UserBoundary.class);

		UserIdBoundary postedUserId = boundaryOnServer.getUserId();
		String userDomain = postedUserId.getDomain();
		String userEmail = postedUserId.getEmail();

		// WHEN I PUT with update of role to be "MANAGER"
		UserBoundary update = new UserBoundary();
		update.setRole(UserRole.MANAGER);
		this.restTemplate.put(this.url + "/users/{userDomain}/{userEmail}", update, userDomain, userEmail);

		// THEN the database contains a user with same id and role: MANAGER
		assertThat(this.restTemplate.getForObject(this.url + "/users/login/{userDomain}/{userEmail}",
				UserBoundary.class, userDomain, userEmail)).extracting("userId", "role")
						.usingRecursiveFieldByFieldElementComparator()
						.containsExactly(boundaryOnServer.getUserId(), update.getRole());
	}

	@Test
	public void test_Put_Update_User_Attribute_Avatar_Then_Avatar_Is_Updated_In_The_DataBase() throws Exception {
		// GIVEN the server is up
		// do nothing

		// AND the database contains a single user with role: PLAYER
		UserBoundary boundaryOnServer = this.restTemplate.postForObject(this.url + "/users",
				new NewUserDetailsBoundary("sapir@gmail.com", UserRole.PLAYER, "sapir", ":-))"), UserBoundary.class);

		UserIdBoundary postedUserId = boundaryOnServer.getUserId();
		String userDomain = postedUserId.getDomain();
		String userEmail = postedUserId.getEmail();

		// WHEN I PUT with update of Avatar from ":-)" to be ";)"
		UserBoundary update = new UserBoundary();
		update.setAvatar(";)");
		this.restTemplate.put(this.url + "/users/{userDomain}/{userEmail}", update, userDomain, userEmail);

		// THEN the database contains a user with same id and Avatar: ";)"
		assertThat(this.restTemplate.getForObject(this.url + "/users/login/{userDomain}/{userEmail}",
				UserBoundary.class, userDomain, userEmail)).extracting("userId", "avatar")
						.usingRecursiveFieldByFieldElementComparator()
						.containsExactly(boundaryOnServer.getUserId(), update.getAvatar());
	}

	@Test
	public void test_Init_Server_With_3_Users_When_We_Get_All_Users_We_Receive_The_Same_Users_Initialized()
			throws Exception {
		// GIVEN the server is up
		// AND the server contains 3 users

		List<UserBoundary> allUsersInDb = IntStream.range(1, 4).mapToObj(i -> ("email" + i + "@gmail.com"))
				.map(email -> new NewUserDetailsBoundary(email, UserRole.PLAYER, "sapir", ":-)"))
				.map(boundary -> this.restTemplate.postForObject(this.url + "/users", boundary, UserBoundary.class))
				.collect(Collectors.toList());

		UserBoundary userAdmin = this.restTemplate.postForObject(this.url + "/users",
				new NewUserDetailsBoundary("sapir@gmail.com", UserRole.ADMIN, "sapir", "???"), UserBoundary.class);

		allUsersInDb.add(userAdmin);

		for (UserBoundary userBoundary : allUsersInDb) {
			System.out.println("before" + userBoundary);
		}

		// WHEN I GET /admin/users/{adminDomain}/{adminEmail}
		UserBoundary[] allUsers = this.restTemplate.getForObject(this.url + "/admin/users/{adminDomain}/{adminEmail}",
				UserBoundary[].class, userAdmin.getUserId().getDomain(), userAdmin.getUserId().getEmail());

		for (UserBoundary userBoundary : allUsers) {
			System.out.println("after" + userBoundary);
		}

		// THEN The server returns the same 3 users initialized
		assertThat(allUsers).hasSize(allUsersInDb.size()).usingRecursiveFieldByFieldElementComparator()
				.containsExactlyInAnyOrderElementsOf(allUsersInDb);
	}

	// Maybe this test is not Relevant need to check - anna
	// it was before to check if the db is empty after deleting, but now we cant
	// check that ceacuse we must have a "admin" user
	@Test
	public void test_Init_Server_With_4_Users_When_We_Delete_all_Users_And_Than_Get_All_Users_We_Receive_an_array_with_size_1()
			throws Exception {
		// GIVEN the server is up
		// AND the server contains 3 users
		List<UserBoundary> allUsersInDb = IntStream.range(1, 4).mapToObj(i -> ("email" + i + "@gmail.com"))
				.map(email -> new NewUserDetailsBoundary(email, UserRole.PLAYER, "myusername", ":-)"))
				.map(boundary -> this.restTemplate.postForObject(this.url + "/users", boundary, UserBoundary.class))
				.collect(Collectors.toList());

		// create user Admin
		UserBoundary userAdmin = this.restTemplate.postForObject(this.url + "/users",
				new NewUserDetailsBoundary("sapir@gmail.com", UserRole.ADMIN, "sapir", "???"), UserBoundary.class);

		allUsersInDb.add(userAdmin);
		// AND I delete all users
		this.restTemplate.delete(this.url + "/admin/users/" + userAdmin.getUserId().getDomain() + "/"
				+ userAdmin.getUserId().getEmail());

		UserBoundary userAdmin1 = this.restTemplate.postForObject(this.url + "/users",
				new NewUserDetailsBoundary("sapir@gmail.com", UserRole.ADMIN, "sapir", "???"), UserBoundary.class);
		allUsersInDb.add(userAdmin1);
		// WHEN I GET for all users
		UserBoundary[] actual = this.restTemplate.getForObject(this.url + "/admin/users/{adminDomain}/{adminEmail}",
				UserBoundary[].class, userAdmin1.getUserId().getDomain(), userAdmin1.getUserId().getEmail());

		// THEN the returned value is an empty array
		assertThat(actual).hasSize(1);
	}

}
