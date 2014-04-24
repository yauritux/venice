package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Local;

import com.gdn.venice.persistence.SeatSlaStatus;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Local
public interface SeatSlaStatusSessionEJBLocal {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatSlaStatusSessionEJBRemote#queryByRange(java.lang
	 * .String, int, int)
	 */
	public List<SeatSlaStatus> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatSlaStatusSessionEJBRemote#persistSeatSlaStatus(com
	 * .gdn.venice.persistence.SeatSlaStatus)
	 */
	public SeatSlaStatus persistSeatSlaStatus(SeatSlaStatus seatSlaStatus);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatSlaStatusSessionEJBRemote#persistSeatSlaStatusList
	 * (java.util.List)
	 */
	public ArrayList<SeatSlaStatus> persistSeatSlaStatusList(
			List<SeatSlaStatus> seatSlaStatusList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatSlaStatusSessionEJBRemote#mergeSeatSlaStatus(com.
	 * gdn.venice.persistence.SeatSlaStatus)
	 */
	public SeatSlaStatus mergeSeatSlaStatus(SeatSlaStatus seatSlaStatus);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatSlaStatusSessionEJBRemote#mergeSeatSlaStatusList(
	 * java.util.List)
	 */
	public ArrayList<SeatSlaStatus> mergeSeatSlaStatusList(
			List<SeatSlaStatus> seatSlaStatusList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatSlaStatusSessionEJBRemote#removeSeatSlaStatus(com
	 * .gdn.venice.persistence.SeatSlaStatus)
	 */
	public void removeSeatSlaStatus(SeatSlaStatus seatSlaStatus);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatSlaStatusSessionEJBRemote#removeSeatSlaStatusList
	 * (java.util.List)
	 */
	public void removeSeatSlaStatusList(List<SeatSlaStatus> seatSlaStatusList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatSlaStatusSessionEJBRemote#findBySeatSlaStatusLike
	 * (com.gdn.venice.persistence.SeatSlaStatus, int, int)
	 */
	public List<SeatSlaStatus> findBySeatSlaStatusLike(SeatSlaStatus seatSlaStatus,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatSlaStatusSessionEJBRemote#findBySeatSlaStatusLikeFR
	 * (com.gdn.venice.persistence.SeatSlaStatus, int, int)
	 */
	public FinderReturn findBySeatSlaStatusLikeFR(SeatSlaStatus seatSlaStatus,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
	

}
