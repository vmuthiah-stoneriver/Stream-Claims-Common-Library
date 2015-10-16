package com.client.iip.integration.core.util;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fiserv.isd.iip.core.meta.annotation.ServiceMethod;
import com.fiserv.isd.iip.core.thread.IIPDataContext;
import com.fiserv.isd.iip.core.thread.IIPThreadContextFactory;
import com.stoneriver.iip.entconfig.date.BusinessDateType;
import com.stoneriver.iip.entconfig.date.DateServiceImpl;

public class ClientDateServiceImpl extends DateServiceImpl {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ClientDateServiceImpl.class);
	
	public ClientDateServiceImpl(){
		super();
		LOGGER.info("ClientDateServiceImpl instantiated");
	}
	
	
	/**
	 * Get the current business date.
	 * 
	 * @param companyId
	 *            the company id
	 * @param dateType
	 *            the type of business date
	 * @return the current business date Add validation here. Retrieve business
	 *         date and compare
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	@ServiceMethod
	public Date getBusinessDate(Long companyId, BusinessDateType dateType) {

		//Check if Date is available in the Thread Context.
		IIPDataContext context = IIPThreadContextFactory.getIIPThreadContext().getDataContext();
		Date transDate = context==null?null:(Date) context.getAppData("transDate");
		Date acctDate =  context==null?null:(Date) context.getAppData("acctDate");
 		LOGGER.info("Ovrd Transaction date : " + transDate);
 		LOGGER.info("Ovrd Accounting date : " + acctDate);
 		
 		if(dateType == BusinessDateType.BUSINESS && transDate != null){
 			return transDate;
 		}else if(dateType == BusinessDateType.ACCOUNTING && acctDate != null){
 			return acctDate;
 		}else{
 			return super.getBusinessDate(companyId, dateType);
 		}
	}
	

}
