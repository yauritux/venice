package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;

import com.gdn.venice.persistence.SeatOrderStatusTracking;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Remote
public interface SeatOrderStatusTrackingSessionEJBRemote {

	/**
	 * queryByRange - allows querying by range/block
	 * 
	 * @param jpqlStmt
	 * @param firstResult
	 * @param maxResults
	 * @return a list of SeatOrderStatusTracking
	 */
	public List<SeatOrderStatusTracking> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/**
	 * persistSeatOrderStatusTracking persists a country
	 * 
	 * @param seatOrderStatusTracking
	 * @return the persisted SeatOrderStatusTracking
	 */
	public SeatOrderStatusTracking persistSeatOrderStatusTracking(SeatOrderStatusTracking seatOrderStatusTracking);

	/**
	 * persistSeatOrderStatusTrackingList - persists a list of SeatOrderStatusTracking
	 * 
	 * @param seatOrderStatusTrackingList
	 * @return the list of persisted SeatOrderStatusTracking
	 */
	public ArrayList<SeatOrderStatusTracking> persistSeatOrderStatusTrackingList(
			List<SeatOrderStatusTracking> seatOrderStatusTrackingList);

	/**
	 * mergeSeatOrderStatusTracking - merges a SeatOrderStatusTracking
	 * 
	 * @param seatOrderStatusTracking
	 * @return the merged SeatOrderStatusTracking
	 */
	public SeatOrderStatusTracking mergeSeatOrderStatusTracking(SeatOrderStatusTracking seatOrderStatusTracking);

	/**
	 * mergeSeatOrderStatusTrackingList - merges a list of SeatOrderStatusTracking
	 * 
	 * @param seatOrderStatusTrackingList
	 * @return the merged list of SeatOrderStatusTracking
	 */
	public ArrayList<SeatOrderStatusTracking> mergeSeatOrderStatusTrackingList(
			List<SeatOrderStatusTracking> seatOrderStatusTrackingList);

	/**
	 * removeSeatOrderStatusTracking - removes a SeatOrderStatusTracking
	 * 
	 * @param seatOrderStatusTracking
	 */
	public void removeSeatOrderStatusTracking(SeatOrderStatusTracking seatOrderStatusTracking);

	/**
	 * removeSeatOrderStatusTrackingList - removes a list of SeatOrderStatusTracking
	 * 
	 * @param seatOrderStatusTrackingList
	 */
	public void removeSeatOrderStatusTrackingList(List<SeatOrderStatusTracking> seatOrderStatusTrackingList);

	/**
	 * findBySeatOrderStatusTrackingLike - finds a list of SeatOrderStatusTracking Like
	 * 
	 * @param seatOrderStatusTracking
	 * @return the list of SeatOrderStatusTracking found
	 */
	public List<SeatOrderStatusTracking> findBySeatOrderStatusTrackingLike(SeatOrderStatusTracking seatOrderStatusTracking,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/**
	 * findBySeatOrderStatusTracking>LikeFR - finds a list of SeatOrderStatusTracking> Like with a finder return object
	 * 
	 * @param seatOrderStatusTracking
	 * @return the list of SeatOrderStatusTracking found
	 */
	public FinderReturn findBySeatOrderStatusTrackingLikeFR(SeatOrderStatusTracking seatOrderStatusTracking,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
}
