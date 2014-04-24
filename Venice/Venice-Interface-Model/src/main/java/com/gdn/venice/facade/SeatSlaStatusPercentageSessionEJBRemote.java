package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;

import com.gdn.venice.persistence.SeatSlaStatusPercentage;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Remote
public interface SeatSlaStatusPercentageSessionEJBRemote {

	/**
	 * queryByRange - allows querying by range/block
	 * 
	 * @param jpqlStmt
	 * @param firstResult
	 * @param maxResults
	 * @return a list of SeatSlaStatusPercentage
	 */
	public List<SeatSlaStatusPercentage> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/**
	 * persistSeatSlaStatusPercentage persists a country
	 * 
	 * @param seatSlaStatusPercentage
	 * @return the persisted SeatSlaStatusPercentage
	 */
	public SeatSlaStatusPercentage persistSeatSlaStatusPercentage(SeatSlaStatusPercentage seatSlaStatusPercentage);

	/**
	 * persistSeatSlaStatusPercentageList - persists a list of SeatSlaStatusPercentage
	 * 
	 * @param seatSlaStatusPercentageList
	 * @return the list of persisted SeatSlaStatusPercentage
	 */
	public ArrayList<SeatSlaStatusPercentage> persistSeatSlaStatusPercentageList(
			List<SeatSlaStatusPercentage> seatSlaStatusPercentageList);

	/**
	 * mergeSeatSlaStatusPercentage - merges a SeatSlaStatusPercentage
	 * 
	 * @param seatSlaStatusPercentage
	 * @return the merged SeatSlaStatusPercentage
	 */
	public SeatSlaStatusPercentage mergeSeatSlaStatusPercentage(SeatSlaStatusPercentage seatSlaStatusPercentage);

	/**
	 * mergeSeatSlaStatusPercentageList - merges a list of SeatSlaStatusPercentage
	 * 
	 * @param seatSlaStatusPercentageList
	 * @return the merged list of SeatSlaStatusPercentage
	 */
	public ArrayList<SeatSlaStatusPercentage> mergeSeatSlaStatusPercentageList(
			List<SeatSlaStatusPercentage> seatSlaStatusPercentageList);

	/**
	 * removeSeatSlaStatusPercentage - removes a SeatSlaStatusPercentage
	 * 
	 * @param seatSlaStatusPercentage
	 */
	public void removeSeatSlaStatusPercentage(SeatSlaStatusPercentage seatSlaStatusPercentage);

	/**
	 * removeSeatSlaStatusPercentageList - removes a list of SeatSlaStatusPercentage
	 * 
	 * @param seatSlaStatusPercentageList
	 */
	public void removeSeatSlaStatusPercentageList(List<SeatSlaStatusPercentage> seatSlaStatusPercentageList);

	/**
	 * findBySeatSlaStatusPercentageLike - finds a list of SeatSlaStatusPercentage Like
	 * 
	 * @param seatSlaStatusPercentage
	 * @return the list of SeatSlaStatusPercentage found
	 */
	public List<SeatSlaStatusPercentage> findBySeatSlaStatusPercentageLike(SeatSlaStatusPercentage seatSlaStatusPercentage,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/**
	 * findBySeatSlaStatusPercentage>LikeFR - finds a list of SeatSlaStatusPercentage> Like with a finder return object
	 * 
	 * @param seatSlaStatusPercentage
	 * @return the list of SeatSlaStatusPercentage found
	 */
	public FinderReturn findBySeatSlaStatusPercentageLikeFR(SeatSlaStatusPercentage seatSlaStatusPercentage,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
}
