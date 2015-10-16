package com.client.iip.integration.claim.details;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;

import com.client.iip.integration.core.cache.CacheKeyNotFoundException;
import com.client.iip.integration.core.cache.ThreadLocalCodeLookUpCache;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.fiserv.isd.iip.core.util.AnnotationUtil;
import com.fiserv.isd.iip.core.validation.Validateable;
import com.fiserv.isd.iip.core.validation.dto.CodeLookUp;
import com.fiserv.isd.iip.util.codelookup.api.CodeLookupBO;
import com.fiserv.isd.iip.util.codelookup.api.CodeLookupService;
import com.fiserv.isd.iip.util.codelookup.api.CodeTableCriteria;

/**
 * Utility to update the code fields to display values for Claim Details Interface.
 * 
 */
public class ClaimDetailLiteralDescriptionUtility {
	
	/**
	 * The CodeLookupService
	 * 
	 * @return the CodeLookupService
	 */
	private CodeLookupService getCodeLookupService() {
		return MuleServiceFactory.getService(CodeLookupService.class);
	}

	/**
	 * Update the dto passed in with the literal description for all code tables.
	 * Iterates over all the attributes on the DTO tree structure and for properties that are configured with CodeLookUp annotation,
	 * invoke the CodeLookupService to fetch the description and populate the literal description map on the DataTransferObject.
	 * 
	 * @param dto the dto
	 * @return the dto with literal description map populated.
	 */
	public Serializable updateCodeToDisplayValue(Serializable dto)	{
		/*
		 * If the dto is null, don't perform any code lookup invocations.
		 */
		if (dto == null)	{
			return dto;
		}
		
		/*
		 * Even if there are no properties configured with a CodeLookUp annotation in this DTO, the
		 * nested DTOs inside this DTO might have CodeLookUp annotation declared. The iteration into
		 * nested DTOs and collection of DTOs should always happen
		 */
		PropertyDescriptor[] dtoProperties = BeanUtils.getPropertyDescriptors(dto.getClass());		
		for (PropertyDescriptor dtoProperty : dtoProperties)	{
			if (Collection.class.isAssignableFrom(dtoProperty.getPropertyType()))	{				
				/*
				 * If the collection contains items that are instance of Serializable,
				 * look for CodeLookUp annotation declaration all of them
				 */
				updateCodeToDisplayValueForList((List<Serializable>) getDTOPropertyValue(dtoProperty.getReadMethod(), dto, dtoProperty));
			} else if (Validateable.class.isAssignableFrom(dtoProperty.getPropertyType()))	{
				/*
				 * If the property is an instance of Serializable, find CodeLookUp annotation on current DTO
				 */
				updateCodeToDisplayValue((Serializable) getDTOPropertyValue(dtoProperty.getReadMethod(), dto, dtoProperty));
			}
		}
		
		/*
		 * For properties that are configured with CodeLookUp annotation
		 */
		for (PropertyDescriptor dtoProperty : dtoProperties)	{
			Object dtoPropertyValue = getDTOPropertyValue(dtoProperty.getReadMethod(), dto, dtoProperty);
			//Only fetch the display value, if the code value is NOT null.
			if(dtoPropertyValue != null) {
				//CodeLookUp codeLookUp = AnnotationUtil.findAnnotation(dtoProperty.getReadMethod(), CodeLookUp.class);
				String cacheKey = dto.getClass().getCanonicalName() + "." + dtoProperty.getDisplayName();
				if(ThreadLocalCodeLookUpCache.isIgnoredAnnotationField(cacheKey)) {
					continue;
				}
				CodeLookUp codeLookUp = (CodeLookUp)ThreadLocalCodeLookUpCache.getCachedAnnotation(cacheKey);
				if(codeLookUp == null) {
					codeLookUp = AnnotationUtil.findAnnotation(dtoProperty.getReadMethod(), CodeLookUp.class);
					if(codeLookUp != null) {
						ThreadLocalCodeLookUpCache.cacheAnnotation(cacheKey,  codeLookUp);
					} else {
						//add to ignored fields list.
						ThreadLocalCodeLookUpCache.addIgnoredAnnotationField(cacheKey);
					}
				}
				if(codeLookUp != null) {				
					fetchDisplayValue(codeLookUp, dtoProperty, dto, dtoPropertyValue);
				}
			}
		}
		
		return dto;
	}

	/**
	 * Update code to display value for each DTOs in the Collection, based on CodeLookUp annoation declaration.
	 * @param dtos the list of DTOs
	 */
	private void updateCodeToDisplayValueForList(List<Serializable> dtos) {
		
		if ((dtos == null) || (dtos.size() == 0))	{
			return;
		}
		
		for (Serializable dto : dtos)	{
			if ((dto != null))	{
				updateCodeToDisplayValue(dto);
			}
		}
	}
	
