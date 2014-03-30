package com.gdn.venice.dao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.VenOrderPayment;
import com.gdn.venice.util.VeniceConstants;

/**
 * 
 * @author yauritux
 *
 */
public interface VenOrderPaymentDAO extends JpaRepository<VenOrderPayment, Long>{

	public static final String FIND_BY_REFERENCEID_BANKID =
		"SELECT o " +
		"FROM VenOrderPayment o " +
		"JOIN o.venBank b " +
		"WHERE o.referenceId = ?1 AND " +
		"o.venPaymentType.paymentTypeId = " + VeniceConstants.VEN_PAYMENT_TYPE_ID_IB + " AND " +
		"b.bankId = ?2";
	
	public static final String FIND_BY_REFERENCEID_BANKID_PAYMENTDATE =
		"SELECT o " +
		"FROM VenOrderPayment o " +
		"JOIN o.venBank b " +
		"WHERE o.referenceId = ?1 AND " +
		"o.amount = ?2 AND " +
		 " ((b.bankId = "+VeniceConstants.VEN_BANK_ID_BCA+" AND cast(o.paymentTimestamp AS date) = ?3)  OR b.bankId <> "+VeniceConstants.VEN_BANK_ID_BCA+")";
			
	
	public static final String GET_ORDERPAYMENTAMOUNTSUM_BY_CREDITCARDNUMBER_PAYMENTTIMERANGE  =
		   "SELECT COALESCE(SUM(op.amount),0) " +
		   "FROM VenOrderPayment op " +
		   "WHERE op.maskedCreditCardNumber = ?1 AND " +
		   " cast(op.paymentTimestamp AS date) BETWEEN ?2 AND ?3 ";
	
	public List<VenOrderPayment> findByWcsPaymentId(String wcsPaymentId);
	
	@Query(FIND_BY_REFERENCEID_BANKID_PAYMENTDATE)
	public VenOrderPayment findByReferenceIdAndAmountAndDatePayment(String referenceId, BigDecimal amount,Date paymentDate);

	public VenOrderPayment findByReferenceIdAndAmount(String referenceId, BigDecimal amount);
	
	public VenOrderPayment findByReferenceId(String referenceId);
	
	@Query(FIND_BY_REFERENCEID_BANKID)
	public VenOrderPayment findWithBankByReferenceIdAndBankId(String referenceId, Long bankId);
	
	@Query(GET_ORDERPAYMENTAMOUNTSUM_BY_CREDITCARDNUMBER_PAYMENTTIMERANGE)
	public BigDecimal getOrderPaymentAmountSumByCreditCardNumberPaymentTimeRange(String maskedCreditCardNumber, Date dateStart, Date dateEnd);
}
