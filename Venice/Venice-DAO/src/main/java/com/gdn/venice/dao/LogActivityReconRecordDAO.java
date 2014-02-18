package com.gdn.venice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.LogActivityReconRecord;
import com.gdn.venice.persistence.LogAirwayBill;

public interface LogActivityReconRecordDAO extends JpaRepository<LogActivityReconRecord, Long> {
	public static final String FIND_BY_LOGAIRWAYBILL_AND_RECON_IS_NOT_SETTLEMENT_MISMATCH_SQL = 
		"SELECT o " +
		"FROM LogActivityReconRecord o " +
		"INNER JOIN FETCH o.logReconActivityRecordResult rarr " +
		"WHERE o.logAirwayBill = ?1 " +
		"AND rarr.reconRecordResultId <> 2";
	
	public List<LogActivityReconRecord> findByLogAirwayBill(LogAirwayBill logAirwayBill);
	
	@Query(FIND_BY_LOGAIRWAYBILL_AND_RECON_IS_NOT_SETTLEMENT_MISMATCH_SQL)
	public List<LogActivityReconRecord> findByLogAirwayBillAndReconIsNotSettlementMismatch(LogAirwayBill logAirwayBill);
}
