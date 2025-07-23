/// Copyright © 2025 Infinite Computer Solution. All rights reserved. 

package com.infinite.jsf.recipient.controller;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.infinite.jsf.insurance.model.PlanType;
import com.infinite.jsf.insurance.model.SubscribedMember;
import com.infinite.jsf.provider.model.MedicalProcedure;
import com.infinite.jsf.recipient.dao.InsuranceDao;
import com.infinite.jsf.recipient.daoImpl.InsuranceDaoImpl;
import com.infinite.jsf.recipient.model.PatientInsuranceDetails;
import com.infinite.jsf.recipient.model.Recipient;

/**
 * JSF Managed Bean responsible for displaying, filtering, and sorting recipient
 * insurance data, handling member information, pagination for insurance and
 * members tables, and managing user session.
 */
public class RecipientShowInsuranceController implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(RecipientShowInsuranceController.class.getName());

	// Data objects
	private MedicalProcedure medicalProcedure;
	private InsuranceDaoImpl insuranceDaoImpl;
	private Recipient recipient = new Recipient();
	private PatientInsuranceDetails selectedItem;
	private String hId = (String) ((Recipient) FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
			.get("loggedInRecipient")).gethId();
	private String userName;
	private String fullName = (String) ((Recipient) FacesContext.getCurrentInstance().getExternalContext()
			.getSessionMap().get("fullName")).getFullName();
	private String selectedStatus;
	private InsuranceDao insuranceDao = new InsuranceDaoImpl();

	private List<PatientInsuranceDetails> patientInsuranceList;
	private List<PatientInsuranceDetails> originalInsuranceList;
	private List<SubscribedMember> subscribedMembers;

	// Sorting state for Insurance
	private String currentInsuranceSortColumn;
	private String currentInsuranceSortDirection; // "asc" or "desc"

	// Sorting state for Members
	private String currentMemberSortColumn;
	private String currentMemberSortDirection; // "asc" or "desc"

	// Pagination
	private int insurancePage = 0;
	private int memberPage = 0;
	private final int pageSize = 4;

	// Date filter fields
	private Date fromDate;
	private Date toDate;

	/**
	 * Retrieves the paginated and sorted list of insurance details for the
	 * recipient.
	 * 
	 * @return List of PatientInsuranceDetails for current page.
	 */
	public List<PatientInsuranceDetails> getInsuranceData() {
		FacesContext context = FacesContext.getCurrentInstance();
		try {
			// Lazy fetch if list is null
			if (patientInsuranceList == null) {
				originalInsuranceList = insuranceDao.showInsuranceOfRecipient(hId);
				patientInsuranceList = originalInsuranceList;

				if (patientInsuranceList == null || patientInsuranceList.isEmpty()) {
					context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "No Insurance Found",
							"Please subscribe to a plan."));
					return Collections.emptyList();
				}

				// Apply default sort if needed
				if (currentInsuranceSortColumn == null || currentInsuranceSortColumn.isEmpty()) {
					currentInsuranceSortColumn = "coverageStart";
					currentInsuranceSortDirection = "desc";
				}
				sortPatientInsuranceList();
				resetInsurancePage();
			}
		} catch (Exception e) {
			LOGGER.warn("Exception occurred while retrieving the insurance data: " + e.getMessage());
		}

		// Always return paginated data
		if (patientInsuranceList == null)
			return Collections.emptyList();
		int from = insurancePage * pageSize;
		int to = Math.min(from + pageSize, patientInsuranceList.size());
		return patientInsuranceList.subList(from, to);
	}

	/**
	 * Handles viewing of family members for a particular insurance item.
	 * 
	 * @param insurance PatientInsuranceDetails
	 * @return Navigation outcome for JSF page redirection.
	 */
	public String viewMembers(PatientInsuranceDetails insurance) {
		this.selectedItem = insurance;
		try {
			if (insurance != null && insurance.getCoverageType() == PlanType.FAMILY) {
				this.subscribedMembers = insurance.getSubscribedMembers();

				FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("hId", insurance.gethId());
				this.currentMemberSortColumn = null;
				this.currentMemberSortDirection = null;
				resetMemberPage();

				if (subscribedMembers != null && !subscribedMembers.isEmpty()) {
					currentMemberSortColumn = "memberId";
					currentMemberSortDirection = "asc";
					sortViewMemberList();
				}
				return "/recipient/ViewMemebers.jsp?faces-redirect=true";
			}
		} catch (Exception e) {
			LOGGER.warn("Exception occurred while fetching the members data: " + e.getMessage());
		}
		return null;
	}

	/**
	 * Filters insurance records by coverage status (ACTIVE or EXPIRED).
	 * 
	 * @param status The status to filter by.
	 */
	public void filterByCoverageStatus(String status) {
		try {
			if (originalInsuranceList == null || status == null) {
				this.patientInsuranceList = Collections.emptyList();
				return;
			}

			this.selectedStatus = status;
			this.fromDate = null;
			this.toDate = null;

			this.patientInsuranceList = originalInsuranceList.stream()
					.filter(p -> p.getCoverageStatus() != null && p.getCoverageStatus().name().equalsIgnoreCase(status))
					.collect(Collectors.toList());

			this.currentInsuranceSortColumn = null;
			this.currentInsuranceSortDirection = null;
			resetInsurancePage();
		} catch (Exception e) {
			LOGGER.warn("Exception occurred while filtering insurance by status: " + e.getMessage());
		}
	}

	/**
	 * Filters patient insurance list by the currently selected date range and
	 * status.
	 */
	public void filterByDateRange() {
		try {
			FacesContext context = FacesContext.getCurrentInstance();

			if (fromDate == null && toDate == null) {
				context.addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Both From and To dates are required.", null));
				return;
			}

			if (fromDate == null) {
				context.addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "From date must be selected", null));
				return;
			}

			if (toDate == null) {
				context.addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "To Date must be selected", null));
				return;
			}

			if (fromDate.after(toDate)) {
				context.addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "From Date cannot be after To Date.", null));
				return;
			}

			if (originalInsuranceList == null || originalInsuranceList.isEmpty()) {
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"No insurance records available to filter.", null));
				return;
			}

			this.patientInsuranceList = originalInsuranceList.stream().filter(p -> {
				Date start = p.getCoverageStartDate();
				boolean isInRange = start != null && !start.before(fromDate) && !start.after(toDate);
				boolean matchesStatus = true;
				if (selectedStatus != null && !selectedStatus.isEmpty()) {
					matchesStatus = p.getCoverageStatus() != null
							&& p.getCoverageStatus().name().equalsIgnoreCase(selectedStatus);
				}
				return isInRange && matchesStatus;
			}).collect(Collectors.toList());

			if (patientInsuranceList.isEmpty()) {
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
						"No insurance records fall within the selected date range.", null));
			}

			this.currentInsuranceSortColumn = null;
			this.currentInsuranceSortDirection = null;
			resetInsurancePage();
		} catch (Exception e) {
			LOGGER.warn("Exception occurred while filtering by date range: " + e.getMessage());
		}
	}

	/**
	 * Gets the paginated member list for the members table.
	 * 
	 * @return Sub-list for current page, or empty list if none.
	 */
	public List<SubscribedMember> getPaginatedMemberList() {
		try {
			if (subscribedMembers == null || subscribedMembers.isEmpty())
				return Collections.emptyList();
		} catch (Exception e) {
			LOGGER.warn("Exception occurred in getPaginatedMemberList: " + e.getMessage());
		}
		int from = memberPage * pageSize;
		int to = Math.min(from + pageSize, subscribedMembers.size());
		return subscribedMembers.subList(from, to);
	}

	/**
	 * Summary string for paginated insurance list.
	 * 
	 * @return Pagination summary ("Showing N of M Results")
	 */
	public String getPaginationIncSummary() {
		try {
			if (patientInsuranceList == null || patientInsuranceList.isEmpty()) {
				return "";
			}
		} catch (Exception e) {
			LOGGER.warn("Exception occurred while getting the Insurance pagination summary: " + e.getMessage());
		}
		int from = Math.min((insurancePage + 1) * pageSize, patientInsuranceList.size());
		int total = patientInsuranceList.size();
		return "Showing " + from + " of " + total + " Results";
	}

	/**
	 * Summary string for paginated member list.
	 * 
	 * @return Pagination summary ("Showing N of M Results")
	 */
	public String getPaginationMemSummary() {
		try {
			if (subscribedMembers == null || subscribedMembers.isEmpty()) {
				return "";
			}
		} catch (Exception e) {
			LOGGER.warn("Exception occurred while getting the members pagination summary: " + e.getMessage());
		}
		int from = Math.min((memberPage + 1) * pageSize, subscribedMembers.size());
		int total = subscribedMembers.size();
		return "Showing " + from + " of " + total + " Results";
	}

	/**
	 * Sorts patientInsuranceList ascending by fieldName.
	 * 
	 * @param fieldName Field to sort on.
	 * @return null for JSF navigation.
	 */
	public String sortByAsc(String fieldName) {
		try {
			this.currentInsuranceSortColumn = fieldName;
			this.currentInsuranceSortDirection = "asc";
			sortPatientInsuranceList();
			resetInsurancePage();
		} catch (Exception e) {
			LOGGER.warn("Exception occurred while sorting by Ascending: " + e.getMessage());
		}
		return null;
	}

	/**
	 * Sorts patientInsuranceList descending by fieldName.
	 * 
	 * @param fieldName Field to sort on.
	 * @return null for JSF navigation.
	 */
	public String sortByDesc(String fieldName) {
		try {
			this.currentInsuranceSortColumn = fieldName;
			this.currentInsuranceSortDirection = "desc";
			sortPatientInsuranceList();
			resetInsurancePage();
		} catch (Exception e) {
			LOGGER.warn("Exception occurred while sorting by Descending: " + e.getMessage());
		}
		return null;
	}

	/**
	 * Sort members ascending by fieldName.
	 * 
	 * @param fieldName Member object field to sort on.
	 * @return null for JSF navigation.
	 */
	public String sortByAscMem(String fieldName) {
		try {
			this.currentMemberSortColumn = fieldName;
			this.currentMemberSortDirection = "asc";
			sortViewMemberList();
			resetMemberPage();
		} catch (Exception e) {
			LOGGER.warn("Exception occurred while sorting members ascending: " + e.getMessage());
		}
		return null;
	}

	/**
	 * Sort members descending by fieldName.
	 * 
	 * @param fieldName Member object field to sort on.
	 * @return null for JSF navigation.
	 */
	public String sortByDescMem(String fieldName) {
		try {
			this.currentMemberSortColumn = fieldName;
			this.currentMemberSortDirection = "desc";
			sortViewMemberList();
			resetMemberPage();
		} catch (Exception e) {
			LOGGER.warn("Exception occurred while sorting members descending: " + e.getMessage());
		}
		return null;
	}

	/**
	 * Resets all insurance and date filters, restoring the original list.
	 * 
	 * @return null for JSF.
	 */
	public String resetFilter() {
		try {
			this.insurancePage = 0;
			this.fromDate = null;
			this.toDate = null;
			this.selectedStatus = null;
			this.patientInsuranceList = insuranceDao.showInsuranceOfRecipient(hId);
			this.currentInsuranceSortColumn = null;
			this.currentInsuranceSortDirection = null;
		} catch (Exception e) {
			LOGGER.warn("Exception occurred while resetting the filters: " + e.getMessage());
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private void sortPatientInsuranceList() {
		try {
			if (patientInsuranceList == null || currentInsuranceSortColumn == null)
				return;

			Collections.sort(patientInsuranceList, (a, b) -> {
				try {
					Field fieldA = a.getClass().getDeclaredField(currentInsuranceSortColumn);
					Field fieldB = b.getClass().getDeclaredField(currentInsuranceSortColumn);
					fieldA.setAccessible(true);
					fieldB.setAccessible(true);
					Comparable<Object> valueA = (Comparable<Object>) fieldA.get(a);
					Comparable<Object> valueB = (Comparable<Object>) fieldB.get(b);

					if (valueA == null)
						return currentInsuranceSortDirection.equals("asc") ? -1 : 1;
					if (valueB == null)
						return currentInsuranceSortDirection.equals("asc") ? 1 : -1;

					return currentInsuranceSortDirection.equals("asc") ? valueA.compareTo(valueB)
							: valueB.compareTo(valueA);
				} catch (Exception e) {
					return 0;
				}
			});
		} catch (Exception e) {
			LOGGER.warn("Exception occurred while sorting Insurance table: " + e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	private void sortViewMemberList() {
		try {
			if (subscribedMembers == null || currentMemberSortColumn == null)
				return;

			Collections.sort(subscribedMembers, (a, b) -> {
				try {
					Field fieldA = a.getClass().getDeclaredField(currentMemberSortColumn);
					Field fieldB = b.getClass().getDeclaredField(currentMemberSortColumn);
					fieldA.setAccessible(true);
					fieldB.setAccessible(true);
					Comparable<Object> valueA = (Comparable<Object>) fieldA.get(a);
					Comparable<Object> valueB = (Comparable<Object>) fieldB.get(b);

					if (valueA == null)
						return currentMemberSortDirection.equals("asc") ? -1 : 1;
					if (valueB == null)
						return currentMemberSortDirection.equals("asc") ? 1 : -1;
					return currentMemberSortDirection.equals("asc") ? valueA.compareTo(valueB)
							: valueB.compareTo(valueA);
				} catch (Exception e) {
					return 0;
				}
			});
		} catch (Exception e) {
			LOGGER.warn("Exception occurred while sorting Members table: " + e.getMessage());
		}
	}

	// Getter and setter methods (no logic change, so left as-is)
	public String gethId() {
		return hId;
	}

	public void sethId(String hId) {
		this.hId = hId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public MedicalProcedure getMedicalProcedure() {
		return medicalProcedure;
	}

	public void setMedicalProcedure(MedicalProcedure medicalProcedure) {
		this.medicalProcedure = medicalProcedure;
	}

	public InsuranceDaoImpl getInsuranceDaoImpl() {
		return insuranceDaoImpl;
	}

	public void setInsuranceDaoImpl(InsuranceDaoImpl insuranceDaoImpl) {
		this.insuranceDaoImpl = insuranceDaoImpl;
	}

	public Recipient getRecipient() {
		return recipient;
	}

	public void setRecipient(Recipient recipient) {
		this.recipient = recipient;
	}

	public PatientInsuranceDetails getSelectedItem() {
		return selectedItem;
	}

	public void setSelectedItem(PatientInsuranceDetails selectedItem) {
		this.selectedItem = selectedItem;
	}

	public InsuranceDao getInsuranceDao() {
		return insuranceDao;
	}

	public void setInsuranceDao(InsuranceDao insuranceDao) {
		this.insuranceDao = insuranceDao;
	}

	public List<PatientInsuranceDetails> getPatientInsuranceList() {
		return patientInsuranceList;
	}

	public void setPatientInsuranceList(List<PatientInsuranceDetails> patientInsuranceList) {
		this.patientInsuranceList = patientInsuranceList;
	}

	public List<SubscribedMember> getSubscribedMembers() {
		return subscribedMembers;
	}

	public void setSubscribedMembers(List<SubscribedMember> subscribedMembers) {
		this.subscribedMembers = subscribedMembers;
	}

	public int getInsurancePage() {
		return insurancePage;
	}

	public void setInsurancePage(int insurancePage) {
		this.insurancePage = insurancePage;
	}

	public String getSortField() {
		return null;
	}

	public void setSortField(String sortField) {
	}

	public boolean isAscending() {
		return true;
	}

	public void setAscending(boolean ascending) {
	}

	public int getPageSize() {
		return pageSize;
	}

	public String getCurrentInsuranceSortColumn() {
		return currentInsuranceSortColumn;
	}

	public void setCurrentInsuranceSortColumn(String currentInsuranceSortColumn) {
		this.currentInsuranceSortColumn = currentInsuranceSortColumn;
	}

	public String getCurrentInsuranceSortDirection() {
		return currentInsuranceSortDirection;
	}

	public void setCurrentInsuranceSortDirection(String currentInsuranceSortDirection) {
		this.currentInsuranceSortDirection = currentInsuranceSortDirection;
	}

	public String getCurrentMemberSortColumn() {
		return currentMemberSortColumn;
	}

	public void setCurrentMemberSortColumn(String currentMemberSortColumn) {
		this.currentMemberSortColumn = currentMemberSortColumn;
	}

	public String getCurrentMemberSortDirection() {
		return currentMemberSortDirection;
	}

	public void setCurrentMemberSortDirection(String currentMemberSortDirection) {
		this.currentMemberSortDirection = currentMemberSortDirection;
	}

	public Date getFromDate() {
		return fromDate;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public Date getToDate() {
		return toDate;
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	public String getSelectedStatus() {
		return selectedStatus;
	}

	public void setSelectedStatus(String selectedStatus) {
		this.selectedStatus = selectedStatus;
	}

	/**
	 * Determines if a sort button (up or down arrow) should be rendered for the
	 * PatientInsuranceDetails table.
	 */
	public boolean renderSortButton(String column, String direction) {
		if (currentInsuranceSortColumn == null || !currentInsuranceSortColumn.equals(column))
			return true;
		return !currentInsuranceSortDirection.equals(direction);
	}

	/**
	 * Determines if a sort button (up or down arrow) should be rendered for the
	 * SubscribedMembers table.
	 */
	public boolean renderSortButtonMem(String column, String direction) {
		if (currentMemberSortColumn == null || !currentMemberSortColumn.equals(column))
			return true;
		return !currentMemberSortDirection.equals(direction);
	}

	// Insurance table pagination methods...
	public void nextPage() {
		if (getHasNextPage())
			insurancePage++;
	}

	public void previousPage() {
		if (getHasPreviousPage())
			insurancePage--;
	}

	public boolean getHasNextPage() {
		return (insurancePage + 1) * pageSize < (patientInsuranceList != null ? patientInsuranceList.size() : 0);
	}

	public boolean getHasPreviousPage() {
		return insurancePage > 0;
	}

	public int getCurrentPage() {
		return insurancePage + 1;
	}

	public int getTotalPages() {
		if (patientInsuranceList == null || patientInsuranceList.isEmpty())
			return 0;
		return (int) Math.ceil((double) patientInsuranceList.size() / pageSize);
	}

	public void resetInsurancePage() {
		this.insurancePage = 0;
	}

	// Members table pagination methods...
	public void nextMemberPage() {
		if (getHasNextMemberPage())
			memberPage++;
	}

	public void previousMemberPage() {
		if (getHasPreviousMemberPage())
			memberPage--;
	}

	public boolean getHasNextMemberPage() {
		return (memberPage + 1) * pageSize < (subscribedMembers != null ? subscribedMembers.size() : 0);
	}

	public boolean getHasPreviousMemberPage() {
		return memberPage > 0;
	}

	public int getMemberPage() {
		return memberPage + 1;
	}

	public int getTotalMemberPages() {
		if (subscribedMembers == null || subscribedMembers.isEmpty())
			return 0;
		return (int) Math.ceil((double) subscribedMembers.size() / pageSize);
	}

	public void resetMemberPage() {
		this.memberPage = 0;
	}

	/**
	 * JSF action for logging out user, clearing session, and redirecting to home
	 * page.
	 * 
	 * @return navigation string
	 */
	public String logout() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
		if (session != null) {
			LOGGER.info(getFullName() + " has logged-out....");
			session.invalidate();
		}
		return "/home/Home.jsp?faces-redirect=true";
	}

	/**
	 * Action to return to insurance listing.
	 * 
	 * @return navigation string to ShowInsurance page
	 */
	public String goBackinc() {
		return "ShowInsurance";
	}
}