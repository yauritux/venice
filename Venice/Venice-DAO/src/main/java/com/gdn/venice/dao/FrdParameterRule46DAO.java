package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.venice.persistence.FrdParameterRule46;

public interface FrdParameterRule46DAO extends JpaRepository<FrdParameterRule46, Long> {
	public FrdParameterRule46 findByDescription(String desc);
}
