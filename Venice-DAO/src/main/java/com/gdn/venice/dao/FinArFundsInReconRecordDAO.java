package com.gdn.venice.dao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.FinArFundsInReconRecord;

/**
 * 
 * @author yauritux
 *
 */
public interface FinArFundsInReconRecordDAO extends JpaRepository<FinArFundsInReconRecord, Long>{
	public static final String FIND_BY_NOMORREFF_PAIDAMOUNT_PAYMENTDATE = 
		"SELECT o " +
		"FROM FinArFundsInReconRecord o " +
		"WHERE o.nomorReff = ?1 AND " +
		"o.providerReportPaidAmount = ?2 AND " +
		"o.reconcilliationRecordTimestamp IS NOT NULL AND " +
		"o.providerReportPaymentDate = ?3";
	
	public List<FinArFundsInReconRecord> findByWcsOrderId(String wcsOrderId);
	
	@Query(FIND_BY_NOMORREFF_PAIDAMOUNT_PAYMENTDATE)
	public List<FinArFundsInReconRecord> findForCreditCardDetail(String paymentConfirmationNumber, BigDecimal paidAmount, Date paymentDate);

}
