package com.gdn.venice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.VenAddress;
import com.gdn.venice.persistence.VenOrder;

/**
 * 
 * @author yauritux
 *
 */
public interface VenAddressDAO extends JpaRepository<VenAddress, Long>{
	public static final String FIND_ORDERADDRESS_BY_ORDER = 
		"SELECT o " +
		"FROM VenAddress o " +
		"WHERE o.addressId in " +
		"( " +
		"	SELECT b.venAddress.addressId " +
		"   FROM VenOrderAddress b " +
		"   WHERE b.venOrder = ?1 " +
		")";
	
	public static final String FIND_GROUPED_PAYMENTADDRESS_BY_ORDER =
		"SELECT o.streetAddress1 " +
		"FROM VenAddress o " +
		"WHERE o.addressId IN " +
		"( " +
		"  SELECT a.venAddress.addressId " +
		"  FROM VenOrderPayment a " +
		"  WHERE a.orderPaymentId IN " +
		"  (" +
		"     SELECT b.venOrderPayment.orderPaymentId " +
		"     FROM " +
		"     VenOrderPaymentAllocation b " +
		"     WHERE b.venOrder = ?1 " +
		"   )" +
		") GROUP BY o.streetAddress1 ";
	
	public static final String FIND_GROUPED_ITEMADDRESS_BY_ORDER =
		"SELECT o.streetAddress1 " +
		"FROM VenAddress o " +
		"WHERE o.addressId IN " +
		"( " +
		"  SELECT a.venAddress.addressId " +
		"  FROM VenOrderItem a " +
		"  WHERE a.venOrder = ?1" +
		") " +
		"GROUP BY o.streetAddress1 ";
	
	@Query(FIND_ORDERADDRESS_BY_ORDER)
	public VenAddress findOrderAddressByOrder(VenOrder order);
	
	@Query(FIND_GROUPED_PAYMENTADDRESS_BY_ORDER)
	public List<String> findGroupedPaymentAddressByOrder(VenOrder order);
	
	@Query(FIND_GROUPED_ITEMADDRESS_BY_ORDER)
	public List<String> findGroupedItemAddressByOrder(VenOrder order);
	
	
}
