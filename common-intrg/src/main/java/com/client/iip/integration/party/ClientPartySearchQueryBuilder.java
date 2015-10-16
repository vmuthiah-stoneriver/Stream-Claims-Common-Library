package com.client.iip.integration.party;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fiserv.isd.iip.bc.party.PartyConstants;
import com.fiserv.isd.iip.core.data.query.rdbms.Criteria;
import com.fiserv.isd.iip.core.data.query.source.QueryBuilder;

/**
 * This is the QueryBuilder for Party search.
 */
@com.fiserv.isd.iip.core.meta.annotation.QueryBuilder(
		id = "client.party.querybuilder.partySearch")
public class ClientPartySearchQueryBuilder implements QueryBuilder {

	private static final Logger LOGGER = LoggerFactory
	.getLogger(ClientPartySearchQueryBuilder.class);

	// define the constants used for SQL fragments in the party-queries.xml file

	// joins
	private static final String PARTY_IDENTIFIER_JOIN = "partyIdentifierJoin";
	private static final String TAX_IDENTIFIER_JOIN = "taxIdentifierJoin";
	private static final String PARTY_PERSON_JOIN = "partyPersonJoin";
	private static final String PARTY_JOIN = "partyJoin";
	private static final String PARTY_ADDRESS_JOIN = "partyAddressJoin";
	private static final String PARTY_ADDRESS_POSTAL_REGION_JOIN = "partyAddressPostalRegionJoin";
	private static final String PARTY_ADDRESS_USAGE_JOIN = "partyAddressUsageJoin";//absaar
	private static final String INTERACTION_CHANNEL_JOIN = "interactionChannelJoin";
	private static final String ROLES_JOIN = "rolesJoin";
	private static final String ROUTING_NUMBER_JOIN = "routingNumberJoin";
	private static final String VENDOR_TYPE_JOIN = "vendorTypeJoin";//absaar
	private static final String MEMBERSHIP_ASSOCIATION_JOIN = "membershipAssociationJoin";
	private static final String VENDOR_SPECIALTY_TYPE_JOIN = "vendorSpecialtyTypeJoin";//sajal
	private static final String VENDOR_PART_OF_BODY_JOIN = "vendorPartOfBodyJoin";//sajal
	private static final String VENDOR_IME_INDICATOR_JOIN = "vendorImeIndicatorJoin";//sajal
	
	
	
	private static final String LANGUAGE_JOIN = "languageJoin";
	
	// filters
	
	private static final String PERSON_FNAME_FILTER = "personFirstNameFilter";
	private static final String PERSON_LNAME_FILTER = "personLastNameFilter";
	private static final String PERSON_MNAME_FILTER = "personMiddleNameFilter";
	private static final String PERSON_DATE_FILTER = "personDateFilter";
	
	private static final String PARTY_ID_FILTER = "partyIdFilter";
	//Events Filter
	private static final String PARTY_EVENT_FILTER = "partyEventFilter";
	private static final String PERSON_NAME_FILTER = "personNameFilter";
	private static final String ORGANIZATION_NAME_FILTER = "organizationNameFilter";
	private static final String PARTY_TYPE_FILTER = "partyTypeFilter";
	private static final String PARTY_DUPLICATE_FILTER = "partyDuplicateFilter";
	private static final String ADDRESS_LINE_1_FILTER = "addressLine1Filter";
	private static final String ADDRESS_LINE_2_FILTER = "addressLine2Filter";
	private static final String ADDRESS_CITY_FILTER = "addressCityFilter";
	private static final String ADDRESS_COUNTY_FILTER = "addressCountyFilter";
	private static final String ADDRESS_STATE_FILTER = "addressStateFilter";
	private static final String ADDRESS_COUNTRY_FILTER = "addressCountryFilter";
	private static final String ADDRESS_ZIPCODE_FILTER = "addressZipcodeFilter";
	private static final String ADDRESS_USAGE_FILTER = "partyAddressUsageFilter"; //absaar
	private static final String PARTY_NUMBER_FILTER = "partyNumberFilter";
	private static final String PARTY_IDENTIFIER_TYPE_FILTER = "partyIdentifierTypeFilter";
	private static final String PARTY_IDENTIFIER_FILTER = "partyIdentifierFilter";
	private static final String TAX_IDENTIFIER_FILTER = "taxIdentifierFilter";
	private static final String PERSON_DATE_OF_BIRTH_FILTER = "personDateOfBirthFilter";
	private static final String PARTY_INTERACTION_CHANNEL_TYPE_FILTER = "communicationTypeFilter";
	private static final String PARTY_INTERACTION_CHANNEL_FILTER = "communicationFilter";
	private static final String ROLES_FILTER = "rolesFilter";
	private static final String ROUTING_NUMBER_FILTER = "routingNumberFilter";
	private static final String VENDOR_TYPE_FILTER = "vendorTypeFilter";//absaar
	private static final String VENDOR_CATEGORY_FILTER = "vendorCategoryFilter";//absaar
	private static final String MEMBERSHIP_ID_FILTER = "membershipIdFilter";
	private static final String INCLUDE_PRIMARY_MEMBERSHIP = "includePrimaryMembership";
	private static final String VENDOR_SPECIALTY_TYPE_FILTER = "vendorSpecialtyTypeFilter";//sajal
	private static final String VENDOR_SPECIALTY_IME_INDICATOR_FILTER = "vendorSpecialtyImeIndicatorFilter";//sajal
	private static final String VENDOR_PART_OF_BODY_FILTER = "vendorPartOfBodyFilter";//sajal
	private static final String VENDOR_PART_OF_BODY_IME_INDICATOR_FILTER = "vendorPartOfBodyImeIndicatorFilter";//sajal
	private static final String VENDOR_IME_INDICATOR_FILTER = "vendorImeIndicatorFilter";//sajal
	private static final String VENDOR_SPECIALTY_POB_IME_OPTIONAL_FILTER="vendorSpecPobImeOptionalFilter";
	
