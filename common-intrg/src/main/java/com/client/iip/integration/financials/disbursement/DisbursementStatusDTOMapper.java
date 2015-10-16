package com.client.iip.integration.financials.disbursement;

import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementBO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementStatusBO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementStatusDTO;
import com.fiserv.isd.iip.core.dto.DTOSynchronizeLifecycle;
import com.fiserv.isd.iip.core.dto.ReflectionBasedDTOMapper;

/**
 * A custom DTOMapper for mapping DisbursementStatus DTO. This is needed because
 * DisbursementStatus cannot be inserted without a reference to Disbursement. 
 *
 */
public class DisbursementStatusDTOMapper extends ReflectionBasedDTOMapper 
	implements DTOSynchronizeLifecycle {
	
	private DisbursementBO disbursement;

	/**
	 * Constructor.
	 * 
	 * @param disbParam the DisbursementBO
	 * @param DisbursementStatusDAOParam the dao
	 */
	public DisbursementStatusDTOMapper(DisbursementBO disbParam)	{
		this.disbursement = disbParam;
	}
	
	/**
	 * Pre Synchronize for AddressDTO. Setting the association parent.
	 * 
	 * @param dto the dto
	 * @param bo the bo
	 */
	public void preSynchronize(DisbursementStatusDTO dto, DisbursementStatusBO bo)	{
		bo.setDisbursement(disbursement);
	}
	
}
