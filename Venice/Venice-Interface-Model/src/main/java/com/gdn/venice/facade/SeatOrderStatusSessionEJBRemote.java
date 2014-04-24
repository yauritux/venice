package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;

import com.gdn.venice.persistence.SeatOrderStatus;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Remote
public interface SeatOrderStatusSessionEJBRemote {

	/**
	 * queryByRange - allows querying by range/block
	 * 
	 * @param jpqlStmt
	 * @param firstResult
	 * @param maxResults
	 * @return a list of SeatOrderStatus
	 */
	public List<SeatOrderStatus> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/**
	 * persistSeatOrderStatus persists a country
	 * 
	 * @param seatOrderStatus
	 * @return the persisted SeatOrderStatus
	 */
	public SeatOrderStatus persistSeatOrderStatus(SeatOrderStatus seatOrderStatus);

	/**
	 * persistSeatOrderStatusList - persists a list of SeatOrderStatus
	 * 
	 * @param seatOrderStatusList
	 * @return the list of persisted SeatOrderStatus
	 */
	public ArrayList<SeatOrderStatus> persistSeatOrderStatusList(
			List<SeatOrderStatus> seatOrderStatusList);

	/**
	 * mergeSeatOrderStatus - merges a SeatOrderStatus
	 * 
	 * @param seatOrderStatus
	 * @return the merged SeatOrderStatus
	 */
	public SeatOrderStatus mergeSeatOrderStatus(SeatOrderStatus seatOrderStatus);

	/**
	 * mergeSeatOrderStatusList - merges a list of SeatOrderStatus
	 * 
	 * @param seatOrderStatusList
	 * @return the merged list of SeatOrderStatus
	 */
	public ArrayList<SeatOrderStatus> mergeSeatOrderStatusList(
			List<SeatOrderStatus> seatOrderStatusList);

	/**
	 * removeSeatOrderStatus - removes a SeatOrderStatus
	 * 
	 * @param seatOrderStatus
	 */
	public void removeSeatOrderStatus(SeatOrderStatus seatOrderStatus);

	/**
	 * removeSeatOrderStatusList - removes a list of SeatOrderStatus
	 * 
	 * @param seatOrderStatusList
	 */
	public void removeSeatOrderStatusList(List<SeatOrderStatus> seatOrderStatusList);

	/**
	 * findBySeatOrderStatusLike - finds a list of SeatOrderStatus Like
	 * 
	 * @param seatOrderStatus
	 * @return the list of SeatOrderStatus found
	 */
	public List<SeatOrderStatus> findBySeatOrderStatusLike(SeatOrderStatus seatOrderStatus,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/**
	 * findBySeatOrderStatus>LikeFR - finds a list of SeatOrderStatus> Like with a finder return object
	 * 
	 * @param seatOrderStatus
	 * @return the list of SeatOrderStatus found
	 */
	public FinderReturn findBySeatOrderStatusLikeFR(SeatOrderStatus seatOrderStatus,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
}
