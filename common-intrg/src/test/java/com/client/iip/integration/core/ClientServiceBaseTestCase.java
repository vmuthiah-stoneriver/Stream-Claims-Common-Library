package com.client.iip.integration.core;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;

import org.junit.After;
import org.junit.Before;
import org.mule.api.MuleException;
import org.mule.module.client.MuleClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.client.iip.integration.core.converter.IIPXStreamDateConverter;
import com.client.iip.integration.core.util.ClientQueryLoaderImpl;
import com.client.iip.integration.core.util.IIPXStream;
import com.fiserv.isd.iip.core.container.ContainerException;
import com.fiserv.isd.iip.core.data.LogicalDataSource;
import com.fiserv.isd.iip.core.data.metadata.DataAccessMetaDataContext;
import com.fiserv.isd.iip.core.data.query.file.Queries;
import com.fiserv.isd.iip.core.data.query.file.Rdbms;
import com.fiserv.isd.iip.core.data.query.source.QuerySource;
import com.fiserv.isd.iip.core.date.DateUtility;
import com.fiserv.isd.iip.core.security.IIPUser;
import com.fiserv.isd.iip.core.service.MuleContextAccessor;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.fiserv.isd.iip.core.thread.IIPThreadContextFactory;
import com.fiserv.isd.iip.core.thread.IIPUserContext;
import com.fiserv.isd.iip.util.dataretriever.DataRetrieverParameter;
import com.fiserv.isd.iip.util.framework.test.BatchBaseServiceTestCase;
import com.stoneriver.iip.entconfig.dataretriever.DataRetrieverConstants;
import com.stoneriver.iip.entconfig.dataretriever.DataRetrieverDataAccess;
import com.stoneriver.iip.entconfig.dataretriever.DataRetrieverException;

public abstract class ClientServiceBaseTestCase extends BatchBaseServiceTestCase {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private IIPXStream xstream;

	private MuleClient muleClient = null;
	
	private String loginUser = "sysadmin";
	
	protected static List<String> aliasFileList = new ArrayList<String>(){{add("properties/client-mule-alias-map");
																add("properties/custom-mule-alias-map");}};

	private Unmarshaller unmarshaller;
	protected LogicalDataSource lds;

	/**
	 * Constructor for ValidationBaseServiceTestCase.
	 * 
	 * @param name
	 *            name.
	 */
	public ClientServiceBaseTestCase(final String name) {
		super(name);
	}
	
	/*
	 * The caller needs to implement this to return appropriate Login user Id. 
	 * This will establish the user context for the rules.
	 */
	
	public String getLoginUser(){
		return this.loginUser;
	}
	
	public void setLoginUser(String userName){
		this.loginUser = userName;
	}

	/**
	 * setUp method.
	 * 
	 * @throws Exception
	 *             when there are errors
	 * 
	 */
	@Before
	public void setUp() throws Exception {
		super.setUp();
		logger.info("In ClientServiceBaseTestCase setUp");
		unmarshaller = (Unmarshaller) getBeanFactory().getBean(
				"jaxbQueryMarshaller");
		lds = getLds();
		setuploginUser();
	}

	/**
	 * tear down method.
	 * 
	 * @throws Exception
	 *             when there are errors
	 */
	@After
	public void tearDown() throws Exception {
		super.tearDown();
		logger.info("In ClientServiceBaseTestCase tearDown");
	}

	/**
	 * Sets the LDS.
	 * 
	 * @return logicalDS lds.
	 */
	public LogicalDataSource getLds() {
		return (LogicalDataSource) getBeanFactory().getBean(
				"clientLogicalDataSource");
	}

	/**
	 * Load the queries from the XML file.
	 * 
	 * @param queryFile
	 *            the query file
	 * @return Map the query context
	 */
	public Map<String, DataAccessMetaDataContext> loadDRQueries(String queryFile) {
		return parseResource(createStreamReader(queryFile));
	}

	/**
	 * Overrides BootStrapConfiguration.
	 * 
	 * @return location for bootstrap
	 */
	@Override
	protected abstract String getIIPBootstrapConfigFile();
		
