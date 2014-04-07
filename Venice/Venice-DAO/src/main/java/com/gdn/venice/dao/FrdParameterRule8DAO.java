package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.venice.persistence.FrdParameterRule8;

public interface FrdParameterRule8DAO extends JpaRepository<FrdParameterRule8, Long> {
	public FrdParameterRule8 findByPaymentType(String paymentType);
}
