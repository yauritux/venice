package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Local;

import com.gdn.venice.persistence.FrdParameterRule42;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Local
public interface FrdParameterRule42SessionEJBLocal {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule42SessionEJBRemote#queryByRange(java.lang
	 * .String, int, int)
	 */
	public List<FrdParameterRule42> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule42SessionEJBRemote#persistFrdParameterRule42(com
	 * .gdn.venice.persistence.FrdParameterRule42)
	 */
	public FrdParameterRule42 persistFrdParameterRule42(FrdParameterRule42 frdParameterRule42);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule42SessionEJBRemote#persistFrdParameterRule42List
	 * (java.util.List)
	 */
	public ArrayList<FrdParameterRule42> persistFrdParameterRule42List(
			List<FrdParameterRule42> frdParameterRule42List);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule42SessionEJBRemote#mergeFrdParameterRule42(com.
	 * gdn.venice.persistence.FrdParameterRule42)
	 */
	public FrdParameterRule42 mergeFrdParameterRule42(FrdParameterRule42 frdParameterRule42);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule42SessionEJBRemote#mergeFrdParameterRule42List(
	 * java.util.List)
	 */
	public ArrayList<FrdParameterRule42> mergeFrdParameterRule42List(
			List<FrdParameterRule42> frdParameterRule42List);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule42SessionEJBRemote#removeFrdParameterRule42(com
	 * .gdn.venice.persistence.FrdParameterRule42)
	 */
	public void removeFrdParameterRule42(FrdParameterRule42 frdParameterRule42);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule42SessionEJBRemote#removeFrdParameterRule42List
	 * (java.util.List)
	 */
	public void removeFrdParameterRule42List(List<FrdParameterRule42> frdParameterRule42List);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule42SessionEJBRemote#findByFrdParameterRule42Like
	 * (com.gdn.venice.persistence.FrdParameterRule42, int, int)
	 */
	public List<FrdParameterRule42> findByFrdParameterRule42Like(FrdParameterRule42 frdParameterRule42,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.FrdParameterRule42SessionEJBRemote#findByFrdParameterRule42LikeFR
	 * (com.gdn.venice.persistence.FrdParameterRule42, int, int)
	 */
	public FinderReturn findByFrdParameterRule42LikeFR(FrdParameterRule42 frdParameterRule42,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
	

}
