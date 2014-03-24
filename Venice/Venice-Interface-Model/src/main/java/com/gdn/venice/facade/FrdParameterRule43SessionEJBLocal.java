package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Local;

import com.gdn.venice.persistence.FrdParameterRule43;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Local
public interface FrdParameterRule43SessionEJBLocal {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule43SessionEJBRemote#queryByRange(java.lang
	 * .String, int, int)
	 */
	public List<FrdParameterRule43> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule43SessionEJBRemote#persistFrdParameterRule43(com
	 * .gdn.venice.persistence.FrdParameterRule43)
	 */
	public FrdParameterRule43 persistFrdParameterRule43(FrdParameterRule43 frdParameterRule43);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule43SessionEJBRemote#persistFrdParameterRule43List
	 * (java.util.List)
	 */
	public ArrayList<FrdParameterRule43> persistFrdParameterRule43List(
			List<FrdParameterRule43> frdParameterRule43List);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule43SessionEJBRemote#mergeFrdParameterRule43(com.
	 * gdn.venice.persistence.FrdParameterRule43)
	 */
	public FrdParameterRule43 mergeFrdParameterRule43(FrdParameterRule43 frdParameterRule43);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule43SessionEJBRemote#mergeFrdParameterRule43List(
	 * java.util.List)
	 */
	public ArrayList<FrdParameterRule43> mergeFrdParameterRule43List(
			List<FrdParameterRule43> frdParameterRule43List);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule43SessionEJBRemote#removeFrdParameterRule43(com
	 * .gdn.venice.persistence.FrdParameterRule43)
	 */
	public void removeFrdParameterRule43(FrdParameterRule43 frdParameterRule43);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule43SessionEJBRemote#removeFrdParameterRule43List
	 * (java.util.List)
	 */
	public void removeFrdParameterRule43List(List<FrdParameterRule43> frdParameterRule43List);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule43SessionEJBRemote#findByFrdParameterRule43Like
	 * (com.gdn.venice.persistence.FrdParameterRule43, int, int)
	 */
	public List<FrdParameterRule43> findByFrdParameterRule43Like(FrdParameterRule43 frdParameterRule43,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule43SessionEJBRemote#findByFrdParameterRule43LikeFR
	 * (com.gdn.venice.persistence.FrdParameterRule43, int, int)
	 */
	public FinderReturn findByFrdParameterRule43LikeFR(FrdParameterRule43 frdParameterRule43,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
	

}
