package com.client.iip.integration.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import com.fiserv.isd.iip.core.data.LogicalDataSource;
import com.fiserv.isd.iip.core.data.query.rdbms.QueryDataAccess;
import com.fiserv.isd.iip.core.event.Event;
import com.fiserv.isd.iip.core.event.EventParameter;
import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.fiserv.isd.iip.util.dataretriever.DataRetrieverParameter;
import com.fiserv.isd.iip.util.dataretriever.DataRetrieverRequest;
import com.stoneriver.iip.entconfig.dataretriever.DataRetrieverUtils;
import com.stoneriver.iip.entconfig.event.action.BlazeBusinessProcessAction;

public final class ClientQueryHelper {
	
	private static LogicalDataSource clientLds;
	private static Logger logger = LoggerFactory.getLogger(ClientQueryHelper.class);
	
	/**
	 * Query by access Id returning a single value.
	 * 
	 * @param <T> Object being returned.
	 * @param accessId The access Id.
	 * @param returnType The return type Class.
	 * @param params The bind parameters.
	 * @return the accessed data.
	 */
	public static <T> T clientQuery(String accessId, Class<T> returnType, 
			Object... params) {
    	Map<String, Object> mapParms = new HashMap<String, Object>();
    	
    	if (params != null 
    		&& params.length %2 != 0) {
    		throw new  IllegalArgumentException("Bind parameters must be in name/value pairs");
    	}
    	
    	if (params != null) {
    		for (int i=0; i < params.length; i++) {
    			mapParms.put(params[i].toString(), params[++i]);
    		}
    	}

		T ret =  QueryDataAccess.query(
    			accessId, 
    			mapParms, returnType, 
    			clientLds);  
		
		logger.info("clientQuery results for: {} - {}", 
				accessId, ret);
		
		return ret;
	}
	
	/**
	 * Query by access Id returning a collection of values.
	 * 
	 * @param <T> Object being returned.
	 * @param accessId The access Id.
	 * @param returnType The return type Class.
	 * @param params The bind parameters.
	 * @return the accessed data.
	 */
	public static <T> Collection<T> clientQueryList(String accessId, 
			Class<T> returnType, Object... params) {
    	Map<String, Object> mapParms = new HashMap<String, Object>();
    	
    	if (params != null 
    		&& params.length %2 != 0) {
    		throw new  IllegalArgumentException("Bind parameters must be in name/value pairs");
    	}
    	
    	if (params != null) {
    		for (int i=0; i < params.length; i++) {
    			mapParms.put(params[i].toString(), params[++i]);
    		}
    	}

    	Collection<T> ret =  QueryDataAccess.queryList(
    			accessId, 
    			mapParms, returnType, 
    			clientLds);  
		
		logger.info("clientQueryList results for: {} - {}", 
				accessId, ret);
		
		return ret;
	}
	
	public static <T> Collection<T> clientQueryList(String accessId, 
			Class<T> returnType, LogicalDataSource lds, Object... params) {
    	Map<String, Object> mapParms = new HashMap<String, Object>();
    	
    	if (params != null 
    		&& params.length %2 != 0) {
    		throw new  IllegalArgumentException("Bind parameters must be in name/value pairs");
    	}
    	
    	if (params != null) {
    		for (int i=0; i < params.length; i++) {
    			mapParms.put(params[i].toString(), params[++i]);
    		}
    	}

    	Collection<T> ret =  QueryDataAccess.queryList(
    			accessId, 
    			mapParms, returnType, 
    			lds);  
		
		logger.info("clientQueryList results for: {} - {}", 
				accessId, ret);
		
		return ret;
	}	
	
	/**
	 * Generate Event Parameters to match Data Retriever Parameters.
	 * 
	 * @param drCode Data Retriever Code.
	 * @return Business Process Action.
	 */
	public static BlazeBusinessProcessAction generateEventParameters(String drCode) {
		DataRetrieverRequest request = DataRetrieverUtils.getParameterNames(
				drCode, clientLds);

		if (request == null) {
			logger.error("Couldn't locate Data Retriever for : {}", drCode);
			throw new IllegalArgumentException(
					"Unknown Data Retriever for: " + drCode);
		}
		
		BlazeBusinessProcessAction bpAction = new BlazeBusinessProcessAction();
		Event event = new Event();
		event.setParams(new ArrayList<EventParameter>());
		bpAction.setEvent(event);

    	for(DataRetrieverParameter drParam : request.getParams()) {
    		EventParameter eventParam = new EventParameter();
    		BeanUtils.copyProperties(drParam, eventParam);
    		event.getParams().add(eventParam);
    	}	

		return bpAction;
	}	
	
	/**
	 * @param logicalDS the lds to set.
	 */
	@Inject(PojoRef = "clientLogicalDataSource")
	public void setLds(LogicalDataSource logicalDS) {
		clientLds = logicalDS;
	}	
}
