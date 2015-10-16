package com.client.iip.integration.core.converter;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlTransient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import com.fiserv.isd.iip.util.bean.ClassUtils;

import flex.messaging.io.ArrayCollection;

public class IIPFlex2JavaCollectionConverter {
	
		
	private final Logger logger = LoggerFactory.getLogger(IIPFlex2JavaCollectionConverter.class);
	
	
	public IIPFlex2JavaCollectionConverter(){
		
	}
	  /*
	   * Helper method to convert Flex 2 Java Collection.
	   */
	  public Object convertFlex2JavaCollection(ArrayCollection flexCollection){
		  	Collection<Object> utilCollection = new ArrayList<Object>();
      	for (int i=0;i< flexCollection.size(); i++){
	        		Object myObject = flexCollection.get(i);
	        		updateFlexCollection(myObject);
	        		utilCollection.add(myObject);
	        	}
	        return utilCollection;
	    }
	  

	 /* This method recursively iterates through the object and converts all flex colletions into java util collections
	   */
	  
	  public void updateFlexCollection(Object bean) {
		  	//Hold the reference to the Initial Object state for restore.
		    logger.debug("Introspect and update Flex collection in object : " + bean);
		  	Object initObj = null;
		  	if(initObj == null){
		  		initObj = bean;
		  	}
		 //Get the Collections properties, except those annotated @XmlTransient
			Field[] collectionFields = ClassUtils.getCollectionDeclaredFields(
					bean.getClass(), XmlTransient.class, true);		  	
			try {
				for(Field collectionField : collectionFields){
					PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(bean.getClass(), collectionField.getName());
		            Method reader =  pd.getReadMethod();
		                Object myObj = reader.invoke(bean, new Object[] {});
		                if (myObj != null) {
		                	if(myObj instanceof flex.messaging.io.ArrayCollection ){
		                		logger.debug("Flex Array Object Found: " + myObj);
		        		  		Object tmpObj = update2UtilCollection((ArrayCollection)myObj);
		        		  		Method writer = pd.getWriteMethod();
		        		  		writer.invoke(bean, tmpObj);
		        		  	}
		                }
 		        }
		    } catch (Exception ex) {
				 logger.error("Exception occurred in Reflection", ex);
				 bean = initObj;
		   }
	  	}
	  
	  
	  public Collection<Object> update2UtilCollection(ArrayCollection _coll){
		  	Collection<Object> utilCollection = new ArrayList<Object>();
		  	
		  	for (int i=0;i<_coll.size(); i++){
		  		Object myObject = _coll.get(i);
	        	utilCollection.add(myObject);
	        }
		  	
	        return utilCollection;		  
	  }	  	

}
