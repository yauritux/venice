package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.venice.persistence.FrdParameterRule40;

public interface FrdParameterRule40DAO extends JpaRepository<FrdParameterRule40, Long> {
	public FrdParameterRule40 findByNoHp(String noHp);
}
