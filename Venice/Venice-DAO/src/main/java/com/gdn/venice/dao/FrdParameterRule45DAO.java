package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.venice.persistence.FrdParameterRule45;

public interface FrdParameterRule45DAO extends JpaRepository<FrdParameterRule45, Long> {
	public FrdParameterRule45 findByDescription(String description);
}
