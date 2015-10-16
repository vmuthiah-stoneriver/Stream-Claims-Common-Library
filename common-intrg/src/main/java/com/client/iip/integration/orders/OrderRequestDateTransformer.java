package com.client.iip.integration.orders;


import java.util.Collection;
import java.util.Date;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.stoneriver.iip.claims.dao.ClaimUnitDAO;
import com.stoneriver.iip.orders.api.OrderRequestStatusDTO;
import com.stoneriver.iip.orders.api.OrdersDTO;

/**
 * 
 * Convert sql date time stamp to Date
 * 
 * @author Saurabh.Bhatnagar
 * 
 */
public class OrderRequestDateTransformer extends AbstractMessageTransformer {
	
	private ClaimUnitDAO claimUnitDAO;
	
	/**
	 * @return the claimUnitDAO
	 */
	public ClaimUnitDAO getClaimUnitDAO() {
		return claimUnitDAO;
	}

	/**
	 * @param value
	 *            the claimUnitDAO to set
	 */
	@Inject(DaoInterface = "claimsall.daointerface.claimsAllUnitDao")
	public void setClaimUnitDAO(ClaimUnitDAO value) {
		this.claimUnitDAO = value;
	}	


	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding)
			throws TransformerException {
		Object payload = message.getPayload();
		if (payload instanceof OrdersDTO){
			OrdersDTO ordersDTO = (OrdersDTO) payload;
			//Get the Claim Id First
			Long claimId = null;
			if(ordersDTO.getAgreementTypeCodeDerived() != null && ordersDTO.getAgreementTypeCodeDerived().equals("clm")){
				claimId = ordersDTO.getAgreementIdDerived();
			}			
			
			// 01/05/2014 @GR - CompanyID is null in come cases. Fetch from DB based on ClaimID
			if(ordersDTO.getCompanyID() == null) {
				Collection<Long> companyIdColl = null;
				if(claimId != null){
					companyIdColl = getClaimUnitDAO().retrieveCompanyIdByClaimId(claimId);
				}
				if(companyIdColl != null && !companyIdColl.isEmpty()){
					Long cmpyId = companyIdColl.iterator().next();
					ordersDTO.setCompanyID(cmpyId);
				}				
			}
			
			OrderRequestStatusDTO orderRequestStatusDTO = ordersDTO.getOrderRequestStatus().getCurrent();
			Date effectiveDate = orderRequestStatusDTO.getEffectiveDateTime();
			Date newEffectiveDate = new Date(effectiveDate.getTime()); 
			orderRequestStatusDTO.setEffectiveDateTime(newEffectiveDate);
			
			if(ordersDTO.getOrderRequestStatus()!=null && ordersDTO.getOrderRequestStatus().getExpired()!=null 
					&& !ordersDTO.getOrderRequestStatus().getExpired().isEmpty()){
				Collection<OrderRequestStatusDTO> orderRequestStatusDTOs = ordersDTO.getOrderRequestStatus().getExpired();
				for(OrderRequestStatusDTO requestStatusDTO :  orderRequestStatusDTOs){
					Date expireEffectiveDate = requestStatusDTO.getEffectiveDateTime();
					Date newExpireEffectiveDate = new Date(expireEffectiveDate.getTime());  
					requestStatusDTO.setEffectiveDateTime(newExpireEffectiveDate);
					
					Date expireEndDate = requestStatusDTO.getEndDateTime();
					Date newExpireEndDate = new Date(expireEndDate.getTime()); 
					requestStatusDTO.setEndDateTime(newExpireEndDate);
				}
				}
		}
		return payload;
	}

}
