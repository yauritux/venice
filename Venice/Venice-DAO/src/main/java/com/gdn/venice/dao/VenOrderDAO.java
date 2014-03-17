package com.gdn.venice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.util.VeniceConstants;

public interface VenOrderDAO extends JpaRepository<VenOrder, Long>{
	public static final String FIND_PAID_BY_CREDITCARD_AND_STATUS_C =
		"SELECT o " +
		"FROM VenOrder o " +
		"INNER JOIN FETCH o.venOrderPaymentAllocations opa " +
		"INNER JOIN FETCH opa.venOrderPayment op" +
		"WHERE o.venOrderStatus.orderStatusId = " + VeniceConstants.VEN_ORDER_STATUS_C +
		"AND op.venPaymentType.paymentTypeId = " + VeniceConstants.VEN_PAYMENT_TYPE_ID_CC +
		"AND o.orderDate => ?1 ";
	
	
	public VenOrder findByWcsOrderId(String wcsOrderId);
	
	@Query(FIND_PAID_BY_CREDITCARD_AND_STATUS_C)
	public List<VenOrder> findPaidByCreditCardAndStatusC(String startDate);
}
