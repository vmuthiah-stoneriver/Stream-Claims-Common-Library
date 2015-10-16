package com.client.iip.integration.claim;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.apache.commons.beanutils.BeanUtils;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

import com.fiserv.isd.iip.core.dto.DataTransferObject;
import com.fiserv.isd.iip.core.dto.search.SearchResultsDTO;
import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.stoneriver.iip.claims.cws.CALClaimBO;
import com.stoneriver.iip.claims.dao.ClaimNoticeOfLossDAO;
import com.stoneriver.iip.claims.search.ClaimSearchResultDTO;

public class ClientClaimSearchResultTransformer extends AbstractMessageTransformer {
	
	private ClaimNoticeOfLossDAO claimNoticeOfLossDAO;
	
	/**
	 * @param value
	 *            the claimNoticeOfLossDAO to set
	 */
	@Inject(DaoInterface = "claimsall.daointerface.ClaimNoticeOfLossDAO")
	public void setClaimNoticeOfLossDAO(ClaimNoticeOfLossDAO value) {
		this.claimNoticeOfLossDAO = value;
	}

	/**
	 * @return the claimNoticeOfLossDAO
	 */
	public ClaimNoticeOfLossDAO getClaimNoticeOfLossDAO() {
		return claimNoticeOfLossDAO;
	}	

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding)
			throws TransformerException {
		Object payload = null;
		try {
			payload = message.getPayload();
			if (!payload.toString().startsWith("{NullPayload}")) {			
			SearchResultsDTO results = (SearchResultsDTO) payload;
			SearchResultsDTO returnResults = new SearchResultsDTO();
			returnResults
					.setSearchResults(new ArrayList<ClientClaimSearchResultDTO>());
			for (DataTransferObject result : results.getSearchResults()) {
				if (result instanceof ClaimSearchResultDTO) {
					ClaimSearchResultDTO searchResultDTO = (ClaimSearchResultDTO) result;
					ClientClaimSearchResultDTO returnResultDTO = new ClientClaimSearchResultDTO();					
					// copy all of the fields to the new object
					BeanUtils.copyProperties(returnResultDTO, searchResultDTO);
					// this is just a placeholder. This field will have to be
					// populated by the
					// base application at some point and this transformer will
					// go away.
					CALClaimBO claimBO = getClaimNoticeOfLossDAO().retrieveClaim(searchResultDTO.getClaimId());
					returnResultDTO.setDateClaimReported(claimBO.getCreatedDateTime());
					returnResults.getSearchResults().add(returnResultDTO);
					payload = returnResults;
				} else {
					
					//not sure if there's a valid exception message to use here
					throw new TransformerException(this, new Exception(
							"UnExpected Search Result Object"));
				}

			  }
			}
			return payload;
		} catch (IllegalAccessException e) {
			throw new TransformerException(this, e);
		} catch (InvocationTargetException e) {
			throw new TransformerException(this, e);
		}
	}
}
