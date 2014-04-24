package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;

import com.gdn.venice.persistence.SeatOrderEtd;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Remote
public interface SeatOrderEtdSessionEJBRemote {

	/**
	 * queryByRange - allows querying by range/block
	 * 
	 * @param jpqlStmt
	 * @param firstResult
	 * @param maxResults
	 * @return a list of SeatOrderEtd
	 */
	public List<SeatOrderEtd> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/**
	 * persistSeatOrderEtd persists a country
	 * 
	 * @param seatOrderEtd
	 * @return the persisted SeatOrderEtd
	 */
	public SeatOrderEtd persistSeatOrderEtd(SeatOrderEtd seatOrderEtd);

	/**
	 * persistSeatOrderEtdList - persists a list of SeatOrderEtd
	 * 
	 * @param seatOrderEtdList
	 * @return the list of persisted SeatOrderEtd
	 */
	public ArrayList<SeatOrderEtd> persistSeatOrderEtdList(
			List<SeatOrderEtd> seatOrderEtdList);

	/**
	 * mergeSeatOrderEtd - merges a SeatOrderEtd
	 * 
	 * @param seatOrderEtd
	 * @return the merged SeatOrderEtd
	 */
	public SeatOrderEtd mergeSeatOrderEtd(SeatOrderEtd seatOrderEtd);

	/**
	 * mergeSeatOrderEtdList - merges a list of SeatOrderEtd
	 * 
	 * @param seatOrderEtdList
	 * @return the merged list of SeatOrderEtd
	 */
	public ArrayList<SeatOrderEtd> mergeSeatOrderEtdList(
			List<SeatOrderEtd> seatOrderEtdList);

	/**
	 * removeSeatOrderEtd - removes a SeatOrderEtd
	 * 
	 * @param seatOrderEtd
	 */
	public void removeSeatOrderEtd(SeatOrderEtd seatOrderEtd);

	/**
	 * removeSeatOrderEtdList - removes a list of SeatOrderEtd
	 * 
	 * @param seatOrderEtdList
	 */
	public void removeSeatOrderEtdList(List<SeatOrderEtd> seatOrderEtdList);

	/**
	 * findBySeatOrderEtdLike - finds a list of SeatOrderEtd Like
	 * 
	 * @param seatOrderEtd
	 * @return the list of SeatOrderEtd found
	 */
	public List<SeatOrderEtd> findBySeatOrderEtdLike(SeatOrderEtd seatOrderEtd,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/**
	 * findBySeatOrderEtd>LikeFR - finds a list of SeatOrderEtd> Like with a finder return object
	 * 
	 * @param seatOrderEtd
	 * @return the list of SeatOrderEtd found
	 */
	public FinderReturn findBySeatOrderEtdLikeFR(SeatOrderEtd seatOrderEtd,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
}
