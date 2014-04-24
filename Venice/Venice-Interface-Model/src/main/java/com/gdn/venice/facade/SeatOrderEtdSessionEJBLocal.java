package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Local;

import com.gdn.venice.persistence.SeatOrderEtd;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Local
public interface SeatOrderEtdSessionEJBLocal {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderEtdSessionEJBRemote#queryByRange(java.lang
	 * .String, int, int)
	 */
	public List<SeatOrderEtd> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderEtdSessionEJBRemote#persistSeatOrderEtd(com
	 * .gdn.venice.persistence.SeatOrderEtd)
	 */
	public SeatOrderEtd persistSeatOrderEtd(SeatOrderEtd seatOrderEtd);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderEtdSessionEJBRemote#persistSeatOrderEtdList
	 * (java.util.List)
	 */
	public ArrayList<SeatOrderEtd> persistSeatOrderEtdList(
			List<SeatOrderEtd> seatOrderEtdList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderEtdSessionEJBRemote#mergeSeatOrderEtd(com.
	 * gdn.venice.persistence.SeatOrderEtd)
	 */
	public SeatOrderEtd mergeSeatOrderEtd(SeatOrderEtd seatOrderEtd);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderEtdSessionEJBRemote#mergeSeatOrderEtdList(
	 * java.util.List)
	 */
	public ArrayList<SeatOrderEtd> mergeSeatOrderEtdList(
			List<SeatOrderEtd> seatOrderEtdList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderEtdSessionEJBRemote#removeSeatOrderEtd(com
	 * .gdn.venice.persistence.SeatOrderEtd)
	 */
	public void removeSeatOrderEtd(SeatOrderEtd seatOrderEtd);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderEtdSessionEJBRemote#removeSeatOrderEtdList
	 * (java.util.List)
	 */
	public void removeSeatOrderEtdList(List<SeatOrderEtd> seatOrderEtdList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderEtdSessionEJBRemote#findBySeatOrderEtdLike
	 * (com.gdn.venice.persistence.SeatOrderEtd, int, int)
	 */
	public List<SeatOrderEtd> findBySeatOrderEtdLike(SeatOrderEtd seatOrderEtd,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatOrderEtdSessionEJBRemote#findBySeatOrderEtdLikeFR
	 * (com.gdn.venice.persistence.SeatOrderEtd, int, int)
	 */
	public FinderReturn findBySeatOrderEtdLikeFR(SeatOrderEtd seatOrderEtd,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
	

}
