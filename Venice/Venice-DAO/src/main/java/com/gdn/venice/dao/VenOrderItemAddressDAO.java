package com.gdn.venice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderItemAddress;

/**
 * 
 * @author yauritux
 *
 */
public interface VenOrderItemAddressDAO extends JpaRepository<VenOrderItemAddress, Long>{
	public static final String FIND_WITH_VENADDRESS_VENCITY_BY_VENORDER =
		"SELECT o " +
		"FROM VenOrderItemAddress o " +
		"INNER JOIN FETCH o.venAddress a " +
		"INNER JOIN FETCH a.venCity c " +
		"WHERE o.venOrderItem.venOrder = ?1 ";
	
	@Query(FIND_WITH_VENADDRESS_VENCITY_BY_VENORDER)
	public List<VenOrderItemAddress> findWithVenAddressVenCityByVenOrder(VenOrder order);
}