	private static final String RECOMMENDED_VENDOR_FILTER = "recommendedVendor";
	private static final String LANGUAGE_FILTER = "language";
	private static final String GENDER_FILTER = "gender";

	// optional includes
	private static final String INCLUDE_ADDRESS_SELECT = "includeAddressSelect";
	private static final String INCLUDE_ADDRESS_GENERIC_FROM = "includeAddressGenericFrom";
	private static final String INCLUDE_ADDRESS_BY_AGREEMENT_TYPE_FROM = "includeAddressByAgreementTypeFrom";
	private static final String INCLUDE_TAX_SELECT = "includeTaxSelect";
	private static final String INCLUDE_TAX_FROM = "includeTaxFrom";
	private static final String INCLUDE_NAME_SELECT = "includeNameSelect";
	private static final String INCLUDE_NAME_FROM = "includeNameFrom";
	
	private static final String INCLUDE_DBANAME_SELECT = "includeDBANameSelect";
	private static final String INCLUDE_VENDOR_SELECT = "includeVendorSelect";
	private static final String INCLUDE_VENDOR_FROM = "includeVendorFrom";
	
		
	private static final String INCLUDE_PROFILE_SELECT = "includeProfileSelect";
	private static final String INCLUDE_PROFILE_FROM = "includeProfileFrom";	
	
	private static final String INCLUDE_LIEN_SELECT = "includeLienSelect";
	private static final String INCLUDE_LIEN_FROM = "includeLienFrom";
	
	private static final String INCLUDE_COMMUNICATION_SELECT = "includeCommunicationSelect";
	private static final String INCLUDE_COMMUNICATION_FROM = "includeCommunicationFrom";	
	
	private static final String INCLUDE_ROUNTING_NUMBER_SELECT = "includeRoutingNumberSelect";
	private static final String INCLUDE_ROUNTING_NUMBER_FROM = "includeRoutingNumberFrom";
	
	private static final String INCLUDE_PARTY_BLOCK_SELECT = "includePartyBlockSelect";
	private static final String INCLUDE_PARTY_BLOCK_FROM = "includePartyBlockFrom";

	// CTE select clauses
	private static final String SELECT_PERSON_OR_BOTH = "personOrBothSelect";
	private static final String SELECT_ORG_OR_BOTH = "organizationOrBothSelect";
	private static final String SELECT_OTHER = "otherSelect";

	// misc
	private static final String QUERY_BEGIN = "begin";
	private static final String QUERY_MIDDLE = "mid";
	private static final String QUERY_END = "end";
	private static final String UNION = "union";
	private static final String AND = "and";
	private static final String WHERE = "where";
	private static final String SPACE = " ";
	private static final String ORDER_BY = "orderBy";
	private static final String QUERY_MIDDLE2 = "mid2";	
	

	// constants for wildcards used during searches
	private static final String SEARCH_WILDCARD = "*";
	private static final String SEARCH_WILDCARD_REGEX = "\\*";
	private static final String SQL_WILDCARD = "%";
	
	
	private static final String SEARCH_BY_LOCATION_ADDRESS_BEGIN = "searchByLocationAddressBegin";
	private static final String	SEARCH_BY_LOCATION_ADDRESS_END ="searchByLocationAddressEnd";
	private static final String SEARCH_BY_LOCATION_ADDRESS_USAGE_BEGIN = "searchByLocationAddressUsageBegin";
	private static final String	SEARCH_BY_LOCATION_ADDRESS_USAGE_END ="searchByLocationAddressUsageEnd";
	private static final String SEARCH_BY_LOCATION_ADDRESS_BY_CITY = "searchByLocationAddressByCity";
	private static final String	SEARCH_BY_LOCATION_ADDRESS_BY_ADDR1 ="searchByLocationAddressByAddress1";
	private static final String SEARCH_BY_LOCATION_ADDRESS_BY_ADDR2 = "searchByLocationAddressByAddress2";
	private static final String	SEARCH_BY_LOCATION_ADDRESS_BY_POSTAL_CODE ="searchByLocationAddressByPostalCode";
	private static final String SEARCH_BY_LOCATION_ADDRESS_BY_COUNTY = "searchByLocationAddressByCounty";
	private static final String SEARCH_BY_LOCATION_ADDRESS_BY_STATE_BEGIN = "searchByLocationAddressByStateBegin";
	private static final String SEARCH_BY_LOCATION_ADDRESS_BY_STATE_END = "searchByLocationAddressByStateEnd";
	private static final String SEARCH_BY_LOCATION_ADDRESS_BY_STATE = "searchByLocationAddressByState";
	private static final String SEARCH_BY_LOCATION_ADDRESS_BY_COUNTRY_BEGIN = "searchByLocationAddressByCountryBegin";
	private static final String SEARCH_BY_LOCATION_ADDRESS_BY_COUNTRY = "searchByLocationAddressByCountry";
	
	private static final String SEARCH_BY_LOCATION_ADDRESS_BY_NAME_ORDERBY = "searchByLocationAddressByNameOrderBy";
	private static final String SEARCH_BY_LOCATION_ADDRESS_BY_TAXIDENTIFER_ORDERBY = "searchByLocationAddressByTaxidentifierOrderBy";
	private static final String SEARCH_BY_LOCATION_ADDRESS_BY_ADDRESS_ORDERBY = "searchByLocationAddressByAddressOrderBy";

