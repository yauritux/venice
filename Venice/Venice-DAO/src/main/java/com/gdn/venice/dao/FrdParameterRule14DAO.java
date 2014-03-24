package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.venice.persistence.FrdParameterRule14;

public interface FrdParameterRule14DAO extends JpaRepository<FrdParameterRule14, Long> {
	public FrdParameterRule14 findByEmailType(String emailType);
}
