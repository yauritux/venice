package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.venice.persistence.FinJournal;

/**
 * 
 * @author Daniel Hutama Putra
 *
 */

public interface FinJournalDAO extends JpaRepository<FinJournal, Long>{
	
	public FinJournal findByJournalId(Long journalId);
	
}
