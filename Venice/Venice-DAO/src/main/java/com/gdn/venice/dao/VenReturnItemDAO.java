package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.VenReturItem;

public interface VenReturnItemDAO extends JpaRepository<VenReturItem, Long> {
	public static final String FIND_WITH_VENORDERSTATUS_BY_VENORDERITEMID_SQL = 
	          "SELECT oi " + 
			  "FROM VenReturItem oi " + 
			  "INNER JOIN FETCH oi.venReturStatus os " +
			  "WHERE oi.wcsReturItemId = ?1";
	
	@Query(FIND_WITH_VENORDERSTATUS_BY_VENORDERITEMID_SQL)
	public VenReturItem findWithVenOrderStatusByWcsReturItemId(String wcsReturItemId);

}
