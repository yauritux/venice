package com.gdn.venice.dao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.VenCustomer;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.util.VeniceConstants;

public interface VenOrderDAO extends JpaRepository<VenOrder, Long>{
	public static final String FIND_PAID_BY_CREDITCARD_AND_STATUS_C =
		"SELECT o " +
		"FROM VenOrder o " +
		"INNER JOIN FETCH o.venOrderPaymentAllocations opa " +
		"INNER JOIN FETCH opa.venOrderPayment op " +
		"WHERE o.venOrderStatus.orderStatusId = " + VeniceConstants.VEN_ORDER_STATUS_C +
		" AND op.venPaymentType.paymentTypeId = " + VeniceConstants.VEN_PAYMENT_TYPE_ID_CC +
		" AND o.orderDate >= ?1 ";
	
	public static final String FIND_WITH_WCSPAYMENTTYPE_BY_ORDER =
		"SELECT o " +
		"FROM VenOrder o " +
		"INNER JOIN FETCH o.venOrderPaymentAllocations opa " +
		"INNER JOIN FETCH opa.venOrderPayment op " +
		"INNER JOIN FETCH op.venWcsPaymentType pt " +
		"WHERE o = ?1 ";
	
	public static final String FIND_WITH_VENCUSTOMER_BY_ORDER =
		"SELECT o " +
		"FROM VenOrder o " +
		"INNER JOIN FETCH o.venCustomer c " +
		"INNER JOIN FETCH c.venParty p " +
		"WHERE o.orderId = ?1 ";
	
	public static final String FIND_OTHER_BY_STATUS_C_AND_ORDERDATERANGE =
		"SELECT o " +
		"FROM VenOrder o " +
		"WHERE o <> ?1 " +
		" AND o.venOrderStatus.orderStatusId = " + VeniceConstants.VEN_ORDER_STATUS_C +
		" AND cast(o.orderDate as date) BETWEEN ?2 AND ?3 ";
	
	public static final String GET_AMOUNTSUM_BY_ORDER_OR_CUSTOMER_ORDERDATERANGE = 
		"SELECT COALESCE(SUM(o.amount),0) " +
		"FROM VenOrder o " +
		"WHERE o= ?1 " +
	    " OR (o.venCustomer = ?2 " +
		" AND cast(o.orderDate as date) BETWEEN ?3 AND ?4)";
	
	public VenOrder findByWcsOrderId(String wcsOrderId);
	
	@Query(FIND_PAID_BY_CREDITCARD_AND_STATUS_C)
	public List<VenOrder> findPaidByCreditCardAndStatusC(String startDate);
	
	@Query(FIND_WITH_WCSPAYMENTTYPE_BY_ORDER)
	public VenOrder findWithWcsPaymentTypeByOrder(VenOrder order);
	
	@Query(FIND_WITH_VENCUSTOMER_BY_ORDER)
	public VenOrder findWithVenCustomerByOrder(Long orderId);
	
	@Query(FIND_OTHER_BY_STATUS_C_AND_ORDERDATERANGE)
	public List<VenOrder> findOtherByStatusCOrderDateRange(VenOrder order, Date startDate, Date endDate);
	
	@Query(GET_AMOUNTSUM_BY_ORDER_OR_CUSTOMER_ORDERDATERANGE)
	public BigDecimal getAmountSumByOrderOrCustomerOrderDateRange(VenOrder order, VenCustomer customer, Date startDate, Date endDate);
	
}
