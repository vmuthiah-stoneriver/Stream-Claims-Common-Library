package com.client.iip.integration.core.converter;

import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fiserv.isd.iip.core.date.DateUtility;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.basic.DateConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class IIPXStreamDateTimestampConverter implements Converter {
	
	private final Logger logger = LoggerFactory.getLogger(IIPXStreamDateTimestampConverter.class);

	private DateConverter dateConverter;

	public IIPXStreamDateTimestampConverter() {
		dateConverter = new DateConverter(
				IIPXStreamDateConverter.DEFAULT_DATETIMEONLY_FORMAT,
				IIPXStreamDateConverter.ACCEPTABLE_DATETIMEONLY_FORMATS);
	}

	@SuppressWarnings("rawtypes")
	public boolean canConvert(Class type) {
		return type.equals(java.util.Date.class)
				|| type.equals(java.sql.Date.class)
				|| type.equals(java.sql.Time.class)
				|| type.equals(java.sql.Timestamp.class);
	}

	@Override
	  public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext context) {

	}
	
	  @Override
	  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		  Date dateObj = null;
		  Calendar cal = Calendar.getInstance();
		  //logger.info("Node Name: " + reader.getNodeName() + " Type: " + context.getRequiredType());
		  if (!reader.getNodeName().endsWith("DateTime") && (context.getRequiredType().equals(java.sql.Date.class) 
				|| context.getRequiredType().equals(java.util.Date.class)) ){
				  //Zero out the time portion for Date Only field
				  dateObj = (Date)dateConverter.fromString(reader.getValue());
				  cal.setTime(dateObj);
				  cal.set(Calendar.HOUR_OF_DAY, 0);
				  cal.set(Calendar.MINUTE, 0);
				  cal.set(Calendar.SECOND, 0);
				  cal.set(Calendar.MILLISECOND, 0);
				  dateObj = cal.getTime();
			  }else if(context.getRequiredType().equals(java.sql.Time.class) 
				|| context.getRequiredType().equals(java.sql.Timestamp.class)){
					//Default to Epoch Date for Timestamp only fields
				  	dateObj = (Date)dateConverter.fromString(reader.getValue());
					cal.setTime(dateObj);
					cal.set(1970, Calendar.JANUARY, 1);
					dateObj = DateUtility.toTimestamp(cal.getTime());
			  }else{
				  //Default should be Date Time Field
				   dateObj = (Date)dateConverter.fromString(reader.getValue());
			  }
	    return dateObj;

	  }

}
