package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;

import com.gdn.venice.persistence.FrdParameterRule44;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.gdn.venice.facade.finder.FinderReturn;

@Remote
public interface FrdParameterRule44SessionEJBRemote {

	/**
	 * queryByRange - allows querying by range/block
	 * 
	 * @param jpqlStmt
	 * @param firstResult
	 * @param maxResults
	 * @return a list of FrdParameterRule44
	 */
	public List<FrdParameterRule44> queryByRange(String jpqlStmt, int firstResult,
			int maxResults);

	/**
	 * persistFrdParameterRule44 persists a country
	 * 
	 * @param frdParameterRule44
	 * @return the persisted FrdParameterRule44
	 */
	public FrdParameterRule44 persistFrdParameterRule44(FrdParameterRule44 frdParameterRule44);

	/**
	 * persistFrdParameterRule44List - persists a list of FrdParameterRule44
	 * 
	 * @param frdParameterRule44List
	 * @return the list of persisted FrdParameterRule44
	 */
	public ArrayList<FrdParameterRule44> persistFrdParameterRule44List(
			List<FrdParameterRule44> frdParameterRule44List);

	/**
	 * mergeFrdParameterRule44 - merges a FrdParameterRule44
	 * 
	 * @param frdParameterRule44
	 * @return the merged FrdParameterRule44
	 */
	public FrdParameterRule44 mergeFrdParameterRule44(FrdParameterRule44 frdParameterRule44);

	/**
	 * mergeFrdParameterRule44List - merges a list of FrdParameterRule44
	 * 
	 * @param frdParameterRule44List
	 * @return the merged list of FrdParameterRule44
	 */
	public ArrayList<FrdParameterRule44> mergeFrdParameterRule44List(
			List<FrdParameterRule44> frdParameterRule44List);

	/**
	 * removeFrdParameterRule44 - removes a FrdParameterRule44
	 * 
	 * @param frdParameterRule44
	 */
	public void removeFrdParameterRule44(FrdParameterRule44 frdParameterRule44);

	/**
	 * removeFrdParameterRule44List - removes a list of FrdParameterRule44
	 * 
	 * @param frdParameterRule44List
	 */
	public void removeFrdParameterRule44List(List<FrdParameterRule44> frdParameterRule44List);

	/**
	 * findByFrdParameterRule44Like - finds a list of FrdParameterRule44 Like
	 * 
	 * @param frdParameterRule44
	 * @return the list of FrdParameterRule44 found
	 */
	public List<FrdParameterRule44> findByFrdParameterRule44Like(FrdParameterRule44 frdParameterRule44,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
			
	/**
	 * findByFrdParameterRule44>LikeFR - finds a list of FrdParameterRule44> Like with a finder return object
	 * 
	 * @param frdParameterRule44
	 * @return the list of FrdParameterRule44 found
	 */
	public FinderReturn findByFrdParameterRule44LikeFR(FrdParameterRule44 frdParameterRule44,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults);
}
