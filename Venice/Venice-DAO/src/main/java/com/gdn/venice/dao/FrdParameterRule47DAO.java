package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.venice.persistence.FrdParameterRule47;

public interface FrdParameterRule47DAO extends JpaRepository<FrdParameterRule47, Long> {
	public FrdParameterRule47 findByDescription(String desc);
}
