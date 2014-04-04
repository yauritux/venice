package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.venice.persistence.FrdParameterRule42;

public interface FrdParameterRule42DAO extends JpaRepository<FrdParameterRule42, Long> {
	public FrdParameterRule42 findByDescription(String desc);
}
