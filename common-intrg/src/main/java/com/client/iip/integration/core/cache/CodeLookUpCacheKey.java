/**
 * 
 */
package com.client.iip.integration.core.cache;

/**
 * CodeLookUpCacheKey 
 * @author savinash
 */
public class CodeLookUpCacheKey {
	private Object context;
	private String clKey;
	
	/**
	 * @param ctx context
	 * @param codeFieldKey key
	 */
	public CodeLookUpCacheKey(Object ctx, String codeFieldKey) {
		context = ctx;
		clKey = codeFieldKey;
	}
	/**
	 * @return the context
	 */
	public Object getContext() {
		return context;
	}
	/**
	 * @param ctx the context to set
	 */
	public void setContext(Object ctx) {
		this.context = ctx;
	}
	/**
	 * @return the clKey
	 */
	public String getClKey() {
		return clKey;
	}
	/**
	 * @param key the clKey to set
	 */
	public void setClKey(String key) {
		this.clKey = key;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 * @return int
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((context == null) ? 0 : context.hashCode());
		result = prime * result + ((clKey == null) ? 0 : clKey.hashCode());
		return result;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 * @param obj Object to compare
	 * @return boolean if equivalent
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null){
			return false;
		}
		if (getClass() != obj.getClass()){
			return false;
		}
		CodeLookUpCacheKey other = (CodeLookUpCacheKey) obj;
		if (context == null) {
			if (other.context != null){
				return false;
			}
		} else if (!context.equals(other.context)){
			return false;
		}
		if (clKey == null) {
			if (other.clKey != null){
				return false;
			}
		} else if (!clKey.equals(other.clKey)){
			return false;
		}
		return true;
	}
	
	
}
