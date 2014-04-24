package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Local;

import com.gdn.venice.persistence.SeatSlaStatusPercentage;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Local
public interface SeatSlaStatusPercentageSessionEJBLocal {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatSlaStatusPercentageSessionEJBRemote#queryByRange(java.lang
	 * .String, int, int)
	 */
	public List<SeatSlaStatusPercentage> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatSlaStatusPercentageSessionEJBRemote#persistSeatSlaStatusPercentage(com
	 * .gdn.venice.persistence.SeatSlaStatusPercentage)
	 */
	public SeatSlaStatusPercentage persistSeatSlaStatusPercentage(SeatSlaStatusPercentage seatSlaStatusPercentage);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatSlaStatusPercentageSessionEJBRemote#persistSeatSlaStatusPercentageList
	 * (java.util.List)
	 */
	public ArrayList<SeatSlaStatusPercentage> persistSeatSlaStatusPercentageList(
			List<SeatSlaStatusPercentage> seatSlaStatusPercentageList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatSlaStatusPercentageSessionEJBRemote#mergeSeatSlaStatusPercentage(com.
	 * gdn.venice.persistence.SeatSlaStatusPercentage)
	 */
	public SeatSlaStatusPercentage mergeSeatSlaStatusPercentage(SeatSlaStatusPercentage seatSlaStatusPercentage);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatSlaStatusPercentageSessionEJBRemote#mergeSeatSlaStatusPercentageList(
	 * java.util.List)
	 */
	public ArrayList<SeatSlaStatusPercentage> mergeSeatSlaStatusPercentageList(
			List<SeatSlaStatusPercentage> seatSlaStatusPercentageList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatSlaStatusPercentageSessionEJBRemote#removeSeatSlaStatusPercentage(com
	 * .gdn.venice.persistence.SeatSlaStatusPercentage)
	 */
	public void removeSeatSlaStatusPercentage(SeatSlaStatusPercentage seatSlaStatusPercentage);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatSlaStatusPercentageSessionEJBRemote#removeSeatSlaStatusPercentageList
	 * (java.util.List)
	 */
	public void removeSeatSlaStatusPercentageList(List<SeatSlaStatusPercentage> seatSlaStatusPercentageList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatSlaStatusPercentageSessionEJBRemote#findBySeatSlaStatusPercentageLike
	 * (com.gdn.venice.persistence.SeatSlaStatusPercentage, int, int)
	 */
	public List<SeatSlaStatusPercentage> findBySeatSlaStatusPercentageLike(SeatSlaStatusPercentage seatSlaStatusPercentage,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatSlaStatusPercentageSessionEJBRemote#findBySeatSlaStatusPercentageLikeFR
	 * (com.gdn.venice.persistence.SeatSlaStatusPercentage, int, int)
	 */
	public FinderReturn findBySeatSlaStatusPercentageLikeFR(SeatSlaStatusPercentage seatSlaStatusPercentage,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
	

}
