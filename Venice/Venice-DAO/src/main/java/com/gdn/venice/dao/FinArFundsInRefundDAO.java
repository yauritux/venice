package com.gdn.venice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.FinArFundsInRefund;

/**
 * 
 * @author yauritux
 *
 */
public interface FinArFundsInRefundDAO extends JpaRepository<FinArFundsInRefund, Long> {
	
	@Query("SELECT o FROM FinArFundsInRefund o WHERE o.finArFundsInReconRecord.reconciliationRecordId = ?1")
	public List<FinArFundsInRefund> findByReconciliationRecordId(Long reconciliationRecordId);
	
	@Query("SELECT o FROM FinArFundsInRefund o WHERE o.finApPayment.apPaymentId = ?1")
	public List<FinArFundsInRefund> findByApPaymentId(Long apPaymentId);
}
