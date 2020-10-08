package acs.dal;

import org.springframework.data.repository.PagingAndSortingRepository;

import acs.data.ActionEntity;

//Create Read Update Delete - CRUD
public interface ActionDao extends PagingAndSortingRepository<ActionEntity, String> {

}
