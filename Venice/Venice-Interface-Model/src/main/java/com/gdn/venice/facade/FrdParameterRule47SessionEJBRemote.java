package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;

import com.gdn.venice.persistence.FrdParameterRule47;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Remote
public interface FrdParameterRule47SessionEJBRemote {

	/**
	 * queryByRange - allows querying by range/block
	 * 
	 * @param jpqlStmt
	 * @param firstResult
	 * @param maxResults
	 * @return a list of FrdParameterRule47
	 */
	public List<FrdParameterRule47> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/**
	 * persistFrdParameterRule47 persists a country
	 * 
	 * @param frdParameterRule47
	 * @return the persisted FrdParameterRule47
	 */
	public FrdParameterRule47 persistFrdParameterRule47(FrdParameterRule47 frdParameterRule47);

	/**
	 * persistFrdParameterRule47List - persists a list of FrdParameterRule47
	 * 
	 * @param frdParameterRule47List
	 * @return the list of persisted FrdParameterRule47
	 */
	public ArrayList<FrdParameterRule47> persistFrdParameterRule47List(
			List<FrdParameterRule47> frdParameterRule47List);

	/**
	 * mergeFrdParameterRule47 - merges a FrdParameterRule47
	 * 
	 * @param frdParameterRule47
	 * @return the merged FrdParameterRule47
	 */
	public FrdParameterRule47 mergeFrdParameterRule47(FrdParameterRule47 frdParameterRule47);

	/**
	 * mergeFrdParameterRule47List - merges a list of FrdParameterRule47
	 * 
	 * @param frdParameterRule47List
	 * @return the merged list of FrdParameterRule47
	 */
	public ArrayList<FrdParameterRule47> mergeFrdParameterRule47List(
			List<FrdParameterRule47> frdParameterRule47List);

	/**
	 * removeFrdParameterRule47 - removes a FrdParameterRule47
	 * 
	 * @param frdParameterRule47
	 */
	public void removeFrdParameterRule47(FrdParameterRule47 frdParameterRule47);

	/**
	 * removeFrdParameterRule47List - removes a list of FrdParameterRule47
	 * 
	 * @param frdParameterRule47List
	 */
	public void removeFrdParameterRule47List(List<FrdParameterRule47> frdParameterRule47List);

	/**
	 * findByFrdParameterRule47Like - finds a list of FrdParameterRule47 Like
	 * 
	 * @param frdParameterRule47
	 * @return the list of FrdParameterRule47 found
	 */
	public List<FrdParameterRule47> findByFrdParameterRule47Like(FrdParameterRule47 frdParameterRule47,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/**
	 * findByFrdParameterRule47>LikeFR - finds a list of FrdParameterRule47> Like with a finder return object
	 * 
	 * @param frdParameterRule47
	 * @return the list of FrdParameterRule47 found
	 */
	public FinderReturn findByFrdParameterRule47LikeFR(FrdParameterRule47 frdParameterRule47,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
}
