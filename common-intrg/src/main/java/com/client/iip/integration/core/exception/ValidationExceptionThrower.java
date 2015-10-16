package com.client.iip.integration.core.exception;

import java.util.Collection;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fiserv.isd.iip.core.messageresolver.MessageConstants;
import com.fiserv.isd.iip.core.validation.BeanPropertyBinding;
import com.fiserv.isd.iip.core.validation.IIPErrors;
import com.fiserv.isd.iip.core.validation.ValidationException;

/**
 * class that throws a validation exception.
 * 
 * @author brook
 * 
 */
public class ValidationExceptionThrower {

	private final Logger logger = LoggerFactory.getLogger(ValidationExceptionThrower.class);

	/**
	 * test method that throws a validation exception.
	 * @param in test.
	 * @throws ValidationException
	 *             throws a generic validation exception.
	 */
	public void throwException(String in) throws ValidationException {
		logger.debug("ValidationExceptionThrower.throwException argument: {} ", in);
		Collection<IIPErrors> errors = new LinkedList<IIPErrors>();
		ValidationException ve = new ValidationException(errors);

		BeanPropertyBinding error = new BeanPropertyBinding("test", "test");
		error.reject("effectiveDateRequired", MessageConstants.SEVERITY_ERROR);

		errors.add(error);
		logger.debug("ValidationExceptionThrower.throwing exception {}", ve);		
		throw ve;
	}
}
