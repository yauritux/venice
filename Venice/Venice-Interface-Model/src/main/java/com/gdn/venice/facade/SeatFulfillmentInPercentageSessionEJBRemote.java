package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;

import com.gdn.venice.persistence.SeatFulfillmentInPercentage;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Remote
public interface SeatFulfillmentInPercentageSessionEJBRemote {

	/**
	 * queryByRange - allows querying by range/block
	 * 
	 * @param jpqlStmt
	 * @param firstResult
	 * @param maxResults
	 * @return a list of SeatFulfillmentInPercentage
	 */
	public List<SeatFulfillmentInPercentage> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/**
	 * persistSeatFulfillmentInPercentage persists a country
	 * 
	 * @param seatFulfillmentInPercentage
	 * @return the persisted SeatFulfillmentInPercentage
	 */
	public SeatFulfillmentInPercentage persistSeatFulfillmentInPercentage(SeatFulfillmentInPercentage seatFulfillmentInPercentage);

	/**
	 * persistSeatFulfillmentInPercentageList - persists a list of SeatFulfillmentInPercentage
	 * 
	 * @param seatFulfillmentInPercentageList
	 * @return the list of persisted SeatFulfillmentInPercentage
	 */
	public ArrayList<SeatFulfillmentInPercentage> persistSeatFulfillmentInPercentageList(
			List<SeatFulfillmentInPercentage> seatFulfillmentInPercentageList);

	/**
	 * mergeSeatFulfillmentInPercentage - merges a SeatFulfillmentInPercentage
	 * 
	 * @param seatFulfillmentInPercentage
	 * @return the merged SeatFulfillmentInPercentage
	 */
	public SeatFulfillmentInPercentage mergeSeatFulfillmentInPercentage(SeatFulfillmentInPercentage seatFulfillmentInPercentage);

	/**
	 * mergeSeatFulfillmentInPercentageList - merges a list of SeatFulfillmentInPercentage
	 * 
	 * @param seatFulfillmentInPercentageList
	 * @return the merged list of SeatFulfillmentInPercentage
	 */
	public ArrayList<SeatFulfillmentInPercentage> mergeSeatFulfillmentInPercentageList(
			List<SeatFulfillmentInPercentage> seatFulfillmentInPercentageList);

	/**
	 * removeSeatFulfillmentInPercentage - removes a SeatFulfillmentInPercentage
	 * 
	 * @param seatFulfillmentInPercentage
	 */
	public void removeSeatFulfillmentInPercentage(SeatFulfillmentInPercentage seatFulfillmentInPercentage);

	/**
	 * removeSeatFulfillmentInPercentageList - removes a list of SeatFulfillmentInPercentage
	 * 
	 * @param seatFulfillmentInPercentageList
	 */
	public void removeSeatFulfillmentInPercentageList(List<SeatFulfillmentInPercentage> seatFulfillmentInPercentageList);

	/**
	 * findBySeatFulfillmentInPercentageLike - finds a list of SeatFulfillmentInPercentage Like
	 * 
	 * @param seatFulfillmentInPercentage
	 * @return the list of SeatFulfillmentInPercentage found
	 */
	public List<SeatFulfillmentInPercentage> findBySeatFulfillmentInPercentageLike(SeatFulfillmentInPercentage seatFulfillmentInPercentage,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/**
	 * findBySeatFulfillmentInPercentage>LikeFR - finds a list of SeatFulfillmentInPercentage> Like with a finder return object
	 * 
	 * @param seatFulfillmentInPercentage
	 * @return the list of SeatFulfillmentInPercentage found
	 */
	public FinderReturn findBySeatFulfillmentInPercentageLikeFR(SeatFulfillmentInPercentage seatFulfillmentInPercentage,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
}
