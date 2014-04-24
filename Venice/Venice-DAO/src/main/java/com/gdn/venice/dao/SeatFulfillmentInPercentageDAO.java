package com.gdn.venice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.LogLogisticsProvider;
import com.gdn.venice.persistence.SeatFulfillmentInPercentage;

public interface SeatFulfillmentInPercentageDAO extends
		JpaRepository<LogLogisticsProvider, Long> {

	public static final String FIND_RECORD_USERROLE = "SELECT o FROM SeatFulfillmentInPercentage o join fetch o.seatOrderStatus u "
			+ "WHERE u.pic in (?1)";

	public static final String FIND_RECORD_USERROLE_AND_FILTER = "SELECT o "
			+ "FROM FinPeriod o "
			+ "WHERE CURRENT_DATE BETWEEN o.fromDatetime " + "AND o.toDatetime";

	@Query(FIND_RECORD_USERROLE)
	public List<SeatFulfillmentInPercentage> findForUserRole(String userRole);

	@Query(FIND_RECORD_USERROLE_AND_FILTER)
	public List<SeatFulfillmentInPercentage> findForUserRoleAndFilter();

}
