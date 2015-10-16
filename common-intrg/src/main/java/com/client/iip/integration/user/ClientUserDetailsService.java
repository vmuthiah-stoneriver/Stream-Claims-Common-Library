package com.client.iip.integration.user;

import java.util.Collection;

import com.fiserv.isd.iip.core.service.ServiceEndpoint;
import com.stoneriver.iip.entconfig.sa.api.users.UserAuthorizationInfoCompositeDTO;
import com.stoneriver.iip.entconfig.sa.api.users.UserCommInformationCompositeDTO;
import com.stoneriver.iip.entconfig.sa.api.users.UserCredentialInfoCompositeDTO;
import com.stoneriver.iip.entconfig.sa.api.users.UserInfoDTO;
import com.stoneriver.iip.entconfig.sa.api.users.UserSearchResultDTO;
import com.stoneriver.iip.entconfig.sa.api.users.UserWorkGroupInfoCompositeDTO;
import com.stoneriver.iip.entconfig.sa.api.users.securitygroups.UserSecurityGroupCompositeDTO;

/**
 * Composite service used to retrieve user details.
 * 
 * @author saurabh.bhatnagar
 *
 */
@ServiceEndpoint("integration.endpoint.userDetailsInboundEndPoint")
public interface ClientUserDetailsService {
	
	/**
	 * This Service method used to retrieve user details.
	 * 
	 * @param request UserDetailRequestCriteria
	 * @return populated ClientUserDetailsCompositeDTO 
	 */
	ClientUserDetailsCompositeDTO retrieveUserDetails(ClientUserDetailRequestCriteria request);
	/**
	 * This is service method used to retrieve User by UserId.
	 * 
	 * @param userId The userId.
	 * @return UserInfoDTO The UserInfoDTO.
	 */
	UserInfoDTO retrieveUser(Long userId);
	/**
	 * This is service method used to retrieve User by User Name.
	 * 
	 * @param userName The user name.
	 * @return UserInfoDTO The UserInfoDTO.
	 */
	UserInfoDTO retrieveUser(String userName);
	
	/**
	 * This is service method used to retrieve all the Users in the System.
	 * It will return the User Details with only Primary Phone number.
	 * 
	 * @param request
	 *            import request
	 *            
	 * @return Collection of UserSearchResultDTO.
	 */
	public Collection<UserSearchResultDTO> retrieveUsers(String request);	
	
	/**
	 * To retrieve phone details Info. for a User.
	 * 
	 * @param userId The User Id.
	 * @return UserCommInformationCompositeDTO The UserCommInformationCompositeDTO
	 */	
	UserCommInformationCompositeDTO retrieveUserCommunication(Long userId);
	
	 /**
	 * To retrieve User Credentials details for a User.
	 * 
	 * @param userId The User Id.
	 * @return UserCredentialInfoCompositeDTO The UserCredentialInfoCompositeDTO
	 */	
	UserCredentialInfoCompositeDTO retrieveUserCredentialInfo(Long userId);
	
	   /**
	 * To retrieve User Authorization details for a User.
	 * 
	 * @param userId The User Id.
	 * @return UserAuthorizationInfoCompositeDTO The UserAuthorizationInfoCompositeDTO
	 * 
	 */	
	UserAuthorizationInfoCompositeDTO retrieveUserAuthorizationInfo(Long userId);
	
	/**
	 * To retrieve work group Info. for a User.
	 * 
	 * @param userId The User Id.
	 * @return UserWorkGroupInfoCompositeDTO The UserWorkGroupInfoCompositeDTO
	 */	
	UserWorkGroupInfoCompositeDTO retrieveUserWorkGroupInfo(Long userId);
	
	/**
	 * To retrieve user Security group Info. for a User.
	 * 
	 * @param userId The User Id.
	 * @return UserWorkGroupInfoCompositeDTO The UserWorkGroupInfoCompositeDTO
	 */	
	UserSecurityGroupCompositeDTO retrieveUserSecurityGroups(Long userId);
	
    /**
	 * This is service method to retrieve the User Authorization Limits for a user.
	 * 
	 * @param userName The userName associated with the request
	 * @param authorizationCriteria The criteria for retrieving authorization limits.
	 * 
	 * @return clientUserAuthorizationLimitDTO The list of ClientUserAuthorizationLimitDTO.
	 * 
	 */
	Collection<ClientUserAuthorizationLimitDTO> retrieveUserAuthorizationLimits(String userName, ClientUserAuthorizationLimitsCriteria authorizationCriteria);
	
	
}
