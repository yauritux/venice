package com.gdn.venice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.VenOrderItemStatusHistory;

public interface VenOrderItemStatusHistoryDAO extends JpaRepository<VenOrderItemStatusHistory, Long>{
	public static final String FIND_CX_FIN_WITH_VENORDERITEM_AND_VENORDERITEMSTATUS_BY_ORDERITEMID_SQL = 
													"SELECT o " +
													"FROM VenOrderItemStatusHistory o " +
													"JOIN FETCH o.venOrderItem oi " +
													"JOIN FETCH o.venOrderStatus s " + 
													"WHERE oi.orderItemId = ?1 " + 
													"AND lower(o.statusChangeReason) like '%cx finance%' ";
	
	@Query(FIND_CX_FIN_WITH_VENORDERITEM_AND_VENORDERITEMSTATUS_BY_ORDERITEMID_SQL)
	public List<VenOrderItemStatusHistory> findCXFinWithVenOrderItemAndVenOrderItemStatusByOrderItemId(Long orderItemId);
	
}
