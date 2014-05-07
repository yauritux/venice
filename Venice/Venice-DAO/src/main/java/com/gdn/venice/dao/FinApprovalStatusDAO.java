package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.venice.persistence.FinApprovalStatus;

/**
 * 
 * @author Daniel Hutama Putra
 *
 */

public interface FinApprovalStatusDAO extends JpaRepository<FinApprovalStatus, Long>{
	
	public FinApprovalStatus findByApprovalStatusId(Long approvalStatusId);
	
}
