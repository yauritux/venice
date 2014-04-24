package com.gdn.venice.dao;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.venice.persistence.SeatOrderEtd;

public interface SeatOrderEtdDAO extends JpaRepository<SeatOrderEtd, Long> {
	public ArrayList<SeatOrderEtd> findByWcsOrderId(String wcsOrderId);
	
}
