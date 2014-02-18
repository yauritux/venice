package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;

import com.gdn.venice.persistence.FrdParameterRule41;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Remote
public interface FrdParameterRule41SessionEJBRemote {

	/**
	 * queryByRange - allows querying by range/block
	 * 
	 * @param jpqlStmt
	 * @param firstResult
	 * @param maxResults
	 * @return a list of FrdParameterRule41
	 */
	public List<FrdParameterRule41> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/**
	 * persistFrdParameterRule41 persists a country
	 * 
	 * @param frdParameterRule41
	 * @return the persisted FrdParameterRule41
	 */
	public FrdParameterRule41 persistFrdParameterRule41(FrdParameterRule41 frdParameterRule41);

	/**
	 * persistFrdParameterRule41List - persists a list of FrdParameterRule41
	 * 
	 * @param frdParameterRule41List
	 * @return the list of persisted FrdParameterRule41
	 */
	public ArrayList<FrdParameterRule41> persistFrdParameterRule41List(
			List<FrdParameterRule41> frdParameterRule41List);

	/**
	 * mergeFrdParameterRule41 - merges a FrdParameterRule41
	 * 
	 * @param frdParameterRule41
	 * @return the merged FrdParameterRule41
	 */
	public FrdParameterRule41 mergeFrdParameterRule41(FrdParameterRule41 frdParameterRule41);

	/**
	 * mergeFrdParameterRule41List - merges a list of FrdParameterRule41
	 * 
	 * @param frdParameterRule41List
	 * @return the merged list of FrdParameterRule41
	 */
	public ArrayList<FrdParameterRule41> mergeFrdParameterRule41List(
			List<FrdParameterRule41> frdParameterRule41List);

	/**
	 * removeFrdParameterRule41 - removes a FrdParameterRule41
	 * 
	 * @param frdParameterRule41
	 */
	public void removeFrdParameterRule41(FrdParameterRule41 frdParameterRule41);

	/**
	 * removeFrdParameterRule41List - removes a list of FrdParameterRule41
	 * 
	 * @param frdParameterRule41List
	 */
	public void removeFrdParameterRule41List(List<FrdParameterRule41> frdParameterRule41List);

	/**
	 * findByFrdParameterRule41Like - finds a list of FrdParameterRule41 Like
	 * 
	 * @param frdParameterRule41
	 * @return the list of FrdParameterRule41 found
	 */
	public List<FrdParameterRule41> findByFrdParameterRule41Like(FrdParameterRule41 frdParameterRule41,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/**
	 * findByFrdParameterRule41>LikeFR - finds a list of FrdParameterRule41> Like with a finder return object
	 * 
	 * @param frdParameterRule41
	 * @return the list of FrdParameterRule41 found
	 */
	public FinderReturn findByFrdParameterRule41LikeFR(FrdParameterRule41 frdParameterRule41,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
}
