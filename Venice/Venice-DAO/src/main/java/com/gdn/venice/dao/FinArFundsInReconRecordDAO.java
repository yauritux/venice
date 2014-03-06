package com.gdn.venice.dao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.FinArFundsInReconRecord;
import com.gdn.venice.util.VeniceConstants;

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
	
	public static final String FIND_BY_NOMORREFF_PAYMENTTYPEIB =
		"SELECT o " +
		"FROM FinArFundsInReconRecord o " +
		"WHERE (o.wcsOrderId = ?1 OR o.nomorReff = ?1) AND " +
		"o.reconcilliationRecordTimestamp IS NOT NULL AND " +
		"o.venOrderPayment.venPaymentType.paymentTypeId = " + VeniceConstants.VEN_PAYMENT_TYPE_ID_IB;
	
	public static final String COUNT_BY_NOMORREFF_PAYMENTTYPEIB_ACTIONAPPLIEDNOTREMOVED =
		"SELECT COUNT(o) " +
		"FROM FinArFundsInReconRecord o " +
		"JOIN o.finArFundsInReport afir " +
		"WHERE o.nomorReff  = ?1 AND " +
		"afir.finArFundsInReportType.paymentReportTypeId = ?2 AND " +
		"o.venOrderPayment.venPaymentType.paymentTypeId="+VeniceConstants.VEN_PAYMENT_TYPE_ID_IB +" AND " +
		"o.finArFundsInActionApplied.actionAppliedId<>"+VeniceConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_REMOVED;
	
	public static final String FIND_BY_NOMORREFF_UNIQUEPAYMENT_REPORTTIMEID = 
		"SELECT o " +
		"FROM FinArFundsInReconRecord o " +
		"WHERE o.nomorReff = ?1 AND " +
		"o.uniquePayment = ?2 AND " +
		"o.reconcilliationRecordTimestamp IS NOT NULL AND " +
		"o.finArFundsIdReportTime.reportTimeId = "+VeniceConstants.FIN_AR_FUNDS_IN_REPORT_TIME_REAL_TIME;
	
	public static final String FIND_BY_NOMORREFF_UNIQUEPAYMENT = 
		"SELECT o " +
		"FROM FinArFundsInReconRecord o " +
		"WHERE o.nomorReff = ?1 AND " +
		"o.uniquePayment = ?2 AND " +
		"o.reconcilliationRecordTimestamp IS NOT NULL";
	
	public static final String FIND_BY_NOMORREFF =
		"SELECT o " +
		"FROM FinArFundsInReconRecord o " +
		"WHERE o.nomorReff = ?1 AND " +
		"o.reconcilliationRecordTimestamp IS NOT NULL ";
	
	public List<FinArFundsInReconRecord> findByWcsOrderId(String wcsOrderId);
	
	@Query(FIND_BY_NOMORREFF_PAIDAMOUNT_PAYMENTDATE)
	public List<FinArFundsInReconRecord> findForCreditCardDetail(String paymentConfirmationNumber, BigDecimal paidAmount, Date paymentDate);

	@Query(FIND_BY_NOMORREFF_PAYMENTTYPEIB)
	public List<FinArFundsInReconRecord> findForInternetBankingDetail(String paymentConfirmationNumber);
	
	@Query(COUNT_BY_NOMORREFF_PAYMENTTYPEIB_ACTIONAPPLIEDNOTREMOVED)
	public int countByNomorReffAndPaymentReportTypeId(String paymentConfirmationNumber, Long reportTypeId);
	
	@Query(FIND_BY_NOMORREFF_UNIQUEPAYMENT_REPORTTIMEID)
	public List<FinArFundsInReconRecord> findByNomorReffUniquePaymentAndRealTimeId(String paymentConfirmationNumber,String uniquePayment);
	
	@Query(FIND_BY_NOMORREFF_UNIQUEPAYMENT)
	public List<FinArFundsInReconRecord> findByNomorReffUniquePayment(String paymentConfirmationNumber,String uniquePayment);
	
	@Query(FIND_BY_NOMORREFF)
	public List<FinArFundsInReconRecord> findByNomorReff(String paymentConfirmationNumber);
}
