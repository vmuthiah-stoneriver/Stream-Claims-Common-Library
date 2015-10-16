package com.client.iip.integration.core.util;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fiserv.isd.iip.core.container.ContainerException;
import com.fiserv.isd.iip.core.data.metadata.DataAccessMetaDataContext;
import com.fiserv.isd.iip.core.data.query.file.QueryLoaderImpl;

public class ClientQueryLoaderImpl extends QueryLoaderImpl{
	
	private Logger logger = LoggerFactory.getLogger(ClientQueryLoaderImpl.class);
	
	
	/**
	 * Load all the queries from the xml files.
	 *
	 * @return Map the query context
	 */
	@Override
	public Map<String, DataAccessMetaDataContext> loadQueries(){
		Map<String, DataAccessMetaDataContext> queryContexts
			= new HashMap<String, DataAccessMetaDataContext>();
		String queryFiles[] = super.getQueryFiles();
		for (int i = 0; i < queryFiles.length; i ++)	{
			try{
				loadQueries(queryFiles[i]);
			}catch(ContainerException ex){
				
				if(ex.toString().contains("cannot be resolved to URL")){
					logger.error("Skipping Resource Error: " + ex.toString());
					continue;
				}
			}
			queryContexts.putAll(loadQueries(queryFiles[i]));
		}

		return queryContexts;
	}
	
}
