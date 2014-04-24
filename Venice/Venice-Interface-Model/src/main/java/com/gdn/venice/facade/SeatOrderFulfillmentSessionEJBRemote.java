package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;

import com.gdn.venice.persistence.SeatOrderFulfillment;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Remote
public interface SeatOrderFulfillmentSessionEJBRemote {

	/**
	 * queryByRange - allows querying by range/block
	 * 
	 * @param jpqlStmt
	 * @param firstResult
	 * @param maxResults
	 * @return a list of SeatOrderFulfillment
	 */
	public List<SeatOrderFulfillment> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/**
	 * persistSeatOrderFulfillment persists a country
	 * 
	 * @param seatOrderFulfillment
	 * @return the persisted SeatOrderFulfillment
	 */
	public SeatOrderFulfillment persistSeatOrderFulfillment(SeatOrderFulfillment seatOrderFulfillment);

	/**
	 * persistSeatOrderFulfillmentList - persists a list of SeatOrderFulfillment
	 * 
	 * @param seatOrderFulfillmentList
	 * @return the list of persisted SeatOrderFulfillment
	 */
	public ArrayList<SeatOrderFulfillment> persistSeatOrderFulfillmentList(
			List<SeatOrderFulfillment> seatOrderFulfillmentList);

	/**
	 * mergeSeatOrderFulfillment - merges a SeatOrderFulfillment
	 * 
	 * @param seatOrderFulfillment
	 * @return the merged SeatOrderFulfillment
	 */
	public SeatOrderFulfillment mergeSeatOrderFulfillment(SeatOrderFulfillment seatOrderFulfillment);

	/**
	 * mergeSeatOrderFulfillmentList - merges a list of SeatOrderFulfillment
	 * 
	 * @param seatOrderFulfillmentList
	 * @return the merged list of SeatOrderFulfillment
	 */
	public ArrayList<SeatOrderFulfillment> mergeSeatOrderFulfillmentList(
			List<SeatOrderFulfillment> seatOrderFulfillmentList);

	/**
	 * removeSeatOrderFulfillment - removes a SeatOrderFulfillment
	 * 
	 * @param seatOrderFulfillment
	 */
	public void removeSeatOrderFulfillment(SeatOrderFulfillment seatOrderFulfillment);

	/**
	 * removeSeatOrderFulfillmentList - removes a list of SeatOrderFulfillment
	 * 
	 * @param seatOrderFulfillmentList
	 */
	public void removeSeatOrderFulfillmentList(List<SeatOrderFulfillment> seatOrderFulfillmentList);

	/**
	 * findBySeatOrderFulfillmentLike - finds a list of SeatOrderFulfillment Like
	 * 
	 * @param seatOrderFulfillment
	 * @return the list of SeatOrderFulfillment found
	 */
	public List<SeatOrderFulfillment> findBySeatOrderFulfillmentLike(SeatOrderFulfillment seatOrderFulfillment,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/**
	 * findBySeatOrderFulfillment>LikeFR - finds a list of SeatOrderFulfillment> Like with a finder return object
	 * 
	 * @param seatOrderFulfillment
	 * @return the list of SeatOrderFulfillment found
	 */
	public FinderReturn findBySeatOrderFulfillmentLikeFR(SeatOrderFulfillment seatOrderFulfillment,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
}
