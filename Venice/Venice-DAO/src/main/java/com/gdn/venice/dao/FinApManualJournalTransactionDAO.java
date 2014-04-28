package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.venice.persistence.FinApManualJournalTransaction;

/**
 * 
 * @author yauritux
 *
 */
public interface FinApManualJournalTransactionDAO extends JpaRepository<FinApManualJournalTransaction, Long> {

}
