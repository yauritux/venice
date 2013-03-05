package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;

import com.gdn.venice.persistence.FrdParameterRule43;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Remote
public interface FrdParameterRule43SessionEJBRemote {

	/**
	 * queryByRange - allows querying by range/block
	 * 
	 * @param jpqlStmt
	 * @param firstResult
	 * @param maxResults
	 * @return a list of FrdParameterRule43
	 */
	public List<FrdParameterRule43> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/**
	 * persistFrdParameterRule43 persists a country
	 * 
	 * @param frdParameterRule43
	 * @return the persisted FrdParameterRule43
	 */
	public FrdParameterRule43 persistFrdParameterRule43(FrdParameterRule43 frdParameterRule43);

	/**
	 * persistFrdParameterRule43List - persists a list of FrdParameterRule43
	 * 
	 * @param frdParameterRule43List
	 * @return the list of persisted FrdParameterRule43
	 */
	public ArrayList<FrdParameterRule43> persistFrdParameterRule43List(
			List<FrdParameterRule43> frdParameterRule43List);

	/**
	 * mergeFrdParameterRule43 - merges a FrdParameterRule43
	 * 
	 * @param frdParameterRule43
	 * @return the merged FrdParameterRule43
	 */
	public FrdParameterRule43 mergeFrdParameterRule43(FrdParameterRule43 frdParameterRule43);

	/**
	 * mergeFrdParameterRule43List - merges a list of FrdParameterRule43
	 * 
	 * @param frdParameterRule43List
	 * @return the merged list of FrdParameterRule43
	 */
	public ArrayList<FrdParameterRule43> mergeFrdParameterRule43List(
			List<FrdParameterRule43> frdParameterRule43List);

	/**
	 * removeFrdParameterRule43 - removes a FrdParameterRule43
	 * 
	 * @param frdParameterRule43
	 */
	public void removeFrdParameterRule43(FrdParameterRule43 frdParameterRule43);

	/**
	 * removeFrdParameterRule43List - removes a list of FrdParameterRule43
	 * 
	 * @param frdParameterRule43List
	 */
	public void removeFrdParameterRule43List(List<FrdParameterRule43> frdParameterRule43List);

	/**
	 * findByFrdParameterRule43Like - finds a list of FrdParameterRule43 Like
	 * 
	 * @param frdParameterRule43
	 * @return the list of FrdParameterRule43 found
	 */
	public List<FrdParameterRule43> findByFrdParameterRule43Like(FrdParameterRule43 frdParameterRule43,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/**
	 * findByFrdParameterRule43>LikeFR - finds a list of FrdParameterRule43> Like with a finder return object
	 * 
	 * @param frdParameterRule43
	 * @return the list of FrdParameterRule43 found
	 */
	public FinderReturn findByFrdParameterRule43LikeFR(FrdParameterRule43 frdParameterRule43,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
}
