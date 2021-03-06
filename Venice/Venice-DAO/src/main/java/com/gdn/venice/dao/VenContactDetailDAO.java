package com.gdn.venice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import antlr.collections.impl.LList;

import com.gdn.venice.persistence.VenContactDetail;
import com.gdn.venice.persistence.VenParty;

/**
 * 
 * @author yauritux
 *
 */
public interface VenContactDetailDAO extends JpaRepository<VenContactDetail, Long>{
	
	String FIND_BY_PARTY 
	   = "SELECT vcd FROM VenContactDetail vcd INNER JOIN vcd.venParty vp WHERE vp = ?1";

    @Query(FIND_BY_PARTY)
	public List<VenContactDetail> findByParty(VenParty venParty);
    
    public List<VenContactDetail> findByContactDetail(String contactDetail);
}
