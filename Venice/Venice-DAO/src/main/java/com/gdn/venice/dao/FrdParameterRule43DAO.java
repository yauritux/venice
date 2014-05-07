package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.venice.persistence.FrdParameterRule43;

public interface FrdParameterRule43DAO extends JpaRepository<FrdParameterRule43, Long> {
	public FrdParameterRule43 findByDescription(String desc);
}
