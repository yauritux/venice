package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;

import com.gdn.venice.persistence.FrdParameterRule45;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Remote
public interface FrdParameterRule45SessionEJBRemote {

	/**
	 * queryByRange - allows querying by range/block
	 * 
	 * @param jpqlStmt
	 * @param firstResult
	 * @param maxResults
	 * @return a list of FrdParameterRule45
	 */
	public List<FrdParameterRule45> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/**
	 * persistFrdParameterRule45 persists a country
	 * 
	 * @param frdParameterRule45
	 * @return the persisted FrdParameterRule45
	 */
	public FrdParameterRule45 persistFrdParameterRule45(FrdParameterRule45 frdParameterRule45);

	/**
	 * persistFrdParameterRule45List - persists a list of FrdParameterRule45
	 * 
	 * @param frdParameterRule45List
	 * @return the list of persisted FrdParameterRule45
	 */
	public ArrayList<FrdParameterRule45> persistFrdParameterRule45List(
			List<FrdParameterRule45> frdParameterRule45List);

	/**
	 * mergeFrdParameterRule45 - merges a FrdParameterRule45
	 * 
	 * @param frdParameterRule45
	 * @return the merged FrdParameterRule45
	 */
	public FrdParameterRule45 mergeFrdParameterRule45(FrdParameterRule45 frdParameterRule45);

	/**
	 * mergeFrdParameterRule45List - merges a list of FrdParameterRule45
	 * 
	 * @param frdParameterRule45List
	 * @return the merged list of FrdParameterRule45
	 */
	public ArrayList<FrdParameterRule45> mergeFrdParameterRule45List(
			List<FrdParameterRule45> frdParameterRule45List);

	/**
	 * removeFrdParameterRule45 - removes a FrdParameterRule45
	 * 
	 * @param frdParameterRule45
	 */
	public void removeFrdParameterRule45(FrdParameterRule45 frdParameterRule45);

	/**
	 * removeFrdParameterRule45List - removes a list of FrdParameterRule45
	 * 
	 * @param frdParameterRule45List
	 */
	public void removeFrdParameterRule45List(List<FrdParameterRule45> frdParameterRule45List);

	/**
	 * findByFrdParameterRule45Like - finds a list of FrdParameterRule45 Like
	 * 
	 * @param frdParameterRule45
	 * @return the list of FrdParameterRule45 found
	 */
	public List<FrdParameterRule45> findByFrdParameterRule45Like(FrdParameterRule45 frdParameterRule45,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/**
	 * findByFrdParameterRule45>LikeFR - finds a list of FrdParameterRule45> Like with a finder return object
	 * 
	 * @param frdParameterRule45
	 * @return the list of FrdParameterRule45 found
	 */
	public FinderReturn findByFrdParameterRule45LikeFR(FrdParameterRule45 frdParameterRule45,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
}
