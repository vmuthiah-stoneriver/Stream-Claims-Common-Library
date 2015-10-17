package com.client.iip.integration;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import net.sf.xframe.xsddoc.Task;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XSDDocumentGenerator {
	
		private final static Logger logger = LoggerFactory.getLogger(XSDDocumentGenerator.class);
	
		//Doc Root Folder - The documentation would be written to this root folder.
		private final static String XSD_DOCUMENT_ROOT = System.getProperty("iip.xsddoc.dir.path");
		
		private final static String[] schemaList = new String[]{"ClaimSearchRequest", "ClaimSearchResult",
		"ClaimDetailRequest", "ClaimImport", "Common", "BatchJobRequest", "BatchJob", "BatchJobStatus",
		"DisbursementExport", "DisbursementImport",	"PolicySearchRequest", "PolicySearchResponse", "PolicyRetrieveDetailsRequest", 
		"PolicyRetrieveDetailsResponse", "PolicyImportRequest", "PolicyReimportRequest", "PolicyImportResponse", "PolicyListUnitsRequest", 
		"PolicyListUnitsResponse", "ClaimNotification", "Party", "CustomerSearchRequest", "CustomerSearchResponse", "DuplicateParty", 
		"AddressVerificationRequest", "AddressVerificationResponse", "AddressGeoCodeRequest", "AddressGeoCodeResponse", "Orders", "OrderReturn", 
		"GLExport", "IIPErrorResponse" };
		
		//private final static String[] schemaList = new String[]{"ClaimImport"};
		
		private final static String SCHEMA_FOLDER = "src\\main\\resources\\xsd";
				
		private final static String SCHEMA_TITLE = "XML Schema for ";
		
		public static void main(String[] args) throws Exception{
			
			if(XSD_DOCUMENT_ROOT == null){
				throw new Exception("Missing environment property iip.xsddoc.dir.path, Set document directory path");
			}
			
			Date startTime = new Date();
			
			for(String fname: schemaList){
				logger.debug("Schema Name : " + fname);
				Task task = new Task();				 
				 File outFile = new File(XSD_DOCUMENT_ROOT + File.separator + fname);
			     if (!outFile.exists()) {
			    	 outFile.mkdirs();
			     }
			     task.setOut(XSD_DOCUMENT_ROOT + File.separator + fname);
			     task.setDoctitle(SCHEMA_TITLE + fname);
			     task.setFile(SCHEMA_FOLDER + File.separator + fname + ".xsd");
			     task.setDebug(true);
				 task.execute();
				 //Update blank href links
				 fixLinks(XSD_DOCUMENT_ROOT + File.separator + fname + "/noNamespace/complexType");
				 fixLinks(XSD_DOCUMENT_ROOT + File.separator + fname + "/noNamespace/element");
				 logger.info("Links Fixed");
			}
			 	logger.info("XSD Doc Completed!!!");
				 //Copy the index files to the root folder
				 FileUtils.copyDirectory(new File("src\\test\\resources\\xsddocindex"), new File(XSD_DOCUMENT_ROOT), 
						 new SuffixFileFilter(new String[]{".html",".gif"}), false);
				 logger.info("Index Files Copied");
				 long timeDiff = (new Date()).getTime() - startTime.getTime();
				 logger.info("Time taken to generate docs : " + timeDiff + "ms");
			}
		
		public static void fixLinks(String folderPath) throws IOException{
			//Read Files from folder
			File fileFolder = new File(folderPath);
			//Iterate through each file and modify Empty links
			for(File f: fileFolder.listFiles()){
				String fileStr = FileUtils.readFileToString(f);
				fileStr = fileStr.replaceAll("href=\"\"", "");
				FileUtils.writeStringToFile(f, fileStr);
			}
			
		}
}
