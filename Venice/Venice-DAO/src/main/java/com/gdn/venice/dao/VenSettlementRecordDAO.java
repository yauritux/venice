package com.gdn.venice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.VenSettlementRecord;

/**
 * 
 * @author yauritux
 *
 */
public interface VenSettlementRecordDAO extends JpaRepository<VenSettlementRecord, Long> {

	@Query("SELECT o FROM VenSettlementRecord o WHERE o.venOrderItem.orderItemId = ?1")
	public List<VenSettlementRecord> findByOrderItemId(Long orderItemId);
}
