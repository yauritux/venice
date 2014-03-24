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

	public static final String FIND_BY_VEN_ORDER = 
		       "SELECT o " +
		       "FROM VenOrderPaymentAllocation o " +
		       "WHERE o.venOrder = ?1";
	
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
	
	public static final String COUNT_BY_PAYMENTTIMERANGE_CREDITCARD_NOTSAMEORDER_SQL =
		   "SELECT COUNT(o) " +
		   "FROM VenOrderPaymentAllocation AS opa " +
		   " JOIN o.venOrderPayment AS op " +
		   "WHERE " +
		   " opa.venOrder <> ?1 AND " +
		   " op.maskedCreditCardNumber like ?2 AND " +
		   " op.paymentTimestamp BETWEEN ?3 AND ?4 ";
	
	public static final String COUNT_MASKEDCREDITCARD_BY_IPADDRESS_ORDERDATERANGE_SQL = 
		   "SELECT COUNT(op.masked_credit_card_number) " +
		   "FROM VenOrderPaymentAllocation AS opa " +
		   " JOIN opa.venOrderPayment AS op " +
		   " JOIN opa.venOrder AS o "+
		   " WHERE op.venPaymentType.paymentTypeId = " + VeniceConstants.VEN_PAYMENT_TYPE_ID_CC +
		   " AND o.ipAddress = ?1 " +
		   " AND op.maskedCreditCardNumber IS NOT NULL " +
		   " AND o.orderDate BETWEEN ?2 AND ?3 GROUP BY op.maskedCreditCardNumber ";
	
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
	
	@Query(COUNT_BY_PAYMENTTIMERANGE_CREDITCARD_NOTSAMEORDER_SQL)
	public int countByPaymentTimeRangeCreditCardNotSameOrder(VenOrder order, String maskedCreditCard, String dateStart, String dateEnd);
	
	@Query(COUNT_MASKEDCREDITCARD_BY_IPADDRESS_ORDERDATERANGE_SQL)
	public List<Integer> countMaskedCreditCardByIpAddressOrderDateRange(String ipAddress, String dateStart, String dateEnd);
}
