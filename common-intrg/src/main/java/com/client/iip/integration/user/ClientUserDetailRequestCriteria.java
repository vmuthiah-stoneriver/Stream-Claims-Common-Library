package com.client.iip.integration.user;

import java.io.Serializable;

/**
 * This is the request DTO for User Details.
 * 
 * @author saurabh.bhatnagar
 * 
 */
public class ClientUserDetailRequestCriteria implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long userId;
	private String userName;
	private boolean returnUserCommunication;
	private boolean returnUserCredentialInfo;
	private boolean returnUserAuthorizationInfo;
	private boolean returnUserWorkGroupInfo;
	private boolean returnUserSecurityGroups;
	private ClientUserAuthorizationLimitsCriteria userAuthorizationLimitsCriteria;

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
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param value
	 *            the userName to set
	 */
	public void setUserName(String value) {
		this.userName = value;
	}

	/**
	 * @return the returnUserCommunication
	 */
	public boolean isReturnUserCommunication() {
		return returnUserCommunication;
	}

	/**
	 * @param value
	 *            the returnUserCommunication to set
	 */
	public void setReturnUserCommunication(boolean value) {
		this.returnUserCommunication = value;
	}

	/**
	 * @return the returnUserCredentialInfo
	 */
	public boolean isReturnUserCredentialInfo() {
		return returnUserCredentialInfo;
	}

	/**
	 * @param value
	 *            the returnUserCredentialInfo to set
	 */
	public void setReturnUserCredentialInfo(boolean value) {
		this.returnUserCredentialInfo = value;
	}

	/**
	 * @return the returnUserAuthorizationInfo
	 */
	public boolean isReturnUserAuthorizationInfo() {
		return returnUserAuthorizationInfo;
	}

	/**
	 * @param value
	 *            the returnUserAuthorizationInfo to set
	 */
	public void setReturnUserAuthorizationInfo(boolean value) {
		this.returnUserAuthorizationInfo = value;
	}

	/**
	 * @return the returnUserWorkGroupInfo
	 */
	public boolean isReturnUserWorkGroupInfo() {
		return returnUserWorkGroupInfo;
	}

	/**
	 * @param value
	 *            the returnUserWorkGroupInfo to set
	 */
	public void setReturnUserWorkGroupInfo(boolean value) {
		this.returnUserWorkGroupInfo = value;
	}

	/**
	 * @return the returnUserSecurityGroups
	 */
	public boolean isReturnUserSecurityGroups() {
		return returnUserSecurityGroups;
	}

	/**
	 * @param value
	 *            the returnUserSecurityGroups to set
	 */
	public void setReturnUserSecurityGroups(boolean value) {
		this.returnUserSecurityGroups = value;
	}

	/**
	 * @return the authorizationCriteria
	 */
	public ClientUserAuthorizationLimitsCriteria getUserAuthorizationLimitsCriteria() {
		return userAuthorizationLimitsCriteria;
	}

	/**
	 * @param authorizationCriteria
	 *            the authorizationCriteria to set
	 */
	public void setUserAuthorizationLimitsCriteria(ClientUserAuthorizationLimitsCriteria userAuthorizationLimitsCriteria) {
		this.userAuthorizationLimitsCriteria = userAuthorizationLimitsCriteria;
	}
}
