package com.client.iip.integration.core.converter;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.AbstractReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriterHelper;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import flex.messaging.io.ArrayCollection;

/**
 * Custom Reflection converter created to marshal objects that contain Collections 
 * that are of type List Types. 
 * 
 */

public class CustomReflectionConverter extends AbstractReflectionConverter {

	
	  private final Logger logger = LoggerFactory.getLogger(CustomReflectionConverter.class);	
	  
	  
	  public CustomReflectionConverter(Mapper mapper, ReflectionProvider reflectionProvider) {
	        super(mapper, reflectionProvider);
	  }
		
	  @Override
	 public boolean canConvert(Class clazz) {
		  return clazz.equals(com.stoneriver.iip.claims.policy.ClaimPolicyFinancialInterestParticipationDTO.class)
	                || clazz.equals(com.stoneriver.iip.claims.unit.ClaimDamageDetailsDTO.class)
	                || clazz.equals(com.fiserv.isd.iip.bc.party.api.ClearPartyAddressDTO.class)
	        		|| clazz.equals(com.stoneriver.iip.orders.api.OrdersDTO.class)
	        		|| clazz.equals(com.fiserv.isd.iip.bc.address.api.AddressDTO.class);
	  }

  
	    public void doMarshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
 	        final Set seenFields = new HashSet();
	        final Map defaultFieldDefinition = new HashMap();
	        // Attributes might be preferred to child elements ...
	         reflectionProvider.visitSerializableFields(source, new ReflectionProvider.Visitor() {
	            public void visit(String fieldName, Class type, Class definedIn, Object value) {
	                if (!mapper.shouldSerializeMember(definedIn, fieldName)) {
	                    return;
	                }
	                if (!defaultFieldDefinition.containsKey(fieldName)) {
	                    Class lookupType = source.getClass();
	                    // See XSTR-457 and OmitFieldsTest
	                    // if (definedIn != source.getClass() && !mapper.shouldSerializeMember(lookupType, fieldName)) {
	                    //    lookupType = definedIn;
	                    // }
	                    defaultFieldDefinition.put(fieldName, reflectionProvider.getField(lookupType, fieldName));
	                }
	                
	                SingleValueConverter converter = mapper.getConverterFromItemType(fieldName, type, definedIn);
	                if (converter != null) {
	                    if (value != null) {
	                        if (seenFields.contains(fieldName)) {
	                            throw new ConversionException("Cannot write field with name '" + fieldName 
	                                + "' twice as attribute for object of type " + source.getClass().getName());
	                        }
	                        final String str = converter.toString(value);
	                        if (str != null) {
	                            writer.addAttribute(mapper.aliasForAttribute(mapper.serializedMember(definedIn, fieldName)), str);
	                        }
	                    }
	                    // TODO: name is not enough, need also "definedIn"! 
	                    seenFields.add(fieldName);
	                }
	            }
	        });

	        // Child elements not covered already processed as attributes ...
	        reflectionProvider.visitSerializableFields(source, new ReflectionProvider.Visitor() {
	            public void visit(String fieldName, Class fieldType, Class definedIn, Object newObj) {
	            	
	                if (!mapper.shouldSerializeMember(definedIn, fieldName)) {
	                    return;
	                }
	                
	                if (!seenFields.contains(fieldName) && newObj != null) {
	                    Mapper.ImplicitCollectionMapping mapping = mapper.getImplicitCollectionDefForFieldName(source.getClass(), fieldName);
	                    if (mapping != null) {
	                        if (mapping.getItemFieldName() != null) {
	                            Collection list = (Collection) newObj;
	                            for (Iterator iter = list.iterator(); iter.hasNext();) {
	                                Object obj = iter.next();
	                                writeField(fieldName, mapping.getItemFieldName(), mapping.getItemType(), definedIn, obj);
	                            }
	                        } else {
	                            context.convertAnother(newObj);
	                        }
	                    } else {
	                        writeField(fieldName, fieldName, fieldType, definedIn, newObj);
	                    }
	                }
	            }

	            private void writeField(String fieldName, String aliasName, Class fieldType, Class definedIn, Object newObj) {
	                ExtendedHierarchicalStreamWriterHelper.startNode(writer, mapper.serializedMember(source.getClass(), aliasName), fieldType); 

	                Class actualType = newObj.getClass();
	    	        //Cast flex Arrays into java Arrays
	                if(actualType.equals(flex.messaging.io.ArrayCollection.class)){
	                	logger.debug("Flex Array Object Detected");
	                	newObj = new IIPFlex2JavaCollectionConverter().convertFlex2JavaCollection((ArrayCollection)newObj);
	                	actualType = newObj.getClass();
	                }
	                
	                Class defaultType = mapper.defaultImplementationOf(fieldType);
	                
	                if (!actualType.equals(defaultType)) {
	                    String serializedClassName = mapper.serializedClass(actualType);
	                    if (!serializedClassName.equals(mapper.serializedClass(defaultType)) && !aliasName.equals("dto")) {
	                        writer.addAttribute(mapper.aliasForAttribute("class"), serializedClassName);
	                    }
	                }else if(actualType.equals(java.util.ArrayList.class)
	                        || actualType.equals(java.util.HashSet.class)
	                        || actualType.equals(java.util.LinkedList.class)
	                        || actualType.equals(java.util.Vector.class)){ //Fix for ListType Marshalling Bug
	                		String serializedClassName = mapper.serializedClass(actualType);
	                		writer.addAttribute(mapper.aliasForAttribute("class"), serializedClassName);
	                }
	                

	                final Field defaultField = (Field)defaultFieldDefinition.get(fieldName);
	                if (defaultField.getDeclaringClass() != definedIn) {
	                    writer.addAttribute(mapper.aliasForAttribute("defined-in"), mapper.serializedClass(definedIn));
	                }

	                Field field = reflectionProvider.getField(definedIn,fieldName);
	                marshallField(context, newObj, field);
	                writer.endNode();
	            }

	        });
	    }

	    protected void marshallField(final MarshallingContext context, Object newObj, Field field) {
	        context.convertAnother(newObj, mapper.getLocalConverter(field.getDeclaringClass(), field.getName()));
	    }
	  

	  @Override
	  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
	    return null;

	  }
}
