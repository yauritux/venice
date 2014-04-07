package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.venice.persistence.FrdParameterRule41;

public interface FrdParameterRule41DAO extends JpaRepository<FrdParameterRule41, Long> {
	public FrdParameterRule41 findByDescription(String desc);
}
