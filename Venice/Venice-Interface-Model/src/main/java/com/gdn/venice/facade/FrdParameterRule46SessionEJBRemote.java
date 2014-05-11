package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;

import com.gdn.venice.persistence.FrdParameterRule46;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Remote
public interface FrdParameterRule46SessionEJBRemote {

	/**
	 * queryByRange - allows querying by range/block
	 * 
	 * @param jpqlStmt
	 * @param firstResult
	 * @param maxResults
	 * @return a list of FrdParameterRule46
	 */
	public List<FrdParameterRule46> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/**
	 * persistFrdParameterRule46 persists a country
	 * 
	 * @param frdParameterRule46
	 * @return the persisted FrdParameterRule46
	 */
	public FrdParameterRule46 persistFrdParameterRule46(FrdParameterRule46 frdParameterRule46);

	/**
	 * persistFrdParameterRule46List - persists a list of FrdParameterRule46
	 * 
	 * @param frdParameterRule46List
	 * @return the list of persisted FrdParameterRule46
	 */
	public ArrayList<FrdParameterRule46> persistFrdParameterRule46List(
			List<FrdParameterRule46> frdParameterRule46List);

	/**
	 * mergeFrdParameterRule46 - merges a FrdParameterRule46
	 * 
	 * @param frdParameterRule46
	 * @return the merged FrdParameterRule46
	 */
	public FrdParameterRule46 mergeFrdParameterRule46(FrdParameterRule46 frdParameterRule46);

	/**
	 * mergeFrdParameterRule46List - merges a list of FrdParameterRule46
	 * 
	 * @param frdParameterRule46List
	 * @return the merged list of FrdParameterRule46
	 */
	public ArrayList<FrdParameterRule46> mergeFrdParameterRule46List(
			List<FrdParameterRule46> frdParameterRule46List);

	/**
	 * removeFrdParameterRule46 - removes a FrdParameterRule46
	 * 
	 * @param frdParameterRule46
	 */
	public void removeFrdParameterRule46(FrdParameterRule46 frdParameterRule46);

	/**
	 * removeFrdParameterRule46List - removes a list of FrdParameterRule46
	 * 
	 * @param frdParameterRule46List
	 */
	public void removeFrdParameterRule46List(List<FrdParameterRule46> frdParameterRule46List);

	/**
	 * findByFrdParameterRule46Like - finds a list of FrdParameterRule46 Like
	 * 
	 * @param frdParameterRule46
	 * @return the list of FrdParameterRule46 found
	 */
	public List<FrdParameterRule46> findByFrdParameterRule46Like(FrdParameterRule46 frdParameterRule46,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/**
	 * findByFrdParameterRule46>LikeFR - finds a list of FrdParameterRule46> Like with a finder return object
	 * 
	 * @param frdParameterRule46
	 * @return the list of FrdParameterRule46 found
	 */
	public FinderReturn findByFrdParameterRule46LikeFR(FrdParameterRule46 frdParameterRule46,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
}
