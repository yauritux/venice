package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.venice.persistence.FrdParameterRule19;

public interface FrdParameterRule19DAO extends JpaRepository<FrdParameterRule19, Long> {
	public FrdParameterRule19 findByCode(String code);
}
