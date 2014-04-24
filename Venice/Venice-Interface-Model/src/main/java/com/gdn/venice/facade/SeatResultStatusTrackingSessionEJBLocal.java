package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Local;

import com.gdn.venice.persistence.SeatResultStatusTracking;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Local
public interface SeatResultStatusTrackingSessionEJBLocal {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatResultStatusTrackingSessionEJBRemote#queryByRange(java.lang
	 * .String, int, int)
	 */
	public List<SeatResultStatusTracking> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatResultStatusTrackingSessionEJBRemote#persistSeatResultStatusTracking(com
	 * .gdn.venice.persistence.SeatResultStatusTracking)
	 */
	public SeatResultStatusTracking persistSeatResultStatusTracking(SeatResultStatusTracking seatResultStatusTracking);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatResultStatusTrackingSessionEJBRemote#persistSeatResultStatusTrackingList
	 * (java.util.List)
	 */
	public ArrayList<SeatResultStatusTracking> persistSeatResultStatusTrackingList(
			List<SeatResultStatusTracking> seatResultStatusTrackingList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatResultStatusTrackingSessionEJBRemote#mergeSeatResultStatusTracking(com.
	 * gdn.venice.persistence.SeatResultStatusTracking)
	 */
	public SeatResultStatusTracking mergeSeatResultStatusTracking(SeatResultStatusTracking seatResultStatusTracking);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatResultStatusTrackingSessionEJBRemote#mergeSeatResultStatusTrackingList(
	 * java.util.List)
	 */
	public ArrayList<SeatResultStatusTracking> mergeSeatResultStatusTrackingList(
			List<SeatResultStatusTracking> seatResultStatusTrackingList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatResultStatusTrackingSessionEJBRemote#removeSeatResultStatusTracking(com
	 * .gdn.venice.persistence.SeatResultStatusTracking)
	 */
	public void removeSeatResultStatusTracking(SeatResultStatusTracking seatResultStatusTracking);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatResultStatusTrackingSessionEJBRemote#removeSeatResultStatusTrackingList
	 * (java.util.List)
	 */
	public void removeSeatResultStatusTrackingList(List<SeatResultStatusTracking> seatResultStatusTrackingList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatResultStatusTrackingSessionEJBRemote#findBySeatResultStatusTrackingLike
	 * (com.gdn.venice.persistence.SeatResultStatusTracking, int, int)
	 */
	public List<SeatResultStatusTracking> findBySeatResultStatusTrackingLike(SeatResultStatusTracking seatResultStatusTracking,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatResultStatusTrackingSessionEJBRemote#findBySeatResultStatusTrackingLikeFR
	 * (com.gdn.venice.persistence.SeatResultStatusTracking, int, int)
	 */
	public FinderReturn findBySeatResultStatusTrackingLikeFR(SeatResultStatusTracking seatResultStatusTracking,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
	

}