	private static final String FETCH_FIRST_N_ROWS = "fetchFirstNRows";
	private static final String SELECT_TOP_N_ROWS = "selectTOPExpression";
	
	/**
	 * Build the person from clause.
	 * 
	 * @param criteria PartySearchCriteria.
	 * @param sqlFragments Map.
	 * @param sql the buffer used to build up the search sql
	 */
	private void addJoins(
			ClientPartySearchCriteria criteria,
			StringBuffer sql,
			Map<String, String> sqlFragments) {

//		if ((criteria.getPartyIdentifierTypeFilter() != null)
//				|| (criteria.getPartyIdentifierFilter() != null)) {
//			sql.append(sqlFragments.get(PARTY_IDENTIFIER_JOIN));
//		}

		sql.append(sqlFragments.get(PARTY_JOIN));
		
		if ((criteria.getPartyIdentifierTypeFilter() != null)) {
			sql.append(sqlFragments.get(PARTY_IDENTIFIER_JOIN));
		}

		if (criteria.getTaxIdentifierFilter() != null) {
			sql.append(sqlFragments.get(TAX_IDENTIFIER_JOIN));
		}

		if (criteria.getDateOfBirthFilter() != null || criteria.getGenderFilter() !=null) {
			sql.append(sqlFragments.get(PARTY_PERSON_JOIN));
		}

		if ((criteria.getAddressLine1Filter() != null)
				|| (criteria.getAddressLine2Filter() != null)
				|| (criteria.getAddressCityFilter() != null)
				|| (criteria.getAddressCountyFilter() != null)
				|| (criteria.getAddressStateFilter() != null)
				|| (criteria.getAddressCountryFilter() != null)
				|| (criteria.getAddressZipcodeFilter() != null)) {
			sql.append(sqlFragments.get(PARTY_ADDRESS_JOIN));
			if ((criteria.getAddressStateFilter() != null)
					|| (criteria.getAddressCountryFilter() != null)) {
				sql.append(sqlFragments.get(PARTY_ADDRESS_POSTAL_REGION_JOIN));
			}			
		}
	

		if (criteria.getCommunicationTypeFilter() != null || criteria.getCommunicationFilter() != null) {
			sql.append(sqlFragments.get(INTERACTION_CHANNEL_JOIN));
			if (criteria.getCommunicationTypeFilter() != null) {
				sql.append(AND);
				sql.append(SPACE);
				sql.append(sqlFragments.get(PARTY_INTERACTION_CHANNEL_TYPE_FILTER));
			}
		}
		
		if (((criteria.getRolesFilter() != null)
				&& (criteria.getRolesFilter().size() > 0)) || (criteria.getMembershipIdFilter() != null) || (criteria.isIncludePrimaryMembership())) {
			sql.append(sqlFragments.get(ROLES_JOIN));
		}
		
		if ((criteria.getRolesFilter() != null)
				&& (criteria.getRolesFilter().size() > 0)) {
			if(criteria.getRoutingNumberFilter()!=null){
				sql.append(sqlFragments.get(ROUTING_NUMBER_JOIN));
			}
			
/*			if((criteria.getVendorCategoryFilter() != null)
					&& (criteria.getVendorTypeFilter() != null)
					&& (criteria.getVendorTypeFilter().equals(PartyConstants.ROLE_VENDOR))){*/
			if(PartyConstants.ROLE_VENDOR.equals(criteria.getRolesFilter().iterator().next())){
				sql.append(sqlFragments.get(VENDOR_TYPE_JOIN));
				
			if(PartyConstants.VENDOR_CATEGORY_HEALTH_CARE_PROVIDER.equals(criteria.getVendorCategoryFilter())){
				if(((criteria.getSpecialtyType()==null&&criteria.getPartOfBody()==null)||(criteria.getSpecialtyType()!=null&&criteria.getPartOfBody()!=null))&&criteria.isMeIndicator()){
					sql.append(sqlFragments.get(VENDOR_IME_INDICATOR_JOIN));
				}else{
					if(criteria.getSpecialtyType()!=null){
						sql.append(sqlFragments.get(VENDOR_SPECIALTY_TYPE_JOIN));
					}
					if(criteria.getPartOfBody()!=null){
						sql.append(sqlFragments.get(VENDOR_PART_OF_BODY_JOIN));
					}
				}	
			}
			}
		}
		
		if (criteria.getMembershipIdFilter() != null || criteria.isIncludePrimaryMembership()){
			sql.append(sqlFragments.get(MEMBERSHIP_ASSOCIATION_JOIN));
		}
		
		if (criteria.getLanguageFilter() != null){
			sql.append(sqlFragments.get(LANGUAGE_JOIN));
		}
		
	}

	/**
	 * @param sql StringBuffer.
	 * @param hasWhereClause boolean.
	 * @param name String.
	 * @param value Object.
	 * @param sqlFragments Map.
	 * @return hasWhereClause
	 */
	private boolean criteriaHelper(
			StringBuffer sql,
			boolean hasWhereClause,
			String name,
			Object value,
			Map<String, String> sqlFragments) {

		boolean newHasWhereClause = hasWhereClause;
		if (value != null) {
			if (hasWhereClause) {
				sql.append(sqlFragments.get(AND));
			} else {
				sql.append(sqlFragments.get(WHERE));
			}
			sql.append(sqlFragments.get(name));
			newHasWhereClause = true;
		}

		return newHasWhereClause;		
	}

