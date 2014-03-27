package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.venice.persistence.FrdParameterRule9;

public interface FrdParameterRule9DAO extends JpaRepository<FrdParameterRule9, Long> {
	public FrdParameterRule9 findByCode(String code);
}
