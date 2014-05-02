package com.gdn.venice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.FinJournalApprovalGroup;

/**
 * 
 * @author yauritux
 *
 */
public interface FinJournalApprovalGroupDAO extends JpaRepository<FinJournalApprovalGroup, Long> {
	
	public static final String FIND_BY_DESCRIPTION_AND_APPROVALSTATUSID = 
			"SELECT o FROM FinJournalApprovalGroup o WHERE o.journalGroupDesc = ?1 AND " +
	        "o.finApprovalStatus.approvalStatusId = ?2";
	
	@Query(FIND_BY_DESCRIPTION_AND_APPROVALSTATUSID)
	public List<FinJournalApprovalGroup> findByDescriptionAndApprovalStatusId(String description, Long approvalStatusId);
}
