package com.client.iip.integration.party;

import java.util.Collection;

import com.fiserv.isd.iip.bc.party.api.PersonDTO;
import com.stoneriver.iip.notepad.model.NotepadDTO;

public class ClientPersonDTO extends PersonDTO {
	

	private static final long serialVersionUID = 5465787688134616153L;
	Collection<NotepadDTO> notes= null;
	
	boolean mergeAdditionalPartyInfoOnly = false;

	public ClientPersonDTO(){
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
