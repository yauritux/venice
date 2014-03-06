package com.gdn.venice.dao;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderPaymentAllocation;
import com.gdn.venice.util.VeniceConstants;

/**
 * 
 * @author yauritux
 *
 */
public interface VenOrderPaymentAllocationDAO extends JpaRepository<VenOrderPaymentAllocation, Long>{

	public static final String FIND_BY_VEN_ORDER 
	  = "select o from VenOrderPaymentAllocation o where o.venOrder = ?1";
	
	public static final String FIND_BY_CREDITCARD_DETAIL = 
		       "SELECT o " +
			   "FROM VenOrderPaymentAllocation AS o " +
			   " JOIN FETCH o.venOrderPayment AS op " +
			   " LEFT JOIN FETCH op.finArFundsInReconRecords AS afirr " +
			   "WHERE " +
			   " op.referenceId = ?1 AND " +
			   " op.amount = ?2 AND " +
			   " afirr.finArFundsInActionApplied.actionAppliedId <> "+VeniceConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_REMOVED+" AND " +
			   " afirr.reconcilliationRecordTimestamp IS NULL";
	
	public static final String FIND_BY_INTERNET_BANKING_DETAIL =
		   "SELECT o " +
		   "FROM VenOrderPaymentAllocation AS o " +
		   " JOIN FETCH o.venOrderPayment AS op " +
		   " LEFT JOIN FETCH op.finArFundsInReconRecords AS afirr " +
		   "WHERE " +
		   " op.referenceId = ?1 AND " +
		   " op.venPaymentType.paymentTypeId = "+VeniceConstants.VEN_PAYMENT_TYPE_ID_IB;
	
	public static final String FIND_BY_VA_DETAIL =
			   "SELECT o " +
			   "FROM VenOrderPaymentAllocation AS o " +
			   " JOIN FETCH o.venOrderPayment AS op " +
			   " LEFT JOIN FETCH op.finArFundsInReconRecords AS afirr " +
			   "WHERE " +
			   " op.referenceId = ?1 ";
	
	public static final String FIND_BY_PAYMENTREFERENCEID =
		   "SELECT o " +
		   "FROM VenOrderPaymentAllocation AS o " +
		   " JOIN FETCH o.venOrderPayment AS op " +
		   " LEFT JOIN FETCH op.finArFundsInReconRecords AS afirr " +
		   "WHERE " +
		   " op.referenceId = ?1 ";
	
	@Query(FIND_BY_VEN_ORDER)
	public List<VenOrderPaymentAllocation> findByVenOrder(VenOrder venOrder);
	
	@Query(FIND_BY_CREDITCARD_DETAIL)
	public List<VenOrderPaymentAllocation> findWithVenOrderPaymentFinArFundsInReconRecordByCreditCardDetail(String referenceId, BigDecimal amount);
	
	@Query(FIND_BY_INTERNET_BANKING_DETAIL)
	public List<VenOrderPaymentAllocation> findWithVenOrderPaymentFinArFundsInReconRecordByInternetBankingDetail(String referenceId);
	
	@Query(FIND_BY_VA_DETAIL)
	public List<VenOrderPaymentAllocation> findWithVenOrderPaymentFinArFundsInReconRecordByVADetail(String referenceId);
	
	@Query(FIND_BY_PAYMENTREFERENCEID)
	public List<VenOrderPaymentAllocation> findWithVenOrderPaymentFinArFundsInReconRecordByPaymentReferenceId(String referenceId);
}
