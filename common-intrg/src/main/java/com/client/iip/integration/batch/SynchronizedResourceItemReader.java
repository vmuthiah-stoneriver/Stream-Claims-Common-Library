package com.client.iip.integration.batch;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.file.ResourcesItemReader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.client.iip.integration.core.util.XSDValidator;


public class SynchronizedResourceItemReader extends
		ResourcesItemReader {
	
	private static final Log logger = LogFactory.getLog(SynchronizedResourceItemReader.class);
	 
	private String xsdFileName = null;
	
	private String filePath;
	
	@BeforeStep
	public void beforeStep(StepExecution _stepExecution) throws Exception 
	{
		PathMatchingResourcePatternResolver filePattern = new PathMatchingResourcePatternResolver();
		Resource[] resources = filePattern.getResources(this.filePath);
		Arrays.sort(resources, comparator);
		super.setResources(Arrays.asList(resources).toArray(new Resource[resources.length]));	
	}	
	 
	public void setSchemaFileName(String _xsdFileName){
		xsdFileName = _xsdFileName;
	}
	 
	 private Comparator<Resource> comparator = new Comparator<Resource>() {

			/**
			 * Compares resource filenames.
			 */
			public int compare(Resource r1, Resource r2) {
				return r1.getFilename().compareTo(r2.getFilename());
			}

		};
	 
	 
	/**
	 * Sort Resources to serve up as items.
	 * 
	 * @param resources the resources
	 */
	public void setResources(String _filePath) throws Exception{
		this.filePath = _filePath;
	}	 
	
	 @Override
	  public synchronized void open(ExecutionContext executionContext) throws ItemStreamException{
		  super.open(executionContext);
	  }
	 
	 @Override
	  public synchronized void update(ExecutionContext executionContext) throws ItemStreamException{
		  super.update(executionContext);
	  }	 
	  
	  @Override
	  @Transactional(propagation = Propagation.REQUIRED)
	  public synchronized Resource read () throws Exception, UnexpectedInputException, ParseException {
	    Resource res = super.read();
	    if(res != null){
		    String strXML = FileUtils.readFileToString(res.getFile());
			//Validate against XSD
		    if(xsdFileName != null){
		    	XSDValidator xsdval = new XSDValidator();
				xsdval.validateSchema(strXML, xsdFileName);
		    }
	    }
		return res;
	  }
}
