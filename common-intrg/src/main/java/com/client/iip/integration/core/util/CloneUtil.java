package com.client.iip.integration.core.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

	/**
	 * Utility for object cloning
	 * 
	 */
public class CloneUtil
	{
	    /**
	     * Provides a deep clone serializing/de-serializng <code>objToClone</code>     
	     * 
	     * @param objToClone The object to be cloned
	     * @return The cloned object
	     */
	    public static final Object deepClone(Object objToClone)
	    {
	        try
	        {
	            ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream(100);
	            ObjectOutputStream objectoutputstream = new ObjectOutputStream(bytearrayoutputstream);
	            objectoutputstream.writeObject(objToClone);
	            byte abyte0[] = bytearrayoutputstream.toByteArray();
	            objectoutputstream.close();
	            ByteArrayInputStream bytearrayinputstream = new ByteArrayInputStream(abyte0);
	            ObjectInputStream objectinputstream = new ObjectInputStream(bytearrayinputstream);
	            Object clone = objectinputstream.readObject();
	            objectinputstream.close();
	            return clone;
	        }
	        catch (Exception ex)
	        {
	        	ex.printStackTrace();
	        }
	        return null;
	    }
	    
	    /*
	     * Copies the Source to the target Object, Both Objects should be of the same type
	     */
	    public static final Object copyObject(Object srcObject, Object targetObject)
	    {
	        try
	        {
	            ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream(100);
	            ObjectOutputStream objectoutputstream = new ObjectOutputStream(bytearrayoutputstream);
	            objectoutputstream.writeObject(srcObject);
	            byte abyte0[] = bytearrayoutputstream.toByteArray();
	            objectoutputstream.close();
	            ByteArrayInputStream bytearrayinputstream = new ByteArrayInputStream(abyte0);
	            ObjectInputStream objectinputstream = new ObjectInputStream(bytearrayinputstream);
	            targetObject = objectinputstream.readObject();
	            objectinputstream.close();
	            return targetObject;
	        }
	        catch (Exception ex)
	        {
	        	ex.printStackTrace();
	        }
	        return null;
	    }	    

}
