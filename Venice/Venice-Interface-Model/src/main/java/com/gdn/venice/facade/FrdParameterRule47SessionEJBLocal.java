package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Local;

import com.gdn.venice.persistence.FrdParameterRule47;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Local
public interface FrdParameterRule47SessionEJBLocal {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule47SessionEJBRemote#queryByRange(java.lang
	 * .String, int, int)
	 */
	public List<FrdParameterRule47> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule47SessionEJBRemote#persistFrdParameterRule47(com
	 * .gdn.venice.persistence.FrdParameterRule47)
	 */
	public FrdParameterRule47 persistFrdParameterRule47(FrdParameterRule47 frdParameterRule47);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule47SessionEJBRemote#persistFrdParameterRule47List
	 * (java.util.List)
	 */
	public ArrayList<FrdParameterRule47> persistFrdParameterRule47List(
			List<FrdParameterRule47> frdParameterRule47List);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule47SessionEJBRemote#mergeFrdParameterRule47(com.
	 * gdn.venice.persistence.FrdParameterRule47)
	 */
	public FrdParameterRule47 mergeFrdParameterRule47(FrdParameterRule47 frdParameterRule47);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule47SessionEJBRemote#mergeFrdParameterRule47List(
	 * java.util.List)
	 */
	public ArrayList<FrdParameterRule47> mergeFrdParameterRule47List(
			List<FrdParameterRule47> frdParameterRule47List);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule47SessionEJBRemote#removeFrdParameterRule47(com
	 * .gdn.venice.persistence.FrdParameterRule47)
	 */
	public void removeFrdParameterRule47(FrdParameterRule47 frdParameterRule47);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule47SessionEJBRemote#removeFrdParameterRule47List
	 * (java.util.List)
	 */
	public void removeFrdParameterRule47List(List<FrdParameterRule47> frdParameterRule47List);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule47SessionEJBRemote#findByFrdParameterRule47Like
	 * (com.gdn.venice.persistence.FrdParameterRule47, int, int)
	 */
	public List<FrdParameterRule47> findByFrdParameterRule47Like(FrdParameterRule47 frdParameterRule47,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule47SessionEJBRemote#findByFrdParameterRule47LikeFR
	 * (com.gdn.venice.persistence.FrdParameterRule47, int, int)
	 */
	public FinderReturn findByFrdParameterRule47LikeFR(FrdParameterRule47 frdParameterRule47,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
	

}
