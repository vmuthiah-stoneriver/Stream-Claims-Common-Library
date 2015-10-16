package com.client.iip.integration.claim;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fiserv.isd.iip.core.data.query.rdbms.Criteria;
import com.fiserv.isd.iip.core.data.query.rdbms.OrderByDTO;
import com.fiserv.isd.iip.core.framework.client.DTO;
import com.stoneriver.iip.entconfig.hierarchy.Hierarchy;

@DTO
public class ControlNumberSearchCriteria extends Hierarchy implements Criteria {
	private static final long serialVersionUID = 8892817938125431340L;

	private Long companyLOBId;

	private static final Long DUMMY_ID = -1L;
	private static final String DUMMY_ID_STR = "-1";
	private List<String> endingWildcardSearch = new ArrayList<String>();
	private List<String> uppercaseWildcardSearch = new ArrayList<String>();
	private List<String> wildcardSearch = new ArrayList<String>();
	private Long pageSize;
	private Long totalCount;
	private Long totalPageCount;
	private Long currentPage;
	private boolean pageable;
	private Collection<OrderByDTO> sortOrderBy;

	/**
	 * @return the companyLOBId
	 */
	public Long getCompanyLOBId() {
		return companyLOBId;
	}

	/**
	 * @param companyLOBId
	 *            the companyLOBId to set
	 */
	public void setCompanyLOBId(Long companyLOBId) {
		this.companyLOBId = companyLOBId;
	}

	/**
	 * Gets the methods names requiring an ending wild card value be set.
	 * 
	 * @return the methods to check for ending wild card values.
	 */
	public List<String> getEndingWildcardSearch() {
		return endingWildcardSearch;
	}

	/**
	 * Gets the list of method names to be upper cased.
	 * 
	 * @return the methods names to be upper cased.
	 */
	public List<String> getUppercaseWildcardSearch() {
		return uppercaseWildcardSearch;
	}

	/**
	 * Gets the wild card search criteria method names.
	 * 
	 * @return wild card search criteria method names.
	 */
	public List<String> getWildcardSearch() {
		return wildcardSearch;
	}

	/**
	 * Sets the criteria method names requiring an ending wild card value. The
	 * list should be the Java Bean property names without the set, get or is
	 * prefix. This is a list of criteria method names that require an ending
	 * wild card value.
	 * 
	 * @param aMethodNames
	 *            method names for wildcard search.
	 */
	public void setEndingWildcardSearch(List<String> aMethodNames) {
		endingWildcardSearch = aMethodNames;
	}

	/**
	 * Sets the value of the criteria fields listed to upper case. The list
	 * should be the Java Bean property names without the set, get or is prefix.
	 * This is a list of criteria method names that require their values to be
	 * in upper case.
	 * 
	 * @param aMethodNames
	 *            List of method names to be upper cased.
	 */
	public void setUppercaseWildcardSearch(List<String> aMethodNames) {
		uppercaseWildcardSearch = aMethodNames;
	}

	/**
	 * Sets the wild card search criteria method names. The list should be the
	 * Java Bean property names without the set, get or is prefix. This is a
	 * list of criteria method names that have wild card properties.
	 * 
	 * @param aMethodNames
	 *            method names for wild card search.
	 */
	public void setWildcardSearch(List<String> aMethodNames) {
		wildcardSearch = aMethodNames;
	}

	/**
	 * Get the property defining how many records will display in one page.
	 * 
	 * @return records per page.
	 */
	public Long getPageSize() {
		return pageSize;
	}

	/**
	 * Sets the property defining how many records will display in one page.
	 * 
	 * @param aPageSize
	 *            records per page.
	 */
	public void setPageSize(Long aPageSize) {
		pageSize = aPageSize;
	}

	/**
	 * Total number of records for this search result.
	 * 
	 * @return total record count.
	 */
	public Long getTotalCount() {
		return totalCount;
	}

	/**
	 * Total number of records for this search result.
	 * 
	 * @param aTotalCount
	 *            The total record count.
	 */
	public void setTotalCount(Long aTotalCount) {
		totalCount = aTotalCount;
	}

	/**
	 * Total number of pages for this search result.
	 * 
	 * @return total number of pages.
	 */
	public Long getTotalPageCount() {
		return totalPageCount;
	}

