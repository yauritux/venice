package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderContactDetail;

/**
 * 
 * @author yauritux
 *
 */
public interface VenOrderContactDetailDAO extends JpaRepository<VenOrderContactDetail, Long>{
	
	public static final String FIND_BY_CONTACTTYPEEMAIL_VENORDER =
		"SELECT o " +
		"FROM VenOrderContactDetail o " +
		"JOIN FETCH o.venContactDetail cd " +
		"WHERE cd.venContactDetailType.contactDetailTypeId = 3 " +
		"AND o.venOrder = ?1";
	
	@Query(FIND_BY_CONTACTTYPEEMAIL_VENORDER)
	public VenOrderContactDetail findByContactEmailVenOrder(VenOrder order);
}
