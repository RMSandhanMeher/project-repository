/// Copyright © 2025 Infinite Computer Solution. All rights reserved. 

package com.infinite.jsf.recipient.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;

import com.infinite.jsf.provider.model.Doctors;
import com.infinite.jsf.recipient.dao.SearchDoctorDao;
import com.infinite.jsf.recipient.daoImpl.SearchDoctorDaoImpl;

/**
 * Controller for searching doctors by name, specialization, or address.
 * Supports filtering, validation, pagination, and sorting.
 */
public class RecipientSearchDoctorController implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(RecipientSearchDoctorController.class.getName());

	private String searchBy = "doctorName";
	private String searchValue;
	private String selectedSpecialization;
	private String searchMode = "exact";

	private List<SelectItem> searchOptions;
	private List<SelectItem> specializationOptions;

	private List<Doctors> searchResults = new ArrayList<>();

	private int currentPage = 0;
	private final int pageSize = 4;

	private String sortField = "doctorName";
	private boolean ascending = true;

	private boolean searchPerformed = false;
	private boolean initialized = false;

	private String currentSortColumn = "doctorName";
	private String currentSortOrder = "asc";

	private SearchDoctorDao doctorDAO = new SearchDoctorDaoImpl();

	/**
	 * Constructor which initializes search options.
	 */
	public RecipientSearchDoctorController() {
		if (!initialized) {
			getSearchOptions();
		}
	}

	/**
	 * Gets the list of search options (doctor name, address, specialization).
	 */
	public List<SelectItem> getSearchOptions() {
		try {
			if (searchOptions == null) {
				searchOptions = new ArrayList<>();
				searchOptions.add(new SelectItem("doctorName", "Doctor Name"));
				searchOptions.add(new SelectItem("specialization", "Specialization"));
				searchOptions.add(new SelectItem("address", "Address"));

				loadSpecializationOptions();
				initialized = true;
			}
		} catch (Exception e) {
			LOGGER.warn("getSearchOptions: Exception occurred - " + e.getMessage());
		}
		return searchOptions;
	}

	/**
	 * Resets UI fields when search type is changed.
	 */
	public void searchByChanged() {
		try {
			LOGGER.info("searchByChanged: changed to " + this.searchBy);

			if ("doctorName".equals(searchBy) || "address".equals(searchBy)) {
				this.searchMode = null;
			}

			searchValue = null;
			selectedSpecialization = "";
			searchResults = new ArrayList<>();
			searchPerformed = false;
			currentPage = 0;
		} catch (Exception e) {
			LOGGER.warn("searchByChanged: Exception occurred - " + e.getMessage());
		}
	}

	/**
	 * Executes the doctor search based on form parameters.
	 */
	public void executeSearch() {
		FacesContext context = FacesContext.getCurrentInstance();
		searchPerformed = true;

		try {
			String currentSearchValue = "specialization".equals(searchBy) ? selectedSpecialization : searchValue;

			if (currentSearchValue == null || currentSearchValue.trim().isEmpty()) {
				String messageId = "specialization".equals(searchBy) ? "searchForm:specializationDropdown"
						: "searchForm:searchValueInput";
				String message = "specialization".equals(searchBy) ? "Please select a specialization from the dropdown."
						: "Please provide search criteria.";

				context.addMessage(messageId, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
				searchResults = new ArrayList<>();
				return;
			}

			currentSearchValue = currentSearchValue.trim().replaceAll("\\s{2,}", " ");

			// Validation
			if ("doctorName".equals(searchBy) && !Pattern.matches("^[a-zA-Z. ]+$", currentSearchValue)) {
				context.addMessage("searchForm:searchValueInput", new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"Doctor Name can only contain letters, spaces, and dot (e.g., Dr.).", null));
				searchResults = new ArrayList<>();
				return;
			} else if ("address".equals(searchBy) && !Pattern.matches("^[a-zA-Z0-9.,\\s#/\\-]+$", currentSearchValue)) {
				context.addMessage("searchForm:searchValueInput", new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"Address can only contain letters, numbers, commas, #, /, and -.", null));
				searchResults = new ArrayList<>();
				return;
			}

			LOGGER.info(
					"executeSearch: Searching by " + searchBy + " Value=" + currentSearchValue + " Mode=" + searchMode);

			// Dispatch to DAO
			switch (searchBy) {
			
			case "doctorName":
				if ("startsWith".equals(searchMode)) {
					searchResults = doctorDAO.findDoctorsByNameStartsWith(currentSearchValue);
				} else if ("contains".equals(searchMode)) {
					searchResults = doctorDAO.findDoctorsByNameContains(currentSearchValue);
				} else {
					searchResults = doctorDAO.searchDoctors(searchBy, currentSearchValue);
				}
				break;
				
			case "address":
				if ("startsWith".equals(searchMode)) {
					searchResults = doctorDAO.findDoctorsByAddressStartsWith(currentSearchValue);
				} else if ("contains".equals(searchMode)) {
					searchResults = doctorDAO.findDoctorsByAddressContains(currentSearchValue);
				} else {
					searchResults = doctorDAO.searchDoctors(searchBy, currentSearchValue);
				}
				break;
				
			case "specialization":
				
			default:
				searchResults = doctorDAO.searchDoctors(searchBy, currentSearchValue);
				break;
			}

			LOGGER.info("executeSearch: Results found = " + (searchResults != null ? searchResults.size() : 0));

			if (searchResults == null || searchResults.isEmpty()) {
				if ("doctorName".equals(searchBy) && (searchMode == null || "exact".equals(searchMode))) {
					context.addMessage("searchForm:searchValueInput", new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"No doctor found with that exact name. Please enter the full and correct name.", null));
				} else {
					context.addMessage("searchForm:searchValueInput", new FacesMessage(FacesMessage.SEVERITY_WARN,
							"No doctors found matching your search criteria.", null));
				}
				searchResults = new ArrayList<>();
			}

			sortResults();
			currentPage = 0;

		} catch (Exception e) {
			LOGGER.warn("executeSearch: Unexpected error - " + e.getMessage());
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"An unexpected error occurred during search. Please try again.", null));
			searchResults = new ArrayList<>();
		}
	}

	/**
	 * Paginates the current list of search results.
	 * 
	 * @return Paginated list
	 */
	public List<Doctors> getPaginatedDoctors() {
		try {
			if (searchResults == null || searchResults.isEmpty())
				return new ArrayList<>();

			int fromIndex = currentPage * pageSize;
			int toIndex = Math.min(fromIndex + pageSize, searchResults.size());

			if (fromIndex >= searchResults.size()) {
				currentPage = 0;
				fromIndex = 0;
				toIndex = Math.min(pageSize, searchResults.size());
			}

			return searchResults.subList(fromIndex, toIndex);
		} catch (Exception e) {
			LOGGER.warn("getPaginatedDoctors: Exception - " + e.getMessage());
			return new ArrayList<>();
		}
	}

	/** Move to next result page */
	public void nextPage() {
		if ((currentPage + 1) * pageSize < searchResults.size()) {
			currentPage++;
		}
	}

	/** Move to previous result page */
	public void prevPage() {
		if (currentPage > 0) {
			currentPage--;
		}
	}

	/**
	 * Sort doctor result list ascending by field.
	 */
	public void sortByAsc(String field) {
		this.sortField = field;
		this.ascending = true;
		this.currentSortColumn = field;
		this.currentSortOrder = "asc";
		sortResults();
		currentPage = 0;
	}

	/**
	 * Sort doctor result list descending by field.
	 */
	public void sortByDesc(String field) {
		this.sortField = field;
		this.ascending = false;
		this.currentSortColumn = field;
		this.currentSortOrder = "desc";
		sortResults();
		currentPage = 0;
	}

	/**
	 * Applies actual comparator-based sorting logic using field.
	 */
	private void sortResults() {
		try {
			if (searchResults == null || searchResults.isEmpty())
				return;

			Comparator<Doctors> comparator;
			switch (sortField) {
			case "specialization":
				comparator = Comparator.comparing(Doctors::getSpecialization,
						Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
				break;
			case "address":
				comparator = Comparator.comparing(Doctors::getAddress,
						Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
				break;
			case "email":
				comparator = Comparator.comparing(Doctors::getEmail,
						Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
				break;
			case "status":
				comparator = Comparator.comparing(d -> d.getStatus() != null ? d.getStatus().toString() : "",
						String.CASE_INSENSITIVE_ORDER);
				break;
			case "type":
				comparator = Comparator.comparing(d -> d.getType() != null ? d.getType().toString() : "",
						String.CASE_INSENSITIVE_ORDER);
				break;
			default:
				comparator = Comparator.comparing(Doctors::getDoctorName,
						Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
			}

			if (!ascending) {
				comparator = comparator.reversed();
			}

			searchResults.sort(comparator);

		} catch (Exception e) {
			LOGGER.warn("sortResults: Sorting exception - " + e.getMessage());
		}
	}

	/** Returns a user-friendly pagination summary */
	public String getPaginationDocSummary() {
		try {
			if (searchResults == null || searchResults.isEmpty())
				return "";
		} catch (Exception e) {
			LOGGER.warn("getPaginationDocSummary: Exception - " + e.getMessage());
			return "";
		}

		int from = Math.min((currentPage + 1) * pageSize, searchResults.size());
		int total = searchResults.size();
		return "Showing " + from + " of " + total + " Results";
	}

	/**
	 * Determines whether to show a specific sort button.
	 */
	public boolean renderSortButton(String column, String order) {
		try {
			if (currentSortColumn == null || !currentSortColumn.equals(column)) {
				return true;
			}
		} catch (Exception e) {
			LOGGER.warn("renderSortButton: Exception - " + e.getMessage());
		}
		return !currentSortOrder.equals(order);
	}

	/**
	 * Resets filters, sorting, and values to default.
	 */
	public String resetSearch() {
		try {
			this.searchBy = "doctorName";
			this.searchValue = null;
			this.selectedSpecialization = "";
			this.searchResults = new ArrayList<>();
			this.currentPage = 0;
			this.searchPerformed = false;
			this.sortField = "doctorName";
			this.ascending = true;
			this.currentSortColumn = "doctorName";
			this.currentSortOrder = "asc";
			this.searchMode = "exact";

			LOGGER.info("resetSearch: Search reset.");
		} catch (Exception e) {
			LOGGER.warn("resetSearch: Exception occurred - " + e.getMessage());
		}
		return "findDoctor";
	}

	// ---------------------------
	// Specialization Helper
	// ---------------------------
	public List<SelectItem> getSpecializationOptions() {
		try {
			if (specializationOptions == null || specializationOptions.isEmpty()) {
				loadSpecializationOptions();
			}
		} catch (Exception e) {
			LOGGER.warn("getSpecializationOptions: Exception - " + e.getMessage());
		}
		return specializationOptions;
	}

	/**
	 * Loads dynamic specialization options into the dropdown.
	 */
	private void loadSpecializationOptions() {
		specializationOptions = new ArrayList<>();
		specializationOptions.add(new SelectItem("", "• Select Specialization •"));
		try {
			List<String> specializations = doctorDAO.fetchAllSpecialization();
			if (specializations != null && !specializations.isEmpty()) {
				specializations.sort(String.CASE_INSENSITIVE_ORDER);
				for (String spec : specializations) {
					if (spec != null && !spec.trim().isEmpty()) {
						specializationOptions.add(new SelectItem(spec, spec));
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("loadSpecializationOptions: Error fetching specializations - " + e.getMessage());
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Error loading specializations. Please try again.", null));
		}
	}

	/**
	 * Placeholder for unimplemented booking logic.
	 */
	public String bookDummy() {
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_INFO, "Booking functionality is not yet implemented.", null));
		return null;
	}

	// ------------------------
	// Getters & Setters
	// ------------------------

	public String getSearchBy() {
		return searchBy;
	}

	public void setSearchBy(String searchBy) {
		this.searchBy = searchBy;
	}

	public String getSearchValue() {
		return searchValue;
	}

	public void setSearchValue(String searchValue) {
		this.searchValue = searchValue;
	}

	public String getSelectedSpecialization() {
		return selectedSpecialization;
	}

	public void setSelectedSpecialization(String selectedSpecialization) {
		this.selectedSpecialization = selectedSpecialization;
	}

	public String getSearchMode() {
		return searchMode;
	}

	public void setSearchMode(String searchMode) {
		this.searchMode = searchMode;
	}

	public List<Doctors> getSearchResults() {
		return searchResults;
	}

	public void setSearchResults(List<Doctors> searchResults) {
		this.searchResults = searchResults;
	}

	public int getCurrentPage() {
		return currentPage + 1;
	}

	public boolean isHasNextPage() {
		return searchResults != null && (currentPage + 1) * pageSize < searchResults.size();
	}

	public boolean isHasPrevPage() {
		return currentPage > 0;
	}

	public int getTotalPages() {
		if (searchResults == null || searchResults.isEmpty())
			return 0;
		return (searchResults.size() + pageSize - 1) / pageSize;
	}

	public String getSortField() {
		return sortField;
	}

	public boolean isAscending() {
		return ascending;
	}

	public boolean isSearchPerformed() {
		return searchPerformed;
	}

	public String getCurrentSortColumn() {
		return currentSortColumn;
	}

	public String getCurrentSortOrder() {
		return currentSortOrder;
	}
}