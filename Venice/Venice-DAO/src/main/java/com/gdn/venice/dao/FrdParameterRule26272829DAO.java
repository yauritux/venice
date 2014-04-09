package com.gdn.venice.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.FrdParameterRule26272829;

public interface FrdParameterRule26272829DAO extends JpaRepository<FrdParameterRule26272829, Long> {
	public static final String FIND_BY_RULE_26_ORDERFROMSAMECUSTOMER_IN_ORDERDATERANGE_SQL= 
		"SELECT * FROM " +
		"Frd_Parameter_Rule_26_27_28_29 " +
		"WHERE id = 26 "+
		"AND ( " +
		"	SELECT COUNT(*) " +
		"   FROM ven_order " +
		"   WHERE customer_id = ?1 " +
		"   AND cast(order_date as date) BETWEEN ?2 AND ?3 " +
		"   AND order_id <> ?4 " +
		") > 0 ";
	
	public static final String FIND_BY_RULE_27_ORDERFROMSAMECUSTOMER_SAMEPRODUCT_IN_ORDERDATERANGE_SQL = 
		"SELECT * " +
		"FROM Frd_Parameter_Rule_26_27_28_29 " +
		"WHERE id = 27 " +
		" AND (SELECT COUNT(*) FROM ven_order_item a "+
		" INNER JOIN (SELECT * FROM ven_order WHERE customer_id= ?1 " +
		" AND cast(order_date as date)  BETWEEN ?2 AND ?3 " +
		" AND order_id <> ?4 ) b ON a.order_id = b.order_id "+
		" INNER JOIN ven_merchant_product c ON c.product_id=a.product_id "+
		" WHERE c.product_id IN (SELECT product_id FROM ven_order_item WHERE order_id= ?4) "+
		" OR c.wcs_product_sku IN (SELECT wcs_product_sku FROM ven_merchant_product WHERE product_id IN " +
		"  (SELECT product_id FROM ven_order_item WHERE order_id = ?4)) ) > 0";
	
	public static final String FIND_BY_RULE_28_ORDERFROMSAMECUSTOMER_SAMEPRODUCTCATEGORY_IN_ORDERDATERANGE_SQL =
		"SELECT * " +
		"from Frd_Parameter_Rule_26_27_28_29 " +
		"where id= 28 " +
		" and (select count(*)from ven_order_item a inner join " +
		"     (select * from ven_order where customer_id= ?1 and cast(order_date as date) between ?2 and ?3 and order_id<>?4 ) b on a.order_id=b.order_id "+
		" inner join ven_product_categories c on c.product_id=a.product_id where c.product_category_id in (select product_category_id from ven_product_categories where product_id in "+
		" (select product_id  from ven_order_item where order_id= ?4 )) )>0";
	
	public static final String FIND_BY_RULE_29_ORDERFROMSAMECUSTOMERADDRESS_IN_ORDERDATERANGE_SQL = 
		"select * " +
		"from Frd_Parameter_Rule_26_27_28_29 " +
		"where id= 29 " +
		" and (select count(*) from ven_order a inner join  ven_order_address b on a.order_id=b.order_id inner join ven_address c on b.address_id=c.address_id where cast(a.order_date as date) between ?1 and ?2 and a.order_id<> ?3 and  c.street_address_1 is not null"+
		" and c.street_address_1 in (select e.street_address_1 from ven_address e inner join ven_order_address f on f.address_id=e.address_id where f.order_id=?3  ) )>0";
	
	@Query(value = FIND_BY_RULE_26_ORDERFROMSAMECUSTOMER_IN_ORDERDATERANGE_SQL, nativeQuery = true)
	public FrdParameterRule26272829 findByRule26OrderFromSameCustomerInOrderDateRange(Long customerId, Date startOrderDate, Date endOrdeDate, Long orderId);

	@Query(value = FIND_BY_RULE_27_ORDERFROMSAMECUSTOMER_SAMEPRODUCT_IN_ORDERDATERANGE_SQL, nativeQuery = true)
	public FrdParameterRule26272829 findByRule27OrderFromSameCustomerSameProductInOrderDateRange(Long customerId, Date startOrderDate, Date endOrderDate, Long orderId);

	@Query(value = FIND_BY_RULE_28_ORDERFROMSAMECUSTOMER_SAMEPRODUCTCATEGORY_IN_ORDERDATERANGE_SQL, nativeQuery = true)
	public FrdParameterRule26272829 findByRule28OrderFromSameCustomerSameProductCategoryInOrderDateRange(Long customerId, Date startOrderDate, Date endOrderDate, Long orderId);

	@Query(value = FIND_BY_RULE_29_ORDERFROMSAMECUSTOMERADDRESS_IN_ORDERDATERANGE_SQL, nativeQuery = true)
	public FrdParameterRule26272829 findByRule29OrderFromSameCustomerAddressInOrderDateRange(Date startOrderDate, Date endOrderDate, Long orderId);

}
	