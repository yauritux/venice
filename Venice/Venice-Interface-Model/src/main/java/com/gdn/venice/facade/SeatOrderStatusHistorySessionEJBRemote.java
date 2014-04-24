package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;

import com.gdn.venice.persistence.SeatOrderStatusHistory;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Remote
public interface SeatOrderStatusHistorySessionEJBRemote {

	/**
	 * queryByRange - allows querying by range/block
	 * 
	 * @param jpqlStmt
	 * @param firstResult
	 * @param maxResults
	 * @return a list of SeatOrderStatusHistory
	 */
	public List<SeatOrderStatusHistory> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/**
	 * persistSeatOrderStatusHistory persists a country
	 * 
	 * @param seatOrderStatusHistory
	 * @return the persisted SeatOrderStatusHistory
	 */
	public SeatOrderStatusHistory persistSeatOrderStatusHistory(SeatOrderStatusHistory seatOrderStatusHistory);

	/**
	 * persistSeatOrderStatusHistoryList - persists a list of SeatOrderStatusHistory
	 * 
	 * @param seatOrderStatusHistoryList
	 * @return the list of persisted SeatOrderStatusHistory
	 */
	public ArrayList<SeatOrderStatusHistory> persistSeatOrderStatusHistoryList(
			List<SeatOrderStatusHistory> seatOrderStatusHistoryList);

	/**
	 * mergeSeatOrderStatusHistory - merges a SeatOrderStatusHistory
	 * 
	 * @param seatOrderStatusHistory
	 * @return the merged SeatOrderStatusHistory
	 */
	public SeatOrderStatusHistory mergeSeatOrderStatusHistory(SeatOrderStatusHistory seatOrderStatusHistory);

	/**
	 * mergeSeatOrderStatusHistoryList - merges a list of SeatOrderStatusHistory
	 * 
	 * @param seatOrderStatusHistoryList
	 * @return the merged list of SeatOrderStatusHistory
	 */
	public ArrayList<SeatOrderStatusHistory> mergeSeatOrderStatusHistoryList(
			List<SeatOrderStatusHistory> seatOrderStatusHistoryList);

	/**
	 * removeSeatOrderStatusHistory - removes a SeatOrderStatusHistory
	 * 
	 * @param seatOrderStatusHistory
	 */
	public void removeSeatOrderStatusHistory(SeatOrderStatusHistory seatOrderStatusHistory);

	/**
	 * removeSeatOrderStatusHistoryList - removes a list of SeatOrderStatusHistory
	 * 
	 * @param seatOrderStatusHistoryList
	 */
	public void removeSeatOrderStatusHistoryList(List<SeatOrderStatusHistory> seatOrderStatusHistoryList);

	/**
	 * findBySeatOrderStatusHistoryLike - finds a list of SeatOrderStatusHistory Like
	 * 
	 * @param seatOrderStatusHistory
	 * @return the list of SeatOrderStatusHistory found
	 */
	public List<SeatOrderStatusHistory> findBySeatOrderStatusHistoryLike(SeatOrderStatusHistory seatOrderStatusHistory,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/**
	 * findBySeatOrderStatusHistory>LikeFR - finds a list of SeatOrderStatusHistory> Like with a finder return object
	 * 
	 * @param seatOrderStatusHistory
	 * @return the list of SeatOrderStatusHistory found
	 */
	public FinderReturn findBySeatOrderStatusHistoryLikeFR(SeatOrderStatusHistory seatOrderStatusHistory,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
}
