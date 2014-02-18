package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Local;

import com.gdn.venice.persistence.VenOrderPaymentInstallmentHistory;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Local
public interface VenOrderPaymentInstallmentHistorySessionEJBLocal {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.VenOrderPaymentInstallmentHistorySessionEJBRemote#queryByRange(java.lang
	 * .String, int, int)
	 */
	public List<VenOrderPaymentInstallmentHistory> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.VenOrderPaymentInstallmentHistorySessionEJBRemote#persistVenOrderPaymentInstallmentHistory(com
	 * .gdn.venice.persistence.VenOrderPaymentInstallmentHistory)
	 */
	public VenOrderPaymentInstallmentHistory persistVenOrderPaymentInstallmentHistory(VenOrderPaymentInstallmentHistory venOrderPaymentInstallmentHistory);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.VenOrderPaymentInstallmentHistorySessionEJBRemote#persistVenOrderPaymentInstallmentHistoryList
	 * (java.util.List)
	 */
	public ArrayList<VenOrderPaymentInstallmentHistory> persistVenOrderPaymentInstallmentHistoryList(
			List<VenOrderPaymentInstallmentHistory> venOrderPaymentInstallmentHistoryList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.VenOrderPaymentInstallmentHistorySessionEJBRemote#mergeVenOrderPaymentInstallmentHistory(com.
	 * gdn.venice.persistence.VenOrderPaymentInstallmentHistory)
	 */
	public VenOrderPaymentInstallmentHistory mergeVenOrderPaymentInstallmentHistory(VenOrderPaymentInstallmentHistory venOrderPaymentInstallmentHistory);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.VenOrderPaymentInstallmentHistorySessionEJBRemote#mergeVenOrderPaymentInstallmentHistoryList(
	 * java.util.List)
	 */
	public ArrayList<VenOrderPaymentInstallmentHistory> mergeVenOrderPaymentInstallmentHistoryList(
			List<VenOrderPaymentInstallmentHistory> venOrderPaymentInstallmentHistoryList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.VenOrderPaymentInstallmentHistorySessionEJBRemote#removeVenOrderPaymentInstallmentHistory(com
	 * .gdn.venice.persistence.VenOrderPaymentInstallmentHistory)
	 */
	public void removeVenOrderPaymentInstallmentHistory(VenOrderPaymentInstallmentHistory venOrderPaymentInstallmentHistory);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.VenOrderPaymentInstallmentHistorySessionEJBRemote#removeVenOrderPaymentInstallmentHistoryList
	 * (java.util.List)
	 */
	public void removeVenOrderPaymentInstallmentHistoryList(List<VenOrderPaymentInstallmentHistory> venOrderPaymentInstallmentHistoryList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.VenOrderPaymentInstallmentHistorySessionEJBRemote#findByVenOrderPaymentInstallmentHistoryLike
	 * (com.gdn.venice.persistence.VenOrderPaymentInstallmentHistory, int, int)
	 */
	public List<VenOrderPaymentInstallmentHistory> findByVenOrderPaymentInstallmentHistoryLike(VenOrderPaymentInstallmentHistory venOrderPaymentInstallmentHistory,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.VenOrderPaymentInstallmentHistorySessionEJBRemote#findByVenOrderPaymentInstallmentHistoryLikeFR
	 * (com.gdn.venice.persistence.VenOrderPaymentInstallmentHistory, int, int)
	 */
	public FinderReturn findByVenOrderPaymentInstallmentHistoryLikeFR(VenOrderPaymentInstallmentHistory venOrderPaymentInstallmentHistory,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
	

}
