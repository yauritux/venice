package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.FrdParameterRule20;

public interface FrdParameterRule20DAO extends JpaRepository<FrdParameterRule20, Long> {
	public static final String FIND_BY_UPPERCASEPROVINCE =
		"SELECT o " +
		"FROM FrdParameterRule20 o " +
		"WHERE UPPER(o.provinsi) = ?1 ";
	
	@Query(FIND_BY_UPPERCASEPROVINCE)
	public FrdParameterRule20 findByUpperCaseProvince(String province);
}
