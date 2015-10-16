/**
 * 
 */
package com.client.iip.integration.core.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds the cache of CodeLookUp results for GetClaimDetails interface, which are keyed by a 
 * combination of the Code Table Name + Column Name and Code Value.
 * @author savinash
 */
public class CodeLookUpCache {
	private Map<CodeLookUpCacheKey, Object> codeLookUpCache = new HashMap<CodeLookUpCacheKey, Object>();
	
	/**
	 * 
	 * @param context the context object
	 * @param clKey the code lookup key
	 * @return Object cached results for the key
	 * @throws CacheKeyNotFoundException if no cache available
	 */
	public Object findCachedResults(Object context, String clKey) throws CacheKeyNotFoundException{
		CodeLookUpCacheKey key = new CodeLookUpCacheKey(context, clKey);
		if(codeLookUpCache.containsKey(key)){
			return codeLookUpCache.get(key);
		}
		throw new CacheKeyNotFoundException();
	}
	
	/**
	 *  
	 * @param context the context object
	 * @param clKey the code lookup key
	 * @param result the cached value for the CodeLookUp key/object.
	 */
	public void cache(Object context, String clKey, Object result){
		CodeLookUpCacheKey key = new CodeLookUpCacheKey(context, clKey);
		codeLookUpCache.put(key, result);
	}
}
