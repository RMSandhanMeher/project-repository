/// Copyright © 2025 Infinite Computer Solution. All rights reserved. 

package com.infinite.jsf.recipient.controller;

import java.util.ArrayList;
import java.util.Collections; // Import Collections for emptyList
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infinite.jsf.provider.model.Doctors;
import com.infinite.jsf.recipient.customException.ProviderSearchException;
import com.infinite.jsf.recipient.dao.SearchDoctorDao;
import com.infinite.jsf.recipient.daoImpl.SearchDoctorDaoImpl;

/**
 * Controller for searching doctors by name, specialization, or address.
 * Supports filtering, validation, pagination, and sorting.
 */
public class RecipientSearchDoctorController {

	private static final Logger LOGGER=LogManager.getLogger(RecipientSearchDoctorController.class);

	private String searchBy = "doctorName";
	private String searchValue;
	private String selectedSpecialization;
	private String searchMode = "exact"; // Initialized to exact match

	private List<SelectItem> searchOptions; // Holds the option like Doctor Name, Specialization & Address
	private List<SelectItem> specializationOptions; //Specialization stored in list fetched from the DB 

	private List<Doctors> searchResults = new ArrayList<>();

	private int currentPage = 0;
	private final int pageSize = 4;

	private String sortField = "doctorName";
	private boolean ascending = true;

	private boolean searchPerformed = false;
	private boolean initialized = false;

	private String currentSortColumn;
	private String currentSortDirection;

	// To simplify dependency management and directly instantiate the DAO.
	private SearchDoctorDao doctorDAO = new SearchDoctorDaoImpl();

	/**
	 * Constructor which initializes search options.
	 */
	public RecipientSearchDoctorController() {
		if (!initialized) {
			getSearchOptions(); 
		}
	}
	
	public static FacesContext context;
	
	

	/**
	 * Gets the list of search options (doctor name, address, specialization).
	 */
	public List<SelectItem> getSearchOptions() {
		if (searchOptions == null) {
			searchOptions = new ArrayList<>();
			searchOptions.add(new SelectItem("doctorName", "Doctor Name"));
			searchOptions.add(new SelectItem("specialization", "Specialization"));
			searchOptions.add(new SelectItem("address", "Address"));

			try {
				loadSpecializationOptions();
			} 
			catch (ProviderSearchException e) {
				LOGGER.error("possible Error while connecting to fetch all the distinct specialization from Databse");
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"An unexpected error occured while retrieving the specializations",e.getMessage()));

			} catch (Exception e) {
				LOGGER.error("Error fetching all the distinct specialization");
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"An unexpected error occured while retrieving the specializations",e.getMessage()));
			}
			initialized = true;
		}
		return searchOptions;
	}

