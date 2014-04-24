package com.gdn.venice.facade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.Query;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

import com.gdn.venice.facade.callback.SessionCallback;
import com.gdn.venice.facade.finder.FinderReturn;
import com.gdn.venice.persistence.SeatFulfillmentInPercentage;
import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.djarum.raf.utilities.JPQLQueryStringBuilder;
import com.djarum.raf.utilities.Log4jLoggerFactory;

/**
 * Session Bean implementation class SeatFulfillmentInPercentageSessionEJBBean
 * 
 * <p>
 * <b>author:</b> <a href="mailto:david@pwsindonesia.com">David Forden</a>
 * <p>
 * <b>version:</b> 1.0
 * <p>
 * <b>since:</b> 2011
 * 
 */
@Stateless(mappedName = "SeatFulfillmentInPercentageSessionEJBBean")
public class SeatFulfillmentInPercentageSessionEJBBean implements SeatFulfillmentInPercentageSessionEJBRemote,
		SeatFulfillmentInPercentageSessionEJBLocal {

	/*
	 * Implements an IOC model for pre/post callbacks to persist, merge, and
	 * remove operations. The onPrePersist, onPostPersist, onPreMerge,
	 * onPostMerge, onPreRemove and OnPostRemove operations must be implemented
	 * by the callback class.
	 */
	private String _sessionCallbackClassName = null;

	// A reference to the callback object that has been instantiated
	private SessionCallback _callback = null;

	protected static Logger _log = null;

	// The configuration file to use
	private String _configFile = System.getenv("VENICE_HOME")
			+ "/conf/module-config.xml";

	//The binding array used when binding variables into a JPQL query
	private Object[] bindingArray = null;

	@PersistenceContext(unitName = "GDN-Venice-Persistence", type = PersistenceContextType.TRANSACTION)
	protected EntityManager em;

	/**
	 * Default constructor.
	 */
	public SeatFulfillmentInPercentageSessionEJBBean() {
		super();
		Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
		_log = loggerFactory
				.getLog4JLogger("com.gdn.venice.facade.SeatFulfillmentInPercentageSessionEJBBean");
		// If the configuration is successful then instantiate the callback
		if (this.configure())
			this.instantiateTriggerCallback();
	}

	/**
	 * Reads the venice configuration file and configures the EJB's
	 * triggerCallbackClassName
	 */
	private Boolean configure() {
		_log.debug("Venice Configuration File:" + _configFile);
		try {
			XMLConfiguration config = new XMLConfiguration(_configFile);

			/*
			 * Get the index entry for the adapter configuration from the
			 * configuration file - there will be multiple adapter
			 * configurations
			 */
			@SuppressWarnings({ "rawtypes" })
			List callbacks = config
					.getList("sessionBeanConfig.callback.[@name]");
			Integer beanConfigIndex = new Integer(Integer.MAX_VALUE);
			@SuppressWarnings("rawtypes")
			Iterator i = callbacks.iterator();
			while (i.hasNext()) {
				String beanName = (String) i.next();
				if (this.getClass().getSimpleName().equals(beanName)) {
					beanConfigIndex = callbacks.indexOf(beanName);
					_log.debug("Bean configuration for " + beanName
							+ " found at " + beanConfigIndex);
				}
			}
			this._sessionCallbackClassName = config
					.getString("sessionBeanConfig.callback(" + beanConfigIndex + ").[@class]");

			_log.debug("Loaded configuration for _sessionCallbackClassName:"
					+ _sessionCallbackClassName);
		} catch (ConfigurationException e) {
			_log.error("A ConfigurationException occured when processing the configuration file"
					+ e.getMessage());
			e.printStackTrace();
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	/**
	 * Instantiates the trigger callback handler class
	 * 
	 * @return
	 */
	Boolean instantiateTriggerCallback() {
		if (_sessionCallbackClassName != null
				&& !_sessionCallbackClassName.isEmpty())
			try {
				Class<?> c = Class.forName(_sessionCallbackClassName);
				_callback = (SessionCallback) c.newInstance();
			} catch (ClassNotFoundException e) {
				_log.error("A ClassNotFoundException occured when trying to instantiate:"
						+ this._sessionCallbackClassName);
				e.printStackTrace();
				return Boolean.FALSE;
			} catch (InstantiationException e) {
				_log.error("A InstantiationException occured when trying to instantiate:"
						+ this._sessionCallbackClassName);
				e.printStackTrace();
				return Boolean.FALSE;
			} catch (IllegalAccessException e) {
				_log.error("A IllegalAccessException occured when trying to instantiate:"
						+ this._sessionCallbackClassName);
				e.printStackTrace();
				return Boolean.FALSE;
			}
		return Boolean.TRUE;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatFulfillmentInPercentageSessionEJBRemote#queryByRange(java.lang
	 * .String, int, int)
	 */
	@Override
	@SuppressWarnings({ "unchecked" })
	public List<SeatFulfillmentInPercentage> queryByRange(String jpqlStmt, int firstResult,
			int maxResults) {
		Long startTime = System.currentTimeMillis();
		_log.debug("queryByRange()");

		Query query = null;
		try {
			query = em.createQuery(jpqlStmt);
			if(this.bindingArray != null){
				for(int i = 0; i < bindingArray.length; ++i){
				    if(bindingArray[i] != null){
						query.setParameter(i+1, bindingArray[i]);
					}
				}
			}
		} catch (Exception e) {
			_log.error("An exception occured when calling em.createQuery():"
					+ e.getMessage());
			throw new EJBException(e);
		}
		try {
			if (firstResult > 0) {
				query = query.setFirstResult(firstResult);
			}
			if (maxResults > 0) {
				query = query.setMaxResults(maxResults);
			}
		} catch (Exception e) {
			_log.error("An exception occured when accessing the result set of a query:"
					+ e.getMessage());
			throw new EJBException(e);
		}		
		List<SeatFulfillmentInPercentage> returnList = (List<SeatFulfillmentInPercentage>)query.getResultList();
		this.bindingArray = null;
		
		Long endTime = System.currentTimeMillis();
		Long duration = startTime - endTime;
		_log.debug("queryByRange() duration:" + duration + "ms");
		return returnList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatFulfillmentInPercentageSessionEJBRemote#persistSeatFulfillmentInPercentage(com
	 * .gdn.venice.persistence.SeatFulfillmentInPercentage)
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public SeatFulfillmentInPercentage persistSeatFulfillmentInPercentage(SeatFulfillmentInPercentage seatFulfillmentInPercentage) {
		Long startTime = System.currentTimeMillis();
		_log.debug("persistSeatFulfillmentInPercentage()");

		// Call the onPrePersist() callback and throw an exception if it fails
		if (this._callback != null) {
			if (!this._callback.onPrePersist(seatFulfillmentInPercentage)) {
				_log.error("An onPrePersist callback operation failed for:"
						+ this._sessionCallbackClassName);
				throw new EJBException(
						"An onPrePersist callback operation failed for:"
								+ this._sessionCallbackClassName);
			}
		}
		
		SeatFulfillmentInPercentage existingSeatFulfillmentInPercentage = null;

		if (seatFulfillmentInPercentage != null && seatFulfillmentInPercentage.getFulfillmentInPercentageId() != null) {
			_log.debug("persistSeatFulfillmentInPercentage:em.find()");
			try {
				existingSeatFulfillmentInPercentage = em.find(SeatFulfillmentInPercentage.class,
						seatFulfillmentInPercentage.getFulfillmentInPercentageId());
			} catch (Exception e) {
				_log.error("An exception occured when calling em.find():"
						+ e.getMessage());
				throw new EJBException(e);
			}
		}
		
		if (existingSeatFulfillmentInPercentage == null) {
			_log.debug("persistSeatFulfillmentInPercentage:em.persist()");
			try {
				em.persist(seatFulfillmentInPercentage);
			} catch (Exception e) {
				_log.error("An exception occured when calling em.persist():"
						+ e.getMessage());
				throw new EJBException(e);
			}
			_log.debug("persistSeatFulfillmentInPercentage:em.flush()");
			try {
				em.flush();
				em.clear();
			} catch (Exception e) {
				_log.error("An exception occured when calling em.flush():"
						+ e.getMessage());
				throw new EJBException(e);
			}
			
			// Call the onPostPersist() callback and throw an exception if it fails
			if (this._callback != null) {
				if (!this._callback.onPostPersist(seatFulfillmentInPercentage)) {
					_log.error("An onPostPersist callback operation failed for:"
							+ this._sessionCallbackClassName);
					throw new EJBException(
							"An onPostPersist callback operation failed for:"
									+ this._sessionCallbackClassName);
				}
			}			
			
			Long endTime = System.currentTimeMillis();
			Long duration = startTime - endTime;
			_log.debug("persistSeatFulfillmentInPercentage() duration:" + duration + "ms");
			
			return seatFulfillmentInPercentage;
		} else {
			throw new EJBException("SeatFulfillmentInPercentage exists!. SeatFulfillmentInPercentage = "
					+ seatFulfillmentInPercentage.getFulfillmentInPercentageId());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatFulfillmentInPercentageSessionEJBRemote#persistSeatFulfillmentInPercentageList
	 * (java.util.List)
	 */
	@Override
	@SuppressWarnings("rawtypes")
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public ArrayList<SeatFulfillmentInPercentage> persistSeatFulfillmentInPercentageList(
			List<SeatFulfillmentInPercentage> seatFulfillmentInPercentageList) {
		_log.debug("persistSeatFulfillmentInPercentageList()");
		Iterator i = seatFulfillmentInPercentageList.iterator();
		while (i.hasNext()) {
			this.persistSeatFulfillmentInPercentage((SeatFulfillmentInPercentage) i.next());
		}
		return (ArrayList<SeatFulfillmentInPercentage>)seatFulfillmentInPercentageList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatFulfillmentInPercentageSessionEJBRemote#mergeSeatFulfillmentInPercentage(com.
	 * gdn.venice.persistence.SeatFulfillmentInPercentage)
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public SeatFulfillmentInPercentage mergeSeatFulfillmentInPercentage(SeatFulfillmentInPercentage seatFulfillmentInPercentage) {
		Long startTime = System.currentTimeMillis();
		_log.debug("mergeSeatFulfillmentInPercentage()");

		// Call the onPreMerge() callback and throw an exception if it fails
		if (this._callback != null) {
			if (!this._callback.onPreMerge(seatFulfillmentInPercentage)) {
				_log.error("An onPreMerge callback operation failed for:"
						+ this._sessionCallbackClassName);
				throw new EJBException(
						"An onPreMerge callback operation failed for:"
								+ this._sessionCallbackClassName);
			}
		}
		
		SeatFulfillmentInPercentage existing = null;
		if (seatFulfillmentInPercentage.getFulfillmentInPercentageId() != null){
			_log.debug("mergeSeatFulfillmentInPercentage:em.find()");
			existing = em.find(SeatFulfillmentInPercentage.class, seatFulfillmentInPercentage.getFulfillmentInPercentageId());
		}
		
		if (existing == null) {
			return this.persistSeatFulfillmentInPercentage(seatFulfillmentInPercentage);
		} else {
			_log.debug("mergeSeatFulfillmentInPercentage:em.merge()");
			try {
				em.merge(seatFulfillmentInPercentage);
			} catch (Exception e) {
				_log.error("An exception occured when calling em.merge():"
						+ e.getMessage());
				throw new EJBException(e);
			}
			_log.debug("mergeSeatFulfillmentInPercentage:em.flush()");
			try {
				em.flush();
				em.clear();
			} catch (Exception e) {
				_log.error("An exception occured when calling em.flush():"
						+ e.getMessage());
				throw new EJBException(e);
			}
			SeatFulfillmentInPercentage newobject = em.find(SeatFulfillmentInPercentage.class,
					seatFulfillmentInPercentage.getFulfillmentInPercentageId());
			_log.debug("mergeSeatFulfillmentInPercentage():em.refresh");
			try {
				em.refresh(newobject);
			} catch (Exception e) {
				_log.error("An exception occured when calling em.refresh():"
						+ e.getMessage());
				throw new EJBException(e);
			}

			// Call the onPostMerge() callback and throw an exception if it fails
			if (this._callback != null) {
				if (!this._callback.onPostMerge(newobject)) {
					_log.error("An onPostMerge callback operation failed for:"
							+ this._sessionCallbackClassName);
					throw new EJBException(
							"An onPostMerge callback operation failed for:"
									+ this._sessionCallbackClassName);
				}
			}	
			
			Long endTime = System.currentTimeMillis();
			Long duration = startTime - endTime;
			_log.debug("mergeSeatFulfillmentInPercentage() duration:" + duration + "ms");
						
			return newobject;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatFulfillmentInPercentageSessionEJBRemote#mergeSeatFulfillmentInPercentageList(
	 * java.util.List)
	 */
	@Override
	@SuppressWarnings("rawtypes")
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public ArrayList<SeatFulfillmentInPercentage> mergeSeatFulfillmentInPercentageList(
			List<SeatFulfillmentInPercentage> seatFulfillmentInPercentageList) {
		_log.debug("mergeSeatFulfillmentInPercentageList()");
		Iterator i = seatFulfillmentInPercentageList.iterator();
		while (i.hasNext()) {
			this.mergeSeatFulfillmentInPercentage((SeatFulfillmentInPercentage) i.next());
		}
		return (ArrayList<SeatFulfillmentInPercentage>)seatFulfillmentInPercentageList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatFulfillmentInPercentageSessionEJBRemote#removeSeatFulfillmentInPercentage(com.
	 * gdn.venice.persistence.SeatFulfillmentInPercentage)
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void removeSeatFulfillmentInPercentage(SeatFulfillmentInPercentage seatFulfillmentInPercentage) {
		Long startTime = System.currentTimeMillis();
		_log.debug("removeSeatFulfillmentInPercentage()");

		// Call the onPreRemove() callback and throw an exception if it fails
		if (this._callback != null) {
			if (!this._callback.onPreRemove(seatFulfillmentInPercentage)) {
				_log.error("An onPreRemove callback operation failed for:"
						+ this._sessionCallbackClassName);
				throw new EJBException(
						"An onPreRemove callback operation failed for:"
								+ this._sessionCallbackClassName);
			}
		}
	
		_log.debug("removeSeatFulfillmentInPercentage:em.find()");
		seatFulfillmentInPercentage = em.find(SeatFulfillmentInPercentage.class, seatFulfillmentInPercentage.getFulfillmentInPercentageId());
		
		try {
			_log.debug("removeSeatFulfillmentInPercentage:em.remove()");
			em.remove(seatFulfillmentInPercentage);
		} catch (Exception e) {
			_log.error("An exception occured when calling em.remove():"
					+ e.getMessage());
			throw new EJBException(e);
		}
		
		// Call the onPostRemove() callback and throw an exception if it fails
		if (this._callback != null) {
			if (!this._callback.onPostRemove(seatFulfillmentInPercentage)) {
				_log.error("An onPostRemove callback operation failed for:"
						+ this._sessionCallbackClassName);
				throw new EJBException(
						"An onPostRemove callback operation failed for:"
								+ this._sessionCallbackClassName);
			}
		}			

		_log.debug("removeSeatFulfillmentInPercentage:em.flush()");
		em.flush();
		em.clear();
		Long endTime = System.currentTimeMillis();
		Long duration = startTime - endTime;
		_log.debug("removeSeatFulfillmentInPercentage() duration:" + duration + "ms");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatFulfillmentInPercentageSessionEJBRemote#removeSeatFulfillmentInPercentageList(
	 * java.util.List)
	 */
	@Override
	@SuppressWarnings("rawtypes")
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void removeSeatFulfillmentInPercentageList(List<SeatFulfillmentInPercentage> seatFulfillmentInPercentageList) {
		_log.debug("removeSeatFulfillmentInPercentageList()");
		Iterator i = seatFulfillmentInPercentageList.iterator();
		while (i.hasNext()) {
			this.removeSeatFulfillmentInPercentage((SeatFulfillmentInPercentage) i.next());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatFulfillmentInPercentageSessionEJBRemote#findBySeatFulfillmentInPercentageLike(
	 * com.gdn.venice.persistence.SeatFulfillmentInPercentage, int, int)
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<SeatFulfillmentInPercentage> findBySeatFulfillmentInPercentageLike(SeatFulfillmentInPercentage seatFulfillmentInPercentage,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults) {
		Long startTime = System.currentTimeMillis();
		_log.debug("findBySeatFulfillmentInPercentageLike()");
		JPQLQueryStringBuilder qb = new JPQLQueryStringBuilder(seatFulfillmentInPercentage);
		HashMap complexTypeBindings = new HashMap();
		String stmt = qb.buildQueryString(complexTypeBindings, criteria);
		if(criteria != null){
			/*
			 * Get the binding array from the query builder and make
			 * it available to the queryByRange method
			 */
			this.bindingArray = qb.getBindingArray();
			for(int i = 0; i < qb.getBindingArray().length; i++){
				_log.debug("Bindings:" + i + ":" + qb.getBindingArray()[i]);
			}
			List<SeatFulfillmentInPercentage> seatFulfillmentInPercentageList = this.queryByRange(stmt, firstResult, maxResults);			
			Long endTime = System.currentTimeMillis();
			Long duration = startTime - endTime;
			_log.debug("findBySeatFulfillmentInPercentageLike() duration:" + duration + "ms");
			return seatFulfillmentInPercentageList;			
		}else{
			String errMsg = "A query has been initiated with null criteria.";
			_log.error(errMsg);
			throw new EJBException(errMsg);
		}		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdn.venice.facade.SeatFulfillmentInPercentageSessionEJBRemote#findBySeatFulfillmentInPercentageLikeFR(
	 * com.gdn.venice.persistence.SeatFulfillmentInPercentage, int, int)
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public FinderReturn findBySeatFulfillmentInPercentageLikeFR(SeatFulfillmentInPercentage seatFulfillmentInPercentage,
			JPQLAdvancedQueryCriteria criteria, int firstResult, int maxResults) {
		Long startTime = System.currentTimeMillis();
		_log.debug("findBySeatFulfillmentInPercentageLikeFR()");
		JPQLQueryStringBuilder qb = new JPQLQueryStringBuilder(seatFulfillmentInPercentage);
		HashMap complexTypeBindings = new HashMap();
		String stmt = qb.buildQueryString(complexTypeBindings, criteria);
		if(criteria != null){
			/*
			 * Get the binding array from the query builder and make
			 * it available to the queryByRange method
			 */
			this.bindingArray = qb.getBindingArray();
			for(int i = 0; i < qb.getBindingArray().length; i++){
				_log.debug("Bindings:" + i + ":" + qb.getBindingArray()[i]);
			}
			
			//Set the finder return object with the count of the total query rows
			FinderReturn fr = new FinderReturn();
			String countStmt = "select count(o) " + stmt.substring(stmt.indexOf("from"));
			Query query = null;
			try {
				query = em.createQuery(countStmt);
				if(this.bindingArray != null){
					for(int i = 0; i < bindingArray.length; ++i){
					    if(bindingArray[i] != null){
							query.setParameter(i+1, bindingArray[i]);
						}
					}
				}
				Long totalRows = (Long)query.getSingleResult();
				fr.setNumQueryRows(totalRows);
			} catch (Exception e) {
				_log.error("An exception occured when calling em.createQuery():"
						+ e.getMessage());
				throw new EJBException(e);
			}
			
			//Set the finder return object with the query list
			fr.setResultList(this.queryByRange(stmt, firstResult, maxResults));			
			Long endTime = System.currentTimeMillis();
			Long duration = startTime - endTime;
			_log.debug("findBySeatFulfillmentInPercentageLike() duration:" + duration + "ms");
			return fr;			
		}else{
			String errMsg = "A query has been initiated with null criteria.";
			_log.error(errMsg);
			throw new EJBException(errMsg);
		}		
	}

}
