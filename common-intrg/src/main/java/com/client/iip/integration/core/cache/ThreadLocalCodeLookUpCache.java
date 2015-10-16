/**
 * 
 */
package com.client.iip.integration.core.cache;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import org.jboss.cache.Node;

import com.fiserv.isd.iip.core.cache.jbosscache.JBossCacheAccessor;
import com.fiserv.isd.iip.core.cache.jbosscache.JBossCacheManager;
import com.fiserv.isd.iip.core.thread.IIPDataContext;
import com.fiserv.isd.iip.core.thread.IIPThreadContextFactory;

/**
 * CodeLookUpCache object that is used to cache the literal description for the Code Value and Table/Colum Name key.
 * Used by Get Claim Details interface.
 * @author savinash
 */
public final class ThreadLocalCodeLookUpCache {	
	/**
	 * constructor.
	 */
	private ThreadLocalCodeLookUpCache() {
		super();
	}

	private static final String CODE_LOOKUP_CACHE = "CL_CACHE";
	private static final String ANNOTATION_CACHE = "CL_ANN_CACHE";
	private static final String IGNORED_ANNOTATION_FIELDS = "CL_IGNORE_ANN";
	
	/**
	 * Method that returns true if results are cached, else false.
	 * @param context the Object
	 * @param codeLookUpKey the code look up key
	 * @return true if cached data exists
	 */
	public static boolean hasCachedResults(Object context, String codeLookUpKey){
		try {
			fetchCachedResults(context, codeLookUpKey);
		} catch (CacheKeyNotFoundException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Fetch the cached results based on the codeLookUpKey passed in.
	 * @param context the Object
	 * @param codeLookUpKey the code look up key
	 * @return the cached result
	 * @throws CacheKeyNotFoundException if no cache is found
	 */
	public static Object fetchCachedResults(Object context, String codeLookUpKey) throws CacheKeyNotFoundException{
		IIPDataContext data = IIPThreadContextFactory.getDataContext();
		if(data == null){
			throw new CacheKeyNotFoundException();
		}
		CodeLookUpCache cache = getLookUpCache();
		if(cache == null){
			throw new CacheKeyNotFoundException();
		}
		return ((CodeLookUpCache)cache).findCachedResults(context, codeLookUpKey);
	}
	
	/**
	 * Add the results to the cache for the key passed in.
	 * @param context Object
	 * @param codeLookUpKey dr key
	 * @param results results to cache
	 */
	public static void addToCache(Object context, String codeLookUpKey, Object results){
		CodeLookUpCache cache = getLookUpCache();
		cache.cache(context, codeLookUpKey, results);
		//dataContext.setAppData(CODE_LOOKUP_CACHE, cache);
	}
	
	/**
	 * util method to store a resolved annotation in the threadlocal cache.
	 * @param key cache key (class.method)
	 * @param ann instance of the CodeLookUp annotation.
	 */
	public static void cacheAnnotation(String key, Annotation ann) {
		Map<String, Annotation> cache = null;
		cache = getLookUpAnnotationCache();
		cache.put(key,  ann);		
	}
	
	/**
	 * try to retrieve an annotation from the cache.
	 * @param key cache key
	 * @return annotation if found, null otherwise.
	 */
	public static Annotation getCachedAnnotation(String key) {
		Map<String, Annotation> cache = getLookUpAnnotationCache();
		Annotation ann = cache.get(key);
		return ann;
	}

	/**
	 * util method to store a resolved annotation in the threadlocal cache.
	 * @param key cache key (class.method)
	 * 
	 */
	public static void addIgnoredAnnotationField(String key) {
		Map<String, String> cache = null;
		cache = getIgnoredAnnotationCache();
		cache.put(key,  key);		
	}

	/**
	 * try to retrieve an annotation from the cache.
	 * @param key cache key
	 * @return true if found, false otherwise.
	 */
	public static boolean isIgnoredAnnotationField(String key) {
		Map<String, String> cache = getIgnoredAnnotationCache();
		return cache.containsKey(key);
	}
	
	/**
	 * util method to get the cached map from JBoss cache.
	 * @return initialized map
	 */
	private static Map<String, String> getIgnoredAnnotationCache() {
		JBossCacheManager cacheMgr = JBossCacheAccessor.getJBossCache();
		Node <Object, Object> cacheNode = cacheMgr.getAppDataCache();
		Map<String, String> ignCache = (Map<String, String>)cacheNode.get(IGNORED_ANNOTATION_FIELDS);
		if(ignCache == null) {
			ignCache = new HashMap<String, String>();
			cacheNode.put(IGNORED_ANNOTATION_FIELDS, ignCache);
		}
		return ignCache;
	}
	
	/**
	 * util method to get the cached map from JBoss cache.
	 * @return initialized map
	 */	
	private static Map<String, Annotation> getLookUpAnnotationCache() {
		JBossCacheManager cacheMgr = JBossCacheAccessor.getJBossCache();
		Node <Object, Object> cacheNode = cacheMgr.getAppDataCache();
		Map<String, Annotation> annCache = (Map<String, Annotation>)cacheNode.get(ANNOTATION_CACHE);
		if(annCache == null) {
			annCache = new HashMap<String, Annotation>();
			cacheNode.put(ANNOTATION_CACHE, annCache);
		}
		return annCache;
	}

	/**
	 * util method to get the cached map from JBoss cache.
	 * @return initialized map
	 */	
	private static CodeLookUpCache getLookUpCache() {
		JBossCacheManager cacheMgr = JBossCacheAccessor.getJBossCache();
		Node <Object, Object> cacheNode = cacheMgr.getAppDataCache();
		CodeLookUpCache cache = (CodeLookUpCache)cacheNode.get(CODE_LOOKUP_CACHE);
		if(cache == null) {
			cache = new CodeLookUpCache();
			cacheNode.put(CODE_LOOKUP_CACHE, cache);
		}
		return cache;
	}

	
}
