package com.gdn.venice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gdn.venice.persistence.FinJournalTransaction;

/**
 * 
 * @author yauritux
 *
 */
public interface FinJournalTransactionDAO extends JpaRepository<FinJournalTransaction, Long> {
	
	public FinJournalTransaction findByTransactionId(Long transactionId);
	
	@Query("SELECT o FROM FinJournalTransaction o WHERE o.comments LIKE %:sComment% AND and o.finTransactionType.transactionTypeId = :sTransType")
	public List<FinJournalTransaction> findByCommentsAndTransactionTypeId(@Param("sComment") String sComment, @Param("sTransType") Long transactionTypeId);	
}
