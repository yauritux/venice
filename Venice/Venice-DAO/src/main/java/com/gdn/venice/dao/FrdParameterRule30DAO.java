package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.FrdParameterRule30;

public interface FrdParameterRule30DAO extends JpaRepository<FrdParameterRule30, Long> {
	public static final String FIND_BY_UPPERNAMAKOTA_SQL = 
		"SELECT o " +
		"FROM FrdParameterRule30 o " +
		"WHERE UPPER(o.namaKota) = ?1 ";
	
	@Query(FIND_BY_UPPERNAMAKOTA_SQL)
	public FrdParameterRule30 findByUpperNamaKota(String uppercaseNamaKota);
}
