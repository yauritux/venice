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
	
	/*
	 select afirr.*
	 from ven_order_payment_allocation opa
	 inner join ven_order_payment op on op.order_payment_id = opa.order_payment_id
	 inner join fin_ar_funds_in_recon_record afirr on opa.order_payment_id = afirr.order_payment_id
	 where
	 op.reference_id = '780241' 
	 AND afirr.action_applied_id <> 0
	 AND afirr.reconcilliation_record_timestamp IS NULL
	 */
	
	@Query(FIND_BY_VEN_ORDER)
	public List<VenOrderPaymentAllocation> findByVenOrder(VenOrder venOrder);
	
	@Query(FIND_BY_CREDITCARD_DETAIL)
	public List<VenOrderPaymentAllocation> findWithVenOrderPaymentFinArFundsInReconRecordByCreditCardDetail(String referenceId, BigDecimal amount);
}
