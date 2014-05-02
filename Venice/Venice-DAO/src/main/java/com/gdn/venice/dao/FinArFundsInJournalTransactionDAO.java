package com.gdn.venice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.FinArFundsInJournalTransaction;

/**
 * 
 * @author yauritux
 *
 */
public interface FinArFundsInJournalTransactionDAO extends JpaRepository<FinArFundsInJournalTransaction, Long> {

	@Query("select o from FinArFundsInJournalTransaction o where o.finArFundsInReconRecords.reconciliationRecordId = ?1")
	public List<FinArFundsInJournalTransaction> findByReconcilicationRecordId(Long reconciliationRecordId);
}
