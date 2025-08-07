/// Copyright © 2025 Infinite Computer Solution. All rights reserved. 

package com.infinite.jsf.recipient.controller;

/*Pagination, sorting and filtering operate in-memory on data fetched once,
improving application performance avoiding repeated DB hits
*/

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;

import com.infinite.jsf.insurance.model.PlanType;
import com.infinite.jsf.insurance.model.SubscribedMember;
import com.infinite.jsf.provider.model.MedicalProcedure;
import com.infinite.jsf.recipient.dao.ShowInsuranceDao;
import com.infinite.jsf.recipient.daoImpl.ShowInsuranceDaoImpl;
import com.infinite.jsf.recipient.model.Recipient;
import com.infinite.jsf.recipient.model.RecipientInsuranceDTO;

/**
 * JSF Managed Bean responsible for displaying, filtering, and sorting recipient
 * insurance data, handling member information, pagination for insurance and
 * members tables, and managing user session.
 */
public class RecipientShowInsuranceController implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LogManager.getLogger(RecipientShowInsuranceController.class);
	// Data objects
	private MedicalProcedure medicalProcedure;
	private Recipient recipient = new Recipient();
	private RecipientInsuranceDTO selectedItem; // This will now hold the detailed item for the view page

	private String hId;
	private String userName;
	private String fullName;
	private String selectedStatus;
	private ShowInsuranceDao insuranceDao = new ShowInsuranceDaoImpl();

	private List<RecipientInsuranceDTO> patientInsuranceList;
	private List<RecipientInsuranceDTO> originalInsuranceList; // All data fetched from DB, unfiltered

	private String planNameSearchInput;

	private List<SubscribedMember> subscribedMembers;

	// Sorting state for Insurance
	private String currentInsuranceSortColumn;
	private String currentInsuranceSortDirection;

	// Sorting state for Members
	private String currentMemberSortColumn;
	private String currentMemberSortDirection;

	// Pagination
	private int insurancePage = 0;
	private int memberPage = 0;
	private final int pageSize = 4;

	// Date filter fields
	private Date fromDate;
	private Date toDate;

	public RecipientShowInsuranceController() {

	}

	/**
	 * Retrieves the paginated and sorted list of insurance details for the
	 * recipient. This method now ensures originalInsuranceList is always populated.
	 * 
	 * @return List of PatientInsuranceDetails for current page.
	 */
	public List<RecipientInsuranceDTO> getInsuranceData() {
		FacesContext context = FacesContext.getCurrentInstance();
		try {
			// Ensure hId is available before DAO call
			String currentHId = gethId();
			if (currentHId == null || currentHId.isEmpty()) {
				LOGGER.error("Controller: hId is null or empty. Cannot retrieve insurance data.");
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Session Error",
						"User ID not found. Please log in again."));
				return Collections.emptyList();
			}

			// This lazy-load logic is correct for a view-scoped bean.
			if (originalInsuranceList == null || originalInsuranceList.isEmpty()) {

				originalInsuranceList = insuranceDao.showInsuranceOfRecipient(currentHId);

				if (originalInsuranceList.isEmpty()) {
					context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "No Insurance Found", null));
					this.patientInsuranceList = Collections.emptyList();
					return Collections.emptyList();
				}

				// Assign the original back to the sub-original i.e patientInsuranceList
				// Upon which the pagination will performed
				this.patientInsuranceList = originalInsuranceList;

				if (selectedStatus == null) {
					selectedStatus = "ALL";
				}

				applyFilters();

				sortPatientInsuranceList();
				resetInsurancePage();
			}

		} catch (HibernateException e) {
			LOGGER.error("Controller: Database error retrieving insurance data for recipient " + gethId() + ": "
					+ e.getMessage(), e);
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Database Error",
					"Failed to load insurance details due to a technical issue. Please try again later."));
			patientInsuranceList = Collections.emptyList();
			originalInsuranceList = Collections.emptyList();
		} catch (IllegalArgumentException e) {
			LOGGER.warn("Controller: Data integrity issue for recipient " + gethId() + ": Invalid enum value from DB: "
					+ e.getMessage(), e);
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Data Issue",
					"Some insurance details may be incomplete or missing due to invalid data in our system."));
			patientInsuranceList = Collections.emptyList();
			originalInsuranceList = Collections.emptyList();
		} catch (Exception e) {
			LOGGER.error("Controller: An unexpected error occurred while retrieving insurance data for recipient "
					+ gethId() + ": " + e.getMessage(), e);
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "System Error",
					"An unexpected error occurred while loading your insurance details."));
			patientInsuranceList = Collections.emptyList();
			originalInsuranceList = Collections.emptyList();
		}

		// This returns the paginated list, 4 data per page
		if (patientInsuranceList == null)
			return Collections.emptyList();
		int from = insurancePage * pageSize;
		int to = Math.min(from + pageSize, patientInsuranceList.size());

		return patientInsuranceList.subList(from, to);
	}

	/**
	 * Handles the "View Details" button click. Sets 'selectedItem' directly and
	 * returns the navigation outcome.
	 *
	 * @param subscribeId, The subscribeId of the insurance policy to view.
	 * @return The navigation outcome to the insurance details page.
	 */
	public String viewInsuranceDetails(String subscribeId) {
		FacesContext context = FacesContext.getCurrentInstance();
		this.selectedItem = null;
		try {
			if (subscribeId == null || subscribeId.isEmpty()) {
				context.addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No insurance ID provided."));
				return null;
			}

			// Assuming the data loading may be differed so pre-populating it before
			// filtering
			getInsuranceData();
			if (this.originalInsuranceList != null && !this.originalInsuranceList.isEmpty()) {
				this.selectedItem = this.originalInsuranceList.stream()
						.filter(item -> subscribeId.equals(item.getSubscribeId())).findFirst().orElse(null);

				if (this.selectedItem != null) {
					return "insuranceDetails?faces-redirect=true";
				} else {
					context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Not Found",
							"No details found for the selected insurance policy."));
				}
			} else {
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
						"Insurance data could not be loaded from the database."));
			}
		} catch (Exception e) {
			LOGGER.error("Error retrieving insurance details for ID: " + subscribeId + " from in-memory list.", e);
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "System Error",
					"Failed to load details due to an unexpected error."));
		}
		this.selectedItem = null;
		return null;
	}

	/**
	 * Handles viewing of family members for a particular insurance item. * @param
	 * insurance PatientInsuranceDetails * @return Navigation outcome for JSF page
	 * redirection.
	 */
	public String viewMembers(RecipientInsuranceDTO insurance) {
		this.selectedItem = insurance;
		try {
			if (insurance != null && insurance.getCoverageType() == PlanType.FAMILY) {
				this.subscribedMembers = insurance.getSubscribedMembers();
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
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not view members."));
		}
		return null;
	}

	/**
	 * Applies status, date range, and plan name filters to the insurance list.
	 */
	public void applyFilters() {
		FacesContext context = FacesContext.getCurrentInstance();
		try {
			if (originalInsuranceList == null || originalInsuranceList.isEmpty()) {
				originalInsuranceList = insuranceDao.showInsuranceOfRecipient(gethId());
				if (originalInsuranceList == null) {
					originalInsuranceList = Collections.emptyList();
				}
			}

			if (originalInsuranceList.isEmpty()) {
				this.patientInsuranceList = Collections.emptyList();
				if (fromDate != null || toDate != null || (selectedStatus != null && !"ALL".equals(selectedStatus))
						|| (planNameSearchInput != null && !planNameSearchInput.trim().isEmpty())) {
					context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
							"No insurance records available to filter.", null));
				}
				sortPatientInsuranceList();
				resetInsurancePage();
				return;
			}

			if (!validateDates(fromDate, toDate, context)) {
				sortPatientInsuranceList();
				resetInsurancePage();
				return;
			}

			if ((fromDate != null && toDate == null) || (fromDate == null && toDate != null)) {
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Partial date range applied. Results may vary.", null));
			}

			// Status Filtering
			this.patientInsuranceList = originalInsuranceList.stream().filter(p -> {
				boolean matchesStatus = selectedStatus == null || selectedStatus.isEmpty()
						|| "ALL".equalsIgnoreCase(selectedStatus) || (p.getCoverageStatus() != null
								&& p.getCoverageStatus().name().equalsIgnoreCase(selectedStatus));

				// Date Filtering
				boolean matchesDateRange = true;
				if (fromDate != null || toDate != null) {
					if (p.getCoverageStartDate() == null) {
						matchesDateRange = false;
					} else {
						Date filterStart = (fromDate != null) ? fromDate : new Date(0);
						Date filterEnd = (toDate != null) ? toDate : new Date(Long.MAX_VALUE);
						boolean noOverlap = p.getCoverageStartDate().before(filterStart)
								|| p.getCoverageStartDate().after(filterEnd);
						matchesDateRange = !noOverlap;
					}
				}

				// Plan name filter
				boolean matchesPlanName = true;
				if (planNameSearchInput != null && !planNameSearchInput.trim().isEmpty()) {
					matchesPlanName = p.getPlanName() != null && p.getPlanName().toLowerCase().replaceAll("\\s+", "")
							.contains(planNameSearchInput.trim().toLowerCase().replaceAll("\\s+", ""));
				}

				return matchesStatus && matchesDateRange && matchesPlanName;
			}).collect(Collectors.toList());

			if (patientInsuranceList.isEmpty()) {
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
						"No insurance records fall within the search criteria.", null));
			}

			this.currentInsuranceSortColumn = null;
			this.currentInsuranceSortDirection = null;
			sortPatientInsuranceList();
			resetInsurancePage();

		} catch (HibernateException e) {
			LOGGER.error(
					"Controller: Database error applying filters for recipient " + gethId() + ": " + e.getMessage(), e);
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Database Error",
					"Failed to apply filters due to a technical issue. Please try again later."));
			patientInsuranceList = Collections.emptyList();
			originalInsuranceList = Collections.emptyList();
		} catch (IllegalArgumentException e) {
			LOGGER.warn("Controller: Data integrity issue applying filters for recipient " + gethId()
					+ ": Invalid enum value from DB: " + e.getMessage(), e);
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Data Issue",
					"Filters may be partially applied due to invalid data in our system."));
			patientInsuranceList = Collections.emptyList();
			originalInsuranceList = Collections.emptyList();
		} catch (Exception e) {
			LOGGER.error("Controller: An unexpected error occurred while applying filters for recipient " + gethId()
					+ ": " + e.getMessage(), e);
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error applying filters.", null));
			patientInsuranceList = Collections.emptyList();
			originalInsuranceList = Collections.emptyList();
		}
	}

	// Date Validation Helper method
	private boolean validateDates(Date from, Date to, FacesContext context) {
		Date today = new Date();

		if ((from != null && from.after(today)) || (to != null && to.after(today))) {
			context.addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Dates cannot be in the future.", null));
			return false;
		}

		if (from != null && to != null && from.after(to)) {
			context.addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "From Date cannot be after To Date.", null));
			return false;
		}

		return true;
	}

	// Validation for Plan Name
	public void searchByPlanName() {
		FacesContext context = FacesContext.getCurrentInstance();
		String input = (planNameSearchInput != null) ? planNameSearchInput.trim() : "";
		if (input.isEmpty()) {
			context.addMessage("insuranceForm:planNameSearchInput",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "PlanName cannot be empty.", null));
			return;
		}
		if (!input.matches("^[A-Za-z ]+$")) {
			context.addMessage("insuranceForm:planNameSearchInput",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "No digits/symbols supported", null));
			return;
		}
		if (input.length() < 5) {
			context.addMessage("insuranceForm:planNameSearchInput",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "PlanName must of 5 Character", null));
			return;
		}
		applyFilters();
	}

	// paginated Member list that to be returned in the UI page
	public List<SubscribedMember> getPaginatedMemberList() {
		try {
			if (subscribedMembers == null || subscribedMembers.isEmpty())
				return Collections.emptyList();
		} catch (Exception e) {
			LOGGER.warn("Exception occurred in getPaginatedMemberList: " + e.getMessage());
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not retrieve member list."));
		}
		int from = memberPage * pageSize;
		int to = Math.min(from + pageSize, subscribedMembers.size());
		return subscribedMembers.subList(from, to);
	}

	// UI summary for InsurancePage like Showing 4 of 12 Results
	public String getPaginationIncSummary() {
		try {
			if (patientInsuranceList == null || patientInsuranceList.isEmpty()) {
				return "Showing 0 of 0 Results";
			}
		} catch (Exception e) {
			LOGGER.warn("Exception occurred while getting the Insurance pagination summary: " + e.getMessage());
		}
		int toCount = Math.min((insurancePage + 1) * pageSize, patientInsuranceList.size());
		int total = patientInsuranceList.size();

		if (total == 0)
			return "Showing 0 of 0 Results";

		return "Showing " + toCount + " of " + total + " Results";
	}

	// UI summary for Member like Showing 4 of 12 Results
	public String getPaginationMemSummary() {
		try {
			if (subscribedMembers == null || subscribedMembers.isEmpty()) {
				return "Showing 0 of 0 Results";
			}
		} catch (Exception e) {
			LOGGER.warn("Exception occurred while getting the members pagination summary: " + e.getMessage());
		}
		int toCount = Math.min((memberPage + 1) * pageSize, subscribedMembers.size());
		int total = subscribedMembers.size();

		if (total == 0)
			return "Showing 0 of 0 Results";

		return "Showing " + toCount + " of " + total + " Results";
	}

	// SORTING METHODS
	// Sort by Ascending arrow method
	public String sortByAsc(String fieldName) {
		try {
			this.currentInsuranceSortColumn = fieldName;
			this.currentInsuranceSortDirection = "asc";
			sortPatientInsuranceList();
			resetInsurancePage();
		} catch (Exception e) {
			LOGGER.warn("Exception occurred while sorting by Ascending: " + e.getMessage());
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not apply sort."));
		}
		return null;
	}

	// Sort by Descending arrow method
	public String sortByDesc(String fieldName) {
		try {
			this.currentInsuranceSortColumn = fieldName;
			this.currentInsuranceSortDirection = "desc";
			sortPatientInsuranceList();
			resetInsurancePage();
		} catch (Exception e) {
			LOGGER.warn("Exception occurred while sorting by Descending: " + e.getMessage());
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not apply sort."));
		}
		return null;
	}

	// Sort by Ascending arrow method
	public String sortByAscMem(String fieldName) {
		try {
			this.currentMemberSortColumn = fieldName;
			this.currentMemberSortDirection = "asc";
			sortViewMemberList();
			resetMemberPage();
		} catch (Exception e) {
			LOGGER.warn("Exception occurred while sorting members ascending: " + e.getMessage());
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not apply member sort."));
		}
		return null;
	}

	// Sort by Descending arrow method
	public String sortByDescMem(String fieldName) {
		try {
			this.currentMemberSortColumn = fieldName;
			this.currentMemberSortDirection = "desc";
			sortViewMemberList();
			resetMemberPage();
		} catch (Exception e) {
			LOGGER.warn("Exception occurred while sorting members descending: " + e.getMessage());
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not apply member sort."));
		}
		return null;
	}

	// Rendering the sort buttons on Insurance page
	public boolean renderSortButton(String column, String direction) {
		if (currentInsuranceSortColumn == null || !currentInsuranceSortColumn.equals(column))
			return true;
		return !currentInsuranceSortDirection.equals(direction);
	}

	// Rendering the sort buttons on Member page
	public boolean renderSortButtonMem(String column, String direction) {
		if (currentMemberSortColumn == null || !currentMemberSortColumn.equals(column))
			return true;
		return !currentMemberSortDirection.equals(direction);
	}

	/**
	 * Actual sorting method for the Insurance sorting
	 */
	@SuppressWarnings("unchecked")
	private void sortPatientInsuranceList() {
		if (patientInsuranceList == null || patientInsuranceList.isEmpty() || currentInsuranceSortColumn == null
				|| currentInsuranceSortColumn.isEmpty()) {
			return;
		}

		try {
			Field field = patientInsuranceList.get(0).getClass().getDeclaredField(currentInsuranceSortColumn);
			field.setAccessible(true);
			boolean ascending = "asc".equalsIgnoreCase(currentInsuranceSortDirection);

			Collections.sort(patientInsuranceList, (a, b) -> {
				try {
					Object valueA = field.get(a);
					Object valueB = field.get(b);

					if (valueA == null && valueB == null)
						return 0;
					if (valueA == null)
						return 1;
					if (valueB == null)
						return -1;

					if (!(valueA instanceof Comparable)) {
						LOGGER.error("Cannot sort column '" + currentInsuranceSortColumn
								+ "' because it is not a Comparable type.");
						return 0;
					}

					// Comparison here
					Comparable<Object> comparableA = (Comparable<Object>) valueA;
					int comparison = comparableA.compareTo(valueB);
					return ascending ? comparison : -comparison;

				} catch (Exception e) {
					LOGGER.error("Internal sorting error for column " + currentInsuranceSortColumn, e);
					return 0;
				}
			});

		} catch (NoSuchFieldException e) {
			LOGGER.error("Sort column '" + currentInsuranceSortColumn + "' not found.", e);
		} catch (Exception e) {
			LOGGER.warn("Exception occurred while sorting Insurance table: " + e.getMessage());
		}
	}

	/**
	 * Actual Sorting method for the Member sorting
	 */
	@SuppressWarnings("unchecked")
	private void sortViewMemberList() {
		if (currentMemberSortColumn == null || subscribedMembers == null || subscribedMembers.isEmpty()) {
			return;
		}

		try {
			Field field = subscribedMembers.get(0).getClass().getDeclaredField(currentMemberSortColumn);
			field.setAccessible(true);
			boolean ascending = "asc".equalsIgnoreCase(currentMemberSortDirection);

			Collections.sort(subscribedMembers, (a, b) -> {
				try {
					Object valueA = field.get(a);
					Object valueB = field.get(b);

					if (valueA == null && valueB == null)
						return 0;
					if (valueA == null)
						return 1;
					if (valueB == null)
						return -1;

					if (!(valueA instanceof Comparable)) {
						LOGGER.error("Cannot sort column '" + currentMemberSortColumn
								+ "' because it is not a Comparable type.");
						return 0;
					}

					Comparable<Object> comparableA = (Comparable<Object>) valueA;
					int comparison = comparableA.compareTo(valueB);
					return ascending ? comparison : -comparison;

				} catch (Exception e) {
					LOGGER.error("Internal sorting error for column " + currentInsuranceSortColumn, e);
					return 0;
				}
			});

		} catch (NoSuchFieldException e) {
			LOGGER.error("Sort column '" + currentInsuranceSortColumn + "' not found.", e);
		}
	}

	// Reset button for clearing all the data chunks on the page
	public String resetFilter() {
		FacesContext context = FacesContext.getCurrentInstance();
		try {
			this.insurancePage = 0;
			this.fromDate = null;
			this.toDate = null;
			this.selectedStatus = "ALL";
			this.planNameSearchInput = null;
			originalInsuranceList = insuranceDao.showInsuranceOfRecipient(gethId());
			if (originalInsuranceList == null) {
				originalInsuranceList = Collections.emptyList();
			}
			this.currentInsuranceSortColumn = null;
			this.currentInsuranceSortDirection = null;
			applyFilters();
		} catch (HibernateException e) {
			LOGGER.error(
					"Controller: Database error resetting filters for recipient " + gethId() + ": " + e.getMessage(),
					e);
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Database Error",
					"Failed to reset filters due to a technical issue. Please try again."));
			patientInsuranceList = Collections.emptyList();
			originalInsuranceList = Collections.emptyList();
		} catch (IllegalArgumentException e) {
			LOGGER.warn("Controller: Data integrity issue resetting filters for recipient " + gethId()
					+ ": Invalid enum value from DB: " + e.getMessage(), e);
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Data Issue",
					"Filters might not have fully reset due to invalid data in our system."));
			patientInsuranceList = Collections.emptyList();
			originalInsuranceList = Collections.emptyList();
		} catch (Exception e) {
			LOGGER.warn("Exception occurred while resetting the filters: " + e.getMessage());
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error resetting filters.", null));
		}
		return null;
	}

	// Getter and setter methods
	public String getPlanNameSearchInput() {
		return planNameSearchInput;
	}

	public void setPlanNameSearchInput(String planNameSearchInput) {
		this.planNameSearchInput = planNameSearchInput;
	}

	public String gethId() {
		if (hId == null) {
			Recipient loggedInRecipient = (Recipient) FacesContext.getCurrentInstance().getExternalContext()
					.getSessionMap().get("loggedInRecipient");
			if (loggedInRecipient != null) {
				hId = loggedInRecipient.gethId();
			}
		}
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
		if (fullName == null) {
			Recipient loggedInRecipient = (Recipient) FacesContext.getCurrentInstance().getExternalContext()
					.getSessionMap().get("loggedInRecipient");
			if (loggedInRecipient != null) {
				fullName = loggedInRecipient.getFullName();
			}
		}
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

	public Recipient getRecipient() {
		return recipient;
	}

	public void setRecipient(Recipient recipient) {
		this.recipient = recipient;
	}

	public RecipientInsuranceDTO getSelectedItem() {
		return selectedItem;
	}

	public void setSelectedItem(RecipientInsuranceDTO selectedItem) {
		this.selectedItem = selectedItem;
	}

	public ShowInsuranceDao getInsuranceDao() {
		return insuranceDao;
	}

	public void setInsuranceDao(ShowInsuranceDao insuranceDao) {
		this.insuranceDao = insuranceDao;
	}

	public List<RecipientInsuranceDTO> getPatientInsuranceList() {
		return patientInsuranceList;
	}

	public void setPatientInsuranceList(List<RecipientInsuranceDTO> patientInsuranceList) {
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

	// The getter name you need to use in the JSP.
	public int getCurrentMemberPage() {
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
	 * Action to return to insurance listing. * @return navigation string to
	 * ShowInsurance page
	 */
	public String goBackinc() {
		return "ShowInsurance";
	}

	/**
	 * Reset all the field before redirecting to other page
	 * 
	 * @return the respective page
	 */
	public String resetAndRedirect(String page) {
		resetFilter();
		return page + "?faces-redirect=true";
	}

}