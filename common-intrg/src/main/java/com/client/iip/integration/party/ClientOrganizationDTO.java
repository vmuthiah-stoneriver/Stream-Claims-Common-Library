package com.client.iip.integration.party;

import java.util.Collection;

import com.fiserv.isd.iip.bc.party.api.OrganizationDTO;
import com.stoneriver.iip.notepad.model.NotepadDTO;

public class ClientOrganizationDTO extends OrganizationDTO {

	Collection<NotepadDTO> notes= null;
	
	boolean mergeAdditionalPartyInfoOnly = false;
	
	public ClientOrganizationDTO(){
		super();
	}

	public Collection<NotepadDTO> getNotes(){
		return notes;
	}
	
	public void setNotes(Collection<NotepadDTO> _notes){
		notes = _notes;
	}

	/**
	 * @return the mergeAdditionalPartyInfoOnly
	 */
	public boolean isMergeAdditionalPartyInfoOnly() {
		return mergeAdditionalPartyInfoOnly;
	}

	/**
	 * @param booleanValue Boolean value to set
	 */
	public void setMergeAdditionalPartyInfoOnly(boolean booleanValue) {
		this.mergeAdditionalPartyInfoOnly = booleanValue;
	}
}
