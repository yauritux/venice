package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Local;

import com.gdn.venice.persistence.FrdParameterRule46;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Local
public interface FrdParameterRule46SessionEJBLocal {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule46SessionEJBRemote#queryByRange(java.lang
	 * .String, int, int)
	 */
	public List<FrdParameterRule46> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule46SessionEJBRemote#persistFrdParameterRule46(com
	 * .gdn.venice.persistence.FrdParameterRule46)
	 */
	public FrdParameterRule46 persistFrdParameterRule46(FrdParameterRule46 frdParameterRule46);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule46SessionEJBRemote#persistFrdParameterRule46List
	 * (java.util.List)
	 */
	public ArrayList<FrdParameterRule46> persistFrdParameterRule46List(
			List<FrdParameterRule46> frdParameterRule46List);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule46SessionEJBRemote#mergeFrdParameterRule46(com.
	 * gdn.venice.persistence.FrdParameterRule46)
	 */
	public FrdParameterRule46 mergeFrdParameterRule46(FrdParameterRule46 frdParameterRule46);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule46SessionEJBRemote#mergeFrdParameterRule46List(
	 * java.util.List)
	 */
	public ArrayList<FrdParameterRule46> mergeFrdParameterRule46List(
			List<FrdParameterRule46> frdParameterRule46List);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule46SessionEJBRemote#removeFrdParameterRule46(com
	 * .gdn.venice.persistence.FrdParameterRule46)
	 */
	public void removeFrdParameterRule46(FrdParameterRule46 frdParameterRule46);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule46SessionEJBRemote#removeFrdParameterRule46List
	 * (java.util.List)
	 */
	public void removeFrdParameterRule46List(List<FrdParameterRule46> frdParameterRule46List);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule46SessionEJBRemote#findByFrdParameterRule46Like
	 * (com.gdn.venice.persistence.FrdParameterRule46, int, int)
	 */
	public List<FrdParameterRule46> findByFrdParameterRule46Like(FrdParameterRule46 frdParameterRule46,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule46SessionEJBRemote#findByFrdParameterRule46LikeFR
	 * (com.gdn.venice.persistence.FrdParameterRule46, int, int)
	 */
	public FinderReturn findByFrdParameterRule46LikeFR(FrdParameterRule46 frdParameterRule46,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
	

}