	/**
	 * If the given value has a wildcard character (*), convert it to a
	 * SQL wildcard.
	 * 
	 * @param value the value to be converted (if necessary)
	 * @return the converted value
	 */
	// DE126 - enhanced for 2.15 of Requirements Standards.doc
	private String wildcard(String value) {
		if (value == null) {
			return null;
		}
		
		String newValue = value.replaceAll(SEARCH_WILDCARD_REGEX, SQL_WILDCARD);
		
		//DE4411
		if(!newValue.contains(SQL_WILDCARD)){			
			newValue += SQL_WILDCARD;			
		}

		return newValue;
	}

	/**
	 * Uppercase the entire string value.
	 * 
	 * @param value the value to be uppercased. May be null.
	 * @return the uppercased value or null if the value parameter is null
	 */
	private String upperCase(String value) {
		return (value == null)
				? null
				: value.toUpperCase();
	}
	/**
	 * Remove spaces before the search.
	 * @param value string.
	 * @return new value with out spaces
	 */
	private String removeSpaces(String value) {
		return (value == null)
				? null
				: value.replace(" ", "");
	}

	/**
	 * Generate all of the filter joins and where clauses.
	 * 
	 * @param criteria ClientPartySearchCriteria.
	 * @param sqlFragments Map.
	 * @param sql buffer for the SQL being built
	 * @param isInPersonOrBoth indicates which part of the CTE is currently being built
	 */
	private void addFilters(
			ClientPartySearchCriteria criteria,
			StringBuffer sql,
			Map<String, String> sqlFragments,
			boolean isInPersonOrBoth) {

		addJoins(criteria, sql, sqlFragments);
		addWhere(criteria, sql, sqlFragments, isInPersonOrBoth);
	}

