package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Local;

import com.gdn.venice.persistence.SeatOrderStatus;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Local
public interface SeatOrderStatusSessionEJBLocal {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderStatusSessionEJBRemote#queryByRange(java.lang
	 * .String, int, int)
	 */
	public List<SeatOrderStatus> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderStatusSessionEJBRemote#persistSeatOrderStatus(com
	 * .gdn.venice.persistence.SeatOrderStatus)
	 */
	public SeatOrderStatus persistSeatOrderStatus(SeatOrderStatus seatOrderStatus);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderStatusSessionEJBRemote#persistSeatOrderStatusList
	 * (java.util.List)
	 */
	public ArrayList<SeatOrderStatus> persistSeatOrderStatusList(
			List<SeatOrderStatus> seatOrderStatusList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderStatusSessionEJBRemote#mergeSeatOrderStatus(com.
	 * gdn.venice.persistence.SeatOrderStatus)
	 */
	public SeatOrderStatus mergeSeatOrderStatus(SeatOrderStatus seatOrderStatus);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderStatusSessionEJBRemote#mergeSeatOrderStatusList(
	 * java.util.List)
	 */
	public ArrayList<SeatOrderStatus> mergeSeatOrderStatusList(
			List<SeatOrderStatus> seatOrderStatusList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderStatusSessionEJBRemote#removeSeatOrderStatus(com
	 * .gdn.venice.persistence.SeatOrderStatus)
	 */
	public void removeSeatOrderStatus(SeatOrderStatus seatOrderStatus);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderStatusSessionEJBRemote#removeSeatOrderStatusList
	 * (java.util.List)
	 */
	public void removeSeatOrderStatusList(List<SeatOrderStatus> seatOrderStatusList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderStatusSessionEJBRemote#findBySeatOrderStatusLike
	 * (com.gdn.venice.persistence.SeatOrderStatus, int, int)
	 */
	public List<SeatOrderStatus> findBySeatOrderStatusLike(SeatOrderStatus seatOrderStatus,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderStatusSessionEJBRemote#findBySeatOrderStatusLikeFR
	 * (com.gdn.venice.persistence.SeatOrderStatus, int, int)
	 */
	public FinderReturn findBySeatOrderStatusLikeFR(SeatOrderStatus seatOrderStatus,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
	

}
