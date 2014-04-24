package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Local;

import com.gdn.venice.persistence.SeatFulfillmentConsistOfSlaStatus;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Local
public interface SeatFulfillmentConsistOfSlaStatusSessionEJBLocal {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatFulfillmentConsistOfSlaStatusSessionEJBRemote#queryByRange(java.lang
	 * .String, int, int)
	 */
	public List<SeatFulfillmentConsistOfSlaStatus> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatFulfillmentConsistOfSlaStatusSessionEJBRemote#persistSeatFulfillmentConsistOfSlaStatus(com
	 * .gdn.venice.persistence.SeatFulfillmentConsistOfSlaStatus)
	 */
	public SeatFulfillmentConsistOfSlaStatus persistSeatFulfillmentConsistOfSlaStatus(SeatFulfillmentConsistOfSlaStatus seatFulfillmentConsistOfSlaStatus);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatFulfillmentConsistOfSlaStatusSessionEJBRemote#persistSeatFulfillmentConsistOfSlaStatusList
	 * (java.util.List)
	 */
	public ArrayList<SeatFulfillmentConsistOfSlaStatus> persistSeatFulfillmentConsistOfSlaStatusList(
			List<SeatFulfillmentConsistOfSlaStatus> seatFulfillmentConsistOfSlaStatusList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatFulfillmentConsistOfSlaStatusSessionEJBRemote#mergeSeatFulfillmentConsistOfSlaStatus(com.
	 * gdn.venice.persistence.SeatFulfillmentConsistOfSlaStatus)
	 */
	public SeatFulfillmentConsistOfSlaStatus mergeSeatFulfillmentConsistOfSlaStatus(SeatFulfillmentConsistOfSlaStatus seatFulfillmentConsistOfSlaStatus);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatFulfillmentConsistOfSlaStatusSessionEJBRemote#mergeSeatFulfillmentConsistOfSlaStatusList(
	 * java.util.List)
	 */
	public ArrayList<SeatFulfillmentConsistOfSlaStatus> mergeSeatFulfillmentConsistOfSlaStatusList(
			List<SeatFulfillmentConsistOfSlaStatus> seatFulfillmentConsistOfSlaStatusList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatFulfillmentConsistOfSlaStatusSessionEJBRemote#removeSeatFulfillmentConsistOfSlaStatus(com
	 * .gdn.venice.persistence.SeatFulfillmentConsistOfSlaStatus)
	 */
	public void removeSeatFulfillmentConsistOfSlaStatus(SeatFulfillmentConsistOfSlaStatus seatFulfillmentConsistOfSlaStatus);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatFulfillmentConsistOfSlaStatusSessionEJBRemote#removeSeatFulfillmentConsistOfSlaStatusList
	 * (java.util.List)
	 */
	public void removeSeatFulfillmentConsistOfSlaStatusList(List<SeatFulfillmentConsistOfSlaStatus> seatFulfillmentConsistOfSlaStatusList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatFulfillmentConsistOfSlaStatusSessionEJBRemote#findBySeatFulfillmentConsistOfSlaStatusLike
	 * (com.gdn.venice.persistence.SeatFulfillmentConsistOfSlaStatus, int, int)
	 */
	public List<SeatFulfillmentConsistOfSlaStatus> findBySeatFulfillmentConsistOfSlaStatusLike(SeatFulfillmentConsistOfSlaStatus seatFulfillmentConsistOfSlaStatus,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatFulfillmentConsistOfSlaStatusSessionEJBRemote#findBySeatFulfillmentConsistOfSlaStatusLikeFR
	 * (com.gdn.venice.persistence.SeatFulfillmentConsistOfSlaStatus, int, int)
	 */
	public FinderReturn findBySeatFulfillmentConsistOfSlaStatusLikeFR(SeatFulfillmentConsistOfSlaStatus seatFulfillmentConsistOfSlaStatus,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
	

}
