package com.gdn.venice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.venice.persistence.FrdCustomerWhitelistBlacklist;

public interface FrdCustomerWhitelistBlacklistDAO extends JpaRepository<FrdCustomerWhitelistBlacklist, Long> {	
	public List<FrdCustomerWhitelistBlacklist> findByCcNumber(String ccNumber);
}