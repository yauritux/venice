package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.venice.persistence.FrdParameterRule48;

public interface FrdParameterRule48DAO extends JpaRepository<FrdParameterRule48, Long> {
	public FrdParameterRule48 findByDescription(String desc);
}