	/**
	 * Generate all of the filter where clauses.
	 * 
	 * @param criteria ClientPartySearchCriteria.
	 * @param sqlFragments Map.
	 * @param sql buffer for the SQL being built
	 * @param isInPersonOrBoth indicates which part of the CTE is currently being built
	 */
	private void addWhere(ClientPartySearchCriteria criteria, StringBuffer sql,
			Map<String, String> sqlFragments, boolean isInPersonOrBoth) {
		// requirement 2.15 - name has an implicit wildcard at the end
		//DE4411
		if ((criteria.getPartyNameFilter() != null)
				&& (criteria.getPartyNameFilter().length() > 0)
				&& !(criteria.getPartyNameFilter().contains(SEARCH_WILDCARD)||criteria.getPartyNameFilter().contains(SQL_WILDCARD))) {
			criteria.setPartyNameFilter(criteria.getPartyNameFilter()
					+ SEARCH_WILDCARD);
		}
		// handle wildcards
		criteria.setAddressLine1Filter(removeSpaces(wildcard(upperCase(criteria
				.getAddressLine1Filter()))));
		criteria.setAddressLine2Filter(removeSpaces(wildcard(upperCase(criteria
				.getAddressLine2Filter()))));
		criteria.setAddressCityFilter(removeSpaces(wildcard(upperCase(criteria
				.getAddressCityFilter()))));
		criteria
				.setAddressCountyFilter(removeSpaces(wildcard(upperCase(criteria
						.getAddressCountyFilter()))));
		// DE159 - Party search should not support wildcard on postal code
		// criteria.setAddressZipcodeFilter(removeSpaces(wildcard(criteria.getAddressZipcodeFilter())));
		criteria
				.setCommunicationFilter(removeSpaces(wildcard(upperCase(criteria
						.getCommunicationFilter()))));
		criteria.setPartyIdentifierFilter(removeSpaces(wildcard(criteria
				.getPartyIdentifierFilter())));
		criteria.setPartyNameFilter(removeSpaces(upperCase(wildcard(criteria
				.getPartyNameFilter()))));
		criteria.setFirstNameFilter(removeSpaces(upperCase(wildcard(criteria
				.getFirstNameFilter()))));
		criteria.setLastNameFilter(removeSpaces(upperCase(wildcard(criteria
				.getLastNameFilter()))));
		criteria.setMiddleNameFilter(removeSpaces(upperCase(wildcard(criteria
				.getMiddleNameFilter()))));
		criteria.setMembershipIdFilter(removeSpaces(wildcard(upperCase(criteria
				.getMembershipIdFilter()))));
		// criteria.setPartyNumberFilter(wildcard(criteria.getPartyNumberFilter()));
		// Rally DE7 - do not support wildcard on tax id
		// criteria.setTaxIdentifierFilter(wildcard(criteria.getTaxIdentifierFilter()));
		// Adding support on Routing Number for wildcard.
		criteria.setRoutingNumberFilter(removeSpaces(upperCase(criteria
				.getRoutingNumberFilter())));
		boolean hasWhereClause = false;
		
		//Call to name filter clause
		hasWhereClause=nameFilterClause(criteria,sql,sqlFragments,isInPersonOrBoth,hasWhereClause);
		
		if (criteria.getPartyIdsFilter() == null || criteria.getPartyIdsFilter().isEmpty()) {
			hasWhereClause = criteriaHelper(sql, hasWhereClause,PARTY_DUPLICATE_FILTER, new Object(), sqlFragments);
		}
		
		if ((criteria.getPartyIdsFilter() != null)
				&& (criteria.getPartyIdsFilter().size() > 0)) {
			hasWhereClause = criteriaHelper(sql, hasWhereClause,PARTY_ID_FILTER, criteria.getPartyIdsFilter(), sqlFragments);
		}
		
		if ((criteria.getPartyEventsFilter() != null)
				&& (criteria.getPartyEventsFilter().size() > 0)) {
			hasWhereClause = criteriaHelper(sql, hasWhereClause,PARTY_EVENT_FILTER, criteria.getPartyEventsFilter(), sqlFragments);
		}
		
		if ((criteria.getRolesFilter() != null)
				&& (criteria.getRolesFilter().size() > 0)) {
			hasWhereClause = criteriaHelper(sql, hasWhereClause, ROLES_FILTER,criteria.getRolesFilter(), sqlFragments);
			if (criteria.getRoutingNumberFilter() != null) {
				hasWhereClause = criteriaHelper(sql, hasWhereClause,ROUTING_NUMBER_FILTER, criteria
								.getRoutingNumberFilter(), sqlFragments);
			}
			//Vendor search Attributes
			if (PartyConstants.ROLE_VENDOR.equals(criteria.getRolesFilter()
					.iterator().next())) {
				hasWhereClause = criteriaHelper(sql, hasWhereClause,
						VENDOR_CATEGORY_FILTER, criteria
								.getVendorCategoryFilter(), sqlFragments);
				hasWhereClause = criteriaHelper(sql, hasWhereClause,VENDOR_TYPE_FILTER, criteria.getVendorTypeFilter(),
						sqlFragments);
				if (criteria.isRecommendedVendor()) {
					hasWhereClause = criteriaHelper(sql, hasWhereClause,RECOMMENDED_VENDOR_FILTER, criteria
									.isRecommendedVendor(), sqlFragments);
				}
				if(PartyConstants.VENDOR_CATEGORY_HEALTH_CARE_PROVIDER.equals(criteria.getVendorCategoryFilter())){
					if(criteria.getSpecialtyType()==null&&criteria.getPartOfBody()==null&&criteria.isMeIndicator()){
						hasWhereClause = criteriaHelper(sql, hasWhereClause,
								VENDOR_IME_INDICATOR_FILTER, criteria
								.isMeIndicator(), sqlFragments);
					}else if(criteria.getSpecialtyType()!=null&&criteria.getPartOfBody()!=null&&criteria.isMeIndicator()){
						hasWhereClause = criteriaHelper(sql, hasWhereClause,VENDOR_SPECIALTY_POB_IME_OPTIONAL_FILTER, criteria
								.getSpecialtyType(), sqlFragments);
					}else{
						if(criteria.getSpecialtyType()!=null){
							hasWhereClause = criteriaHelper(sql, hasWhereClause,VENDOR_SPECIALTY_TYPE_FILTER, criteria
											.getSpecialtyType(), sqlFragments);
							if(criteria.isMeIndicator()){
								hasWhereClause = criteriaHelper(sql, hasWhereClause,VENDOR_SPECIALTY_IME_INDICATOR_FILTER, criteria
												.isMeIndicator(), sqlFragments);
							}
						}
						if(criteria.getPartOfBody()!=null){
							hasWhereClause = criteriaHelper(sql, hasWhereClause,VENDOR_PART_OF_BODY_FILTER, criteria.getPartOfBody(), sqlFragments);
							if(criteria.isMeIndicator()){
								hasWhereClause = criteriaHelper(sql, hasWhereClause,VENDOR_PART_OF_BODY_IME_INDICATOR_FILTER, criteria.isMeIndicator(), sqlFragments);
							}
						}
					}
					
				}
			}
		}
		if (criteria.isIncludePrimaryMembership()) {
			hasWhereClause = criteriaHelper(sql, hasWhereClause,INCLUDE_PRIMARY_MEMBERSHIP, criteria
							.isIncludePrimaryMembership(), sqlFragments);
		}
		hasWhereClause = criteriaHelper(sql, hasWhereClause,isInPersonOrBoth ? PERSON_NAME_FILTER: ORGANIZATION_NAME_FILTER, criteria
						.getPartyNameFilter(), sqlFragments);
		hasWhereClause = criteriaHelper(sql, hasWhereClause,ADDRESS_LINE_1_FILTER, criteria.getAddressLine1Filter(),
				sqlFragments);
		hasWhereClause = criteriaHelper(sql, hasWhereClause,ADDRESS_LINE_2_FILTER, criteria.getAddressLine2Filter(),
				sqlFragments);
		hasWhereClause = criteriaHelper(sql, hasWhereClause,ADDRESS_CITY_FILTER, criteria.getAddressCityFilter(),
				sqlFragments);
		hasWhereClause = criteriaHelper(sql, hasWhereClause,ADDRESS_COUNTY_FILTER, criteria.getAddressCountyFilter(),
				sqlFragments);
		hasWhereClause = criteriaHelper(sql, hasWhereClause,ADDRESS_STATE_FILTER, criteria.getAddressStateFilter(),
				sqlFragments);
		hasWhereClause = criteriaHelper(sql, hasWhereClause,ADDRESS_COUNTRY_FILTER, criteria.getAddressCountryFilter(),
				sqlFragments);
		hasWhereClause = criteriaHelper(sql, hasWhereClause,ADDRESS_ZIPCODE_FILTER, criteria.getAddressZipcodeFilter(),
				sqlFragments);
		hasWhereClause = criteriaHelper(sql, hasWhereClause,PARTY_NUMBER_FILTER, criteria.getPartyNumberFilter(),
				sqlFragments);
		hasWhereClause = criteriaHelper(sql, hasWhereClause,PARTY_IDENTIFIER_TYPE_FILTER, criteria
						.getPartyIdentifierTypeFilter(), sqlFragments);
		hasWhereClause = criteriaHelper(sql, hasWhereClause,PARTY_IDENTIFIER_FILTER, criteria.getPartyIdentifierFilter(),
				sqlFragments);
		hasWhereClause = criteriaHelper(sql, hasWhereClause,TAX_IDENTIFIER_FILTER, criteria.getTaxIdentifierFilter(),
				sqlFragments);
		hasWhereClause = criteriaHelper(sql, hasWhereClause,PARTY_INTERACTION_CHANNEL_TYPE_FILTER, criteria
						.getCommunicationTypeFilter(), sqlFragments);
		hasWhereClause = criteriaHelper(sql, hasWhereClause,PARTY_INTERACTION_CHANNEL_FILTER, criteria
						.getCommunicationFilter(), sqlFragments);
		hasWhereClause = criteriaHelper(sql, hasWhereClause,PERSON_DATE_OF_BIRTH_FILTER, criteria.getDateOfBirthFilter(),
				sqlFragments);
		hasWhereClause = criteriaHelper(sql, hasWhereClause,MEMBERSHIP_ID_FILTER, criteria.getMembershipIdFilter(),
				sqlFragments); 
		hasWhereClause = criteriaHelper(sql, hasWhereClause, LANGUAGE_FILTER,criteria.getLanguageFilter(), sqlFragments);
		hasWhereClause = criteriaHelper(sql, hasWhereClause, GENDER_FILTER,criteria.getGenderFilter(), sqlFragments);
	}
	
