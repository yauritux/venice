package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.venice.persistence.FrdParameterRule39;

public interface FrdParameterRule39DAO extends JpaRepository<FrdParameterRule39, Long> {
	public FrdParameterRule39 findByNoHp(String noHP);
}
