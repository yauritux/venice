package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Local;

import com.gdn.venice.persistence.SeatOrderFulfillment;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Local
public interface SeatOrderFulfillmentSessionEJBLocal {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderFulfillmentSessionEJBRemote#queryByRange(java.lang
	 * .String, int, int)
	 */
	public List<SeatOrderFulfillment> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderFulfillmentSessionEJBRemote#persistSeatOrderFulfillment(com
	 * .gdn.venice.persistence.SeatOrderFulfillment)
	 */
	public SeatOrderFulfillment persistSeatOrderFulfillment(SeatOrderFulfillment seatOrderFulfillment);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderFulfillmentSessionEJBRemote#persistSeatOrderFulfillmentList
	 * (java.util.List)
	 */
	public ArrayList<SeatOrderFulfillment> persistSeatOrderFulfillmentList(
			List<SeatOrderFulfillment> seatOrderFulfillmentList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderFulfillmentSessionEJBRemote#mergeSeatOrderFulfillment(com.
	 * gdn.venice.persistence.SeatOrderFulfillment)
	 */
	public SeatOrderFulfillment mergeSeatOrderFulfillment(SeatOrderFulfillment seatOrderFulfillment);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderFulfillmentSessionEJBRemote#mergeSeatOrderFulfillmentList(
	 * java.util.List)
	 */
	public ArrayList<SeatOrderFulfillment> mergeSeatOrderFulfillmentList(
			List<SeatOrderFulfillment> seatOrderFulfillmentList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderFulfillmentSessionEJBRemote#removeSeatOrderFulfillment(com
	 * .gdn.venice.persistence.SeatOrderFulfillment)
	 */
	public void removeSeatOrderFulfillment(SeatOrderFulfillment seatOrderFulfillment);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderFulfillmentSessionEJBRemote#removeSeatOrderFulfillmentList
	 * (java.util.List)
	 */
	public void removeSeatOrderFulfillmentList(List<SeatOrderFulfillment> seatOrderFulfillmentList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderFulfillmentSessionEJBRemote#findBySeatOrderFulfillmentLike
	 * (com.gdn.venice.persistence.SeatOrderFulfillment, int, int)
	 */
	public List<SeatOrderFulfillment> findBySeatOrderFulfillmentLike(SeatOrderFulfillment seatOrderFulfillment,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderFulfillmentSessionEJBRemote#findBySeatOrderFulfillmentLikeFR
	 * (com.gdn.venice.persistence.SeatOrderFulfillment, int, int)
	 */
	public FinderReturn findBySeatOrderFulfillmentLikeFR(SeatOrderFulfillment seatOrderFulfillment,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
	

}
