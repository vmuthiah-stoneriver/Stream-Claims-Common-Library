package com.client.iip.integration.user;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fiserv.isd.iip.core.meta.annotation.Pojo;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.stoneriver.iip.entconfig.sa.api.users.UserSearchCriteriaDTO;
import com.stoneriver.iip.entconfig.sa.api.users.UserSearchResultDTO;
import com.stoneriver.iip.entconfig.users.EnterpriseUserConfigurationService;

/**
 * 
 * 
 * @author Saurabh.Bhatnagar
 * 
 */

@Pojo(id = "client.endpoint.userInformationComposite")
public class UserInformationComposite {
	
	private final Logger logger = LoggerFactory.getLogger(UserInformationComposite.class);
	
	/**
	 * This is service method used to retrieve all the Users in the System.
	 * It will return the User Details with only Primary Phone number.
	 * 
	 * @param request
	 *            import request
	 *            
	 * @return Collection of UserSearchResultDTO.
	 */
	public Collection<UserSearchResultDTO> retrieveUsers(String request){
		
		Collection<UserSearchResultDTO> resultDTOs = new ArrayList<UserSearchResultDTO>();
		
		EnterpriseUserConfigurationService enterpriseUserConfigurationService = 
			MuleServiceFactory.getService(EnterpriseUserConfigurationService.class);
		UserSearchCriteriaDTO criteria = new  UserSearchCriteriaDTO();
		logger.info("Setting Empty Criteria as it is not using criteria");
		resultDTOs = enterpriseUserConfigurationService.retrieveUsers(criteria);
		return resultDTOs;
	}
	
	

}
