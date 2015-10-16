package com.client.iip.integration.financials.disbursement;

import java.math.BigDecimal;
import java.util.Date;

/**
 * The DisbursementRecord class will map the query result to the disbursement
 * transfer object.
 * 
 * @author skumar
 */
public class DisbursementRecord {

	private long DSB_ID;
	private long CMPY_ID;
	private long CMPY_ORG_UNT_ID;
	private String FNCL_AGRE_TYP_CD;
	private String PAY_METH_PRF_CD;
	private String DSB_TYP_CD;
	private long DSB_RQST_NO;
	private String DSB_NO;
	private Date DSB_CREA_DT;
	private BigDecimal DSB_AMT;
	private String DSB_CMNT;
	private String DSB_STT_TYP_CD;
	private String DSB_STS_TYP_CD;
	private Date EFF_DTM;
	private Date END_DTM;

	/**
	 * @return the dSB_ID
	 */
	public long getDSB_ID() {
		return DSB_ID;
	}

	/**
	 * @param dSB_ID
	 *            the dSB_ID to set
	 */
	public void setDSB_ID(long dSB_ID) {
		DSB_ID = dSB_ID;
	}

	/**
	 * @return the cMPY_ID
	 */
	public long getCMPY_ID() {
		return CMPY_ID;
	}

	/**
	 * @param cMPY_ID
	 *            the cMPY_ID to set
	 */
	public void setCMPY_ID(long cMPY_ID) {
		CMPY_ID = cMPY_ID;
	}

	/**
	 * @return the cMPY_ORG_UNT_ID
	 */
	public long getCMPY_ORG_UNT_ID() {
		return CMPY_ORG_UNT_ID;
	}

	/**
	 * @param cMPY_ORG_UNT_ID
	 *            the cMPY_ORG_UNT_ID to set
	 */
	public void setCMPY_ORG_UNT_ID(long cMPY_ORG_UNT_ID) {
		CMPY_ORG_UNT_ID = cMPY_ORG_UNT_ID;
	}

	/**
	 * @return the fNCL_AGRE_TYP_CD
	 */
	public String getFNCL_AGRE_TYP_CD() {
		return FNCL_AGRE_TYP_CD;
	}

	/**
	 * @param fNCL_AGRE_TYP_CD
	 *            the fNCL_AGRE_TYP_CD to set
	 */
	public void setFNCL_AGRE_TYP_CD(String fNCL_AGRE_TYP_CD) {
		FNCL_AGRE_TYP_CD = fNCL_AGRE_TYP_CD;
	}

	/**
	 * @return the pAY_METH_PRF_CD
	 */
	public String getPAY_METH_PRF_CD() {
		return PAY_METH_PRF_CD;
	}

	/**
	 * @param pAY_METH_PRF_CD
	 *            the pAY_METH_PRF_CD to set
	 */
	public void setPAY_METH_PRF_CD(String pAY_METH_PRF_CD) {
		PAY_METH_PRF_CD = pAY_METH_PRF_CD;
	}

	/**
	 * @return the dSB_TYP_CD
	 */
	public String getDSB_TYP_CD() {
		return DSB_TYP_CD;
	}

	/**
	 * @param dSB_TYP_CD
	 *            the dSB_TYP_CD to set
	 */
	public void setDSB_TYP_CD(String dSB_TYP_CD) {
		DSB_TYP_CD = dSB_TYP_CD;
	}

	/**
	 * @return the dSB_RQST_NO
	 */
	public long getDSB_RQST_NO() {
		return DSB_RQST_NO;
	}

	/**
	 * @param dSB_RQST_NO
	 *            the dSB_RQST_NO to set
	 */
	public void setDSB_RQST_NO(long dSB_RQST_NO) {
		DSB_RQST_NO = dSB_RQST_NO;
	}

	/**
	 * @return the dSB_NO
	 */
	public String getDSB_NO() {
		return DSB_NO;
	}

	/**
	 * @param dSB_NO
	 *            the dSB_NO to set
	 */
	public void setDSB_NO(String dSB_NO) {
		DSB_NO = dSB_NO;
	}

	/**
	 * @return the dSB_CREA_DT
	 */
	public Date getDSB_CREA_DT() {
		return DSB_CREA_DT;
	}

	/**
	 * @param dSB_CREA_DT
	 *            the dSB_CREA_DT to set
	 */
	public void setDSB_CREA_DT(Date dSB_CREA_DT) {
		DSB_CREA_DT = dSB_CREA_DT;
	}

	/**
	 * @return the dSB_AMT
	 */
	public BigDecimal getDSB_AMT() {
		return DSB_AMT;
	}

	/**
	 * @param dSB_AMT
	 *            the dSB_AMT to set
	 */
	public void setDSB_AMT(BigDecimal dSB_AMT) {
		DSB_AMT = dSB_AMT;
	}

	/**
	 * @return the dSB_CMNT
	 */
	public String getDSB_CMNT() {
		return DSB_CMNT;
	}

	/**
	 * @param dSB_CMNT
	 *            the dSB_CMNT to set
	 */
	public void setDSB_CMNT(String dSB_CMNT) {
		DSB_CMNT = dSB_CMNT;
	}

	/**
	 * @return the dSB_STT_TYP_CD
	 */
	public String getDSB_STT_TYP_CD() {
		return DSB_STT_TYP_CD;
	}

	/**
	 * @param dSB_STT_TYP_CD
	 *            the dSB_STT_TYP_CD to set
	 */
	public void setDSB_STT_TYP_CD(String dSB_STT_TYP_CD) {
		DSB_STT_TYP_CD = dSB_STT_TYP_CD;
	}

	/**
	 * @return the dSB_STS_TYP_CD
	 */
	public String getDSB_STS_TYP_CD() {
		return DSB_STS_TYP_CD;
	}

	/**
	 * @param dSB_STS_TYP_CD
	 *            the dSB_STS_TYP_CD to set
	 */
	public void setDSB_STS_TYP_CD(String dSB_STS_TYP_CD) {
		DSB_STS_TYP_CD = dSB_STS_TYP_CD;
	}

	/**
	 * @return the eFF_DTM
	 */
	public Date getEFF_DTM() {
		return EFF_DTM;
	}

	/**
	 * @param eFF_DTM
	 *            the eFF_DTM to set
	 */
	public void setEFF_DTM(Date eFF_DTM) {
		EFF_DTM = eFF_DTM;
	}

	/**
	 * @return the eND_DTM
	 */
	public Date getEND_DTM() {
		return END_DTM;
	}

	/**
	 * @param eND_DTM
	 *            the eND_DTM to set
	 */
	public void setEND_DTM(Date eND_DTM) {
		END_DTM = eND_DTM;
	}

}
