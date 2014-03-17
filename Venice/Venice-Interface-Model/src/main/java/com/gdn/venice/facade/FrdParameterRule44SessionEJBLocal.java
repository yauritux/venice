package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Local;

import com.gdn.venice.persistence.FrdParameterRule44;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Local
public interface FrdParameterRule44SessionEJBLocal {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule44SessionEJBRemote#queryByRange(java.lang
	 * .String, int, int)
	 */
	public List<FrdParameterRule44> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule44SessionEJBRemote#persistFrdParameterRule44(com
	 * .gdn.venice.persistence.FrdParameterRule44)
	 */
	public FrdParameterRule44 persistFrdParameterRule44(FrdParameterRule44 frdParameterRule44);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule44SessionEJBRemote#persistFrdParameterRule44List
	 * (java.util.List)
	 */
	public ArrayList<FrdParameterRule44> persistFrdParameterRule44List(
			List<FrdParameterRule44> frdParameterRule44List);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule44SessionEJBRemote#mergeFrdParameterRule44(com.
	 * gdn.venice.persistence.FrdParameterRule44)
	 */
	public FrdParameterRule44 mergeFrdParameterRule44(FrdParameterRule44 frdParameterRule44);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule44SessionEJBRemote#mergeFrdParameterRule44List(
	 * java.util.List)
	 */
	public ArrayList<FrdParameterRule44> mergeFrdParameterRule44List(
			List<FrdParameterRule44> frdParameterRule44List);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule44SessionEJBRemote#removeFrdParameterRule44(com
	 * .gdn.venice.persistence.FrdParameterRule44)
	 */
	public void removeFrdParameterRule44(FrdParameterRule44 frdParameterRule44);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule44SessionEJBRemote#removeFrdParameterRule44List
	 * (java.util.List)
	 */
	public void removeFrdParameterRule44List(List<FrdParameterRule44> frdParameterRule44List);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule44SessionEJBRemote#findByFrdParameterRule44Like
	 * (com.gdn.venice.persistence.FrdParameterRule44, int, int)
	 */
	public List<FrdParameterRule44> findByFrdParameterRule44Like(FrdParameterRule44 frdParameterRule44,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule44SessionEJBRemote#findByFrdParameterRule44LikeFR
	 * (com.gdn.venice.persistence.FrdParameterRule44, int, int)
	 */
	public FinderReturn findByFrdParameterRule44LikeFR(FrdParameterRule44 frdParameterRule44,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
	

}