	/**
	 * Load all the Data Retriever queries from the XML files.
	 * 
	 * @return Map the query context
	 */
	public Map<String, DataAccessMetaDataContext> getDRQueries() {
		return (Map<String, DataAccessMetaDataContext>) ((ClientQueryLoaderImpl) getPojo("clientDRQueryLoader"))
				.loadQueries();
	}

	public IIPXStream getXStreamInstance() throws Exception {
		if (xstream == null) {
			xstream = new IIPXStream(aliasFileList);
			xstream.registerConverter(new IIPXStreamDateConverter());
			xstream.addDefaultImplementation(java.sql.Date.class, java.util.Date.class);
			xstream.addDefaultImplementation(java.sql.Timestamp.class, java.util.Date.class);
			xstream.addDefaultImplementation(java.sql.Time.class, java.util.Date.class);
			xstream.addDefaultImplementation(java.util.ArrayList.class, java.util.Collection.class);
			xstream.addDefaultImplementation(java.util.HashMap.class, java.util.Map.class);
		}
		return xstream;
	}

	/*
	 * Send Mule Event
	 */

	public Object sendSyncEvent(String url, Object payload,
			Map<String, Object> messageProperties, int timeout)
			throws MuleException {

		return getMuleClient().send(url, payload, messageProperties, timeout)
				.getPayload();

	}

	/*
	 * Lookup MuleClient
	 */

	public MuleClient getMuleClient() throws MuleException {

		if (muleClient == null) {
			muleClient = new MuleClient(MuleContextAccessor.getMuleContext());
		}

		return muleClient;
	}

	/**
	 * Sets the query files.
	 * 
	 * @return queryFilesParam the query files
	 */
	public String[] getDRQueryFiles() {
		return new String[] { "" };
	}

	/**
	 * Create a stream reader for the resource.
	 * 
	 * @param queryFile
	 *            the resource to parse
	 * @return XMLEventReader the reader
	 */
	private Queries[] createStreamReader(String queryFile) {
		URL u = null;
		try {
			ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			Resource[] resources = resolver.getResources(queryFile);
			Queries[] queries = new Queries[resources.length];

			if (resources.length == 0) {
				throw new ContainerException("Resource not Found :" + queryFile);
			}

			// only using first resource returned
			for (int i = 0; i < resources.length; i++) {
				u = resources[i].getURL();
				InputStream in = u.openStream();
				queries[i] = (Queries) unmarshaller.unmarshal(new StreamSource(
						in));
				if (logger.isInfoEnabled()) {
					logger.info("Queries: {}", u.getFile());
				}
			}
			return queries;
		} catch (IOException ioEx) {
			logger.error(
					"IOException trying to read the provided resource : {}" + u,
					ioEx);
			throw new ContainerException(
					"IOException trying to read the provided resource :" + u
							+ ". Exception is : " + ioEx.getMessage());
		} catch (XmlMappingException streamEx) {
			logger.error(
					"XML Mapping Exception trying to read the provided resource : "
							+ u, streamEx);
			throw new ContainerException(
					"XML Stream Exception trying to read the provided resource :"
							+ u + ". Exception is : " + streamEx.getMessage());
		}
	}

	/**
	 * Parse the stream reader and create QueryDescription objects from the
	 * queries file.
	 * 
	 * @param queries
	 *            data access queries.
	 * @return Collection the query description
	 */
	private Map<String, DataAccessMetaDataContext> parseResource(
			Queries[] queries) {

		DataAccessMetaDataContext queryContext = null;

		Map<String, DataAccessMetaDataContext> queryContexts = new HashMap<String, DataAccessMetaDataContext>();

		for (Queries q : queries) {
			for (Rdbms rdbms : q.getRdbms()) {
				queryContext = new DataAccessMetaDataContext();
				queryContext.setAccessId(rdbms.getAccessId());

				if (rdbms.getDescription() != null) {
					queryContext.setDescription(rdbms.getDescription().trim());
				}

				if (rdbms.getContent() != null) {
					queryContext.setContent(rdbms.getContent().trim());

					QuerySource querySource = new QuerySource();
					querySource.setUnformattedSql(rdbms.getContent().trim());
					querySource.initialize();
					queryContext.setQuerySource(querySource);
				}

				if (rdbms.getName() != null) {
					queryContext.setName(rdbms.getName().trim());

				}

				queryContexts.put(queryContext.getAccessId(), queryContext);
			}
		}

		return queryContexts;
	}
	
