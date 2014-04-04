package com.gdn.venice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderAddress;

/**
 * 
 * @author yauritux
 *
 */
public interface VenOrderAddressDAO extends JpaRepository<VenOrderAddress, Long>{
	
	public static final String FIND_WITH_VENADDRESS_BY_VENORDER = 
		"SELECT o " +
		"FROM VenOrderAddress o " +
		"INNER JOIN FETCH o.venAddress a " +
		"WHERE o.venOrder = ?1 ";
	
	public static final String FIND_WITH_VENADDRESS_VENSTATE_BY_VENORDER = 
		"SELECT o " +
		"FROM VenOrderAddress o " +
		"INNER JOIN FETCH o.venAddress a " +
		"INNER JOIN FETCH a.venState s " +
		"WHERE o.venOrder = ?1 ";
	
	public static final String FIND_WITH_VENADDRESS_VENCITY_BY_VENORDER = 
		"SELECT o " +
		"FROM VenOrderAddress o " +
		"INNER JOIN FETCH o.venAddress a " +
		"INNER JOIN FETCH a.venCity c " +
		"WHERE o.venOrder = ?1 ";
	
	public List<VenOrderAddress> findByVenOrder(VenOrder order);
	
	@Query(FIND_WITH_VENADDRESS_BY_VENORDER)
	public VenOrderAddress findWithVenAddressByVenOrder(VenOrder order);
	
	@Query(FIND_WITH_VENADDRESS_VENSTATE_BY_VENORDER)
	public VenOrderAddress findWithVenAddressVenStateByVenOrder(VenOrder order);
	
	@Query(FIND_WITH_VENADDRESS_VENCITY_BY_VENORDER)
	public VenOrderAddress findWithVenAddressVenCityByVenOrder(VenOrder order);
}
