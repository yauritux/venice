package com.gdn.venice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.LogAirwayBill;

public interface LogAirwayBillDAO extends JpaRepository<LogAirwayBill, Long>{

	public static final String COUNT_BY_GDN_REF = "SELECT COUNT(ab) FROM LogAirwayBill AS ab WHERE ab.gdnReference = ?1";
	
	public static final String FIND_BY_ORDERITEMID = "SELECT ab FROM LogAirwayBill AS ab INNER JOIN ab.venOrderItem oi WHERE oi.orderItemId = ?1";
	
	public static final String FIND_BY_AIRWAYBILLID="SELECT ab FROM LogAirwayBill AS ab INNER JOIN ab.logInvoiceAirwaybillRecord ac WHERE ab.airwayBillId = ?1";
	
	@Query(COUNT_BY_GDN_REF)
    public int countByGdnReference(String gdnReference);
	
	@Query(FIND_BY_ORDERITEMID)
	public List<LogAirwayBill> findByOrderItemId(long orderItemId);
	
	@Query(FIND_BY_AIRWAYBILLID)
	public LogAirwayBill findByAirwayBillId(long airwayBillId);
	
}
