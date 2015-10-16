package com.client.iip.integration.financials.disbursement.helper;

import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementConstants;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementStatusDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.importPayment.DisbursementImportDetailDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.importPayment.helper.AbstractImportPojo;
import com.fiserv.isd.iip.core.date.DateUtility;
import com.fiserv.isd.iip.core.meta.annotation.Pojo;

/**
 * Update disbursement information for record type 'I'. 
 *
 */
@Pojo(id="client.financials.disbursement.pojo.issuedPaymentImportPojo")
public class ClientIssuedPaymentImportPojo implements AbstractImportPojo {
	
	/**
	 * This method will update disbursement information related to status and disbursement number.
	 * 
	 * @param disbursementDTO disbursement
	 * @param detailDTO DisbursementImportDetailDTO
	 */
	public void updateDisbursementInformation(DisbursementDTO disbursementDTO, 
			DisbursementImportDetailDTO detailDTO) {
		DisbursementStatusDTO currentStatus = new DisbursementStatusDTO();
		currentStatus.setDisbursementState(DisbursementConstants.DISB_STATE_TYPE_CODE_OPEN);
		currentStatus.setDisbursementStatusType(DisbursementConstants.DISB_STATUS_TYPE_CODE_ISSUED_PAYMENT);
		/* TD 33060 -If the date is null then use the System Date */
		currentStatus.setEffectiveDateTime(detailDTO.getIssuedDate()==null?DateUtility.getSystemDateTime():detailDTO.getIssuedDate());
        disbursementDTO.setDisbursementNumber(detailDTO.getDisbursementNumber());
		
		// update status in disbursement dto
		disbursementDTO.getHistoryStatuses().add(disbursementDTO.getCurrentStatus());
		disbursementDTO.setCurrentStatus(currentStatus);
	}

}

