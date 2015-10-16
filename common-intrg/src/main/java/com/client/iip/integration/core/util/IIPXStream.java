package com.client.iip.integration.core.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;

public class IIPXStream extends XStream {
	
	private static final Logger logger = LoggerFactory.getLogger(IIPXStream.class);

	private List<String> aliasFileList = new ArrayList<String>();

	public IIPXStream() throws ClassNotFoundException {
		super(new PureJavaReflectionProvider());

		aliasFileList.add("properties/client-mule-alias-map");
		init();

	}

	public IIPXStream(List<String> aliasPathListIn)
			throws ClassNotFoundException {
		super(new PureJavaReflectionProvider());

		aliasFileList.addAll(aliasPathListIn);
		init();
	}

	public String convert2XML(Object _obj) throws Exception{
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		Writer writer = new OutputStreamWriter(outStream, "UTF-8");
		super.toXML(_obj, writer);
		return outStream.toString("UTF-8");
	}

	public Object convert2Object(String _xml) throws Exception{
		InputStream inStream = new ByteArrayInputStream(_xml.getBytes("UTF-8"));
		return super.fromXML(inStream);
	}

	private void init() throws ClassNotFoundException {

		loadAliases();

		setMode(XStream.NO_REFERENCES);

	}

	private void loadAliases() throws ClassNotFoundException {
		for (Object aliasObj : aliasFileList) {
			String aliasFile = (String) aliasObj;
			ResourceBundle aliasMap = null;
			try{
				aliasMap = ResourceBundle.getBundle(aliasFile);
			}catch(MissingResourceException ex){
				logger.info("Ignoring Resource Not Found: " + aliasFile);
				continue;
			}
			Enumeration<String> keys = aliasMap.getKeys();

			while (keys.hasMoreElements()) {
				String alias = (String) keys.nextElement().trim();
				String className = aliasMap.getString(alias).trim();
				super.alias(alias, Class.forName(className));
			}
		}
	}
}
