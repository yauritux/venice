package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;

import com.gdn.venice.persistence.VenOrderPaymentInstallmentHistory;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Remote
public interface VenOrderPaymentInstallmentHistorySessionEJBRemote {

	/**
	 * queryByRange - allows querying by range/block
	 * 
	 * @param jpqlStmt
	 * @param firstResult
	 * @param maxResults
	 * @return a list of VenOrderPaymentInstallmentHistory
	 */
	public List<VenOrderPaymentInstallmentHistory> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/**
	 * persistVenOrderPaymentInstallmentHistory persists a country
	 * 
	 * @param venOrderPaymentInstallmentHistory
	 * @return the persisted VenOrderPaymentInstallmentHistory
	 */
	public VenOrderPaymentInstallmentHistory persistVenOrderPaymentInstallmentHistory(VenOrderPaymentInstallmentHistory venOrderPaymentInstallmentHistory);

	/**
	 * persistVenOrderPaymentInstallmentHistoryList - persists a list of VenOrderPaymentInstallmentHistory
	 * 
	 * @param venOrderPaymentInstallmentHistoryList
	 * @return the list of persisted VenOrderPaymentInstallmentHistory
	 */
	public ArrayList<VenOrderPaymentInstallmentHistory> persistVenOrderPaymentInstallmentHistoryList(
			List<VenOrderPaymentInstallmentHistory> venOrderPaymentInstallmentHistoryList);

	/**
	 * mergeVenOrderPaymentInstallmentHistory - merges a VenOrderPaymentInstallmentHistory
	 * 
	 * @param venOrderPaymentInstallmentHistory
	 * @return the merged VenOrderPaymentInstallmentHistory
	 */
	public VenOrderPaymentInstallmentHistory mergeVenOrderPaymentInstallmentHistory(VenOrderPaymentInstallmentHistory venOrderPaymentInstallmentHistory);

	/**
	 * mergeVenOrderPaymentInstallmentHistoryList - merges a list of VenOrderPaymentInstallmentHistory
	 * 
	 * @param venOrderPaymentInstallmentHistoryList
	 * @return the merged list of VenOrderPaymentInstallmentHistory
	 */
	public ArrayList<VenOrderPaymentInstallmentHistory> mergeVenOrderPaymentInstallmentHistoryList(
			List<VenOrderPaymentInstallmentHistory> venOrderPaymentInstallmentHistoryList);

	/**
	 * removeVenOrderPaymentInstallmentHistory - removes a VenOrderPaymentInstallmentHistory
	 * 
	 * @param venOrderPaymentInstallmentHistory
	 */
	public void removeVenOrderPaymentInstallmentHistory(VenOrderPaymentInstallmentHistory venOrderPaymentInstallmentHistory);

	/**
	 * removeVenOrderPaymentInstallmentHistoryList - removes a list of VenOrderPaymentInstallmentHistory
	 * 
	 * @param venOrderPaymentInstallmentHistoryList
	 */
	public void removeVenOrderPaymentInstallmentHistoryList(List<VenOrderPaymentInstallmentHistory> venOrderPaymentInstallmentHistoryList);

	/**
	 * findByVenOrderPaymentInstallmentHistoryLike - finds a list of VenOrderPaymentInstallmentHistory Like
	 * 
	 * @param venOrderPaymentInstallmentHistory
	 * @return the list of VenOrderPaymentInstallmentHistory found
	 */
	public List<VenOrderPaymentInstallmentHistory> findByVenOrderPaymentInstallmentHistoryLike(VenOrderPaymentInstallmentHistory venOrderPaymentInstallmentHistory,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/**
	 * findByVenOrderPaymentInstallmentHistory>LikeFR - finds a list of VenOrderPaymentInstallmentHistory> Like with a finder return object
	 * 
	 * @param venOrderPaymentInstallmentHistory
	 * @return the list of VenOrderPaymentInstallmentHistory found
	 */
	public FinderReturn findByVenOrderPaymentInstallmentHistoryLikeFR(VenOrderPaymentInstallmentHistory venOrderPaymentInstallmentHistory,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
}
