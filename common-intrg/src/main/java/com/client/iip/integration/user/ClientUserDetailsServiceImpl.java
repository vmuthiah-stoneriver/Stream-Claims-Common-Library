package com.client.iip.integration.user;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.client.iip.integration.sa.ClientEnterpriseConfigDAO;
import com.fiserv.isd.iip.bc.financials.FinancialsConstants;
import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.fiserv.isd.iip.core.meta.annotation.Service;
import com.fiserv.isd.iip.core.security.dao.IIPSecurityDAO;
import com.fiserv.isd.iip.core.security.model.UserAuthorizationLimitRequest;
import com.fiserv.isd.iip.core.security.model.UserLoginDTO;
import com.fiserv.isd.iip.core.security.services.IIPSecurityService;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.fiserv.isd.iip.core.util.IIPCoreSystemException;
import com.stoneriver.iip.entconfig.sa.api.users.UserAuthorizationInfoCompositeDTO;
import com.stoneriver.iip.entconfig.sa.api.users.UserCommInformationCompositeDTO;
import com.stoneriver.iip.entconfig.sa.api.users.UserCredentialInfoCompositeDTO;
import com.stoneriver.iip.entconfig.sa.api.users.UserInfoDTO;
import com.stoneriver.iip.entconfig.sa.api.users.UserSearchCriteriaDTO;
import com.stoneriver.iip.entconfig.sa.api.users.UserSearchResultDTO;
import com.stoneriver.iip.entconfig.sa.api.users.UserWorkGroupInfoCompositeDTO;
import com.stoneriver.iip.entconfig.sa.api.users.securitygroups.UserSecurityGroupCompositeDTO;
import com.stoneriver.iip.entconfig.users.EnterpriseUserConfigurationService;

/**
 * Implementation of Client User Details Composite Service for returning User
 * Details
 * 
 * @author saurabh.bhatnagar
 * 
 */
@Service(id = "integration.serviceObject.ClientUserDetailsService")
public class ClientUserDetailsServiceImpl implements ClientUserDetailsService {

	private final Logger logger = LoggerFactory.getLogger(ClientUserDetailsServiceImpl.class);
	EnterpriseUserConfigurationService enterpriseUserConfigurationService;

	private IIPSecurityDAO iipSecurityDAO;

	ClientEnterpriseConfigDAO clientEnterpriseConfigDAO = null;
	/**
	 * @return clientClaimStatusDAO
	 */
	public ClientEnterpriseConfigDAO getClientEnterpriseConfigDAO() {
		return clientEnterpriseConfigDAO;
	}

	/**
	 * @param value
	 *            Client Enterprise Config DAO to set
	 */
	@Inject(DaoInterface = "client.daointerface.clientEntConfigDao")
	public void setClientClaimStatusDAO(ClientEnterpriseConfigDAO value) {
		this.clientEnterpriseConfigDAO = value;
	}

	private static final String PASSWORD_MASK = "********";

