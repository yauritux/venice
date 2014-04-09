package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.venice.persistence.FrdParameterRule1;

public interface FrdParameterRule1DAO extends JpaRepository<FrdParameterRule1, Long> {
	public FrdParameterRule1 findByCustomerType(String customerType);
}
