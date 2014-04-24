package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;

import com.gdn.venice.persistence.SeatSlaStatus;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Remote
public interface SeatSlaStatusSessionEJBRemote {

	/**
	 * queryByRange - allows querying by range/block
	 * 
	 * @param jpqlStmt
	 * @param firstResult
	 * @param maxResults
	 * @return a list of SeatSlaStatus
	 */
	public List<SeatSlaStatus> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/**
	 * persistSeatSlaStatus persists a country
	 * 
	 * @param seatSlaStatus
	 * @return the persisted SeatSlaStatus
	 */
	public SeatSlaStatus persistSeatSlaStatus(SeatSlaStatus seatSlaStatus);

	/**
	 * persistSeatSlaStatusList - persists a list of SeatSlaStatus
	 * 
	 * @param seatSlaStatusList
	 * @return the list of persisted SeatSlaStatus
	 */
	public ArrayList<SeatSlaStatus> persistSeatSlaStatusList(
			List<SeatSlaStatus> seatSlaStatusList);

	/**
	 * mergeSeatSlaStatus - merges a SeatSlaStatus
	 * 
	 * @param seatSlaStatus
	 * @return the merged SeatSlaStatus
	 */
	public SeatSlaStatus mergeSeatSlaStatus(SeatSlaStatus seatSlaStatus);

	/**
	 * mergeSeatSlaStatusList - merges a list of SeatSlaStatus
	 * 
	 * @param seatSlaStatusList
	 * @return the merged list of SeatSlaStatus
	 */
	public ArrayList<SeatSlaStatus> mergeSeatSlaStatusList(
			List<SeatSlaStatus> seatSlaStatusList);

	/**
	 * removeSeatSlaStatus - removes a SeatSlaStatus
	 * 
	 * @param seatSlaStatus
	 */
	public void removeSeatSlaStatus(SeatSlaStatus seatSlaStatus);

	/**
	 * removeSeatSlaStatusList - removes a list of SeatSlaStatus
	 * 
	 * @param seatSlaStatusList
	 */
	public void removeSeatSlaStatusList(List<SeatSlaStatus> seatSlaStatusList);

	/**
	 * findBySeatSlaStatusLike - finds a list of SeatSlaStatus Like
	 * 
	 * @param seatSlaStatus
	 * @return the list of SeatSlaStatus found
	 */
	public List<SeatSlaStatus> findBySeatSlaStatusLike(SeatSlaStatus seatSlaStatus,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/**
	 * findBySeatSlaStatus>LikeFR - finds a list of SeatSlaStatus> Like with a finder return object
	 * 
	 * @param seatSlaStatus
	 * @return the list of SeatSlaStatus found
	 */
	public FinderReturn findBySeatSlaStatusLikeFR(SeatSlaStatus seatSlaStatus,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
}
