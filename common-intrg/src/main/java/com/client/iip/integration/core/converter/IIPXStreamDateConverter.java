package com.client.iip.integration.core.converter;

import com.thoughtworks.xstream.converters.basic.DateConverter;

public class IIPXStreamDateConverter extends DateConverter {
	
	public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS z";
	
	public static final String DEFAULT_DATETIMEONLY_FORMAT = "yyyy-MM-dd HH:mm:ss.S";
	
	// Alternate formats
	public static final String[] ACCEPTABLE_DATETIMEONLY_FORMATS = new String[] {"yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd HH:mm:ss"};
	
	public static final String[] ACCEPTABLE_FORMATS = new String[] {
			"yyyy-MM-dd HH:mm:ss.S z", "yyyy-MM-dd HH:mm:ss.SSS zzz",
			"yyyy-MM-dd HH:mm:ss.S zzz", "yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd HH:mm:ss.S"};

	/**
	 * Construct a DateConverter with standard formats and lenient set off.
	 */
	public IIPXStreamDateConverter() {
		super(DEFAULT_FORMAT, ACCEPTABLE_FORMATS, false);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(java.util.Date.class)
				|| type.equals(java.sql.Date.class)
				|| type.equals(java.sql.Time.class)
				|| type.equals(java.sql.Timestamp.class);
	}
}