//	/**
//	 * Resets UI fields when search type is changed.
//	 */
//	public void searchByChanged() {
//		LOGGER.info("searchByChanged: changed to " + this.searchBy);
//	}
	
	
	/**
	 * Used for handling exposure of fetched specialization from loadSpecializationOptions()
	 */
	public List<SelectItem> getSpecializationOptions() {
		if (specializationOptions == null || specializationOptions.isEmpty()) {
			loadSpecializationOptions();
		}
		return specializationOptions;
	}

	/**
	 * Loads dynamic specialization options into the dropdown and populate all spec. available in the DB
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
		} 
		catch (ProviderSearchException e) {
			LOGGER.error("loadSpecializationOptions: Database error fetching specializations - " + e.getMessage(), e);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Error loading specializations due to a database issue. Please try again.", null));
			specializationOptions = new ArrayList<>(); 
			specializationOptions.add(new SelectItem("", "• Error Loading •"));
		} catch (Exception e) { 
			LOGGER.error("loadSpecializationOptions: Unexpected error fetching specializations - " + e.getMessage(), e);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"An unexpected error occurred loading specializations. Please try again.", null));
			specializationOptions = new ArrayList<>(); 
			specializationOptions.add(new SelectItem("", "• Error Loading •")); 
		}
	}

	
	
	
	
	
	
	/**
	 * Executes the doctor search based on form parameters.
	 */
	public void executeSearch() {
		this.currentSortDirection=null;
		this.currentSortColumn = null;
		this.ascending = true;
		FacesContext context = FacesContext.getCurrentInstance();
		searchPerformed = true;

		try {
//			JSF assigns the chosen value to selectedSpecialization.
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
			}
			
			else if ("address".equals(searchBy) && !Pattern.matches("^[a-zA-Z0-9.,\\s#/\\-]+$", currentSearchValue)) {
				context.addMessage("searchForm:searchValueInput", new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"Address can only contain letters, numbers, commas", null));
				searchResults = new ArrayList<>();
				return;
			}
			
			else if ("doctorName".equals(searchBy) || "address".equals(searchBy)) {
				if(currentSearchValue.length()<2) {
					
					context.addMessage("searchForm:searchValueInput", new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"A minimum of 2 character needed for the search",null));
					searchResults = new ArrayList<>();
					return;
				}
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
				}

				if ("address".equals(searchBy) && (searchMode == null) || "exact".equals(searchMode)) {
					context.addMessage("searchForm:searchValueInput", new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"No address found with the exact name. Please enter full and correct name", null));
				} else {
					context.addMessage("searchForm:searchValueInput", new FacesMessage(FacesMessage.SEVERITY_WARN,
							"No doctors found matching your search criteria.", null));
				}
				searchResults = new ArrayList<>();
			}

			sortResults();

			currentPage = 0;

		}

		catch (ProviderSearchException e) {
			LOGGER.error("executeSearch: Database error during search: " + e.getMessage(), e);
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"A database error occurred during search. Please try again later.", null));
			searchResults = Collections.emptyList();
		}

		catch (Exception e) {
			LOGGER.warn("executeSearch: Unexpected error - " + e.getMessage());
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"An unexpected error occurred during search. Please try again.", null));
			searchResults = new ArrayList<>();
		}
	}
			
			
			
			
			
			
			
			
	public List<Doctors> getPaginatedDoctors() {
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
	}

	public void nextPage() {
		if (searchResults != null && (currentPage + 1) * pageSize < searchResults.size()) {
			currentPage++;
		}
	}

	public void prevPage() {
		if (currentPage > 0) {
			currentPage--;
		}
	}

	public void sortByAsc(String field) {
		this.sortField = field;
		this.ascending = true;
		this.currentSortColumn = field;
		this.currentSortDirection = "asc";
		sortResults();
		currentPage = 0;
	}

	public void sortByDesc(String field) {
		this.sortField = field;
		this.ascending = false;
		this.currentSortColumn = field;
		this.currentSortDirection = "desc";
		sortResults();
		currentPage = 0;
	}
	
	private void sortResults() {
		if (searchResults == null || searchResults.isEmpty())
			return;

		Comparator<Doctors> comparator;
		switch (sortField) {
		case "specialization":
			comparator = Comparator.comparing(Doctors::getSpecialization,
					Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
			break;
		case "address":
			comparator = Comparator.comparing(Doctors::getAddress, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
			break;
		case "email":
			comparator = Comparator.comparing(Doctors::getEmail, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
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
	}
	
	public String getPaginationDocSummary() {
		if (searchResults == null || searchResults.isEmpty())
			return "";

		int from = Math.min((currentPage + 1) * pageSize, searchResults.size());
		int total = searchResults.size();
		return "Showing " + from + " of " + total + " Results";
	}
	
	public boolean renderSortButton(String column, String order) {
		if (currentSortColumn == null || !currentSortColumn.equals(column)) {
			return true;
		}
		return !currentSortDirection.equals(order);
	}
	
	public String resetSearch() {
		this.searchBy = "doctorName";
		this.searchValue = null;
		this.selectedSpecialization = "";
		this.searchResults = new ArrayList<>();
		this.currentPage = 0;
		this.searchPerformed = false;
		this.sortField = "doctorName";
		this.ascending = true;
		this.currentSortColumn = "doctorName";
		this.currentSortDirection = "asc";
		this.searchMode = "exact"; // Aligned with the UI radio button
		LOGGER.info("resetSearch: Search reset.");
		return "findDoctor";
	}

	// Getters & Setters
	public String getSearchBy() {
		return searchBy;
	}

	public void setSearchBy(String searchBy) {
		String oldSearchBy = this.searchBy;
		this.searchBy = searchBy; 

		if (oldSearchBy != null && !oldSearchBy.equals(this.searchBy)) {
			if (this.searchBy.equals("specialization")) {
				this.searchValue = null;
				this.searchMode = null; 
			} else {
				this.selectedSpecialization = null;
				this.searchMode = "exact"; 
			}
			
			this.searchResults = new ArrayList<>();
			this.currentPage = 0;
			this.searchPerformed = false;
			clearInputMessages(); 
		}
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

	public String getcurrentSortDirection() {
		return currentSortDirection;
	}
	
	private void clearInputMessages() {
        FacesContext context = FacesContext.getCurrentInstance();
        while(context.getMessages("searchForm:searchValueInput").hasNext()) {
            context.getMessages("searchForm:searchValueInput").next();
        }
        while(context.getMessages("searchForm:specializationDropdown").hasNext()) {
            context.getMessages("searchForm:specializationDropdown").next();
        }
    }
}