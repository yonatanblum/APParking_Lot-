package acs.logic.implementation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import acs.data.Converter;
import acs.data.UserEntity;
import acs.logic.ObjectNotFoundException;
import acs.logic.UserService;
import acs.rest.boundaries.user.UserBoundary;

//@Service
public class UserServiceImplementation implements UserService {

	private String projectName;
	private Map<String, UserEntity> usersDatabase;
	private Converter converter;

	@Autowired
	public UserServiceImplementation(Converter converter) {
		this.converter = converter;
	}

	// injection of project name from the spring boot configuration
	@Value("${spring.application.name: generic}")
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	@PostConstruct
	public void init() {
		// since this class is a singleton, we generate a thread safe collection
		this.usersDatabase = Collections.synchronizedMap(new TreeMap<>());
	}

	public UserBoundary createUser(UserBoundary user) {

		user.getUserId().setDomain(projectName);

		UserEntity entity = this.converter.toEntity(user);
		// this.usersDatabase.put(entity.getUserId(), entity);
		return this.converter.fromEntity(entity);
	}

	@Override
	public UserBoundary login(String userDomain, String userEmail) {
		UserEntity existing = this.usersDatabase.get(userDomain + "#" + userEmail);
		if (existing != null) {
			return this.converter.fromEntity(existing);
		} else {
			throw new ObjectNotFoundException(
					"could not find object by UserDomain: " + userDomain + "or userEmail: " + userEmail);
		}

	}

	@Override
	public UserBoundary updateUser(String userDomain, String userEmail, UserBoundary update) {

		if (userDomain != null && !userDomain.trim().isEmpty() && userEmail != null && !userEmail.trim().isEmpty()) {

			UserEntity existing = this.usersDatabase.get(userDomain + "#" + userEmail);

			if (existing == null) {
				throw new ObjectNotFoundException(
						"could not find object by UserDomain: " + userDomain + "or userEmail: " + userEmail);
			}

			boolean dirty = false;

			if (update.getRole() != null) {
				existing.setRole(this.converter.toEntity(update.getRole()));
				dirty = true;
			}

			if (update.getUsername() != null) {
				existing.setUsername(update.getUsername());
				dirty = true;
			}

			if (update.getAvatar() != null) {
				existing.setAvatar(update.getAvatar());
				dirty = true;
			}

			if (dirty) {
				this.usersDatabase.put(userDomain + "#" + userEmail, existing);
			}

			return this.converter.fromEntity(existing);
		} else {
			throw new RuntimeException("User Domain and User Email must not be empty or null");

		}
	}

	@Override
	public List<UserBoundary> getAllUsers(String adminDomain, String adminEmail) {
		if (adminDomain != null && !adminDomain.trim().isEmpty() && adminEmail != null
				&& !adminEmail.trim().isEmpty()) {
			return this.usersDatabase // Map<String, UserEntity>
					.values() // Collection<UserEntity>
					.stream() // Stream<UserEntity>
					.map(e -> this.converter.fromEntity(e)) // Stream<UserBoundary>
					.collect(Collectors.toList()); // List<UserBoundary>
		} else {
			throw new RuntimeException("Admin Domain and Admin Email must not be empty or null");

		}
	}

	@Override
	public void deleteAllUsers(String adminDomain, String adminEmail) {
		if (adminDomain != null && !adminDomain.trim().isEmpty() && adminEmail != null
				&& !adminEmail.trim().isEmpty()) {
			this.usersDatabase.clear();
		} else {
			throw new RuntimeException("Admin Domain and Admin Email must not be empty or null");
		}
	}

}
