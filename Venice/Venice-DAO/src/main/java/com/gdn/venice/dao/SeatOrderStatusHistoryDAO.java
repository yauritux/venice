package com.gdn.venice.dao;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.SeatOrderStatusHistory;

public interface SeatOrderStatusHistoryDAO extends JpaRepository<SeatOrderStatusHistory, Long> {
	public static final String QUERY_BY_ORDERID_AND_ORDERITEMID = "Select o from SeatOrderStatusHistory o where o.venOrder.orderId =? and o.venOrderItem.orderItemId=?";

	@Query(QUERY_BY_ORDERID_AND_ORDERITEMID)
	public ArrayList<SeatOrderStatusHistory> findByOrderIdAndOrderItemId(Long orderId,Long orderItemId);
}
