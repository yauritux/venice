package com.gdn.venice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.FinArFundsInAllocatePayment;

/**
 * 
 * @author yauritux
 *
 */
public interface FinArFundsInAllocatePaymentDAO extends JpaRepository<FinArFundsInAllocatePayment, Long> {
	
	public static final String FIND_BY_IDRECONRECORDSOURCE_ISACTIVE = "SELECT o FROM FinArFundsInAllocatePayment o " +
	   "WHERE o.idReconRecordSource = ?1 AND o.isactive = true";
	
	public static final String FIND_BY_IDRECONRECORDDESTINATION_ISACTIVE = "SELECT o FROM FinArFundsInAllocatePayment o " +
	   "WHERE o.idReconRecordDest = ?1 AND o.isactive = true";
	
	@Query(FIND_BY_IDRECONRECORDSOURCE_ISACTIVE)
	public List<FinArFundsInAllocatePayment> findByIdReconRecordSourceIsActive(Long idReconRecordSource);
	
	@Query(FIND_BY_IDRECONRECORDDESTINATION_ISACTIVE)
	public List<FinArFundsInAllocatePayment> findByIdReconRecordDestinationIsActive(Long idReconRecordDest);
	
	public List<FinArFundsInAllocatePayment> findByIdReconRecordSource(Long idReconRecordSource);
	public List<FinArFundsInAllocatePayment> findByIdReconRecordDest(Long idReconRecordDest);
}
