package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.venice.persistence.FrdParameterRule15;

public interface FrdParameterRule15DAO extends JpaRepository<FrdParameterRule15, Long> {
	public FrdParameterRule15 findByCode(String code);
}
