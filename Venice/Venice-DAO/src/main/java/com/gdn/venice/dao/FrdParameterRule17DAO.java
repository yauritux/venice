package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.FrdParameterRule17;
import com.gdn.venice.persistence.VenOrder;

public interface FrdParameterRule17DAO extends JpaRepository<FrdParameterRule17, Long> {
	public static final String SUM_BLACKEDLISTEDCITY_RISK_POINT_BY_VENORDER = 
		"SELECT SUM(o.riskPoint) " +
		"FROM FrdParameterRule17 o " +
		"WHERE UPPER(o.cityName) IN "+
		 "(SELECT " +
		 " UPPER(a.venCity.cityName) " +
		 " FROM VenAddress a  " +
		 " WHERE a.addressId IN " +
		 " (SELECT b.venAddress.addressId " +
		 "  FROM VenOrderItem b WHERE b.venOrder = ?1 " +
		 "  )" +
		 " )";
	
	@Query(SUM_BLACKEDLISTEDCITY_RISK_POINT_BY_VENORDER)
	public int sumBlacklistedCityRiskPointByVenOrder(VenOrder order);
}
