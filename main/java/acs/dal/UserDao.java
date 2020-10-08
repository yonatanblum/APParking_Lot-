package acs.dal;

import org.springframework.data.repository.PagingAndSortingRepository;

import acs.data.UserEntity;
import acs.data.UserIdEntity;


//Create Read Update Delete - CRUD
public interface UserDao extends  PagingAndSortingRepository<UserEntity, UserIdEntity>{

}
