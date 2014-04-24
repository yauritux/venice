package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;

import com.gdn.venice.persistence.SeatResultStatusTracking;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Remote
public interface SeatResultStatusTrackingSessionEJBRemote {

	/**
	 * queryByRange - allows querying by range/block
	 * 
	 * @param jpqlStmt
	 * @param firstResult
	 * @param maxResults
	 * @return a list of SeatResultStatusTracking
	 */
	public List<SeatResultStatusTracking> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/**
	 * persistSeatResultStatusTracking persists a country
	 * 
	 * @param seatResultStatusTracking
	 * @return the persisted SeatResultStatusTracking
	 */
	public SeatResultStatusTracking persistSeatResultStatusTracking(SeatResultStatusTracking seatResultStatusTracking);

	/**
	 * persistSeatResultStatusTrackingList - persists a list of SeatResultStatusTracking
	 * 
	 * @param seatResultStatusTrackingList
	 * @return the list of persisted SeatResultStatusTracking
	 */
	public ArrayList<SeatResultStatusTracking> persistSeatResultStatusTrackingList(
			List<SeatResultStatusTracking> seatResultStatusTrackingList);

	/**
	 * mergeSeatResultStatusTracking - merges a SeatResultStatusTracking
	 * 
	 * @param seatResultStatusTracking
	 * @return the merged SeatResultStatusTracking
	 */
	public SeatResultStatusTracking mergeSeatResultStatusTracking(SeatResultStatusTracking seatResultStatusTracking);

	/**
	 * mergeSeatResultStatusTrackingList - merges a list of SeatResultStatusTracking
	 * 
	 * @param seatResultStatusTrackingList
	 * @return the merged list of SeatResultStatusTracking
	 */
	public ArrayList<SeatResultStatusTracking> mergeSeatResultStatusTrackingList(
			List<SeatResultStatusTracking> seatResultStatusTrackingList);

	/**
	 * removeSeatResultStatusTracking - removes a SeatResultStatusTracking
	 * 
	 * @param seatResultStatusTracking
	 */
	public void removeSeatResultStatusTracking(SeatResultStatusTracking seatResultStatusTracking);

	/**
	 * removeSeatResultStatusTrackingList - removes a list of SeatResultStatusTracking
	 * 
	 * @param seatResultStatusTrackingList
	 */
	public void removeSeatResultStatusTrackingList(List<SeatResultStatusTracking> seatResultStatusTrackingList);

	/**
	 * findBySeatResultStatusTrackingLike - finds a list of SeatResultStatusTracking Like
	 * 
	 * @param seatResultStatusTracking
	 * @return the list of SeatResultStatusTracking found
	 */
	public List<SeatResultStatusTracking> findBySeatResultStatusTrackingLike(SeatResultStatusTracking seatResultStatusTracking,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/**
	 * findBySeatResultStatusTracking>LikeFR - finds a list of SeatResultStatusTracking> Like with a finder return object
	 * 
	 * @param seatResultStatusTracking
	 * @return the list of SeatResultStatusTracking found
	 */
	public FinderReturn findBySeatResultStatusTrackingLikeFR(SeatResultStatusTracking seatResultStatusTracking,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
}
