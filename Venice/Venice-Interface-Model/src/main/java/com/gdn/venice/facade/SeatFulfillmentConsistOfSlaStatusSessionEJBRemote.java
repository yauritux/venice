package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;

import com.gdn.venice.persistence.SeatFulfillmentConsistOfSlaStatus;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Remote
public interface SeatFulfillmentConsistOfSlaStatusSessionEJBRemote {

	/**
	 * queryByRange - allows querying by range/block
	 * 
	 * @param jpqlStmt
	 * @param firstResult
	 * @param maxResults
	 * @return a list of SeatFulfillmentConsistOfSlaStatus
	 */
	public List<SeatFulfillmentConsistOfSlaStatus> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/**
	 * persistSeatFulfillmentConsistOfSlaStatus persists a country
	 * 
	 * @param seatFulfillmentConsistOfSlaStatus
	 * @return the persisted SeatFulfillmentConsistOfSlaStatus
	 */
	public SeatFulfillmentConsistOfSlaStatus persistSeatFulfillmentConsistOfSlaStatus(SeatFulfillmentConsistOfSlaStatus seatFulfillmentConsistOfSlaStatus);

	/**
	 * persistSeatFulfillmentConsistOfSlaStatusList - persists a list of SeatFulfillmentConsistOfSlaStatus
	 * 
	 * @param seatFulfillmentConsistOfSlaStatusList
	 * @return the list of persisted SeatFulfillmentConsistOfSlaStatus
	 */
	public ArrayList<SeatFulfillmentConsistOfSlaStatus> persistSeatFulfillmentConsistOfSlaStatusList(
			List<SeatFulfillmentConsistOfSlaStatus> seatFulfillmentConsistOfSlaStatusList);

	/**
	 * mergeSeatFulfillmentConsistOfSlaStatus - merges a SeatFulfillmentConsistOfSlaStatus
	 * 
	 * @param seatFulfillmentConsistOfSlaStatus
	 * @return the merged SeatFulfillmentConsistOfSlaStatus
	 */
	public SeatFulfillmentConsistOfSlaStatus mergeSeatFulfillmentConsistOfSlaStatus(SeatFulfillmentConsistOfSlaStatus seatFulfillmentConsistOfSlaStatus);

	/**
	 * mergeSeatFulfillmentConsistOfSlaStatusList - merges a list of SeatFulfillmentConsistOfSlaStatus
	 * 
	 * @param seatFulfillmentConsistOfSlaStatusList
	 * @return the merged list of SeatFulfillmentConsistOfSlaStatus
	 */
	public ArrayList<SeatFulfillmentConsistOfSlaStatus> mergeSeatFulfillmentConsistOfSlaStatusList(
			List<SeatFulfillmentConsistOfSlaStatus> seatFulfillmentConsistOfSlaStatusList);

	/**
	 * removeSeatFulfillmentConsistOfSlaStatus - removes a SeatFulfillmentConsistOfSlaStatus
	 * 
	 * @param seatFulfillmentConsistOfSlaStatus
	 */
	public void removeSeatFulfillmentConsistOfSlaStatus(SeatFulfillmentConsistOfSlaStatus seatFulfillmentConsistOfSlaStatus);

	/**
	 * removeSeatFulfillmentConsistOfSlaStatusList - removes a list of SeatFulfillmentConsistOfSlaStatus
	 * 
	 * @param seatFulfillmentConsistOfSlaStatusList
	 */
	public void removeSeatFulfillmentConsistOfSlaStatusList(List<SeatFulfillmentConsistOfSlaStatus> seatFulfillmentConsistOfSlaStatusList);

	/**
	 * findBySeatFulfillmentConsistOfSlaStatusLike - finds a list of SeatFulfillmentConsistOfSlaStatus Like
	 * 
	 * @param seatFulfillmentConsistOfSlaStatus
	 * @return the list of SeatFulfillmentConsistOfSlaStatus found
	 */
	public List<SeatFulfillmentConsistOfSlaStatus> findBySeatFulfillmentConsistOfSlaStatusLike(SeatFulfillmentConsistOfSlaStatus seatFulfillmentConsistOfSlaStatus,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/**
	 * findBySeatFulfillmentConsistOfSlaStatus>LikeFR - finds a list of SeatFulfillmentConsistOfSlaStatus> Like with a finder return object
	 * 
	 * @param seatFulfillmentConsistOfSlaStatus
	 * @return the list of SeatFulfillmentConsistOfSlaStatus found
	 */
	public FinderReturn findBySeatFulfillmentConsistOfSlaStatusLikeFR(SeatFulfillmentConsistOfSlaStatus seatFulfillmentConsistOfSlaStatus,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
}
