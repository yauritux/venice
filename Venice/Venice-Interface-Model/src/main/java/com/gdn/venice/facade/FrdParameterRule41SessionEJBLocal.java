package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Local;

import com.gdn.venice.persistence.FrdParameterRule41;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Local
public interface FrdParameterRule41SessionEJBLocal {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule41SessionEJBRemote#queryByRange(java.lang
	 * .String, int, int)
	 */
	public List<FrdParameterRule41> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule41SessionEJBRemote#persistFrdParameterRule41(com
	 * .gdn.venice.persistence.FrdParameterRule41)
	 */
	public FrdParameterRule41 persistFrdParameterRule41(FrdParameterRule41 frdParameterRule41);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule41SessionEJBRemote#persistFrdParameterRule41List
	 * (java.util.List)
	 */
	public ArrayList<FrdParameterRule41> persistFrdParameterRule41List(
			List<FrdParameterRule41> frdParameterRule41List);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule41SessionEJBRemote#mergeFrdParameterRule41(com.
	 * gdn.venice.persistence.FrdParameterRule41)
	 */
	public FrdParameterRule41 mergeFrdParameterRule41(FrdParameterRule41 frdParameterRule41);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule41SessionEJBRemote#mergeFrdParameterRule41List(
	 * java.util.List)
	 */
	public ArrayList<FrdParameterRule41> mergeFrdParameterRule41List(
			List<FrdParameterRule41> frdParameterRule41List);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule41SessionEJBRemote#removeFrdParameterRule41(com
	 * .gdn.venice.persistence.FrdParameterRule41)
	 */
	public void removeFrdParameterRule41(FrdParameterRule41 frdParameterRule41);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule41SessionEJBRemote#removeFrdParameterRule41List
	 * (java.util.List)
	 */
	public void removeFrdParameterRule41List(List<FrdParameterRule41> frdParameterRule41List);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule41SessionEJBRemote#findByFrdParameterRule41Like
	 * (com.gdn.venice.persistence.FrdParameterRule41, int, int)
	 */
	public List<FrdParameterRule41> findByFrdParameterRule41Like(FrdParameterRule41 frdParameterRule41,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule41SessionEJBRemote#findByFrdParameterRule41LikeFR
	 * (com.gdn.venice.persistence.FrdParameterRule41, int, int)
	 */
	public FinderReturn findByFrdParameterRule41LikeFR(FrdParameterRule41 frdParameterRule41,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
	

}
