package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.venice.persistence.FrdParameterRule44;

public interface FrdParameterRule44DAO extends JpaRepository<FrdParameterRule44, Long> {
	public FrdParameterRule44 findByDescription(String desc);
}
