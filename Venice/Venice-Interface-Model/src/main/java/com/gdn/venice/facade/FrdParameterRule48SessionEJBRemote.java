package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;

import com.gdn.venice.persistence.FrdParameterRule48;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Remote
public interface FrdParameterRule48SessionEJBRemote {

	/**
	 * queryByRange - allows querying by range/block
	 * 
	 * @param jpqlStmt
	 * @param firstResult
	 * @param maxResults
	 * @return a list of FrdParameterRule48
	 */
	public List<FrdParameterRule48> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/**
	 * persistFrdParameterRule48 persists a country
	 * 
	 * @param frdParameterRule48
	 * @return the persisted FrdParameterRule48
	 */
	public FrdParameterRule48 persistFrdParameterRule48(FrdParameterRule48 frdParameterRule48);

	/**
	 * persistFrdParameterRule48List - persists a list of FrdParameterRule48
	 * 
	 * @param frdParameterRule48List
	 * @return the list of persisted FrdParameterRule48
	 */
	public ArrayList<FrdParameterRule48> persistFrdParameterRule48List(
			List<FrdParameterRule48> frdParameterRule48List);

	/**
	 * mergeFrdParameterRule48 - merges a FrdParameterRule48
	 * 
	 * @param frdParameterRule48
	 * @return the merged FrdParameterRule48
	 */
	public FrdParameterRule48 mergeFrdParameterRule48(FrdParameterRule48 frdParameterRule48);

	/**
	 * mergeFrdParameterRule48List - merges a list of FrdParameterRule48
	 * 
	 * @param frdParameterRule48List
	 * @return the merged list of FrdParameterRule48
	 */
	public ArrayList<FrdParameterRule48> mergeFrdParameterRule48List(
			List<FrdParameterRule48> frdParameterRule48List);

	/**
	 * removeFrdParameterRule48 - removes a FrdParameterRule48
	 * 
	 * @param frdParameterRule48
	 */
	public void removeFrdParameterRule48(FrdParameterRule48 frdParameterRule48);

	/**
	 * removeFrdParameterRule48List - removes a list of FrdParameterRule48
	 * 
	 * @param frdParameterRule48List
	 */
	public void removeFrdParameterRule48List(List<FrdParameterRule48> frdParameterRule48List);

	/**
	 * findByFrdParameterRule48Like - finds a list of FrdParameterRule48 Like
	 * 
	 * @param frdParameterRule48
	 * @return the list of FrdParameterRule48 found
	 */
	public List<FrdParameterRule48> findByFrdParameterRule48Like(FrdParameterRule48 frdParameterRule48,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/**
	 * findByFrdParameterRule48>LikeFR - finds a list of FrdParameterRule48> Like with a finder return object
	 * 
	 * @param frdParameterRule48
	 * @return the list of FrdParameterRule48 found
	 */
	public FinderReturn findByFrdParameterRule48LikeFR(FrdParameterRule48 frdParameterRule48,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
}