	/**
	 * Generate Party Name filter in where clauses.
	 * 
	 * @param criteria ClientPartySearchCriteria.
	 * @param sql buffer for the SQL being built.
	 * @param sqlFragments Map.
	 * @param isInPersonOrBoth indicates which part of the CTE is currently being built.
	 * @param hasWhereClause boolean.
	 * @return hasWhereClause
	 */
	public boolean nameFilterClause(ClientPartySearchCriteria criteria, StringBuffer sql,
			Map<String, String> sqlFragments, boolean isInPersonOrBoth,boolean hasWhereClause) {
		
		if ((criteria.getFirstNameFilter()!=null && criteria.getFirstNameFilter().trim().length()>0) 
				|| (criteria.getLastNameFilter()!=null && criteria.getLastNameFilter().trim().length()>0)
				|| (criteria.getMiddleNameFilter()!=null && criteria.getMiddleNameFilter().trim().length()>0)) {
			if (criteria.getFirstNameFilter()!=null && criteria.getFirstNameFilter().trim().length()>0){
				hasWhereClause = criteriaHelper(sql, hasWhereClause,
						PERSON_FNAME_FILTER, criteria.getFirstNameFilter(),
						sqlFragments);
			}
			if (criteria.getLastNameFilter()!=null && criteria.getLastNameFilter().trim().length()>0){
				hasWhereClause = criteriaHelper(sql, hasWhereClause,
						PERSON_LNAME_FILTER, criteria.getLastNameFilter(),
						sqlFragments);
			}
			if (criteria.getMiddleNameFilter()!=null && criteria.getMiddleNameFilter().trim().length()>0){
				hasWhereClause = criteriaHelper(sql, hasWhereClause,
						PERSON_MNAME_FILTER, criteria.getMiddleNameFilter(),
						sqlFragments);
			}
			// Here Type is used because we are doing this thing for Person Only so Always Party Type will not be null
			hasWhereClause = criteriaHelper(sql, hasWhereClause,
					PERSON_DATE_FILTER, criteria.getPartyTypeFilter(),
					sqlFragments);
		}else if (criteria.getPartyNameFilter() == null) {
			// party type filter is only relevant when no name filter given due to
			// the way the CTE is constructed
			hasWhereClause = criteriaHelper(sql, hasWhereClause,
					PARTY_TYPE_FILTER, criteria.getPartyTypeFilter(),
					sqlFragments);
		}
		
		return hasWhereClause;
	}

