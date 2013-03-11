package com.gdn.venice.dao;

import java.math.BigDecimal;
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
	
	public List<VenOrderPayment> findByWcsPaymentId(String wcsPaymentId);
	
	public VenOrderPayment findByReferenceIdAndAmount(String referenceId, BigDecimal amount);
	
	public VenOrderPayment findByReferenceId(String referenceId);
	
	@Query(FIND_BY_REFERENCEID_BANKID)
	public VenOrderPayment findWithBankByReferenceIdAndBankId(String referenceId, Long bankId);
}