	/**
	 * Sets the total number of pages for this search result.
	 * 
	 * @param aTotalPageCount
	 *            total number of pages.
	 */
	public void setTotalPageCount(Long aTotalPageCount) {
		totalPageCount = aTotalPageCount;
	}

	/**
	 * Get current page number.
	 * 
	 * @return the current page number.
	 */
	public Long getCurrentPage() {
		return currentPage;
	}

	/**
	 * Sets current page number.
	 * 
	 * @param aCurrentPage
	 *            The current page number.
	 */
	public void setCurrentPage(Long aCurrentPage) {
		currentPage = aCurrentPage;
	}

	/**
	 * Does this search have pagination.
	 * 
	 * @return true if search is pageable.
	 */
	public boolean isPageable() {
		return pageable;
	}

	/**
	 * Sets the pagination indicator.
	 * 
	 * @param aPageable
	 *            pagination indicator.
	 */
	public void setPageable(boolean aPageable) {
		pageable = aPageable;
	}

	/**
	 * Gets the Order By Clause(s).
	 * 
	 * @return The Order By clause(s).
	 */
	public Collection<OrderByDTO> getSortOrderBy() {
		return sortOrderBy;
	}

	/**
	 * Sets the Order By clause.
	 * 
	 * @param aSortOrderedBy
	 *            The collection of Order By Clause(s).
	 */
	public void setSortOrderBy(Collection<OrderByDTO> aSortOrderedBy) {
		sortOrderBy = aSortOrderedBy;
	}

	/**
	 * Default constructor which creates the Search Criteria at system level.
	 */
	public ControlNumberSearchCriteria() {
		super();
	}

	/**
	 * Constructor which creates the Search Criteria at level specified in the
	 * argument. It also accepts a system option code to search value for
	 * 
	 * @param companyLOBIdIn
	 *            company line of business
	 * @param jurisdictionIdIn
	 *            jurisdiction
	 */
	public ControlNumberSearchCriteria(Long companyLOBIdIn,
			Long jurisdictionIdIn) {
		super();
		this.companyLOBId = companyLOBIdIn;
		setJurisdictionId(jurisdictionIdIn);
	}

	/**
	 * Gets the Corporation Id for use in search of SystemOption association.
	 * 
	 * @return id for Corporation
	 */
	@Override
	public Long getCorporationId() {
		Long corpId = super.getCorporationId();
		return corpId == null ? DUMMY_ID : corpId;
	}

	/**
	 * Gets the Company Id for use in search of SystemOption association.
	 * 
	 * @return id for Company
	 */
	@Override
	public Long getCompanyId() {
		Long compId = super.getCompanyId();
		return compId == null ? DUMMY_ID : compId;
	}

	/**
	 * Gets the LineOfBusiness Id for use in search of SystemOption association.
	 * 
	 * @return id for LineOfBusiness
	 */
	@Override
	public Long getLineOfBusinessId() {
		Long lobId = super.getLineOfBusinessId();
		return lobId == null ? DUMMY_ID : lobId;
	}

	/**
	 * Gets the Market Type Cd for use in search of SystemOption association.
	 * 
	 * @return id for Product
	 */
	@Override
	public String getMarketTypeCd() {
		String mrktType = super.getMarketTypeCd();
		return mrktType == null ? DUMMY_ID_STR : mrktType;
	}

	/**
	 * Gets the Product Id for use in search of SystemOption association.
	 * 
	 * @return id for Product
	 */
	@Override
	public String getProductCd() {
		String prodId = super.getProductCd();
		return prodId == null ? DUMMY_ID_STR : prodId;
	}

	/**
	 * Gets the Program Id for use in search of SystemOption association.
	 * 
	 * @return id for Program
	 */
	@Override
	public String getProgramCd() {
		String progCd = super.getProgramCd();
		return progCd == null ? DUMMY_ID_STR : progCd;
	}

	/**
	 * Gets the Plan Id for use in search of SystemOption association.
	 * 
	 * @return id for Plan
	 */
	@Override
	public String getPlanCd() {
		String planId = super.getPlanCd();
		return planId == null ? DUMMY_ID_STR : planId;
	}

	/**
	 * Gets the Jurisdiction Id for use in search of SystemOption association.
	 * 
	 * @return id for Jurisdiction
	 */
	@Override
	public Long getJurisdictionId() {
		Long jurId = super.getJurisdictionId();
		return jurId == null ? DUMMY_ID : jurId;
	}

}
