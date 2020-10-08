package acs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.PostConstruct;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import acs.data.UserRole;
import acs.rest.boundaries.action.ActionBoundary;
import acs.rest.boundaries.action.ActionIdBoundary;
import acs.rest.boundaries.action.ElementOfAction;
import acs.rest.boundaries.action.InvokingUser;
import acs.rest.boundaries.element.ElementBoundary;
import acs.rest.boundaries.element.ElementIdBoundary;
import acs.rest.boundaries.element.Location;
import acs.rest.boundaries.user.NewUserDetailsBoundary;
import acs.rest.boundaries.user.UserBoundary;
import acs.rest.boundaries.user.UserIdBoundary;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ActionTests {

	private RestTemplate restTemplate;
	private String actionPostUrl, userPostUrl, elementPostUrl;
	private int port;
	private String delete_And_Get_Url;
	UserBoundary managerUser, playerUser, adminUser;
	ElementBoundary activeElement, inActiveElement;

	@LocalServerPort
	public void setPort(int port) {
		this.port = port;
	}

	@PostConstruct
	public void init() {
		this.restTemplate = new RestTemplate();
		this.actionPostUrl = "http://localhost:" + this.port + "/acs/actions";
		this.elementPostUrl = "http://localhost:" + this.port + "/acs/elements/";
		this.userPostUrl = "http://localhost:" + this.port + "/acs/users";
		this.delete_And_Get_Url = "http://localhost:" + this.port + "/acs/admin/actions/";

	}

	@AfterEach
	public void tear_down() {
		this.restTemplate.delete(delete_And_Get_Url);
	}

	@BeforeEach
	public void setup() {
		create_Admin_Manager_Player_Users();
		this.delete_And_Get_Url += this.adminUser.getUserId().getDomain() + "/" + this.adminUser.getUserId().getEmail();
		create_Active_And_InActive_Elements();
	}

	public void create_Admin_Manager_Player_Users() {

		NewUserDetailsBoundary user = new NewUserDetailsBoundary("manager@gmail.com", UserRole.MANAGER, "managerTamir",
				":)");
		this.managerUser = this.restTemplate.postForObject(userPostUrl, user, UserBoundary.class);

		user = new NewUserDetailsBoundary("player@gmail.com", UserRole.PLAYER, "playerTamir", ":)");

		this.playerUser = this.restTemplate.postForObject(userPostUrl, user, UserBoundary.class);

		user = new NewUserDetailsBoundary("admin@gmail.com", UserRole.ADMIN, "adminTamir", ":)");

		this.adminUser = this.restTemplate.postForObject(userPostUrl, user, UserBoundary.class);

	}

	public void create_Active_And_InActive_Elements() {
		Map<String, UserIdBoundary> createBy = new HashMap<>();
		createBy.put("userId",
				new UserIdBoundary(this.managerUser.getUserId().getDomain(), this.managerUser.getUserId().getEmail()));

		this.activeElement = new ElementBoundary(new ElementIdBoundary("2020b.tamir.reznik", "random"), "car", "tamir",
				true, new Date(), new Location(30.1, 42.4), null, createBy);

		activeElement = this.restTemplate.postForObject(this.elementPostUrl + this.managerUser.getUserId().getDomain()
				+ "/" + this.managerUser.getUserId().getEmail(), this.activeElement, ElementBoundary.class);

		this.inActiveElement = new ElementBoundary(new ElementIdBoundary("2020b.tamir.reznik", "random"), "uniq",
				"tamir", false, new Date(), new Location(30.1, 42.4), null, createBy);

		this.inActiveElement = this.restTemplate.postForObject(this.elementPostUrl
				+ this.managerUser.getUserId().getDomain() + "/" + this.managerUser.getUserId().getEmail(),
				this.inActiveElement, ElementBoundary.class);

	}

//Test for POST + GET + Service reliability
	@Test
	public void test_Init_Server_with_5_Actions_When_We_Get_All_Actions_We_Receive_The_Same_Actions() {

		InvokingUser invokedBy = new InvokingUser();
		invokedBy.setUserId(
				new UserIdBoundary(this.playerUser.getUserId().getDomain(), this.playerUser.getUserId().getEmail()));

		List<Object> actionList = IntStream.range(0, 5)
				.mapToObj(i -> new ActionBoundary(
						new ActionIdBoundary("2020b.tamir.reznik", "random" + Integer.toString(i)),
						"actionType" + Integer.toString(i), new ElementOfAction(this.activeElement.getElementId()),
						new Date(), invokedBy, new HashMap<>()))
				.map(boundary -> this.restTemplate.postForObject(actionPostUrl, boundary, ActionBoundary.class))
				.collect(Collectors.toList());

		Object[] results = this.restTemplate.getForObject(this.delete_And_Get_Url, ActionBoundary[].class,
				adminUser.getUserId().getDomain(), adminUser.getUserId().getEmail());

		assertThat(results).hasSize(actionList.size()).usingRecursiveFieldByFieldElementComparator()
				.containsExactlyInAnyOrderElementsOf(actionList);

		this.restTemplate.delete(delete_And_Get_Url, "adminDomain", "adminEmail");
		results = this.restTemplate.getForObject(this.delete_And_Get_Url, ActionBoundary[].class,
				adminUser.getUserId().getDomain(), adminUser.getUserId().getEmail());
		assertThat(results).isEmpty();

	}

	@Test
	public void test_Post_Action_Via_Manager_User_And_Get_UnauthorizedException() {

		ActionBoundary boundary = new ActionBoundary(new ActionIdBoundary("2020b.tamir.reznik", "random"), "actionType",
				new ElementOfAction(this.activeElement.getElementId()), new Date(),
				new InvokingUser(this.managerUser.getUserId()), new HashMap<>());

		assertThrows(HttpClientErrorException.Unauthorized.class, () -> this.restTemplate
				.postForObject("http://localhost:" + this.port + "/acs/actions", boundary, ActionBoundary.class));

	}

	@Test
	public void test_Post_Action_Via_Admin_User_And_Get_Unauthorized_Exception() {

		ActionBoundary boundary = new ActionBoundary(new ActionIdBoundary("2020b.tamir.reznik", "random"), "actionType",
				new ElementOfAction(this.activeElement.getElementId()), new Date(),
				new InvokingUser(this.adminUser.getUserId()), new HashMap<>());

		assertThrows(HttpClientErrorException.Unauthorized.class, () -> this.restTemplate
				.postForObject("http://localhost:" + this.port + "/acs/actions", boundary, ActionBoundary.class));

	}

	@Test
	public void test_Init_Server_with_5_Actions_Contains_InActive_Elements_When_We_Get_All_Actions_We_Receive_NotFound_Exception() {

		InvokingUser invokedBy = new InvokingUser();
		invokedBy.setUserId(
				new UserIdBoundary(this.playerUser.getUserId().getDomain(), this.playerUser.getUserId().getEmail()));

		assertThrows(HttpClientErrorException.NotFound.class, () -> IntStream.range(0, 5)
				.mapToObj(i -> new ActionBoundary(
						new ActionIdBoundary("2020b.tamir.reznik", "random" + Integer.toString(i)),
						"actionType" + Integer.toString(i), new ElementOfAction(this.inActiveElement.getElementId()),
						new Date(), invokedBy, new HashMap<>()))
				.map(boundary -> this.restTemplate.postForObject(actionPostUrl, boundary, ActionBoundary.class))
				.collect(Collectors.toList()));

	}
}
