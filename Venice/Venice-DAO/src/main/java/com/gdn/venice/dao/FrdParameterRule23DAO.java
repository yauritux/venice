package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.venice.persistence.FrdParameterRule23;

public interface FrdParameterRule23DAO extends JpaRepository<FrdParameterRule23, Long> {
	public FrdParameterRule23 findByCode(String code);
}