	/**
	 * Fetch the display value for the CodeLookUp annotation passed in.
	 * 
	 * @param codeLookUpAnnotation the annotation that contains the table and column name.
	 * @param dtoProperty the property descriptor object on the current dto.
	 * @param dto the dto that contains the property which has a CodeLookUp definition.
	 * @param dtoPropertyValue the code value on the property that has the CodeLookUp definition.
	 */
	private void fetchDisplayValue(CodeLookUp codeLookUpAnnotation, PropertyDescriptor dtoProperty,  
			Serializable dto, Object dtoPropertyValue)	{
		if(dtoPropertyValue != null) {
			//get the value from cache
			String displayValue = null;
			try {
				Object key = codeLookUpAnnotation.tableName() + codeLookUpAnnotation.columnName();
				Object results = ThreadLocalCodeLookUpCache.fetchCachedResults(key, dtoPropertyValue.toString()); 
				if(results != null) {
					displayValue = results.toString();
				}
			} catch (CacheKeyNotFoundException e) {
				displayValue = getCodeDescription(codeLookUpAnnotation.tableName(), codeLookUpAnnotation.columnName(), dtoPropertyValue);
				String key = codeLookUpAnnotation.tableName() + codeLookUpAnnotation.columnName();
				ThreadLocalCodeLookUpCache.addToCache(key, dtoPropertyValue.toString(), displayValue);
			}
			if(displayValue != null) {
				try {
					Class[] args1 = new Class[1];
				    args1[0] = Map.class;
				    Method setMtd = dto.getClass().getMethod("setLiteralDescriptionMap", args1);
					if(setMtd != null) {
						Method getMtd = dto.getClass().getMethod("getLiteralDescriptionMap");
						Map<String, String> userDefinedMap = (Map<String, String>) getDTOPropertyValue(getMtd, dto, dtoProperty);
						if(userDefinedMap == null) {
							userDefinedMap = new HashMap<String, String>();
						}
						userDefinedMap.put(dtoProperty.getDisplayName(), displayValue);
						setDTOPropertyValue(setMtd, dto, dtoProperty, userDefinedMap);
					}
				} catch (SecurityException securityEx) {
					throw new RuntimeException("SecurityException on updating the " +
							"literal description map for DTO: " + dto, securityEx);
				} catch (NoSuchMethodException noSuchMethodEx) {
					throw new RuntimeException("NoSuchMethodException during update of " +
							"literal description map for DTO: " + dto, noSuchMethodEx);
				}
			}
		}
	}
	
	/**
	 * Method to get the description for the table, column and code value passed in.
	 * 
	 * @param tableName the table name
	 * @param columnName the column name
	 * @param codeName the code name
	 * @return the display value for the code
	 */
	private String getCodeDescription(String tableName, String columnName,
			Object codeName) {
		String value = "";
		if(codeName != null) {
			CodeTableCriteria criteria = new CodeTableCriteria();
			criteria.setTableName(tableName);
			criteria.setCodeColumnName(columnName);
			criteria.setCodeName(codeName.toString());
			CodeLookupBO clb = getCodeLookupService().findByCodeName(criteria);
			value = clb.getValue();
		}
		return value;
	}
	
	/**
	 * Method to get the value of the DTO Property.
	 * 
	 * @param getter the getter
	 * @param dto the dto
	 * @param dtoProperty the dto property
	 * @return the dto property value
	 */
	public Object getDTOPropertyValue(Method getter, Serializable dto, PropertyDescriptor dtoProperty)	{
		try	{
			return getter.invoke(dto, null);
		} catch (InvocationTargetException invocationEx)	{
			throw new RuntimeException("Error getting property :" + dtoProperty.getDisplayName()
					+ " for DTO :" + dto.getClass().getName(), invocationEx);
		} catch (IllegalAccessException illegalEx)	{
			throw new RuntimeException("Error getting property :" + dtoProperty.getDisplayName()
					+ " for DTO :" + dto.getClass().getName(), illegalEx);			
		}
	}
	
	/**
	 * Method to set the value of the DTO Property, specially the setLiteralDescriptionMap method.
	 * 
	 * @param setter the setter
	 * @param dto the dto
	 * @param dtoProperty the dto property
	 * @return the dto property value
	 */
	private Object setDTOPropertyValue(Method setter, Serializable dto, PropertyDescriptor dtoProperty, Object arg)	{
		try	{
			return setter.invoke(dto, arg);
		} catch (InvocationTargetException invocationEx)	{
			throw new RuntimeException("Error setting property :" + dtoProperty.getDisplayName()
					+ " for DTO :" + dto.getClass().getName(), invocationEx);
		} catch (IllegalAccessException illegalEx)	{
			throw new RuntimeException("Error setting property :" + dtoProperty.getDisplayName()
					+ " for DTO :" + dto.getClass().getName(), illegalEx);			
		}		
	}

}
