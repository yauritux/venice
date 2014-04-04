package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.venice.persistence.FrdParameterRule34;

public interface FrdParameterRule34DAO extends JpaRepository<FrdParameterRule34, Long> {
	public FrdParameterRule34 findByIpAddress(String ipAddress);
}
