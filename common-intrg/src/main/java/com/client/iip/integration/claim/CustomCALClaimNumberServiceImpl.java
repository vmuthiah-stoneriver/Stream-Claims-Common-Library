package com.client.iip.integration.claim;


import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.fiserv.isd.iip.core.meta.annotation.Service;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.stoneriver.iip.claims.CALClaimNumberService;
import com.stoneriver.iip.claims.ClaimDTO;
import com.stoneriver.iip.claims.ClaimNumberException;
import com.stoneriver.iip.claims.ClaimsServiceConstants;
import com.stoneriver.iip.claims.cws.CALClaimNumberServiceImpl;
import com.stoneriver.iip.entconfig.sequence.SequenceService;

@Service(id = "claims.serviceObject.custom.calClaimNumberService")
public class CustomCALClaimNumberServiceImpl  extends CALClaimNumberServiceImpl implements CALClaimNumberService {
	
	private final Logger logger = LoggerFactory.getLogger(CustomCALClaimNumberServiceImpl.class);
	
	private ControlNumberDAO controlNumberDAO;
	private SequenceService sequenceService;

	/**
	 * @return the controlNumberDAO
	 */
	public ControlNumberDAO getControlNumberDAO() {
		return controlNumberDAO;
	}

	/**
	 * @param controlNumberDAO
	 *            the controlNumberDAO to set
	 */
	@Inject(DaoInterface = "entconfig.daointerface.controlNumberDao")
	public void setControlNumberDAO(ControlNumberDAO controlNumberDAO) {
		this.controlNumberDAO = controlNumberDAO;
	}

	/**
	 * Getter for sequenceService.
	 * 
	 * @return the sequenceService
	 */
	public SequenceService getSequenceService() {
		if (sequenceService == null) {
			sequenceService = MuleServiceFactory
					.getService(SequenceService.class);
		}
		return sequenceService;
	}

	@Override
	public String generateClaimNumber(ClaimDTO claimDTO)
			throws ClaimNumberException {
		logger.debug("Executing generateClaimNumber for {}", claimDTO);		
		Long companyLOBId;
		Long jurisdictionId;
		String claimNumber = null;
		
		if( System.getProperty("claimNumberConfig") == null){
			claimNumber = super.generateClaimNumber(claimDTO);
		}
		else if(System.getProperty("claimNumberConfig").equals("prc")){
		// get companyLOB and jurisdiction from the policy, otherwise the claim
		if ((claimDTO.getPolicy() != null)
				&& (claimDTO.getPolicy().getCompanyLOBId() != null)) {
			jurisdictionId = claimDTO.getPolicy().getJurisdictionId();
			companyLOBId = claimDTO.getPolicy().getCompanyLOBId();
		} else {
			jurisdictionId = claimDTO.getJurisdictionId();
			companyLOBId = claimDTO.getCompanyLOBId();
		}

		// lookup the control number based on the companyLOB and jurisdiction
		ControlNumberSearchCriteria criteria = new ControlNumberSearchCriteria(companyLOBId, jurisdictionId);
		Collection<ControlNumberBO> controlNumberCollection = getControlNumberDAO()
				.retrieveControlNumberByCompanyLOBAndJurisdiction(criteria);
		Iterator<ControlNumberBO> iter = controlNumberCollection.iterator();
		ControlNumberBO controlNumberBO = null;
		if (iter.hasNext()) {
			controlNumberBO = (ControlNumberBO) iter.next();
		}
		String controlNumber = null;
		if ((null != controlNumberBO) && (StringUtils.isNotBlank(controlNumberBO.getControlNumber()))) {
			controlNumber = controlNumberBO.getControlNumber();
		}else{
			ClaimNumberException e = new ClaimNumberException("claimNumberConfig not defined");
			e.setMessage("claimNumberConfig not defined");
			throw e;			
		}
		// get the next claim number in the sequence
		Long nextKey = getSequenceService().getNextNumberInSequence(
				ClaimsServiceConstants.CAL_CLAIM_NUM_SEQ);

		// format the string according to the PRC requirements
		claimNumber = formatClaimNumber(controlNumber, nextKey);
		}else{
			ClaimNumberException e = new ClaimNumberException("Control number not found");
			e.setMessage("Control number not found");			
		}

		return claimNumber;
	}

	private String formatClaimNumber(String controlNumber, Long nextKey) {
		// format: controlNumber + last 2 digits of claim number + claim number
		String keyStr = Long.toString(nextKey);
		String checkNumber = StringUtils.right(keyStr, 2);
		keyStr = StringUtils.leftPad(keyStr, 8, '0');
				
		String formattedNumber = controlNumber + checkNumber + keyStr;
		return formattedNumber;
	}
}
