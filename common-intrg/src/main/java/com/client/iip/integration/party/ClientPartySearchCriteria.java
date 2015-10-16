package com.client.iip.integration.party;

import java.util.Collection;
import java.util.Date;

import com.fiserv.isd.iip.bc.party.search.PartySearchCriteria;

public class ClientPartySearchCriteria extends PartySearchCriteria {
	
	private boolean includeLien;
	
	private String includeProfileName;
	
	private Collection<String> partyEventsFilter;
	
	private Date eventStartDate;
	
	private Date eventEndDate;
	
	
	public boolean isIncludeLien(){
		return this.includeLien;
	}
	
	public void setIncudeLien(boolean _includeLien){
		this.includeLien = _includeLien;
	}

	public String getIncludeProfileName() {
		return this.includeProfileName;
	}

	public void setIncludeProfileName(String _includeProfileName) {
		this.includeProfileName = _includeProfileName;
	}


	/**
	 * @return the eventsFilter
	 */
	public Collection<String> getPartyEventsFilter() {
		return partyEventsFilter;
	}

	/**
	 * @param eventsFilter the eventsFilter to set
	 */
	public void setPartyEventsFilter(Collection<String> _partyEventsFilter) {
		this.partyEventsFilter = _partyEventsFilter;
	}

	/**
	 * @return the eventStartDate
	 */
	public Date getEventStartDate() {
		return eventStartDate==null?new java.sql.Date(System.currentTimeMillis()):eventStartDate;
	}

	/**
	 * @param eventStartDate the eventStartDate to set
	 */
	public void setEventStartDate(Date _eventStartDate) {
		this.eventStartDate = _eventStartDate;
	}

	/**
	 * @return the eventEndDate
	 */
	public Date getEventEndDate() {
		return eventEndDate==null?new java.sql.Date(System.currentTimeMillis()):eventEndDate;
	}

	/**
	 * @param eventEndDate the eventEndDate to set
	 */
	public void setEventEndDate(Date _eventEndDate) {
		this.eventEndDate = _eventEndDate;
	}
	
}
