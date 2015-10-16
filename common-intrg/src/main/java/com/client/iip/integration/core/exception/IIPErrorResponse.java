package com.client.iip.integration.core.exception;

import java.util.ArrayList;
import java.util.Collection;

import com.fiserv.isd.iip.core.framework.client.IIPResponse;
import com.fiserv.isd.iip.core.validation.IIPObjectError;


/**
 * Simple representation of the IIPObjectError class.
 * @author brook
 *
 */
public class IIPErrorResponse implements IIPResponse {

	private static final long serialVersionUID = 3530933531348794751L;

	private Object responsePayload;
	private Collection<IIPObjectError> formattedErrors 
		= new ArrayList<IIPObjectError>();
	private Collection<String> confirmationCodes;

	/**
	 * Returns the response payload.
	 * 
	 * @return Object the response payload
	 */
	public Object getResponsePayload() {
		return responsePayload;
	}
	
	/**
	 * sets the response payload.
	 * 
	 * @param responsePayloadParam the response payload
	 */
	public void setResponsePayload(Object responsePayloadParam) {
		this.responsePayload = responsePayloadParam;
	}
	
	/**
	 * Returns the formatted error message objects.
	 * 
	 * @return Collection the message exception objects
	 */
	public Collection<IIPObjectError> getFormattedErrors() {
		return formattedErrors;
	}
	
	/**
	 * Sets the formatted error message objects.
	 * 
	 * @param formattedErrorsParam the formatted error objects
	 */
	public void setFormattedErrors(Collection<IIPObjectError> formattedErrorsParam) {
		this.formattedErrors = formattedErrorsParam;
	}

	/**
	 * Get the confirmation codes.
	 * @return a collection of the confirmation codes.
	 */
	public Collection<String> getConfirmedErrorCodes() {
		return confirmationCodes;
	}

	/**
	 * Set the confirmation codes.
	 * @param confirmedErrorCodes the confirmation codes to set.
	 */
	public void setConfirmedErrorCodes(Collection<String> confirmedErrorCodes) {
		this.confirmationCodes = confirmedErrorCodes;
	}
	
}
