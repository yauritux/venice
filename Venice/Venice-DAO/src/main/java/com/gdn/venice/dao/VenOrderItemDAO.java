package com.gdn.venice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderItem;

public interface VenOrderItemDAO extends JpaRepository<VenOrderItem, Long>{
	public static final String FIND_WITH_VENORDERSTATUS_BY_VENORDER_SQL = 
																          "SELECT oi " + 
																		  "FROM VenOrderItem oi " + 
																		  "INNER JOIN FETCH oi.venOrder o " +
																		  "INNER JOIN FETCH oi.venOrderStatus os " +
																		  "WHERE o = ?1";
	
	public static final String FIND_WITH_VENORDERSTATUS_BY_VENORDERITEM_SQL = 
																          "SELECT oi " + 
																		  "FROM VenOrderItem oi " + 
																		  "INNER JOIN FETCH oi.venOrder o " +
																		  "INNER JOIN FETCH oi.venOrderStatus os " +
																		  "WHERE oi = ?1";
	
	public static final String FIND_WITH_VENORDERSTATUS_AND_LOGAIRWAYBILL_BY_VENORDERITEM_SQL = 
																          "SELECT oi " + 
																		  "FROM VenOrderItem oi " + 
																		  "INNER JOIN FETCH oi.venOrder o " +
																		  "INNER JOIN FETCH oi.venOrderStatus os " +
																		  "LEFT JOIN FETCH oi.logAirwayBills ab " +
																		  "WHERE oi.wcsOrderItemId = ?1";
	
	public static final String COUNT_BY_WCSORDERITEMID_SQL = 
																		  "SELECT COUNT(oi) FROM VenOrderItem AS oi " +
																		  "WHERE oi.wcsOrderItemId = ?1";
	
	public static final String FIND_WITH_VENORDERSTATUS_BY_VENORDERITEMID_SQL = 
																          "SELECT oi " + 
																		  "FROM VenOrderItem oi " + 
																		  "INNER JOIN FETCH oi.venOrderStatus os " +
																		  "WHERE oi.wcsOrderItemId = ?1";
	
	public static final String COUNT_WHERE_LOGISTICSERVICE_IS_BOPIS_OR_BIGPRODUCT_BY_WCSORDERITEMID_SQL = 
																          "SELECT COUNT(oi) " + 
																		  "FROM VenOrderItem AS oi " + 
																		  "JOIN oi.logLogisticService AS ls " +
																		  "WHERE " +
																		  "(ls.serviceCode = 'BOPIS' OR ls.serviceCode = 'Big Product') " +
																		  "AND oi.wcsOrderItemId = ?1 ";
	
	@Query(COUNT_BY_WCSORDERITEMID_SQL)
    public int countByWcsOrderItemId(String wcsOrderItemId);
	
	public List<VenOrderItem> findByVenOrder(VenOrder venOrder);
	
	@Query(FIND_WITH_VENORDERSTATUS_BY_VENORDER_SQL)
	public List<VenOrderItem> findWithVenOrderStatusByVenOrder(VenOrder venOrder);
	
	@Query(FIND_WITH_VENORDERSTATUS_BY_VENORDERITEM_SQL)
	public VenOrderItem findWithVenOrderStatusByVenOrderItem(VenOrderItem venOrderItem);
	
	@Query(FIND_WITH_VENORDERSTATUS_AND_LOGAIRWAYBILL_BY_VENORDERITEM_SQL)
	public VenOrderItem findWithVenOrderStatusAndLogAirwayBillByWcsOrderItemId(String wcsOrderItemId);
	
	public VenOrderItem findByWcsOrderItemId(String wcsOrderItemId);
	
	@Query(FIND_WITH_VENORDERSTATUS_BY_VENORDERITEMID_SQL)
	public VenOrderItem findWithVenOrderStatusByWcsOrderItemId(String wcsOrderItemId);
	
	@Query(COUNT_WHERE_LOGISTICSERVICE_IS_BOPIS_OR_BIGPRODUCT_BY_WCSORDERITEMID_SQL)
    public int countWhereLogisticServiceIsBopisOrBigProductByWcsOrderItemId(String wcsOrderItemId);
}