	/**
	 * This method will return the user details.
	 * 
	 * @param request
	 *            ClientUserDetailRequestCriteria
	 * @return ClientUserDetailsCompositeDTO
	 */
	@Override
	public ClientUserDetailsCompositeDTO retrieveUserDetails(ClientUserDetailRequestCriteria request) {

		ClientUserDetailsCompositeDTO compositeDTO = new ClientUserDetailsCompositeDTO();
		try {
			
			String userName = request.getUserName();
			if (userName != null) {
				UserLoginDTO user = getIIPSecurityService().findUserLoginByLoginName(request.getUserName());
				request.setUserId(user.getUserId());
			}

			UserInfoDTO userInfoDTO = this.retrieveUser(request.getUserId());
			compositeDTO.setUserId(request.getUserId());
			compositeDTO.setUserInfoDTO(userInfoDTO);

			if (request.isReturnUserCommunication()) {

				UserCommInformationCompositeDTO informationCompositeDTO = this.retrieveUserCommunication(request
						.getUserId());
				compositeDTO.setUserCommInformationCompositeDTO(informationCompositeDTO);
			}
			if (request.isReturnUserAuthorizationInfo()) {

				UserAuthorizationInfoCompositeDTO userAuthorizationInfoCompositeDTO = this
						.retrieveUserAuthorizationInfo(request.getUserId());
				userAuthorizationInfoCompositeDTO.getUserAuthorizationInfoDTO().setPassword(PASSWORD_MASK);
				userAuthorizationInfoCompositeDTO.getUserAuthorizationInfoDTO().setConfirmPassword(PASSWORD_MASK);
				compositeDTO.setUserAuthorizationInfoCompositeDTO(userAuthorizationInfoCompositeDTO);
			}
			if (request.isReturnUserCredentialInfo()) {

				UserCredentialInfoCompositeDTO userCredentialInfoCompositeDTO = this.retrieveUserCredentialInfo(request
						.getUserId());
				compositeDTO.setUserCredentialInfoCompositeDTO(userCredentialInfoCompositeDTO);
			}
			if (request.isReturnUserSecurityGroups()) {

				UserSecurityGroupCompositeDTO userSecurityGroupCompositeDTO = this.retrieveUserSecurityGroups(request
						.getUserId());
				compositeDTO.setUserSecurityGroupCompositeDTO(userSecurityGroupCompositeDTO);
			}
			if (request.isReturnUserWorkGroupInfo()) {

				UserWorkGroupInfoCompositeDTO workGroupInfoCompositeDTO = this.retrieveUserWorkGroupInfo(request
						.getUserId());
				compositeDTO.setWorkGroupInfoCompositeDTO(workGroupInfoCompositeDTO);
			}
			ClientUserAuthorizationLimitsCriteria userAuthCriteria = request.getUserAuthorizationLimitsCriteria();
			if ( (userAuthCriteria != null) && (CollectionUtils.isNotEmpty(userAuthCriteria.getAuthorizationTypes())) ) {

				if (StringUtils.isEmpty(userName)) {
					userName = retrieveUsername(request.getUserId());
				}
				
				Collection<ClientUserAuthorizationLimitDTO> authorizationLimits = this
						.retrieveUserAuthorizationLimits(userName, userAuthCriteria);
				compositeDTO.setUserAuthorizationLimits(authorizationLimits);

			}
		} catch (Exception e) {
			String errorMessage;
			if ( StringUtils.isNotEmpty(request.getUserName()) ) {
				errorMessage = "Error retrieving user details for userName - " + request.getUserName();
				logger.error("Error retrieving user details for userName {}, exception - {} ", request.getUserName(), e);
			} else {
				errorMessage = "Error retrieving user details for user ID - " + request.getUserId();
				logger.error("Error retrieving user details for user ID {}, exception - {} ", request.getUserId(), e);
			}
			throw new IIPCoreSystemException(errorMessage);
		
		}
		return compositeDTO;
	}

	/**
	 * This is service method used to retrieve User by UserId.
	 * 
	 * @param userId
	 *            The userId.
	 * @return UserInfoDTO The UserInfoDTO.
	 */
	@Override
	public UserInfoDTO retrieveUser(Long userId) {
		logger.info("retrieveUser from user Id.");
		UserInfoDTO dto = getEnterpriseUserConfigurationService().retrieveUser(userId);
		return dto;
	}

	/**
	 * This is service method used to retrieve User by User Name.
	 * 
	 * @param userName
	 *            The user name.
	 * @return UserInfoDTO The UserInfoDTO.
	 */
	@Override
	public UserInfoDTO retrieveUser(String userName) {
		logger.info("retrieveUser from User Name.");
		UserLoginDTO user = getIIPSecurityService().findUserLoginByLoginName(userName);
		return getEnterpriseUserConfigurationService().retrieveUser(user.getUserId());
	}
	
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

