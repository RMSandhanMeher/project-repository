package com.infinite.jsf.recipient.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.infinite.jsf.insurance.model.SubscribedMember;
import com.infinite.jsf.insurance.model.SubscriptionStatus;
import com.infinite.jsf.insurance.model.PlanType;

/**
 * DTO for Patient Insurance Details. Designed for use with
 * Transformers.aliasToBean (Hibernate). Enum fields (coverageStatus,
 * coverageType) must be mapped manually from their String counterparts.
 */
public class RecipientInsuranceDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String subscribeId;
	private String hId;
	private String coverageid;
	private String patientName;
	private String companyName;
	private String planName;
	private Date coverageStartDate;
	private Date coverageEndDate;
	private Double coverageLimit;
	private Double remaining;
	private Double claimed;
	private List<SubscribedMember> subscribedMembers;
	// Fields for receiving raw enum values via DB & mapping then aliasToBean)
	private String coverageStatusString;
	private String coverageTypeString;
	// Actual application enums (populated from mapping then to be set to aliasToBean)
	private SubscriptionStatus coverageStatus;
	private PlanType coverageType;

	public RecipientInsuranceDTO() {
		// Default constructor
	}

	// -- Getters and Setters for all fields --

	public String getSubscribeId() {
		return subscribeId;
	}

	public void setSubscribeId(String subscribeId) {
		this.subscribeId = subscribeId;
	}

	public String getHId() {
		return hId;
	}

	public void setHId(String hId) {
		this.hId = hId;
	}

	public String getPatientName() {
		return patientName;
	}

	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getPlanName() {
		return planName;
	}

	public void setPlanName(String planName) {
		this.planName = planName;
	}


	public Date getCoverageStartDate() {
		return coverageStartDate;
	}

	public void setCoverageStartDate(Date coverageStartDate) {
		this.coverageStartDate = coverageStartDate;
	}

	public Date getCoverageEndDate() {
		return coverageEndDate;
	}

	public void setCoverageEndDate(Date coverageEndDate) {
		this.coverageEndDate = coverageEndDate;
	}

	public double getCoverageLimit() {
		return coverageLimit;
	}

	public void setCoverageLimit(BigDecimal coverageLimit) {
	    this.coverageLimit = (coverageLimit != null) ? coverageLimit.doubleValue() : 0.0;
	}


	public double getRemaining() {
		return remaining;
	}

	public void setRemaining(BigDecimal remaining) {
		this.remaining = (remaining!=null) ? remaining.doubleValue() : 0.0;
	}

	public double getClaimed() {
		return claimed;
	}

	public void setClaimed(BigDecimal claimed) {
		this.claimed = (claimed!=null) ? claimed.doubleValue() : 0.0;
	}


	public List<SubscribedMember> getSubscribedMembers() {
		return subscribedMembers;
	}

	public void setSubscribedMembers(List<SubscribedMember> subscribedMembers) {
		this.subscribedMembers = subscribedMembers;
	}

	// -- String fields (for mapping via aliasToBean) --
	public String getCoverageStatusString() {
		return coverageStatusString;
	}

	public void setCoverageStatusString(String coverageStatusString) {
		this.coverageStatusString = coverageStatusString;
	}

	public String getCoverageTypeString() {
		return coverageTypeString;
	}

	public void setCoverageTypeString(String coverageTypeString) {
		this.coverageTypeString = coverageTypeString;
	}

	// -- Enum fields (populate separately after mapping) --
	public SubscriptionStatus getCoverageStatus() {
		return coverageStatus;
	}

	public void setCoverageStatus(SubscriptionStatus coverageStatus) {
		this.coverageStatus = coverageStatus;
	}

	public PlanType getCoverageType() {
		return coverageType;
	}

	public void setCoverageType(PlanType coverageType) {
		this.coverageType = coverageType;
	}

	public String gethId() {
		return hId;
	}

	public void sethId(String hId) {
		this.hId = hId;
	}

	public String getCoverageid() {
		return coverageid;
	}

	public void setCoverageid(String coverageid) {
		this.coverageid = coverageid;
	}

//	public void setRemaining(Double remaining) {
//		this.remaining = remaining;
//	}

//	public void setClaimed(Double claimed) {
//		this.claimed = claimed;
//	}

	@Override
	public String toString() {
		return "RecipientInsuranceDTO [subscribeId=" + subscribeId + ", hId=" + hId + ", coverageid=" + coverageid
				+ ", patientName=" + patientName + ", companyName=" + companyName + ", planName=" + planName
				+ ", coverageStartDate=" + coverageStartDate + ", coverageEndDate=" + coverageEndDate
				+ ", coverageLimit=" + coverageLimit + ", remaining=" + remaining + ", claimed=" + claimed
				+ ", subscribedMembers=" + subscribedMembers + ", coverageStatusString=" + coverageStatusString
				+ ", coverageTypeString=" + coverageTypeString + ", coverageStatus=" + coverageStatus
				+ ", coverageType=" + coverageType + "]";
	}

	public RecipientInsuranceDTO(String subscribeId, String hId, String coverageid, String patientName,
			String companyName, String planName, Date coverageStartDate, Date coverageEndDate, Double coverageLimit,
			Double remaining, Double claimed, List<SubscribedMember> subscribedMembers, String coverageStatusString,
			String coverageTypeString, SubscriptionStatus coverageStatus, PlanType coverageType) {
		super();
		this.subscribeId = subscribeId;
		this.hId = hId;
		this.coverageid = coverageid;
		this.patientName = patientName;
		this.companyName = companyName;
		this.planName = planName;
		this.coverageStartDate = coverageStartDate;
		this.coverageEndDate = coverageEndDate;
		this.coverageLimit = coverageLimit;
		this.remaining = remaining;
		this.claimed = claimed;
		this.subscribedMembers = subscribedMembers;
		this.coverageStatusString = coverageStatusString;
		this.coverageTypeString = coverageTypeString;
		this.coverageStatus = coverageStatus;
		this.coverageType = coverageType;
	}

	
	
	
}