	/**
	 * build SQL.
	 * @param queryCriteria Criteria.
	 * @param sqlFragments Map.
	 * @param drl String.
	 * @return OrganizationWhereClause
	 */
	public String buildSQL(Criteria queryCriteria,
			Map<String, String> sqlFragments, String drl) {

		if (!(queryCriteria instanceof ClientPartySearchCriteria)) {
			throw new IllegalArgumentException("expecting a " + ClientPartySearchCriteria.class);
		}

		ClientPartySearchCriteria criteria = (ClientPartySearchCriteria)queryCriteria;
		/*
		if (criteria.getSearchResultType() == null) {
			throw new IllegalArgumentException("seachType is required");
		}
		*/

		// build the SQL
		StringBuffer sql = new StringBuffer(4096);

		sql.append(sqlFragments.get(QUERY_BEGIN));
		
		if ((criteria.getFirstNameFilter()!=null && criteria.getFirstNameFilter().trim().length()>0) 
				|| (criteria.getLastNameFilter()!=null && criteria.getLastNameFilter().trim().length()>0)
				|| (criteria.getMiddleNameFilter()!=null && criteria.getMiddleNameFilter().trim().length()>0)) {
			
			// if person or both
			if ((criteria.getPartyTypeFilter() == null)
					|| criteria.getPartyTypeFilter().equals(PartyConstants.PARTY_TYPE_PERSON)) {
				sql.append(sqlFragments.get(SELECT_PERSON_OR_BOTH));
				addFilters(criteria, sql, sqlFragments, true);
			}
			
		}else if ((criteria.getPartyNameFilter() != null)
				&& !criteria.getPartyNameFilter().equals("")) {

			// if person or both
			if ((criteria.getPartyTypeFilter() == null)
					|| criteria.getPartyTypeFilter().equals(PartyConstants.PARTY_TYPE_PERSON)) {
				sql.append(sqlFragments.get(SELECT_PERSON_OR_BOTH));
				addFilters(criteria, sql, sqlFragments, true);
			}

			if (criteria.getPartyTypeFilter() == null) {
				sql.append(sqlFragments.get(UNION));
			}

			// if org or both
			if ((criteria.getPartyTypeFilter() == null)
					|| criteria.getPartyTypeFilter().equals(PartyConstants.PARTY_TYPE_ORGANIZATION)) {
				sql.append(sqlFragments.get(SELECT_ORG_OR_BOTH));
				addFilters(criteria, sql, sqlFragments, false);
			}
		} else {
			sql.append(sqlFragments.get(SELECT_OTHER));
			addFilters(criteria, sql, sqlFragments, false);
		}

		sql.append(sqlFragments.get(QUERY_MIDDLE));

		if(criteria.getMaxSearchResults() != null && criteria.getMaxSearchResults().compareTo(0L) > 0) {
			sql.append(sqlFragments.get(SELECT_TOP_N_ROWS));
		}
		
		sql.append(sqlFragments.get(QUERY_MIDDLE2));

		if (criteria.isIncludeAddress()) {
			sql.append(sqlFragments.get(INCLUDE_ADDRESS_SELECT));
		}
		if (criteria.isIncludeTaxInfo()) {
			sql.append(sqlFragments.get(INCLUDE_TAX_SELECT));
		}
		
		// Add party block SELECT clause
		if(criteria.isIncludeBlockedCustomers() || criteria.isIncludeBlockedVendors()){
			sql.append(sqlFragments.get(INCLUDE_PARTY_BLOCK_SELECT));
		}
		
		if(criteria.getRolesFilter() != null && !criteria.getRolesFilter().isEmpty() 
		   && PartyConstants.ROLE_VENDOR.equals(criteria.getRolesFilter().iterator().next())){
			sql.append(sqlFragments.get(INCLUDE_VENDOR_SELECT));
		}
		
		if(criteria.getIncludeProfileName() != null){
			sql.append(sqlFragments.get(INCLUDE_PROFILE_SELECT));
		}		
				
		if(criteria.isIncludeLien()){
			sql.append(sqlFragments.get(INCLUDE_LIEN_SELECT));
		}
		
		if(criteria.isIncludeCommunication()){
			sql.append(sqlFragments.get(INCLUDE_COMMUNICATION_SELECT));
		}		
		
		if (criteria.isIncludeName()) {
			sql.append(sqlFragments.get(INCLUDE_NAME_SELECT));
			sql.append(sqlFragments.get(INCLUDE_DBANAME_SELECT));
		}
		
		/*Only Add it partyType is organization as routing number can exist in case of Organization only.
		 * and if role if financial institution, as routing number can exist only with this role
		 * There is no meaning to append query for routing number if this is not a role with Financial institution.		
		 */
		if (criteria.getRolesFilter()!=null && criteria.getRolesFilter().size()>0
				&& criteria.getRolesFilter().contains(PartyConstants.ROLE_FNCL_INST)){
			if ((criteria.getPartyTypeFilter() != null)
					&& criteria.getPartyTypeFilter().equals(PartyConstants.PARTY_TYPE_ORGANIZATION)) {
					sql.append(sqlFragments.get(INCLUDE_ROUNTING_NUMBER_SELECT));
			}
		}
		sql.append(sqlFragments.get(QUERY_END));

		if (criteria.isIncludeAddress()) {
			if ((criteria.getResultAddressAgreementType() == null)
					|| (criteria.getResultAddressAgreementType().length() == 0)) {
				
				//TODO Change the below if condition to if(criteria.isLocationSearch())
				if(criteria.isLocationSearch()){
					buildLocationSearchSQL(criteria,sqlFragments,sql);
				}else{
					sql.append(sqlFragments.get(INCLUDE_ADDRESS_GENERIC_FROM));
				}
			} else {
				sql.append(sqlFragments.get(INCLUDE_ADDRESS_BY_AGREEMENT_TYPE_FROM));
			}
		}
		
		if (criteria.isIncludeTaxInfo()) {
			sql.append(sqlFragments.get(INCLUDE_TAX_FROM));
		}
		
		// Add party block LEFT OUTER JOIN clause
		if(criteria.isIncludeBlockedCustomers() || criteria.isIncludeBlockedVendors()){
			sql.append(sqlFragments.get(INCLUDE_PARTY_BLOCK_FROM));
		}
		
		if (criteria.isIncludeName()) {
			sql.append(sqlFragments.get(INCLUDE_NAME_FROM));
		}
		
		if(criteria.getRolesFilter() != null && !criteria.getRolesFilter().isEmpty() 
			&& PartyConstants.ROLE_VENDOR.equals(criteria.getRolesFilter().iterator().next())){
			sql.append(sqlFragments.get(INCLUDE_VENDOR_FROM));
		}
		
		if(criteria.getIncludeProfileName() != null){
				sql.append(sqlFragments.get(INCLUDE_PROFILE_FROM));
		}		
		
		if(criteria.isIncludeLien()){
			sql.append(sqlFragments.get(INCLUDE_LIEN_FROM));
		}	
		
		if(criteria.isIncludeCommunication()){
			sql.append(sqlFragments.get(INCLUDE_COMMUNICATION_FROM));
		}			
		
		/**Only Add it partyType is organization as routing number can exist in case of Organization only.
		 * and if role if financial institution, as routing number can exist only with this role
		 * There is no meaning to append query for routing number if this is not a role with Financial institution.		
		 */
		if (criteria.getRolesFilter()!=null && criteria.getRolesFilter().size()>0
				&& criteria.getRolesFilter().contains(PartyConstants.ROLE_FNCL_INST)){
			if ((criteria.getPartyTypeFilter() != null)
					&& criteria.getPartyTypeFilter().equals(PartyConstants.PARTY_TYPE_ORGANIZATION)) {
					sql.append(sqlFragments.get(INCLUDE_ROUNTING_NUMBER_FROM));
			}
		}
		
		//if it is location search, then add order by clause
		if(criteria.isLocationSearch()){
			buildOrderyByClauseForLocationSearch(criteria, sqlFragments, sql);
		}
		
		//Limit the Number of Rows returned.
		if(criteria.getMaxSearchResults() != null && criteria.getMaxSearchResults().compareTo(0L) > 0){
			sql.append(sqlFragments.get(FETCH_FIRST_N_ROWS));
		}		
		
		LOGGER.debug("The Party Search Query is {} ", sql);

		return sql.toString();
	}
	