	/**
	 * To retrieve phone details Info. for a User.
	 * 
	 * @param userId
	 *            The User Id.
	 * @return UserCommInformationCompositeDTO The
	 *         UserCommInformationCompositeDTO
	 */
	@Override
	public UserCommInformationCompositeDTO retrieveUserCommunication(Long userId) {
		logger.info("retrieveUserCommunication");
		UserCommInformationCompositeDTO dto = getEnterpriseUserConfigurationService().retrieveUserCommunication(userId);
		return dto;
	}

	/**
	 * To retrieve User Credentials details for a User.
	 * 
	 * @param userId
	 *            The User Id.
	 * @return UserCredentialInfoCompositeDTO The UserCredentialInfoCompositeDTO
	 */
	@Override
	public UserCredentialInfoCompositeDTO retrieveUserCredentialInfo(Long userId) {
		logger.info("retrieveUserCredentialInfo");
		UserCredentialInfoCompositeDTO dto = getEnterpriseUserConfigurationService().retrieveUserCredentialInfo(userId);
		return dto;

	}

	/**
	 * To retrieve User Authorization details for a User.
	 * 
	 * @param userId
	 *            The User Id.
	 * @return UserAuthorizationInfoCompositeDTO The
	 *         UserAuthorizationInfoCompositeDTO
	 * 
	 */
	@Override
	public UserAuthorizationInfoCompositeDTO retrieveUserAuthorizationInfo(Long userId) {
		logger.info("retrieveUserAuthorizationInfo");
		UserAuthorizationInfoCompositeDTO dto = getEnterpriseUserConfigurationService().retrieveUserAuthorizationInfo(
				userId);
		return dto;
	}

	/**
	 * To retrieve work group Info. for a User.
	 * 
	 * @param userId
	 *            The User Id.
	 * @return UserWorkGroupInfoCompositeDTO The UserWorkGroupInfoCompositeDTO
	 */
	@Override
	public UserWorkGroupInfoCompositeDTO retrieveUserWorkGroupInfo(Long userId) {
		logger.info("retrieveUserWorkGroupInfo");
		UserWorkGroupInfoCompositeDTO dto = getEnterpriseUserConfigurationService().retrieveUserWorkGroupInfo(userId);
		return dto;
	}

	/**
	 * To retrieve user Security group Info. for a User.
	 * 
	 * @param userId
	 *            The User Id.
	 * @return UserWorkGroupInfoCompositeDTO The UserWorkGroupInfoCompositeDTO
	 */
	@Override
	public UserSecurityGroupCompositeDTO retrieveUserSecurityGroups(Long userId) {
		logger.info("retrieveUserSecurityGroups");
		UserSecurityGroupCompositeDTO dto = getEnterpriseUserConfigurationService().retrieveUserSecurityGroups(userId);
		return dto;

	}

