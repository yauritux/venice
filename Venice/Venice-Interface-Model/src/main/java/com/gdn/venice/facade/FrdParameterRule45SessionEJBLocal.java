package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Local;

import com.gdn.venice.persistence.FrdParameterRule45;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Local
public interface FrdParameterRule45SessionEJBLocal {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule45SessionEJBRemote#queryByRange(java.lang
	 * .String, int, int)
	 */
	public List<FrdParameterRule45> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule45SessionEJBRemote#persistFrdParameterRule45(com
	 * .gdn.venice.persistence.FrdParameterRule45)
	 */
	public FrdParameterRule45 persistFrdParameterRule45(FrdParameterRule45 frdParameterRule45);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule45SessionEJBRemote#persistFrdParameterRule45List
	 * (java.util.List)
	 */
	public ArrayList<FrdParameterRule45> persistFrdParameterRule45List(
			List<FrdParameterRule45> frdParameterRule45List);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule45SessionEJBRemote#mergeFrdParameterRule45(com.
	 * gdn.venice.persistence.FrdParameterRule45)
	 */
	public FrdParameterRule45 mergeFrdParameterRule45(FrdParameterRule45 frdParameterRule45);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule45SessionEJBRemote#mergeFrdParameterRule45List(
	 * java.util.List)
	 */
	public ArrayList<FrdParameterRule45> mergeFrdParameterRule45List(
			List<FrdParameterRule45> frdParameterRule45List);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule45SessionEJBRemote#removeFrdParameterRule45(com
	 * .gdn.venice.persistence.FrdParameterRule45)
	 */
	public void removeFrdParameterRule45(FrdParameterRule45 frdParameterRule45);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule45SessionEJBRemote#removeFrdParameterRule45List
	 * (java.util.List)
	 */
	public void removeFrdParameterRule45List(List<FrdParameterRule45> frdParameterRule45List);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule45SessionEJBRemote#findByFrdParameterRule45Like
	 * (com.gdn.venice.persistence.FrdParameterRule45, int, int)
	 */
	public List<FrdParameterRule45> findByFrdParameterRule45Like(FrdParameterRule45 frdParameterRule45,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule45SessionEJBRemote#findByFrdParameterRule45LikeFR
	 * (com.gdn.venice.persistence.FrdParameterRule45, int, int)
	 */
	public FinderReturn findByFrdParameterRule45LikeFR(FrdParameterRule45 frdParameterRule45,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
	

}
