package com.client.iip.integration.party;

import com.client.iip.integration.core.converter.ProfileAttributeConverter;
import com.fiserv.isd.iip.bc.party.search.PartySearchResult;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamOmitField;


/**
 * @author gradhak
 *
 */
public class ClientPartySearchResult extends PartySearchResult {
	
	/**
	 * Serialization ID.
	 */
	private static final long serialVersionUID = -602136441275822948L;
	
	private String vendorTypeCode;
	
	private String vendorCategoryCode;
	
	private String vendorStatus;
		
	private String lienRestrictionCode;
	
	private String businessPhone;
	
	private String homePhone;
	
	private String cellPhone;
	
	private String personalEmail;
	
	private String businessEmail;
	
	private String primaryDBAName;

	private String secondaryDBAName;

	@XStreamOmitField
	private String profileName;
	
	@XStreamOmitField
	private String profileValue;
	
	@XStreamConverter(ProfileAttributeConverter.class)
	private ClientProfileDTO profile;
	
	/**
	 * New attributes added for Party Block Enhancement
	 */
	private boolean blockExists;
	
	private String blockType;
	
	public void setBlockExists(boolean _blockFlag){
		blockExists = _blockFlag;
	}
	
	public boolean isBlockExists(){
		return blockExists;
	}
	
	
	public void setBlockType(String _blockType){
		 blockType = _blockType;
	}
	
	public String getBlockType(){
		return blockType;
	}
	
	
	
	/**
	 * @return the profile
	 */
	public ClientProfileDTO getProfile() {
		return profile;
	}

	/**
	 * @param profile the profile to set
	 */
	public void setProfile(ClientProfileDTO _profile) {
		this.profile = _profile;
	}

	/**
	 * @return the primaryDBAName
	 */
	public String getPrimaryDBAName() {
		return this.primaryDBAName;
	}

	/**
	 * @param primaryDBAName the primaryDBAName to set
	 */
	public void setPrimaryDBAName(String _primaryDBAName) {
		this.primaryDBAName = _primaryDBAName;
	}

	/**
	 * @return the secondaryDBAName
	 */
	public String getSecondaryDBAName() {
		return this.secondaryDBAName;
	}

	/**
	 * @param secondaryDBAName the secondaryDBAName to set
	 */
	public void setSecondaryDBAName(String _secondaryDBAName) {
		this.secondaryDBAName = _secondaryDBAName;
	}


	
	/**
	 * @return the businessPhone
	 */
	public String getBusinessPhone() {
		return this.businessPhone;
	}

	/**
	 * @param businessPhone the _businessPhone to set
	 */
	public void setBusinessPhone(String _businessPhone) {
		this.businessPhone = _businessPhone;
	}

	/**
	 * @return the homePhone
	 */
	public String getHomePhone() {
		return this.homePhone;
	}

	/**
	 * @param homePhone the _homePhone to set
	 */
	public void setHomePhone(String _homePhone) {
		this.homePhone = _homePhone;
	}

	/**
	 * @return the cellPhone
	 */
	public String getCellPhone() {
		return this.cellPhone;
	}

	/**
	 * @param cellPhone the _cellPhone to set
	 */
	public void setCellPhone(String _cellPhone) {
		this.cellPhone = _cellPhone;
	}

	/**
	 * @return the personalEmail
	 */
	public String getPersonalEmail() {
		return this.personalEmail;
	}

	/**
	 * @param personalEmail the _personalEmail to set
	 */
	public void setPersonalEmail(String _personalEmail) {
		this.personalEmail = _personalEmail;
	}

	/**
	 * @return the businessEmail
	 */
	public String getBusinessEmail() {
		return this.businessEmail;
	}

	/**
	 * @param businessEmail the _businessEmail to set
	 */
	public void setBusinessEmail(String _businessEmail) {
		this.businessEmail = _businessEmail;
	}

	public String getVendorTypeCode(){
		return this.vendorTypeCode;
	}
	
	public void setVendorTypeCode(String _vendorType){
		this.vendorTypeCode = _vendorType;
	}
	
	public String getVendorStatus(){
		return this.vendorStatus;
	}
	
	public void setVendorStatus(String _vendorStatus){
		this.vendorStatus = _vendorStatus;
	}
	
	public String getLienRestrictionCode(){
		return this.lienRestrictionCode;
	}
	
	public void setLienRestrictionCode(String _lienRestrictionCode){
		this.lienRestrictionCode = _lienRestrictionCode;
	}

	/**
	 * @return the vendorCategory
	 */
	public String getVendorCategoryCode() {
		return this.vendorCategoryCode;
	}

	/**
	 * @param vendorCategory the vendorCategory to set
	 */
	public void setVendorCategoryCode(String _vendorCategory) {
		this.vendorCategoryCode = _vendorCategory;
	}

	/**
	 * @return the profileName
	 */
	public String getProfileName() {
		return this.profileName;
	}

	/**
	 * @param profileName the profileName to set
	 */
	public void setProfileName(String _profileName) {
		this.profileName = _profileName;
	}

	/**
	 * @return the profileValue
	 */
	public String getProfileValue() {
		return this.profileValue;
	}

	/**
	 * @param profileValue the profileValue to set
	 */
	public void setProfileValue(String _profileValue) {
		this.profileValue = _profileValue;
	}

	
	

}
