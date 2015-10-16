package com.client.iip.integration.user;

import java.util.Collection;

import com.stoneriver.iip.entconfig.sa.api.users.UserAuthorizationInfoCompositeDTO;
import com.stoneriver.iip.entconfig.sa.api.users.UserCommInformationCompositeDTO;
import com.stoneriver.iip.entconfig.sa.api.users.UserCredentialInfoCompositeDTO;
import com.stoneriver.iip.entconfig.sa.api.users.UserInfoDTO;
import com.stoneriver.iip.entconfig.sa.api.users.UserWorkGroupInfoCompositeDTO;
import com.stoneriver.iip.entconfig.sa.api.users.securitygroups.UserSecurityGroupCompositeDTO;

/**
 * 
 * @author saurabh.bhatnagar
 * 
 */
public class ClientUserDetailsCompositeDTO {

	private Long userId;
	private UserInfoDTO userInfoDTO;
	private UserCommInformationCompositeDTO userCommInformationCompositeDTO;
	private UserCredentialInfoCompositeDTO userCredentialInfoCompositeDTO;
	private UserAuthorizationInfoCompositeDTO userAuthorizationInfoCompositeDTO;
	private UserWorkGroupInfoCompositeDTO workGroupInfoCompositeDTO;
	private UserSecurityGroupCompositeDTO userSecurityGroupCompositeDTO;
	private Collection<ClientUserAuthorizationLimitDTO> userAuthorizationLimits;

	/**
	 * @return the userInfoDTO
	 */
	public UserInfoDTO getUserInfoDTO() {
		return userInfoDTO;
	}

	/**
	 * @param value
	 *            the userInfoDTO to set
	 */
	public void setUserInfoDTO(UserInfoDTO value) {
		this.userInfoDTO = value;
	}

	/**
	 * @return the userCommInformationCompositeDTO
	 */
	public UserCommInformationCompositeDTO getUserCommInformationCompositeDTO() {
		return userCommInformationCompositeDTO;
	}

	/**
	 * @param value
	 *            the userCommInformationCompositeDTO to set
	 */
	public void setUserCommInformationCompositeDTO(UserCommInformationCompositeDTO value) {
		this.userCommInformationCompositeDTO = value;
	}

	/**
	 * @return the userCredentialInfoCompositeDTO
	 */
	public UserCredentialInfoCompositeDTO getUserCredentialInfoCompositeDTO() {
		return userCredentialInfoCompositeDTO;
	}

	/**
	 * @param value
	 *            the userCredentialInfoCompositeDTO to set
	 */
	public void setUserCredentialInfoCompositeDTO(UserCredentialInfoCompositeDTO value) {
		this.userCredentialInfoCompositeDTO = value;
	}

	/**
	 * @return the userAuthorizationInfoCompositeDTO
	 */
	public UserAuthorizationInfoCompositeDTO getUserAuthorizationInfoCompositeDTO() {
		return userAuthorizationInfoCompositeDTO;
	}

	/**
	 * @param value
	 *            the userAuthorizationInfoCompositeDTO to set
	 */
	public void setUserAuthorizationInfoCompositeDTO(UserAuthorizationInfoCompositeDTO value) {
		this.userAuthorizationInfoCompositeDTO = value;
	}

	/**
	 * @return the workGroupInfoCompositeDTO
	 */
	public UserWorkGroupInfoCompositeDTO getWorkGroupInfoCompositeDTO() {
		return workGroupInfoCompositeDTO;
	}

	/**
	 * @param value
	 *            the workGroupInfoCompositeDTO to set
	 */
	public void setWorkGroupInfoCompositeDTO(UserWorkGroupInfoCompositeDTO value) {
		this.workGroupInfoCompositeDTO = value;
	}

	/**
	 * @return the userSecurityGroupCompositeDTO
	 */
	public UserSecurityGroupCompositeDTO getUserSecurityGroupCompositeDTO() {
		return userSecurityGroupCompositeDTO;
	}

	/**
	 * @param value
	 *            the userSecurityGroupCompositeDTO to set
	 */
	public void setUserSecurityGroupCompositeDTO(UserSecurityGroupCompositeDTO value) {
		this.userSecurityGroupCompositeDTO = value;
	}

	/**
	 * @return the userId
	 */
	public Long getUserId() {
		return userId;
	}

	/**
	 * @param value
	 *            the userId to set
	 */
	public void setUserId(Long value) {
		this.userId = value;
	}

	/**
	 * @return the authorizationLimits
	 */
	Collection<ClientUserAuthorizationLimitDTO> getUserAuthorizationLimits() {
		return userAuthorizationLimits;
	}

	/**
	 * @param authorizationLimits
	 *            the authorizationLimits to set
	 */
	public void setUserAuthorizationLimits(Collection<ClientUserAuthorizationLimitDTO> userAuthorizationLimits) {
		this.userAuthorizationLimits = userAuthorizationLimits;
	}
}