	/**
	 * builds order by clause for location search.
	 * @param criteria criteria object
	 * @param sqlFragments sql fragments
	 * @param sql sql
	 */
	private void buildOrderyByClauseForLocationSearch(ClientPartySearchCriteria criteria,
			Map<String, String> sqlFragments,StringBuffer sql){
		String orderBy = "ORDER BY ";
		boolean orderByRequired = false;
		
		if(criteria.isIncludeName()){
			orderBy = orderBy + sqlFragments.get(SEARCH_BY_LOCATION_ADDRESS_BY_NAME_ORDERBY) + ",";
			orderByRequired = true;
		}
		
		if(criteria.isIncludeTaxInfo()){
			orderBy = orderBy + sqlFragments.get(SEARCH_BY_LOCATION_ADDRESS_BY_TAXIDENTIFER_ORDERBY) + ",";
			orderByRequired = true;
		}
		
		if(criteria.isIncludeAddress()){
			orderBy = orderBy + sqlFragments.get(SEARCH_BY_LOCATION_ADDRESS_BY_ADDRESS_ORDERBY) + ",";
			orderByRequired = true;
		}
		
		if(orderByRequired){
			sql.append(orderBy.substring(0, orderBy.length()-1));
		}		
	}
	
	/**
	 * build Location Search SQL.
	 * @param criteria ClientPartySearchCriteria.
	 * @param sqlFragments Map.
	 * @param sql StringBuffer
	 */
	private void buildLocationSearchSQL(ClientPartySearchCriteria criteria,
			Map<String, String> sqlFragments,StringBuffer sql){
		//if(criteria.isLocationSearch()){
		if(criteria.getPartyAddressUsageFilter()!=null){
			sql.append(sqlFragments.get(SEARCH_BY_LOCATION_ADDRESS_USAGE_BEGIN));
			if(criteria.getAddressCityFilter()!=null){
				sql.append(sqlFragments.get(SEARCH_BY_LOCATION_ADDRESS_BY_CITY));
			}
			if(criteria.getAddressLine1Filter()!=null){
				sql.append(sqlFragments.get(SEARCH_BY_LOCATION_ADDRESS_BY_ADDR1));
			}
			if(criteria.getAddressLine2Filter()!=null){
				sql.append(sqlFragments.get(SEARCH_BY_LOCATION_ADDRESS_BY_ADDR2));
			}
			if(criteria.getAddressCountyFilter()!=null){
				sql.append(sqlFragments.get(SEARCH_BY_LOCATION_ADDRESS_BY_COUNTY));
			}
			if(criteria.getAddressZipcodeFilter()!=null){
				sql.append(sqlFragments.get(SEARCH_BY_LOCATION_ADDRESS_BY_POSTAL_CODE));
			}
			sql.append(sqlFragments.get(SEARCH_BY_LOCATION_ADDRESS_BY_STATE_BEGIN));
			if(criteria.getAddressStateFilter()!=null){
				sql.append(sqlFragments.get(SEARCH_BY_LOCATION_ADDRESS_BY_STATE));
			}
			sql.append(sqlFragments.get(SEARCH_BY_LOCATION_ADDRESS_BY_STATE_END));
			sql.append(sqlFragments.get(SEARCH_BY_LOCATION_ADDRESS_BY_COUNTRY_BEGIN));
			
			if(criteria.getAddressCountryFilter()!=null){
				sql.append(sqlFragments.get(SEARCH_BY_LOCATION_ADDRESS_BY_COUNTRY));
			}
			sql.append(sqlFragments.get(SEARCH_BY_LOCATION_ADDRESS_USAGE_END));
		}else{
			sql.append(sqlFragments.get(SEARCH_BY_LOCATION_ADDRESS_BEGIN));
			if(criteria.getAddressCityFilter()!=null){
				sql.append(sqlFragments.get(SEARCH_BY_LOCATION_ADDRESS_BY_CITY));
			}
			if(criteria.getAddressLine1Filter()!=null){
				sql.append(sqlFragments.get(SEARCH_BY_LOCATION_ADDRESS_BY_ADDR1));
			}
			if(criteria.getAddressLine2Filter()!=null){
				sql.append(sqlFragments.get(SEARCH_BY_LOCATION_ADDRESS_BY_ADDR2));
			}
			if(criteria.getAddressZipcodeFilter()!=null){
				sql.append(sqlFragments.get(SEARCH_BY_LOCATION_ADDRESS_BY_POSTAL_CODE));
			}
			sql.append(sqlFragments.get(SEARCH_BY_LOCATION_ADDRESS_BY_STATE_BEGIN));
			if(criteria.getAddressStateFilter()!=null){
				sql.append(sqlFragments.get(SEARCH_BY_LOCATION_ADDRESS_BY_STATE));
			}
			sql.append(sqlFragments.get(SEARCH_BY_LOCATION_ADDRESS_BY_STATE_END));
			sql.append(sqlFragments.get(SEARCH_BY_LOCATION_ADDRESS_BY_COUNTRY_BEGIN));
			
			if(criteria.getAddressCountryFilter()!=null){
				sql.append(sqlFragments.get(SEARCH_BY_LOCATION_ADDRESS_BY_COUNTRY));
			}
			sql.append(sqlFragments.get(SEARCH_BY_LOCATION_ADDRESS_END));
		}
	}
}

