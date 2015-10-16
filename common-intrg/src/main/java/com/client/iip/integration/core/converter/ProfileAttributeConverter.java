package com.client.iip.integration.core.converter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.client.iip.integration.party.ClientProfileDTO;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ProfileAttributeConverter implements Converter{

  private final Logger logger = LoggerFactory.getLogger(ProfileAttributeConverter.class);	
	
  @Override
  public boolean canConvert(Class clazz) {
    return clazz.equals(ClientProfileDTO.class);
  }

  @Override
  public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext context) {
	  if(object != null){
		  	logger.debug("object = {}", object);
	  		ClientProfileDTO profile = (ClientProfileDTO) object;
	  		writer.addAttribute("name", profile.getProfileName());  
	  		writer.setValue(profile.getProfileValue());
  		}

  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    return null;

  } 

}
