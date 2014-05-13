package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Local;

import com.gdn.venice.persistence.FrdParameterRule48;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Local
public interface FrdParameterRule48SessionEJBLocal {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule48SessionEJBRemote#queryByRange(java.lang
	 * .String, int, int)
	 */
	public List<FrdParameterRule48> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule48SessionEJBRemote#persistFrdParameterRule48(com
	 * .gdn.venice.persistence.FrdParameterRule48)
	 */
	public FrdParameterRule48 persistFrdParameterRule48(FrdParameterRule48 frdParameterRule48);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule48SessionEJBRemote#persistFrdParameterRule48List
	 * (java.util.List)
	 */
	public ArrayList<FrdParameterRule48> persistFrdParameterRule48List(
			List<FrdParameterRule48> frdParameterRule48List);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule48SessionEJBRemote#mergeFrdParameterRule48(com.
	 * gdn.venice.persistence.FrdParameterRule48)
	 */
	public FrdParameterRule48 mergeFrdParameterRule48(FrdParameterRule48 frdParameterRule48);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule48SessionEJBRemote#mergeFrdParameterRule48List(
	 * java.util.List)
	 */
	public ArrayList<FrdParameterRule48> mergeFrdParameterRule48List(
			List<FrdParameterRule48> frdParameterRule48List);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule48SessionEJBRemote#removeFrdParameterRule48(com
	 * .gdn.venice.persistence.FrdParameterRule48)
	 */
	public void removeFrdParameterRule48(FrdParameterRule48 frdParameterRule48);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule48SessionEJBRemote#removeFrdParameterRule48List
	 * (java.util.List)
	 */
	public void removeFrdParameterRule48List(List<FrdParameterRule48> frdParameterRule48List);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule48SessionEJBRemote#findByFrdParameterRule48Like
	 * (com.gdn.venice.persistence.FrdParameterRule48, int, int)
	 */
	public List<FrdParameterRule48> findByFrdParameterRule48Like(FrdParameterRule48 frdParameterRule48,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule48SessionEJBRemote#findByFrdParameterRule48LikeFR
	 * (com.gdn.venice.persistence.FrdParameterRule48, int, int)
	 */
	public FinderReturn findByFrdParameterRule48LikeFR(FrdParameterRule48 frdParameterRule48,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
	

}
