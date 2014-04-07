package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.venice.persistence.FrdParameterRule6;

public interface FrdParameterRule6DAO extends JpaRepository<FrdParameterRule6, Long> {
	
	public FrdParameterRule6 findByShippingCountry(String countryCode);
	
}
