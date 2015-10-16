package com.client.iip.integration;

/*
 * @author Guru Radhakrishnan
 * This Class Reflects the defined class attributes from the XStream mapping file
 * It introspects Root Object hierarchy and Contained Object Hierarchy and
 * Compares them against the XSD. It also includes Collection Object Complex Types and corresponding field
 * attributes are checked against the XSD.
 * It recursively looks up the complexType definition until the match is found for the field element name.
 * Rules -> 1. Complex Types have to be defined with the Same name as the Class Object Name
 *		   2. element attribute and complex types should use the default namespace mapping for the object types.  
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestSuite;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fiserv.isd.iip.util.bean.ClassUtils;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

public class StreamDTO2XSDValidationTestCase extends TestSuite{
	
	private static final Logger logger = LoggerFactory.getLogger(StreamDTO2XSDValidationTestCase.class);
	
	//private String foundClassName = "None";
	
	private String srchComplexType = "None";
	
	private String sourcePath = "src/main/resources/properties/"; //Source property files containing the class element mapping
	private String targetPath = "src/main/resources/xsd/"; // directory = target xsd directory.
	private Map<String, String> masterMap = new HashMap<String, String>();
	private Map<String, String> dupCheckMap = new HashMap<String, String>();
	private Map<String, String> collTypeMap = new HashMap<String, String>();
	private Map<String, String> errorMap = new HashMap<String, String>();
	private Map<String, String> aliasMap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
	private Map<String, String> dummyTypeMap = new HashMap<String, String>(){{
		put("java.lang.String", "java.lang.String");
		put("java.lang.Exception", "java.lang.Exception");
		put("java.lang.Long", "java.lang.Long");
		put("com.stoneriver.iip.claims.participation.ClaimParticipantCoverageDTO", "com.stoneriver.iip.claims.participation.ClaimParticipantCoverageDTO");
		put("com.stoneriver.iip.claims.participation.ClaimParticipantReserveDTO", "com.stoneriver.iip.claims.participation.ClaimParticipantReserveDTO");
		put("com.stoneriver.iip.claims.participation.FinancialInformationDTO", "com.stoneriver.iip.claims.participation.FinancialInformationDTO");
		put("com.stoneriver.iip.workflow.api.WorkItemDocumentExternalDTO", "com.stoneriver.iip.workflow.api.WorkItemDocumentExternalDTO");
		put("com.stoneriver.iip.workflow.api.WorkItemStatusCompositeDTO", "com.stoneriver.iip.workflow.api.WorkItemStatusCompositeDTO");
		put("com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentHeaderDTO", "com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentHeaderDTO");
	}};
	
	private Map<String, String> falsePositiveMap = new HashMap<String, String>(){{
		put("long", "long");
		put("parentComposite", "parentComposite");
		put("dataTransferObject", "dataTransferObject");
		put("criteria", "criteria");
		put("controlNumberDTO", "controlNumberDTO");
		put("clueReportDTO", "clueReportDTO");
		put("iipCoreSystemException", "iipCoreSystemException");
		put("serialVersionUID", "serialVersionUID");
		put("docs", "docs");
		put("current", "current");
		put("expired", "expired");		
		//put("status", "status");
		//put("currentStatus","currentStatus");
		//put("expiredStatusCollection","expiredStatusCollection");		
		/*put("address", "address");			
		put("claimReserveSummary", "claimReserveSummary");		
		put("claimStatusAdditionalReasonDTO","claimStatusAdditionalReasonDTO");		
		put("caseIssueStatusDTO", "caseIssueStatusDTO");
		put("claimBillDetailDTO", "claimBillDetailDTO");
		put("clientWorkItemDTO", "clientWorkItemDTO");
		put("workItemStatus", "workItemStatus");*/
	}};
	
	/**
	 * Constructor for CheckPartyDuplicateTestCase.
	 * 
	 * @param name
	 *            name.
	 */
	public StreamDTO2XSDValidationTestCase(){
		super("XSDElementValidator");
		logger.debug("------------------------------ Process started ------------------------------");
		logger.debug("Test initiated.");
	}



	/**
	 * Test for xsd completeness.
	 * 
	 * @throws Exception
	 *             when there are errors.
	 */
	public void checkForXSDElements() throws Exception {

	try{
	
		//Iterate through each property file
		File[] fileList = new File(sourcePath).listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name)
    		{
    		    return (name.endsWith("client-mule-alias-map.properties"));	
    		}
    	      });
		//Read Each Property File
		for (int i=0; i < fileList.length; i++) {
			//System.out.println("Looking up prop file: " + fileList[i].getName());
			InputStream input = new FileInputStream(fileList[i]);
			Properties props = new Properties(); 
			props.load(input);
			//Enumeration keys = props.keys();
			//Load Master Map with Containment Objects
			loadAllContainmentObjects(props);
			System.out.println("masterMap Size: " + masterMap.size());
			System.out.println("masterMap: " + masterMap);
		
			Iterator<Map.Entry<String, String>> entries = masterMap.entrySet().iterator();
		
			while (entries.hasNext()) {
				Map.Entry<String, String> entry = entries.next();
				//String elementName = (String) keys.nextElement();
				//String className = props.getProperty(elementName);
				String elementName = entry.getKey();
				String className = entry.getValue();
				/*Uncomment this for Testing a specific Element */
				//if(!elementName.equals("authClaimPaymentStatusDTO")){
				//	continue;
				//}				
				//System.out.println("Element Name :" + elementName);
				//First Load the containing class element name to check if it is available to make sure we are searching on the same namespace.
				String srchString = "element name=\""+elementName+ "\"";				
				//Now Fetch all attributes for the class recursively and check if available in the XSD Files. 
				//If not Found Print name of the element that is not found
				if(falsePositiveMap.get(elementName) != null || dummyTypeMap.get(className) != null){
					continue;
				}
				//System.out.println("Looking up " + Class.forName(className));
				Field[]  fields = ClassUtils.getDeclaredFields(Class.forName(className), true);

			if(searchAllElements(targetPath, srchString, className)) { // If classnamespace exists then proceed

				if(dummyTypeMap.get(className) != null){
					continue;
				}
				String srchClassFoundFile = searchFileNameContainsComplexType(targetPath, srchString);
				//logger.debug("Class Found File: " + srchClassFoundFile);
				for (Field fld : fields) {
					//System.out.println("Looking for Field Name: " + fld.getName() + " in the XSD.");
					//Exclude serialVersion and static fields			
					if(java.lang.reflect.Modifier.isStatic(fld.getModifiers()) || java.lang.reflect.Modifier.isTransient(fld.getModifiers())
							|| fld.isAnnotationPresent(XStreamOmitField.class)	|| falsePositiveMap.get(fld.getName()) != null){
							continue;
					}					

					String srchfieldName = "element name=\""+fld.getName()+ "\"";
					
					//System.out.println("Searching for Keyword : " + srchfieldName);
					//System.out.println("srchComplexType : " + srchComplexType);
					if(!srchClassFoundFile.equals("None") 
							&& strictSearch(targetPath+srchClassFoundFile, className, fld.getName(), fld.getGenericType().toString(), srchComplexType, srchfieldName)){
							continue;
						}else{
							//Check Master ErrorList
							if(errorMap.get(fld.getName()+fld.getGenericType().toString()+Class.forName(className).getName()+srchClassFoundFile) == null && !srchClassFoundFile.startsWith("Client")){
								//System.out.println("Keyword : " + srchfieldName);
								//System.out.println("srchComplexType : " + srchComplexType);								
								System.out.println("!ERROR! Could Not Find Field Node: "+ fld.getName() + " Type: " + fld.getGenericType().toString() + " from class " + Class.forName(className).getName() + " in XSD. File :" +srchClassFoundFile);
								errorMap.put(fld.getName()+fld.getGenericType().toString()+Class.forName(className).getName()+srchClassFoundFile, 
										fld.getName()+fld.getGenericType().toString()+Class.forName(className).getName()+srchClassFoundFile);
								//System.out.println("Search Key : " +srchString);
							}
							
						}
				}	
			}else {
				System.out.println("!ERROR! Could Not Find base element Node: "+ elementName + " Type: " + Class.forName(className).getName() + " in XSD.");
			}
		  }
			input.close();
		}
		System.out.println("dummyTypeMap Size: " + dummyTypeMap.size());
		System.out.println("dummyTypeMap: " + dummyTypeMap);
		System.out.println("collTypeMap Size: " + collTypeMap.size());
		System.out.println("collTypeMap: " + collTypeMap);			
		//Now Look for collection Type definitions
		System.out.println("Scanning Collection Complex Types against XSD....");
		searchCollectionTypes();
		//All collection types should be properly aliased. Check to make sure and report unmapped ones.
		Iterator<Map.Entry<String, String>> collMapEntries = collTypeMap.entrySet().iterator();

		while(collMapEntries.hasNext()){
			Map.Entry<String, String> entry = collMapEntries.next();
			String elementName = entry.getKey();
			String className = entry.getValue();
			//Check if the class name is a dummyType
			if(dummyTypeMap.get(className) != null) continue;
			INNER:
			if(aliasMap.get(elementName) == null){
				//Now check if the class is name is not mapped as well
				for (Map.Entry<String, String> aliasEntry : aliasMap.entrySet()) {
				   if(className.equals(aliasEntry.getValue())){
					   break INNER;
				   }
				}
				System.out.println("$$ERROR$$ Alias Mapping is not present for the Collection Type Object Key:" + elementName + " Value : " + entry.getValue());
			}
		}

		
	} catch (IOException e) {
			logger.error("IOException caught during search: "
					+ e.getMessage());
	}

}
	
	public void searchCollectionTypes() throws Exception{
		
		Iterator<Map.Entry<String, String>> collentries = collTypeMap.entrySet().iterator();
		while (collentries.hasNext()) {
			Map.Entry<String, String> entry = collentries.next();
			String elementName = entry.getKey();
			String className = entry.getValue();
			String srchComplexType = "complexType name=\""+elementName+ "\"";
			/*if(elementName.equals("Exception") || elementName.equals("String") 
					|| elementName.equals("Long") || elementName.equals("BigDecimal")){
				continue;
			}*/
			if(dummyTypeMap.get(className) != null){
				continue;
			}
			
			/*Uncomment this for Testing a specific Element */
			//if(!elementName.equals("UserWorkGroupInfoDTO")){
			//	continue;
			//}				
			  	//System.out.println("Lookup class : " + className);
				Field[]  fields = ClassUtils.getDeclaredFields(Class.forName(className), true);
			  	String srchClassFoundFile = searchFileForKeyWord(targetPath, srchComplexType);
			  	
			  	//If not found then search with ClientPrefix
			  	if(srchClassFoundFile.equals("None")){
			  		srchComplexType = "complexType name=\"Client"+elementName+ "\"";
			  		srchClassFoundFile = searchFileForKeyWord(targetPath, srchComplexType);
			  		if(srchClassFoundFile.equals("None")){
			  			srchComplexType = "complexType name=\""+elementName+ "Type\"";
			  			srchClassFoundFile = searchFileForKeyWord(targetPath, srchComplexType);
			  		}
			  	}

			  	for (Field fld : fields) {
			  		//Exclude serialVersion and static fields
			  		if(java.lang.reflect.Modifier.isStatic(fld.getModifiers()) || java.lang.reflect.Modifier.isTransient(fld.getModifiers())
						|| fld.isAnnotationPresent(XStreamOmitField.class)	|| falsePositiveMap.get(fld.getName()) != null){
						continue;
					}				
			  		String srchfieldName = "element name=\""+fld.getName()+ "\"";
			  		if(!srchClassFoundFile.equals("None")
			  				&& strictSearch(targetPath+srchClassFoundFile, className, fld.getName(), fld.getGenericType().toString(), srchComplexType, srchfieldName)){
			  			continue;
			  		}else{
			  			String errorLevel = srchClassFoundFile.equals("None")?"!WARNING!":"!ERROR!";
			  		//Check Master ErrorList
					if(errorMap.get(fld.getName()+fld.getGenericType().toString()+Class.forName(className).getName()+srchClassFoundFile) == null
							&& 	dummyTypeMap.get(className) == null && !srchClassFoundFile.startsWith("Client")){  			  			
			  				System.out.println(errorLevel + " Could Not Find Field Node: "+ fld.getName() + " Type: " + fld.getGenericType().toString() + " from class " + Class.forName(className).getName() + " in XSD. File :" +srchClassFoundFile);
			  				//System.out.println("Search Key : " +srchComplexType);
			  				errorMap.put(fld.getName()+fld.getGenericType().toString()+Class.forName(className).getName()+srchClassFoundFile, 
								fld.getName()+fld.getGenericType().toString()+Class.forName(className).getName()+srchClassFoundFile);  			  				
						}
			  		}
			  	}
		}		
	}
	
	public void loadAllContainmentObjects(Properties prop) throws Exception{
		
		 //1. Obtain the filed elements for the class
		 Enumeration keys = prop.keys();
		 Map<String, String> baseMap = new HashMap<String, String>();
		
		 //Load to lookupmap for Dup Checking
		while(keys.hasMoreElements()){
			String fieldName = (String) keys.nextElement();
			String className = (String) prop.getProperty(fieldName);
			dupCheckMap.put(className, className);
		}
		keys = prop.keys();
		while (keys.hasMoreElements()) {
			String fieldName = (String) keys.nextElement();
			String className = (String) prop.getProperty(fieldName);
			//2. Load the Root key value pair in hashmap, This is what we found from prop file
			masterMap.put(fieldName, className);
			//3. Now Reflect the attributes to look for any complex types. If found load them into the Mater List
		    Field[]  fields = ClassUtils.getDeclaredFields(Class.forName(className), true);
		   
		    for (Field fld : fields) {
		    	//If the class is present then go to the next one.
		    	if(dupCheckMap.get(fld.getType().getName()) != null){
		    		continue;
		    	}
		    	if(fld.getType().getName().startsWith("com.stoneriver") || fld.getType().getName().startsWith("com.fiserv")){
		    		baseMap.put(fld.getName(), fld.getType().getName());
		    	}else if(fld.getGenericType().toString().startsWith("java.util.Collection")){
		    		String classNameString = ((ParameterizedType)fld.getGenericType()).getActualTypeArguments()[0].toString();
		    		classNameString = classNameString.replace("class", "").replace("?", "").replace("extends", "").trim();
		    		if(!classNameString.isEmpty()){
		    			collTypeMap.put(classNameString.substring(classNameString.lastIndexOf(".")<0?0:classNameString.lastIndexOf(".")+1), classNameString);
		    		}	    		
		    	}
		    	
		    }
		}
		
		//Load the key into a case insentiveMap to cross check with the collection types to make sure all collection objects are properly aliased.
		aliasMap.putAll(masterMap);
		
		if(!baseMap.isEmpty()){
			//Add to the master Map
			masterMap.putAll(baseMap);
			System.out.println("baseMap Size: " + baseMap.size());
			System.out.println("baseMap: " + baseMap);
		 	//Now Transfer the baseMap to recursively Lookup for similar types within them and load master Map
		 	recursiveLookUp(baseMap);
		}
		
		
	}
	
	private void recursiveLookUp(Map<String, String> lookup) throws Exception{
		
		Iterator<Map.Entry<String, String>> entries = lookup.entrySet().iterator();
		
		Map<String, String> tempMap = new HashMap<String, String>();
		while (entries.hasNext()) {
			Map.Entry<String, String> entry = entries.next();
			//If Dup type found then skip
			if(dupCheckMap.get(entry.getValue()) != null){
				continue;
			}//else{
			//	System.out.println("$$ERROR$$ Alias Mapping is not present for the Containment Object Key:" + entry.getKey() + " Value : " + entry.getValue());
			//}
			masterMap.put(entry.getKey(), entry.getValue());
		    String className = entry.getValue();
		    //System.out.println("key Name : " + entry.getKey());
		    //System.out.println("ClassName : " + className);
		    if(masterMap.get(entry.getKey()) != null){
		    	//Field available, go to the next one.
		    	continue;
		    }
		    Field[]  fields = ClassUtils.getDeclaredFields(Class.forName(className), true);
		    for (Field fld : fields) {
		    	if(fld.getType().getName().startsWith("com.stoneriver") || fld.getType().getName().startsWith("com.fiserv")){
		    			tempMap.put(fld.getName(), fld.getType().getName());
		    			dupCheckMap.put(fld.getGenericType().toString(), fld.getGenericType().toString());
		    	}else if(fld.getGenericType().toString().startsWith("java.util.Collection")){
		    		String classNameString = ((ParameterizedType)fld.getGenericType()).getActualTypeArguments()[0].toString();
		    		classNameString = classNameString.replace("class", "").replace("?", "").replace("extends", "").trim();
		    		if(!classNameString.isEmpty()){
		    			collTypeMap.put(classNameString.substring(classNameString.lastIndexOf(".")<0?0:classNameString.lastIndexOf(".")+1), classNameString);
		    		}
		    	}
		    }
		}
		
		if(!tempMap.isEmpty()){
			//Add to the master Map
			masterMap.putAll(tempMap);
		    //Now Transfer the baseMap to recursively Lookup for similar types within them
		    recursiveLookUp(tempMap);
		}
				
	}
	
	public boolean strictSearch(String fileName, String className, String fieldName, String fieldType, String primsearchName, String secsearchName) throws Exception{
		
		/*System.out.println("fileName :" + fileName);
		System.out.println("className :" + className);
		System.out.println("fieldName :" + fieldName);
		System.out.println("fieldType :" + fieldType);
		System.out.println("primsearchName :" + primsearchName);
		System.out.println("secsearchName :" + secsearchName);*/
		if(fileName.contains("None")){
			return false;
		}
		
		//Return true for summy types
		if(primsearchName.contains("dummyType")){
			//Load dummyTypeMap
			dummyTypeMap.put(className, className);
			return true;
		}

		
		File sf = new File(fileName);
		Pattern p = Pattern.compile(primsearchName, Pattern.CASE_INSENSITIVE);
		Pattern f = Pattern.compile(secsearchName);
		 try
		    {
		      FileInputStream fis = new FileInputStream(sf);
		      StringBuffer sb = new StringBuffer();
		      sb.append(IOUtils.toString(fis));

		      Matcher m = p.matcher(sb.toString());
		      
		      if(m.find())
		      {
		    	  //logger.debug("Match Found");
	    	  
		    	 //Now find for the secondary name
		    	 // m = f.matcher(sb.toString());

		    	  String searchString = sb.toString().split("(?i)" + primsearchName)[1].split("</xs:complexType>")[0];
		    	  //logger.debug("searchString: " + searchString);
		    	  if(searchString.contains("type=\"collectionType\" use=\"optional\" ")){
		    		  	//logger.debug("Collection Found");
		    		  	 String collectionType = searchString.split("type=\"")[1].split("\"")[0];
		    		  	 String srchComplexType = "complexType name=\""+collectionType+ "\"";	
		    			  //System.out.println("srchComplexType : " + srchComplexType);
		    			  String srchClassFoundFile = searchFileForKeyWord(targetPath, srchComplexType);
		    			  //Recursive Call
		    			  return strictSearch(targetPath+srchClassFoundFile, className, fieldName, fieldType, srchComplexType, secsearchName);		    		  	
		    	  }
		    	  //searchString = searchString.contains("type=\"collectionType\" use=\"required\" ")?sb.toString():searchString;
		    	  //System.out.println("Substring Value: " + searchString);
		    	  m = f.matcher(searchString);
		    	  if(m.find()){
				      //If the field is of dummyType then load the falsePositive Map
				      String secsearchNameType =  sb.toString().split(secsearchName)[1].split(">")[0];
				      secsearchNameType = secsearchNameType.split("type=\"")[1].split("\"")[0];
				      //logger.debug("secsearchNameType : " + secsearchNameType);		      
				      if(secsearchNameType.equals("dummyType")){
				    	  String classNameString = null;
				    	  if(fieldType.startsWith("java.util.Collection")){
					    	classNameString = fieldType.split("<")[1].split(">")[0];
				    	  }else{
				    		  classNameString = fieldType;
				    	  }
						//Load dummyTypeMap
				    	if(!classNameString.isEmpty()){
				    		classNameString = classNameString.replace("class", "").replace("?", "").replace("extends", "").trim();
				    		dummyTypeMap.put(classNameString, classNameString);
				    	}
				      }
		    		  return true;
		    	  }else{
		    		  //If Not found then Look for the base extension complexType and recursively go up the hierarchy
		    		  if(searchString.contains("extension base=")){
		    			  String baseExtension = searchString.split("extension base=\"")[1].split("\"")[0];
		    			  String srchComplexType = "complexType name=\""+baseExtension+ "\"";	
		    			  //System.out.println("Checking fields from base extension : " + baseExtension);
		    			  String srchClassFoundFile = searchFileForKeyWord(targetPath, srchComplexType);
		    			  //Recursive Call
		    			  return strictSearch(targetPath+srchClassFoundFile, className, fieldName, fieldType, srchComplexType, secsearchName);
		    		  }
		    	  }
		      }
		      fis.close();
		    } 
		    catch(Exception e)
		    {
		    	e.printStackTrace();
		      	logger.error("fileName :" + fileName);
		      	logger.error("className :" + className);
		      	logger.error("fieldName :" + fieldName);
		      	logger.error("fieldType :" + fieldType);
		      	logger.error("primsearchName :" + primsearchName);
		      	logger.error("secsearchName :" + secsearchName);		      
				System.out.println("Error processing file0 : "+sf.getName());
		    }		
		
		return false;
		
	}
	
	/*public boolean searchObjectType(String srcfilePath, String targetfilePath, String objectType) throws Exception{
		foundClassName = "None";
		File[] fileList = new File(srcfilePath).listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name)
    		{
    		    return (name.endsWith("map.properties"));	
    		}
    	      });
		//Read Each File
		for (int i=0; i < fileList.length; i++) {
			InputStream input = new FileInputStream(fileList[i]);
			Properties props = new Properties(); 
			props.load(input);
			Enumeration keys = props.keys();

			while (keys.hasMoreElements()) {
				String elementName = (String) keys.nextElement();
				String className = props.getProperty(elementName);
				if(elementName.equals("long")){
					continue;
				}
				Field[] fields = ClassUtils.getDeclaredFields(Class.forName(className), true);	//Get all declared fields for the class
				for (Field fld : fields) {
					if(fld.getType().getName().equals(objectType) ){
						//System.out.println("Object Type :" + objectType + " Found in class " + Class.forName(className).getName() + " is not available in XSD.");
						//Check if the filed name is defined in the xsd then it is valid
						String srcfldName = "name=\""+fld.getName()+ "\"";
						if(searchAllFiles(targetfilePath, srcfldName)){
							return true;
						}else{
							foundClassName = className;							
							return false;
							
						}
					}
				}
			}
			
		}
		
		return false;
	
	}*/
	
	
	public boolean searchAllElements(String filePath, String keyWord, String className){
		File dir = new File(filePath);
		Pattern p = Pattern.compile(keyWord);
		if(dir.exists()) // Directory exists then proceed.
		{ 
		  for(File f : dir.listFiles())
		  {
		    if(!f.isFile()) continue;
		    if(f.getName().equals("CALClaimReserve.xsd") || f.getName().equals("CUID.xsd")) continue;
		    try
		    {
		      FileInputStream fis = new FileInputStream(f);
		      //byte[] data = new byte[fis.available()];
		      //fis.read(data);
		      StringBuffer sb = new StringBuffer();
		      sb.append(IOUtils.toString(fis));
		      String text = sb.toString();
		      Matcher m = p.matcher(text);
		      if(m.find())
		      {
			      //If the field is of dummyType then load the falsePositive Map
			      String keyWordType =  sb.toString().split(keyWord)[1].split(">")[0];
			      keyWordType = keyWordType.split("type=\"")[1].split("\"")[0];
			      //logger.debug("secsearchNameType : " + secsearchNameType);		      
			      if(keyWordType.equals("dummyType") || keyWordType.equals("string")
			    		  || keyWordType.contains("boolean") || keyWordType.contains("long")  || keyWordType.contains("int") 
		    			  ||keyWordType.contains("double") || keyWordType.contains("float") || keyWordType.contains("decimal")){
			    	  	//Load dummyTypeMap
			    		dummyTypeMap.put(className, "dummyType");
			    	}		    	  
		    	  return true;
		      }
		      fis.close();
		    } 
		    catch(Exception e)
		    {
		      System.out.println("Error processing file1 : "+f.getName());
		    }

		  }

		}	
		  
		return false;		
		
	}
	
		
	public String searchFileNameContainsComplexType(String filePath, String keyword){
		//System.out.println(" In  searchFileNameContainsCompelexType: " + keyword +  " File Path: " + filePath);
		srchComplexType = "None";
		String srchString = "";
		File dir = new File(filePath);
		Pattern p = Pattern.compile(keyword);
		if(dir.exists()) // Directory exists then proceed.
		{ 
		  for(File f : dir.listFiles())
		  {
		    if(!f.isFile()) continue;
		    if(f.getName().equals("CALClaimReserve.xsd") || f.getName().equals("CUID.xsd")) continue;
		    try
		    {
		      FileInputStream fis = new FileInputStream(f);
		      //byte[] data = new byte[fis.available()];
		      //fis.read(data);
		      StringBuffer sb = new StringBuffer();
		      sb.append(IOUtils.toString(fis));
		      String fileString = sb.toString();
		      Matcher m = p.matcher(fileString);
		      if(m.find())
		      {
		    	  int begin = fileString.lastIndexOf(keyword);
		    	  int end = fileString.substring(begin).indexOf(">") + begin;
		    	  //Looks for Type and Parse it.
		    	  srchString = fileString.substring(begin, end);
		    	  //System.out.println("srchString: " + srchString);
		    	  int startChar = srchString.indexOf("type=\"");
		    	  int endChar = srchString.substring(startChar+6).indexOf("\"") +startChar;
		    	  //System.out.println("startChar: " +startChar);
		    	  String cmplxType = srchString.substring(startChar+6, endChar+6);
		    	  if(cmplxType.contains("boolean") || cmplxType.contains("long")  || cmplxType.contains("int") 
		    			  ||cmplxType.contains("double") || cmplxType.contains("float") || cmplxType.contains("decimal")
		    			  || cmplxType.contains("string") ) continue;
		    	  srchComplexType = "complexType name=\"" + cmplxType + "\"";
		    	  //System.out.println("search complexType: " + srchComplexType);
		    	  return searchFileForKeyWord(filePath, srchComplexType);
		      }
		      fis.close();
		    } 
		    catch(Exception e)
		    {
		    	//System.out.println("srchString: " + srchString);
		    	e.printStackTrace();
		    	System.out.println("Error processing file2 : "+f.getName() + "keyword: " + keyword);
		    }

		  }

		} 
		  
		return "None";			
		
	}
	
	public String searchFileForKeyWord(String filePath, String keyword){
		File dir = new File(filePath);
		Pattern p = Pattern.compile(keyword, Pattern.CASE_INSENSITIVE);
		if(dir.exists()) // Directory exists then proceed.
		{ 
		  for(File f : dir.listFiles())
		  {
		    if(!f.isFile()) continue;
		    if(f.getName().equals("CALClaimReserve.xsd") || f.getName().equals("CUID.xsd")) continue;
		    try
		    {
		      FileInputStream fis = new FileInputStream(f);
		      //byte[] data = new byte[fis.available()];
		      //fis.read(data);
		      StringBuffer sb = new StringBuffer();
		      sb.append(IOUtils.toString(fis));
		      String fileString = sb.toString();
		      Matcher m = p.matcher(fileString);

		      if(m.find())
		      {
		    	  return f.getName();
		      }
		      fis.close();
		    } 
		    catch(Exception e)
		    {
		      System.out.println("Error processing file3 : "+f.getName());
		    }

		  }

		} 
		//System.out.println("Keyword Not Found : " + keyword);
		return "None";			
	}
	

	
	public static void main(String[] args) throws Exception{
		logger.info("Begin XSD Scanner.....");
		StreamDTO2XSDValidationTestCase ct = new StreamDTO2XSDValidationTestCase();
		ct.checkForXSDElements();
		logger.info("Scanning Completed.");
        /*Field field = StreamDTO2XSDValidationTestCase.class.getDeclaredField("fooList");   
        System.out.println("fieldtype: " + field.getType());
        Type type = field.getGenericType();   
        System.out.println("type: " + type);   
        if (type instanceof ParameterizedType) {   
            ParameterizedType pt = (ParameterizedType) type;   
            System.out.println("raw type: " + pt.getRawType());   
            System.out.println("owner type: " + pt.getOwnerType());   
            System.out.println("actual type args:");   
            for (Type t : pt.getActualTypeArguments()) {   
                System.out.println("    " + t);   
            }   
        }   
    
        System.out.println();   
    
        Object obj = field.get(new StreamDTO2XSDValidationTestCase());   
        System.out.println("obj: " + obj);   
        System.out.println("obj class: " + obj.getClass());  	*/	
	}
	
}