package com.gdn.venice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.FinSalesRecord;

public interface FinSalesRecordDAO extends JpaRepository<FinSalesRecord, Long> {
	public static String FIND_WITH_VENORDERITEM_BY_ORDERITEMID = 
										"SELECT o " +
										"FROM FinSalesRecord o " +
										"JOIN FETCH o.venOrderItem oi " +
										"WHERE oi.orderItemId = ?1";

	@Query(FIND_WITH_VENORDERITEM_BY_ORDERITEMID)
	public FinSalesRecord findWithVenOrderItemByOrderItemId(Long orderItemId);
	
	@Query("SELECT o from FinSalesRecord o inner join fetch o.venOrderItem i left join fetch i.venOrderItemAdjustments where o.salesRecordId = ?1")
    public List<FinSalesRecord> findBySalesRecordIdInnerJoinOrderItemLeftJoinOrderItemAdjustment(
    		Long salesRecordId);	
}
