package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Local;

import com.gdn.venice.persistence.SeatOrderStatusTracking;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Local
public interface SeatOrderStatusTrackingSessionEJBLocal {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderStatusTrackingSessionEJBRemote#queryByRange(java.lang
	 * .String, int, int)
	 */
	public List<SeatOrderStatusTracking> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderStatusTrackingSessionEJBRemote#persistSeatOrderStatusTracking(com
	 * .gdn.venice.persistence.SeatOrderStatusTracking)
	 */
	public SeatOrderStatusTracking persistSeatOrderStatusTracking(SeatOrderStatusTracking seatOrderStatusTracking);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderStatusTrackingSessionEJBRemote#persistSeatOrderStatusTrackingList
	 * (java.util.List)
	 */
	public ArrayList<SeatOrderStatusTracking> persistSeatOrderStatusTrackingList(
			List<SeatOrderStatusTracking> seatOrderStatusTrackingList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderStatusTrackingSessionEJBRemote#mergeSeatOrderStatusTracking(com.
	 * gdn.venice.persistence.SeatOrderStatusTracking)
	 */
	public SeatOrderStatusTracking mergeSeatOrderStatusTracking(SeatOrderStatusTracking seatOrderStatusTracking);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderStatusTrackingSessionEJBRemote#mergeSeatOrderStatusTrackingList(
	 * java.util.List)
	 */
	public ArrayList<SeatOrderStatusTracking> mergeSeatOrderStatusTrackingList(
			List<SeatOrderStatusTracking> seatOrderStatusTrackingList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderStatusTrackingSessionEJBRemote#removeSeatOrderStatusTracking(com
	 * .gdn.venice.persistence.SeatOrderStatusTracking)
	 */
	public void removeSeatOrderStatusTracking(SeatOrderStatusTracking seatOrderStatusTracking);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderStatusTrackingSessionEJBRemote#removeSeatOrderStatusTrackingList
	 * (java.util.List)
	 */
	public void removeSeatOrderStatusTrackingList(List<SeatOrderStatusTracking> seatOrderStatusTrackingList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderStatusTrackingSessionEJBRemote#findBySeatOrderStatusTrackingLike
	 * (com.gdn.venice.persistence.SeatOrderStatusTracking, int, int)
	 */
	public List<SeatOrderStatusTracking> findBySeatOrderStatusTrackingLike(SeatOrderStatusTracking seatOrderStatusTracking,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderStatusTrackingSessionEJBRemote#findBySeatOrderStatusTrackingLikeFR
	 * (com.gdn.venice.persistence.SeatOrderStatusTracking, int, int)
	 */
	public FinderReturn findBySeatOrderStatusTrackingLikeFR(SeatOrderStatusTracking seatOrderStatusTracking,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
	

}
