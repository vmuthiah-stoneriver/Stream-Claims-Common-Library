package com.client.iip.integration.policy;

import java.util.Collection;

import com.client.iip.integration.party.ClientOrganizationDTO;
import com.client.iip.integration.party.ClientPersonDTO;
import com.stoneriver.iip.policy.mediation.policyimport.PolicyImportDTO;

/**
 * Wrapper class to bundle up party data along with the Policy data when serialized to xml.
 * 
 * @author sudharsan.sriram
 *
 */
public class ClientPolicyImportWrapperDTO {

	private PolicyImportDTO policy;
	private Collection<ClientPersonDTO> persons;
	private Collection<ClientOrganizationDTO> organizations;
	private boolean partiesLoaded = false;
	/**
	 * getter.
	 * @return the policy
	 */
	public PolicyImportDTO getPolicy() {
		return policy;
	}
	/**
	 * setter.
	 * @param policyVal the policy to set
	 */
	public void setPolicy(PolicyImportDTO policyVal) {
		this.policy = policyVal;
	}
	/**
	 * @param personsVal the persons to set
	 */
	public void setPersons(Collection<ClientPersonDTO> personsVal) {
		this.persons = personsVal;
	}
	/**
	 * @return the persons
	 */
	public Collection<ClientPersonDTO> getPersons() {
		return persons;
	}
	/**
	 * @param organizationsVal the organizations to set
	 */
	public void setOrganizations(Collection<ClientOrganizationDTO> organizationsVal) {
		this.organizations = organizationsVal;
	}
	/**
	 * @return the organizations
	 */
	public Collection<ClientOrganizationDTO> getOrganizations() {
		return organizations;
	}
	/**
	 * setter.
	 * @param partiesLoadedVal the partiesLoaded to set
	 */
	public void setPartiesLoaded(boolean partiesLoadedVal) {
		this.partiesLoaded = partiesLoadedVal;
	}
	/**
	 * getter.
	 * @return the partiesLoaded
	 */
	public boolean isPartiesLoaded() {
		return partiesLoaded;
	}
}
