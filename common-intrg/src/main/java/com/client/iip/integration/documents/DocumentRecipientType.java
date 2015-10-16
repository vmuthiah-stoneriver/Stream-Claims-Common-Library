package com.client.iip.integration.documents;

import java.util.Date;

/**
 * Recipient Details.
 * @author saurabh.bhatnagar
 *
 */

public class DocumentRecipientType {
	
	
	private Long recipientRecordId;
	private Long recipientPartyId;
	private Date sentDate;
	private String sentToDevice;
	private String sentStatus;
	/**
	 * @return the recipientRecordId
	 */
	public Long getRecipientRecordId() {
		return recipientRecordId;
	}
	/**
	 * @param value the recipientRecordId to set
	 */
	public void setRecipientRecordId(Long value) {
		this.recipientRecordId = value;
	}
	/**
	 * @return the recipientPartyId
	 */
	public Long getRecipientPartyId() {
		return recipientPartyId;
	}
	/**
	 * @param value the recipientPartyId to set
	 */
	public void setRecipientPartyId(Long value) {
		this.recipientPartyId = value;
	}
	/**
	 * @return the sentDate
	 */
	public Date getSentDate() {
		return sentDate;
	}
	/**
	 * @param value the sentDate to set
	 */
	public void setSentDate(Date value) {
		this.sentDate = value;
	}
	/**
	 * @return the sentToDevice
	 */
	public String getSentToDevice() {
		return sentToDevice;
	}
	/**
	 * @param value the sentToDevice to set
	 */
	public void setSentToDevice(String value) {
		this.sentToDevice = value;
	}
	/**
	 * @return the sentStatus
	 */
	public String getSentStatus() {
		return sentStatus;
	}
	/**
	 * @param value the sentStatus to set
	 */
	public void setSentStatus(String value) {
		this.sentStatus = value;
	}
	
	

}
