package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Local;

import com.gdn.venice.persistence.SeatOrderStatusHistory;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Local
public interface SeatOrderStatusHistorySessionEJBLocal {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderStatusHistorySessionEJBRemote#queryByRange(java.lang
	 * .String, int, int)
	 */
	public List<SeatOrderStatusHistory> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderStatusHistorySessionEJBRemote#persistSeatOrderStatusHistory(com
	 * .gdn.venice.persistence.SeatOrderStatusHistory)
	 */
	public SeatOrderStatusHistory persistSeatOrderStatusHistory(SeatOrderStatusHistory seatOrderStatusHistory);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderStatusHistorySessionEJBRemote#persistSeatOrderStatusHistoryList
	 * (java.util.List)
	 */
	public ArrayList<SeatOrderStatusHistory> persistSeatOrderStatusHistoryList(
			List<SeatOrderStatusHistory> seatOrderStatusHistoryList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderStatusHistorySessionEJBRemote#mergeSeatOrderStatusHistory(com.
	 * gdn.venice.persistence.SeatOrderStatusHistory)
	 */
	public SeatOrderStatusHistory mergeSeatOrderStatusHistory(SeatOrderStatusHistory seatOrderStatusHistory);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderStatusHistorySessionEJBRemote#mergeSeatOrderStatusHistoryList(
	 * java.util.List)
	 */
	public ArrayList<SeatOrderStatusHistory> mergeSeatOrderStatusHistoryList(
			List<SeatOrderStatusHistory> seatOrderStatusHistoryList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderStatusHistorySessionEJBRemote#removeSeatOrderStatusHistory(com
	 * .gdn.venice.persistence.SeatOrderStatusHistory)
	 */
	public void removeSeatOrderStatusHistory(SeatOrderStatusHistory seatOrderStatusHistory);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderStatusHistorySessionEJBRemote#removeSeatOrderStatusHistoryList
	 * (java.util.List)
	 */
	public void removeSeatOrderStatusHistoryList(List<SeatOrderStatusHistory> seatOrderStatusHistoryList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderStatusHistorySessionEJBRemote#findBySeatOrderStatusHistoryLike
	 * (com.gdn.venice.persistence.SeatOrderStatusHistory, int, int)
	 */
	public List<SeatOrderStatusHistory> findBySeatOrderStatusHistoryLike(SeatOrderStatusHistory seatOrderStatusHistory,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderStatusHistorySessionEJBRemote#findBySeatOrderStatusHistoryLikeFR
	 * (com.gdn.venice.persistence.SeatOrderStatusHistory, int, int)
	 */
	public FinderReturn findBySeatOrderStatusHistoryLikeFR(SeatOrderStatusHistory seatOrderStatusHistory,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
	

}
