package com.gdn.venice.dao;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.venice.persistence.VenOrderPayment;

/**
 * 
 * @author yauritux
 *
 */
public interface VenOrderPaymentDAO extends JpaRepository<VenOrderPayment, Long>{

	public List<VenOrderPayment> findByWcsPaymentId(String wcsPaymentId);
	
	public VenOrderPayment findByReferenceIdAndAmount(String referenceId, BigDecimal amount);
}
