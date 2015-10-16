package com.client.iip.integration.sa;

import java.util.ArrayList;
import java.util.Collection;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

import com.client.iip.integration.core.exception.IIPErrorResponse;
import com.fiserv.isd.iip.core.messageresolver.MessageConstants;
import com.fiserv.isd.iip.core.validation.IIPObjectError;
import com.fiserv.isd.iip.util.codelookup.api.CodeLookupBO;
import com.stoneriver.iip.entconfig.sa.api.SysAdminCompanyDTO;

/**
 * 
 * Convert Code LookUp BO to Collection of DTO.
 * 
 * @author Saurabh.Bhatnagar
 * 
 */

public class CodeLookUpBOToDTOTransformer extends AbstractMessageTransformer {

	/**
	 * Convert a Collection of CodeLookupBO into Collection of
	 * SysAdminCompanyDTO.
	 * 
	 * @param src
	 *            the collection of CodeLookupBO
	 * @param encoding
	 *            unused
	 * @return the collection of SysAdminCompanyDTO
	 * @throws TransformerException
	 *             on error
	 */
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding)
			throws TransformerException {

		Collection<SysAdminCompanyDTO> companies = new ArrayList<SysAdminCompanyDTO>();
		Object payload = message.getPayload();
		if (payload instanceof Collection<?>) {
			@SuppressWarnings("unchecked")
			Collection<CodeLookupBO> companyIds = (Collection<CodeLookupBO>) payload;
			for (CodeLookupBO codeLookupBO : companyIds) {
				String companyId = codeLookupBO.getCode().toString();
				SysAdminCompanyDTO company = new SysAdminCompanyDTO();
				company.setCompanyName(codeLookupBO.getValue());
				company.setCompanyNumber(companyId);
				companies.add(company);
			}
		} else {
			Collection<IIPErrorResponse> errResponse = new ArrayList<IIPErrorResponse>();
			IIPErrorResponse thisError = new IIPErrorResponse();
			Collection<IIPObjectError> ioes = new ArrayList<IIPObjectError>();
			String[] codes = new String[] {"ObjectError"};
			IIPObjectError ioe = new IIPObjectError("CodeLookUpBOToDTOTransformer", "transformMessage",
					null, codes, null, MessageConstants.SEVERITY_ERROR);
			ioe.setFormattedMessage("Payload is not of type Collection<CodeLookupBO>");
			ioes.add(ioe);
			thisError.setFormattedErrors(ioes);
			errResponse.add(thisError);
			message.setPayload(errResponse);
			message.setExceptionPayload(null);			
			return message;
		}
		return companies;
	}

}