	/**
	 * Retrieve User Authorization Limits.
	 * 
	 * @param authorizationCriteria
	 *            the authorizationCriteria
	 * 
	 */
	@Override
	public Collection<ClientUserAuthorizationLimitDTO> retrieveUserAuthorizationLimits(String userName,
			ClientUserAuthorizationLimitsCriteria authorizationCriteria) {
		Collection<ClientUserAuthorizationLimitDTO> authorizationLimitDTOs = new ArrayList<ClientUserAuthorizationLimitDTO>();

		if (authorizationCriteria.getAuthorizationTypes() == null) {
			throw new IIPCoreSystemException(" Authorization type is required.");
		}

		for (String authType : authorizationCriteria.getAuthorizationTypes()) {
			
			UserAuthorizationLimitRequest userAuthorizationLimitRequest = new UserAuthorizationLimitRequest();
			userAuthorizationLimitRequest.setUserName(userName);
			userAuthorizationLimitRequest.setAuthorizationType(authType);
			
			userAuthorizationLimitRequest.setAgreementTypeCode(FinancialsConstants.AGREEMENT_TYPE_CLAIM);
			userAuthorizationLimitRequest.setClaimCoverageCode(authorizationCriteria.getCoverageCode());
			userAuthorizationLimitRequest.setCoverageCode(authorizationCriteria.getCoverageCode());
			userAuthorizationLimitRequest.setMonetoryCatCode(authorizationCriteria.getMonetaryCode());
			userAuthorizationLimitRequest.setJurisdictionId(authorizationCriteria.getJurisdictionId());
			userAuthorizationLimitRequest.setCompanyLobId(authorizationCriteria.getCompanyLobId());

			
			Collection<BigDecimal> authorizedLimitList = getIIPSecurityDAO().retrieveUserOverideLimit(userAuthorizationLimitRequest);
			
			ClientUserAuthorizationLimitDTO clientUserAuthorizationLimitDTO = new ClientUserAuthorizationLimitDTO();
			clientUserAuthorizationLimitDTO.setCoverageType(authorizationCriteria.getCoverageCode());
			clientUserAuthorizationLimitDTO.setMonetaryCategory(authorizationCriteria.getMonetaryCode());
			clientUserAuthorizationLimitDTO.setJurisdictionId(authorizationCriteria.getJurisdictionId());
			clientUserAuthorizationLimitDTO.setLob(authorizationCriteria.getCompanyLobId());
			
			if (authorizedLimitList.size() > 1) {
				throw new IIPCoreSystemException(
						"Authorization limit returned multiple values, please verify authorization limit configuration");
			}

			if (authorizedLimitList.size() == 1) {
				BigDecimal authorizationLimitFirstlevel = authorizedLimitList.iterator().next();
				if (authorizationLimitFirstlevel == null) {
					authorizedLimitList = getIIPSecurityDAO().retrieveUserAuthorityLimit(
							userAuthorizationLimitRequest);
				} else {
					if (authorizationLimitFirstlevel != null) {
						clientUserAuthorizationLimitDTO.setAuthorizationLimit(authorizationLimitFirstlevel);
						clientUserAuthorizationLimitDTO.setAuthorizationType(authType);
					}
				}
			}

			if (authorizedLimitList.size() > 1) {
				throw new IIPCoreSystemException(
						"Authorization limit returned multiple values, please verify authorization limit configuration");
			} else if (authorizedLimitList.size() == 1) {
				BigDecimal authorizationLimitdbValue = authorizedLimitList.iterator().next();
				if (authorizationLimitdbValue != null) {
					clientUserAuthorizationLimitDTO.setAuthorizationLimit(authorizationLimitdbValue);
					clientUserAuthorizationLimitDTO.setAuthorizationType(authType);
				}
			}
			authorizationLimitDTOs.add(clientUserAuthorizationLimitDTO);
		}
		return authorizationLimitDTOs;
	}
	
	/**
	 * Retrieve User Name for a given User ID.
	 * 
	 * @param userId
	 *            the user name associated with the User ID
	 * 
	 */
	private String retrieveUsername(long userId) {

		String foundUserName = StringUtils.EMPTY;
		Collection<String> userNames = getClientEnterpriseConfigDAO().retrieveUserLoginNameByUserId(userId);

		for (String userName : userNames) {
			foundUserName = userName;
			break;
		}
		return foundUserName;
	}

	/**
	 * @return the enterpriseUserConfigurationService
	 */
	public EnterpriseUserConfigurationService getEnterpriseUserConfigurationService() {
		EnterpriseUserConfigurationService enterpriseUserConfigurationService = MuleServiceFactory
				.getService(EnterpriseUserConfigurationService.class);
		return enterpriseUserConfigurationService;
	}

	/**
	 * @return the IIPSecurityService
	 */
	public IIPSecurityService getIIPSecurityService() {
		return MuleServiceFactory.getService(IIPSecurityService.class);
	}

	/**
	 * @return the iipSecurityDAO
	 */
	public IIPSecurityDAO getIIPSecurityDAO() {
		return iipSecurityDAO;
	}

	/**
	 * @param aiipSecurityDAO
	 *            the iipSecurityDAO to set
	 */
	@Inject(DaoRef = "entconfig.daointerface.securityDao")
	public void setIIPSecurityDAO(IIPSecurityDAO aiipSecurityDAO) {
		this.iipSecurityDAO = aiipSecurityDAO;
	}

}