	/**
	 * Get all the request parameters for the Data Retriever.
	 * 
	 * @param drCode The Data Retriever code.
	 * @return list of Data Retriever parameters.
	 */
	public List<DataRetrieverParameter> getParmValue(String drCode) {
		List<DataRetrieverParameter> reqParams = 
			new ArrayList<DataRetrieverParameter>();
    	Map<String, Object> mapParms = new HashMap<String, Object>();
    	mapParms.put(DataRetrieverConstants.DR_CONFIG_CONTEXT_PARAMETER_KEY, 
    			drCode);
		
    	// set the other DR parameters
    	Collection<DataRetrieverParameter> parms = 
    		DataRetrieverDataAccess.queryList(
    		DataRetrieverConstants.DR_PARAMETER_ACCESS_ID, 
    		mapParms, DataRetrieverParameter.class, lds);
    	reqParams.addAll(parms);
    	
    	// set the context DR parameters
    	parms = DataRetrieverDataAccess.queryList(
    		DataRetrieverConstants.DR_CONTEXT_PARAMETER_ACCESS_ID, 
    		mapParms, DataRetrieverParameter.class, lds);
    	if (parms != null && !parms.isEmpty()) {
    		for (DataRetrieverParameter parm : parms) {
    			parm.setContext(true);
    		}
    	}
    	reqParams.addAll(parms);
    	
    	// set the sub-context DR parameters
    	parms = DataRetrieverDataAccess.queryList(
    		DataRetrieverConstants.DR_SUBCONTEXT_PARAMETER_ACCESS_ID, 
    		mapParms, DataRetrieverParameter.class, lds);
    	if (parms != null && !parms.isEmpty()) {
    		for (DataRetrieverParameter parm : parms) {
    			parm.setSubcontext(true);
    		}
    	}
    	reqParams.addAll(parms);
		
    	return reqParams;
	}
	
	/**
	 * Sets the Data Retriever parameter value based on the parameters type.
	 * 
	 * @param reqParams List of Data Retriever parameters.
	 * @param drCode The Data Retriever code.
	 */
	public void setParmValue(List<DataRetrieverParameter> reqParams, String drCode) {
		for (DataRetrieverParameter param : reqParams) {
			String type = param.getParmType();
			Object value = null;
			
			if (type.equals("char") 
					|| type.equals("varchar")
					|| type.equals("charset")) {
				value = "a";
			} else if (type.equals("boolean")) {
				value = Boolean.FALSE;
			} else if (type.equals("nbr")) {
				value = new Long("1");
			} else if (type.equals("money")
				|| type.equals("dec")) {
				value = new BigDecimal("1");
			} else if (type.equals("date")
					|| type.equals("datetm")
					|| type.equals("time")) {
				value = DateUtility.getSQLSystemDateTime();
			} else {
				throw new DataRetrieverException("Unknown Data Retriever "
						+ "parameter type "	+ type + " for " + drCode);
			}
			
			if (param.isCollectionInd()) {
				param.setValue(Arrays.asList(value));
			} else {
				param.setValue(value);
			}
		}
	}
	
	protected void setuploginUser(){
	    	try{
	    		UserDetailsService service = MuleServiceFactory.getService(UserDetailsService.class);
	    		UserDetails details = service.loadUserByUsername(getLoginUser());
	    		IIPUser iipUser = (IIPUser)details;
	    		IIPUserContext userContext = IIPThreadContextFactory.getUserContext();
	    		userContext.setId(iipUser.getUserId());
			
	    		//Creating User instance to set in Authentication object
	    		User userDetails = new User(details.getUsername(), details.getPassword(), iipUser.isEnabled(), 
					iipUser.isAccountNonExpired(), iipUser.isCredentialsNonExpired(), 
					iipUser.isAccountNonLocked(), iipUser.getAuthorities());
			
	    		UsernamePasswordAuthenticationToken login = 
	    				new UsernamePasswordAuthenticationToken(userDetails, details.getPassword(), iipUser.getAuthorities());
	    		SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_THREADLOCAL);
	    		SecurityContextHolder.getContext().setAuthentication(login);
	    	}catch(Exception ex){
	    		ex.printStackTrace();
	    	}
	    }	
	

}
