package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.venice.persistence.FrdParameterRule16;

public interface FrdParameterRule16DAO extends JpaRepository<FrdParameterRule16, Long> {
	public FrdParameterRule16 findByCode(String code);
}
