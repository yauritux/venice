package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Local;

import com.gdn.venice.persistence.SeatStatusUom;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Local
public interface SeatStatusUomSessionEJBLocal {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatStatusUomSessionEJBRemote#queryByRange(java.lang
	 * .String, int, int)
	 */
	public List<SeatStatusUom> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatStatusUomSessionEJBRemote#persistSeatStatusUom(com
	 * .gdn.venice.persistence.SeatStatusUom)
	 */
	public SeatStatusUom persistSeatStatusUom(SeatStatusUom seatStatusUom);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatStatusUomSessionEJBRemote#persistSeatStatusUomList
	 * (java.util.List)
	 */
	public ArrayList<SeatStatusUom> persistSeatStatusUomList(
			List<SeatStatusUom> seatStatusUomList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatStatusUomSessionEJBRemote#mergeSeatStatusUom(com.
	 * gdn.venice.persistence.SeatStatusUom)
	 */
	public SeatStatusUom mergeSeatStatusUom(SeatStatusUom seatStatusUom);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatStatusUomSessionEJBRemote#mergeSeatStatusUomList(
	 * java.util.List)
	 */
	public ArrayList<SeatStatusUom> mergeSeatStatusUomList(
			List<SeatStatusUom> seatStatusUomList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatStatusUomSessionEJBRemote#removeSeatStatusUom(com
	 * .gdn.venice.persistence.SeatStatusUom)
	 */
	public void removeSeatStatusUom(SeatStatusUom seatStatusUom);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatStatusUomSessionEJBRemote#removeSeatStatusUomList
	 * (java.util.List)
	 */
	public void removeSeatStatusUomList(List<SeatStatusUom> seatStatusUomList);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatStatusUomSessionEJBRemote#findBySeatStatusUomLike
	 * (com.gdn.venice.persistence.SeatStatusUom, int, int)
	 */
	public List<SeatStatusUom> findBySeatStatusUomLike(SeatStatusUom seatStatusUom,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatStatusUomSessionEJBRemote#findBySeatStatusUomLikeFR
	 * (com.gdn.venice.persistence.SeatStatusUom, int, int)
	 */
	public FinderReturn findBySeatStatusUomLikeFR(SeatStatusUom seatStatusUom,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
	

}
