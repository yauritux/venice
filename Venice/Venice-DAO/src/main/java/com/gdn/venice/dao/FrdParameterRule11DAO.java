package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.venice.persistence.FrdParameterRule11;
import com.gdn.venice.persistence.FrdParameterRule9;

public interface FrdParameterRule11DAO extends JpaRepository<FrdParameterRule11, Long> {
	public FrdParameterRule9 findByCode(String code);
}
