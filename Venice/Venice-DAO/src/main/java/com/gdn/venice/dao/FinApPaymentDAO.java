package com.gdn.venice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.FinApPayment;

/**
 * 
 * @author yauritux
 *
 */
public interface FinApPaymentDAO extends JpaRepository<FinApPayment, Long> {

	@Query("SELECT o FROM FinApPayment o join fetch o.finSalesRecords WHERE o.apPaymentId = ?1")
	public List<FinApPayment> findByApPaymentIdJoinFinSalesRecord(Long apPaymentId);
}
