package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Local;

import com.gdn.venice.persistence.SeatFulfillmentInPercentage;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Local
public interface SeatFulfillmentInPercentageSessionEJBLocal {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatFulfillmentInPercentageSessionEJBRemote#queryByRange(java.lang
	 * .String, int, int)
	 */
	public List<SeatFulfillmentInPercentage> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatFulfillmentInPercentageSessionEJBRemote#persistSeatFulfillmentInPercentage(com
	 * .gdn.venice.persistence.SeatFulfillmentInPercentage)
	 */
	public SeatFulfillmentInPercentage persistSeatFulfillmentInPercentage(SeatFulfillmentInPercentage seatFulfillmentInPercentage);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatFulfillmentInPercentageSessionEJBRemote#persistSeatFulfillmentInPercentageList
	 * (java.util.List)
	 */
	public ArrayList<SeatFulfillmentInPercentage> persistSeatFulfillmentInPercentageList(
			List<SeatFulfillmentInPercentage> seatFulfillmentInPercentageList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatFulfillmentInPercentageSessionEJBRemote#mergeSeatFulfillmentInPercentage(com.
	 * gdn.venice.persistence.SeatFulfillmentInPercentage)
	 */
	public SeatFulfillmentInPercentage mergeSeatFulfillmentInPercentage(SeatFulfillmentInPercentage seatFulfillmentInPercentage);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatFulfillmentInPercentageSessionEJBRemote#mergeSeatFulfillmentInPercentageList(
	 * java.util.List)
	 */
	public ArrayList<SeatFulfillmentInPercentage> mergeSeatFulfillmentInPercentageList(
			List<SeatFulfillmentInPercentage> seatFulfillmentInPercentageList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatFulfillmentInPercentageSessionEJBRemote#removeSeatFulfillmentInPercentage(com
	 * .gdn.venice.persistence.SeatFulfillmentInPercentage)
	 */
	public void removeSeatFulfillmentInPercentage(SeatFulfillmentInPercentage seatFulfillmentInPercentage);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatFulfillmentInPercentageSessionEJBRemote#removeSeatFulfillmentInPercentageList
	 * (java.util.List)
	 */
	public void removeSeatFulfillmentInPercentageList(List<SeatFulfillmentInPercentage> seatFulfillmentInPercentageList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatFulfillmentInPercentageSessionEJBRemote#findBySeatFulfillmentInPercentageLike
	 * (com.gdn.venice.persistence.SeatFulfillmentInPercentage, int, int)
	 */
	public List<SeatFulfillmentInPercentage> findBySeatFulfillmentInPercentageLike(SeatFulfillmentInPercentage seatFulfillmentInPercentage,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatFulfillmentInPercentageSessionEJBRemote#findBySeatFulfillmentInPercentageLikeFR
	 * (com.gdn.venice.persistence.SeatFulfillmentInPercentage, int, int)
	 */
	public FinderReturn findBySeatFulfillmentInPercentageLikeFR(SeatFulfillmentInPercentage seatFulfillmentInPercentage,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
	

}
