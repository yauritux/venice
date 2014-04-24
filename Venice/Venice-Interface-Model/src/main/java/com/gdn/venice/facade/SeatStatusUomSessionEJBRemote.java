package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;

import com.gdn.venice.persistence.SeatStatusUom;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Remote
public interface SeatStatusUomSessionEJBRemote {

	/**
	 * queryByRange - allows querying by range/block
	 * 
	 * @param jpqlStmt
	 * @param firstResult
	 * @param maxResults
	 * @return a list of SeatStatusUom
	 */
	public List<SeatStatusUom> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/**
	 * persistSeatStatusUom persists a country
	 * 
	 * @param seatStatusUom
	 * @return the persisted SeatStatusUom
	 */
	public SeatStatusUom persistSeatStatusUom(SeatStatusUom seatStatusUom);

	/**
	 * persistSeatStatusUomList - persists a list of SeatStatusUom
	 * 
	 * @param seatStatusUomList
	 * @return the list of persisted SeatStatusUom
	 */
	public ArrayList<SeatStatusUom> persistSeatStatusUomList(
			List<SeatStatusUom> seatStatusUomList);

	/**
	 * mergeSeatStatusUom - merges a SeatStatusUom
	 * 
	 * @param seatStatusUom
	 * @return the merged SeatStatusUom
	 */
	public SeatStatusUom mergeSeatStatusUom(SeatStatusUom seatStatusUom);

	/**
	 * mergeSeatStatusUomList - merges a list of SeatStatusUom
	 * 
	 * @param seatStatusUomList
	 * @return the merged list of SeatStatusUom
	 */
	public ArrayList<SeatStatusUom> mergeSeatStatusUomList(
			List<SeatStatusUom> seatStatusUomList);

	/**
	 * removeSeatStatusUom - removes a SeatStatusUom
	 * 
	 * @param seatStatusUom
	 */
	public void removeSeatStatusUom(SeatStatusUom seatStatusUom);

	/**
	 * removeSeatStatusUomList - removes a list of SeatStatusUom
	 * 
	 * @param seatStatusUomList
	 */
	public void removeSeatStatusUomList(List<SeatStatusUom> seatStatusUomList);

	/**
	 * findBySeatStatusUomLike - finds a list of SeatStatusUom Like
	 * 
	 * @param seatStatusUom
	 * @return the list of SeatStatusUom found
	 */
	public List<SeatStatusUom> findBySeatStatusUomLike(SeatStatusUom seatStatusUom,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/**
	 * findBySeatStatusUom>LikeFR - finds a list of SeatStatusUom> Like with a finder return object
	 * 
	 * @param seatStatusUom
	 * @return the list of SeatStatusUom found
	 */
	public FinderReturn findBySeatStatusUomLikeFR(SeatStatusUom seatStatusUom,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
}
