package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.venice.persistence.FrdParameterRule5;

public interface FrdParameterRule5DAO extends JpaRepository<FrdParameterRule5, Long> {
	
	public FrdParameterRule5 findByShippingType(String shippingType);
}
