package com.client.iip.integration.core.exception;

import java.io.Serializable;
import java.util.Collection;

/**
 * Holds the formatted error objects when an exception occurs.
 * @author brook
 *
 */
public class ClientMessageResponse implements Serializable {


	private static final long serialVersionUID = -9085299181809741344L;
	
	private Collection<IIPErrorResponse> formattedErrors;
	
    /**
     * Set list of Errors objects.
     * 
     * @param fe the formatted errors
     */
    public void setFormattedErrors(
    		Collection<IIPErrorResponse> fe) {
        this.formattedErrors = fe;
    }

    /**
     * Get list of Errors objects.
     * 
     * @return  Collection of IIPObjectErrors
     */
    public Collection<IIPErrorResponse> getFormattedErrors() {
        return formattedErrors;
    }

}
