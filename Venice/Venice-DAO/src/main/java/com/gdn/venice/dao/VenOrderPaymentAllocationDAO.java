package com.gdn.venice.dao;

import java.math.BigDecimal;
import java.util.Date;
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
		   " ((op.venBank.bankId = "+VeniceConstants.VEN_BANK_ID_BCA+" AND cast(op.paymentTimestamp AS date ) = ?3)  OR op.venBank.bankId <> "+VeniceConstants.VEN_BANK_ID_BCA+") AND " +
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
			   " JOIN FETCH op.finArFundsInReconRecords AS afirr " +
			   "WHERE " +
			   " op.referenceId = ?1 " +
			   " AND op.venPaymentType.paymentTypeId = " + VeniceConstants.VEN_PAYMENT_TYPE_ID_VA +
			   " AND afirr.finArFundsInActionApplied.actionAppliedId = " + VeniceConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_NONE +
			   " AND afirr.finApprovalStatus.approvalStatusId = " + VeniceConstants.FIN_APPROVAL_STATUS_NEW + 
			   " AND afirr.reconcilliationRecordTimestamp IS NULL";
	
	public static final String FIND_BY_PAYMENTREFERENCEID =
		   "SELECT o " +
		   "FROM VenOrderPaymentAllocation AS o " +
		   " JOIN FETCH o.venOrderPayment AS op " +
		   " LEFT JOIN FETCH op.finArFundsInReconRecords AS afirr " +
		   "WHERE " +
		   " op.referenceId = ?1 ";
	
	public static final String COUNT_BY_PAYMENTTIMERANGE_CREDITCARD_NOTSAMEORDER_SQL =
		   "SELECT COUNT(o) " +
		   "FROM VenOrderPaymentAllocation AS o " +
		   " JOIN o.venOrderPayment AS op " +
		   "WHERE " +
		   " o.venOrder <> ?1 AND " +
		   " op.maskedCreditCardNumber like ?2 AND " +
		   " cast(op.paymentTimestamp AS date) BETWEEN ?3 AND ?4 ";
	
	public static final String COUNT_MASKEDCREDITCARD_BY_IPADDRESS_ORDERDATERANGE_SQL = 
		   "SELECT COUNT(op.maskedCreditCardNumber) " +
		   "FROM VenOrderPaymentAllocation AS opa " +
		   " JOIN opa.venOrderPayment AS op " +
		   " JOIN opa.venOrder AS o "+
		   " WHERE op.venPaymentType.paymentTypeId = " + VeniceConstants.VEN_PAYMENT_TYPE_ID_CC +
		   " AND o.ipAddress = ?1 " +
		   " AND op.maskedCreditCardNumber IS NOT NULL " +
		   " AND cast(o.orderDate as date) BETWEEN ?2 AND ?3 GROUP BY op.maskedCreditCardNumber ";
	
	public static final String FIND_BY_VENORDER_ORDERPAYMENTLESSTHANLIMIT_SQL = 
		   "SELECT o " +
		   "FROM VenOrderPaymentAllocation o " +
		   "WHERE o.venOrder = ?1 " +
		   "AND o.venOrderPayment.amount < ?2 ";
	
	public static final String FIND_BY_VENORDER_PAYMENTTYPECC_SQL = 
		   "SELECT o " +
		   "FROM VenOrderPaymentAllocation o " +
		   " JOIN FETCH o.venOrderPayment op " +
		   "WHERE o.venOrder = ?1 " +
		   "AND o.venOrderPayment.venPaymentType.paymentTypeId="+VeniceConstants.VEN_PAYMENT_TYPE_ID_CC;
	
	public static final String FIND_WITH_VENADDRESS_VENCITY_BY_VENORDER = 
		   "SELECT o " +
		   "FROM VenOrderPaymentAllocation o " +
		   "INNER JOIN FETCH o.venOrderPayment op " +
		   "INNER JOIN FETCH op.venAddress a " +
		   "INNER JOIN FETCH a.venCity c " +
		   "WHERE o.venOrder = ?1 ";
	
	public static final String FIND_BY_ORDERPAYMENTID = 
		   "SELECT o FROM VenOrderPaymentAllocation o " +
	       "WHERE o.venOrderPayment.orderPaymentId = ?1";
	
	public static final String COUNT_BY_WCSORDERIDANDPAYMENTREFID_SQL =
		   "SELECT COUNT(o) " +
		   "FROM VenOrderPaymentAllocation AS o " +
		   " JOIN o.venOrderPayment AS op " +
		   " JOIN o.venOrder AS order " +
		   "WHERE " +
		   " order.wcsOrderId = ?1 AND " +
		   " op.referenceId = ?2 ";
	
	@Query(FIND_BY_VEN_ORDER)
	public List<VenOrderPaymentAllocation> findByVenOrder(VenOrder venOrder);
	
	@Query(FIND_BY_CREDITCARD_DETAIL)
	public List<VenOrderPaymentAllocation> findWithVenOrderPaymentFinArFundsInReconRecordByCreditCardDetail(String referenceId, BigDecimal amount, Date paymentDate);
	
	@Query(FIND_BY_INTERNET_BANKING_DETAIL)
	public List<VenOrderPaymentAllocation> findWithVenOrderPaymentFinArFundsInReconRecordByInternetBankingDetail(String referenceId);
	
	@Query(FIND_BY_VA_DETAIL)
	public List<VenOrderPaymentAllocation> findWithVenOrderPaymentFinArFundsInReconRecordByVADetail(String referenceId);
	
	@Query(FIND_BY_PAYMENTREFERENCEID)
	public List<VenOrderPaymentAllocation> findWithVenOrderPaymentFinArFundsInReconRecordByPaymentReferenceId(String referenceId);
	
	@Query(COUNT_BY_PAYMENTTIMERANGE_CREDITCARD_NOTSAMEORDER_SQL)
	public int countByPaymentTimeRangeCreditCardNotSameOrder(VenOrder order, String maskedCreditCard, Date dateStart, Date dateEnd);
	
	@Query(COUNT_MASKEDCREDITCARD_BY_IPADDRESS_ORDERDATERANGE_SQL)
	public List<Integer> countMaskedCreditCardByIpAddressOrderDateRange(String ipAddress, Date dateStart, Date dateEnd);
	
	@Query(FIND_BY_VENORDER_ORDERPAYMENTLESSTHANLIMIT_SQL)
	public List<VenOrderPaymentAllocation> findByVenOrderOrderPaymentLessThanLimit(VenOrder venOrder, BigDecimal amountLimit);
	
	@Query(FIND_BY_VENORDER_PAYMENTTYPECC_SQL) 
	public List<VenOrderPaymentAllocation> findByVenOrderPaymentTypeCC(VenOrder venOrder);
	
	@Query(FIND_WITH_VENADDRESS_VENCITY_BY_VENORDER)
	public List<VenOrderPaymentAllocation> findWithVenAddressVenCityByVenOrder(VenOrder order);
	
	@Query(FIND_BY_ORDERPAYMENTID)
	public List<VenOrderPaymentAllocation> findByOrderPaymentId(Long orderPaymentId);
	
	@Query(COUNT_BY_WCSORDERIDANDPAYMENTREFID_SQL)
	public int countByWcsOrderIdAndPaymentRef(String wcsOrderId, String paymentRefId);
}
