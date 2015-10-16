package com.client.iip.integration.claim.imports;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fiserv.isd.iip.core.meta.annotation.ServiceMethod;
import com.fiserv.isd.iip.core.thread.IIPDataContext;
import com.fiserv.isd.iip.core.thread.IIPThreadContextFactory;
import com.stoneriver.iip.claims.imports.recovery.ClaimRecovery;
import com.stoneriver.iip.claims.recovery.AllocatedRecoveryDTO;
import com.stoneriver.iip.claims.recovery.ClaimRecoveryDTO;
import com.stoneriver.iip.claims.recovery.ClaimReserveRecoveryServiceImpl;
import com.stoneriver.iip.claims.recovery.RecoveryListingDTO;
import com.stoneriver.iip.claims.reserve.ClaimReserveCommonDTO;

public class ClientClaimReserveRecoveryServiceImpl extends
		ClaimReserveRecoveryServiceImpl {
	
	private Logger logger = LoggerFactory.getLogger(ClientClaimReserveRecoveryServiceImpl.class);
	
	public ClientClaimReserveRecoveryServiceImpl(){
		super();
		logger.info("ClientClaimReserveRecoveryServiceImpl instantiated");
	}
	
	/**
	 * The method is used for creating and applying recovery transaction for a claim reserve from externalSystem.
	 * Recovery transaction can be applied through existing cash receipt or new remittance record.
	 * @param claimRecovery of type ClaimRecovery having all information about recoveryListing,existing CashReceipt/new Remittance record
	 * 
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	@ServiceMethod
	public void createAndApplyRecoveriesFromExternalSystem(ClaimRecovery claimRecovery){
		logger.info("Create and Apply Claim PaymentRecoveries From External System with ClaimRecovery DTO Structure : {}", claimRecovery.toString());
		ClaimRecoveryDTO claimRecoveryDTO = getImportRecoveryPojo().createClaimRecoveryDTO(claimRecovery);
		for (AllocatedRecoveryDTO allocatedRecoveryDTO : claimRecoveryDTO.getAllocatedRecoveries()) {
			//Fix Impact incurred - 09/15/2014 @GR - Set Monetary Category and Reserve Type Code.
			Collection<ClaimReserveCommonDTO> colls = getClaimReserveDAO().retrieveReserveByReserveId(allocatedRecoveryDTO.getReserveId());
			allocatedRecoveryDTO.setMonetoryCatCode(colls.iterator().next().getMonetoryCatCode());
			allocatedRecoveryDTO.setReserveTypeCode(colls.iterator().next().getReserveTypeCode());
			allocatedRecoveryDTO.setClaimParticpantId(colls.iterator().next().getClaimParticpantId());
			allocatedRecoveryDTO.setUnitId(colls.iterator().next().getClaimUnitId());
			//set close recovery flag
			IIPDataContext context = IIPThreadContextFactory.getIIPThreadContext().getDataContext();
			allocatedRecoveryDTO.setCloseReserveIndicator(((Boolean)context.getAppData("closeRecoveryReserve")).booleanValue());
			for (RecoveryListingDTO recoveryListingDTO : allocatedRecoveryDTO.getRecoveryListingDTO()) {
				getClaimReservePaymentHelper().setImpactIncurredIndicatorValuesForRecovery(allocatedRecoveryDTO,recoveryListingDTO);
			}	
		}
		claimRecoveryDTO.getFinancialClaimReceiptDTO().setRecoveryDescription(claimRecoveryDTO.getDescription());
		getClaimReserveRecoveryService().createAndApplyRecoveries(claimRecoveryDTO);
	}	
	

}
