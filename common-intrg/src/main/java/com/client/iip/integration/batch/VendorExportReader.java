package com.client.iip.integration.batch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemReader;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.client.iip.integration.party.ClientPartyDAO;
import com.client.iip.integration.party.ClientPartySearchCriteria;
import com.client.iip.integration.party.ClientPartySearchResult;
import com.fiserv.isd.iip.bc.party.PartyConstants;
import com.fiserv.isd.iip.core.date.DateUtility;
import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.stoneriver.iip.entconfig.systemoption.SystemOptionAssociationDTO;
import com.stoneriver.iip.entconfig.systemoption.SystemOptionService;

public class VendorExportReader implements ItemReader<ClientPartySearchResult> {
	
	private static final Logger logger = LoggerFactory.getLogger(VendorExportReader.class);
	
	private static final String MAX_VENDOR_RESULTS_SYSTEM_OPTION_CODE = "mvrc";
	private static final String MAX_SEARCH_RESULTS_SYSTEM_OPTION_CODE = "mprc";
	
	private Collection<ClientPartySearchResult> vendorList = null;
	
	private Date beginDate = null;
	
	private Date endDate = null;
	
	private ClientPartyDAO clientPartyDao;
	
	private boolean startNewBatchProcess = true;
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.batch.core.StepExecutionListener#beforeStep(org.springframework.batch.core.StepExecution)
	 * Sort the Files before Job Step
	 */
	@BeforeStep
	public void beforeStep(StepExecution stepExecution) {
		JobParameters jobParameters = stepExecution.getJobParameters();
		endDate = jobParameters.getDate("runDate");
		beginDate = jobParameters.getDate("lastRunDate");
	}
	
	/*
	 * This API reads all eligible vendors
	 */
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public synchronized ClientPartySearchResult read() throws Exception {
		logger.debug("In VendorExportReader read()");
		SystemOptionAssociationDTO option = null;
		//retrieve accounts list for next company only if accounts collection is null
		if(vendorList==null || this.startNewBatchProcess){
			try{
				option = MuleServiceFactory.getService(
					SystemOptionService.class).retrieveSystemOptionValue(
							MAX_VENDOR_RESULTS_SYSTEM_OPTION_CODE);
			}catch(Exception ex){
				//Fetch from max customer search threshold
				option = MuleServiceFactory.getService(
						SystemOptionService.class).retrieveSystemOptionValue(
								MAX_SEARCH_RESULTS_SYSTEM_OPTION_CODE);			
			}
			//Default to 500 if not configured.
			int maxResults = Integer.valueOf(option==null?"500":option.getStringValue());
			
			//int maxResults = Integer.valueOf(option.getStringValue());
			
			//Obtain All Party Events
			Collection<String> partyEvents = new ArrayList<String>();
			partyEvents.add("party_addpers");
			partyEvents.add("party_updpers");
			partyEvents.add("party_addorg");
			partyEvents.add("party_updorg");
			
	        Collection<String> roles = new ArrayList<String>();
	        roles.add(PartyConstants.ROLE_VENDOR);
	       
			Collection<ClientPartySearchResult> partySearchResults = new ArrayList<ClientPartySearchResult>();
			ClientPartySearchCriteria criteria = new ClientPartySearchCriteria();
			criteria.setPartyEventsFilter(partyEvents);
			criteria.setEventStartDate(DateUtility.toSQLTimestamp(beginDate));
			criteria.setEventEndDate(DateUtility.toSQLTimestamp(endDate));
	        criteria.setRolesFilter(roles);
	        //Set the Vendor Extract Threshold
	        criteria.setMaxSearchResults(new Long(maxResults));
	        // 11/18/2013 - @GR - Party Blocks Enhancement - Fetch all blocked vendors
	        criteria.setIncludeBlockedVendors(true);
	        
	        partySearchResults = getClientPartyDao().search(criteria);
	        
	        vendorList = new ArrayList<ClientPartySearchResult>();

	        vendorList.addAll(partySearchResults);

	        logger.info("vendorList size: " + vendorList.size());
		}
		
		if(vendorList != null && !vendorList.isEmpty()){
			this.startNewBatchProcess = false;
			ClientPartySearchResult partyResult = vendorList.iterator().next();
			logger.info("Party Id : " + partyResult.getPartyId());
			vendorList.remove(partyResult);
			return partyResult;			
		}
		this.startNewBatchProcess = true;
		return null;
	
	}
	
	/**
	 * return DAO object for customer search.
	 * @return ClientPartySearchDAO.
	 */
	public ClientPartyDAO getClientPartyDao() {
		return clientPartyDao;
	}

	/**
	 * Set ClientPartySearchDAO object.
	 * @param value ClientPartySearchDAO.
	 */
	@Inject(DaoInterface="client.daointerface.clientPartyDao")
	public void setClientPartyDao(ClientPartyDAO value) {
		this.clientPartyDao = value;
	}	
	

}
