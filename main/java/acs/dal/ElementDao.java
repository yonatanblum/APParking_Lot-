package acs.dal;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import acs.data.ElementEntity;
import acs.data.ElementIdEntity;

//Create Read Update Delete - CRUD
public interface ElementDao extends PagingAndSortingRepository<ElementEntity, ElementIdEntity> {
	// CrudRepository<ElementEntity, ElementIdEntity>

	public List<ElementEntity> findAllByParent_ElementId_IdAndParent_ElementId_ElementDomain(@Param("id") String id,
			@Param("elementDomain") String domain, Pageable pageable);

	public List<ElementEntity> findAllByParent_ElementId_IdAndParent_ElementId_ElementDomainAndActive(
			@Param("id") String id, @Param("elementDomain") String domain, @Param("active") boolean active,
			Pageable pageable);

	public List<ElementEntity> findAllByLocation_LatBetweenAndLocation_LngBetweenAndActive(
			@Param("minLat") Double minLat, @Param("maxLat") Double maxLat, @Param("minLng") Double minLng,
			@Param("maxLng") Double maxLng, @Param("active") Boolean active, Pageable pageable);

	public List<ElementEntity> findAllByLocation_LatBetweenAndLocation_LngBetween(@Param("minLat") double minLat,
			@Param("maxLat") double maxLat, @Param("minLng") double minLng, @Param("maxLng") double maxLng,
			Pageable pageable);

	public List<ElementEntity> findAllByLocation_LatBetweenAndLocation_LngBetweenAndType(@Param("minLat") double minLat,
			@Param("maxLat") double maxLat, @Param("minLng") double minLng, @Param("maxLng") double maxLng,
			@Param("type") String type, Pageable pageable);

	public List<ElementEntity> findAllByLocation_LatBetweenAndLocation_LngBetweenAndActiveAndType(
			@Param("minLat") Double minLat, @Param("maxLat") Double maxLat, @Param("minLng") Double minLng,
			@Param("maxLng") Double maxLng, @Param("active") Boolean active, @Param("type") String type,
			Pageable pageable);

	public List<ElementEntity> findAllByNameAndActive(@Param("name") String name, @Param("active") Boolean active,
			Pageable pageable);

	public List<ElementEntity> findAllByName(@Param("name") String name, Pageable pageable);

	// SELECT ... FROM ELEMENTS WHERE ACTIVE=?
	public List<ElementEntity> findAllByActive(@Param("active") boolean active, Pageable pageable);

	public List<ElementEntity> findAllByType(@Param("type") String type, Pageable pageable);

	public List<ElementEntity> findAllByTypeAndActive(@Param("type") String type, @Param("active") boolean active,
			Pageable pageable);

}
