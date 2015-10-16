package com.client.iip.integration.claim;

import java.util.Collection;

import com.fiserv.isd.iip.core.data.annotation.Query;
import com.fiserv.isd.iip.core.meta.annotation.DaoInterface;

@DaoInterface(id = "entconfig.daointerface.controlNumberDao", daoInterfaceFactory="clientDaoInterfaceFactory")
public interface ControlNumberDAO {

	@Query(accessId = "client.claims.retrieveControlNumberByCompanyLOBAndJurisdiction")
	Collection<ControlNumberBO> retrieveControlNumberByCompanyLOBAndJurisdiction(ControlNumberSearchCriteria criteria);

	// This API has been added for Get Configuration requirement to fetch all prefixes aka control number
	@Query(accessId = "client.claims.retrieveAllPrefixes")
	Collection<ControlNumberBO> retrieveAllPrefixes();
		
}
