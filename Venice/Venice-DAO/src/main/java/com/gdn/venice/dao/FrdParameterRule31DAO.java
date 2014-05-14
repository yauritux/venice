package com.gdn.venice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.FrdParameterRule31;

public interface FrdParameterRule31DAO extends JpaRepository<FrdParameterRule31, Long> {
	public static final String FIND_BY_GENUINE_LIST =
		"select a " +
		"from FrdParameterRule31 a " +
		"where a.email in " +
		" (select b.customerUserName " +
		"  from VenCustomer b " +
		"  where b.customerId in " +
		"  (select c.venCustomer.customerId " +
		"   from VenOrder c " +
		"   where c.orderId = ?1 " +
		"   ) " +
		" ) " +
		"and a.noCc in " +
		" (select o.venOrderPayment.maskedCreditCardNumber " +
		"  from VenOrderPaymentAllocation o " +
		"  where o.venOrder.orderId= ?1 " +
		"  )";		
	
	@Query(FIND_BY_GENUINE_LIST)
	public List<FrdParameterRule31> findByGenuineList(Long orderId);
	
	public List<FrdParameterRule31> findByEmailAndNoCc(String email, String noCc);
}
