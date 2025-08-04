/*
* -----------------------------------------------------------------------------
* Copyright © 2025 Infinite Computer Solution. All rights reserved.
* -----------------------------------------------------------------------------
*
* @Author   : Sandhan Meher
* @Purpose  : This class serves as the controller for managing appointment
* details and operations from the recipient's perspective. It handles fetching,
* filtering, sorting, and pagination of upcoming and past appointments.
* It also provides functionality for cancelling appointments and sending
* corresponding email notifications.
*
* -----------------------------------------------------------------------------
*/
package com.infinite.jsf.provider.controller;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.*;
import java.util.Comparator;
import java.util.Date; // Import Date

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

import com.infinite.jsf.provider.daoImpl.AppointmentDaoImpl;
import com.infinite.jsf.provider.daoImpl.DoctorDaoImpl;
import com.infinite.jsf.provider.dto.AppointmentSlip;
import com.infinite.jsf.provider.model.Appointment;
import com.infinite.jsf.provider.model.AppointmentStatus;
import com.infinite.jsf.provider.model.Doctors;
import com.infinite.jsf.recipient.model.Recipient;
import com.infinite.jsf.util.MailSend;

@Named
@ViewScoped
public class RecipientAppointmentController implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(RecipientAppointmentController.class.getName());

	private final AppointmentDaoImpl appointmentDao = new AppointmentDaoImpl();

	private String hId;
	private Recipient recipient;
	private List<Appointment> upcomingAppointments = new ArrayList<>();
	private List<Appointment> pastAppointments = new ArrayList<>();
	private List<Appointment> filteredAppointments = new ArrayList<>();
	private List<Appointment> paginatedAppointments = new ArrayList<>();

	private Map<String, Boolean> cancellableMap = new HashMap<>();

	private Appointment selectedAppointment;

	private String timeFilterType = "future";
	private String statusFilterType = "ALL";

	// NEW: Date filter properties
	private Date fromDate;
	private Date toDate;

	private final int pageSize = 5;
	private int currentPage = 0; // 0-based for sublist operations

	private String sortField = "start";
	private boolean ascending = true;
	private String currentSortColumn = "start";
	private String currentSortOrder = "asc";

	/**
	 * Initializes the controller after its construction. This method is annotated with {@code @PostConstruct}
	 * and is called by the JSF runtime. It loads the recipient's appointments.
	 */
	@PostConstruct
	public void init() {
		LOGGER.info("RecipientAppointmentController initialized.");
		loadAppointments();
	}

	/**
	 * Loads all upcoming and past appointments for the logged-in recipient from the database.
	 * It also initializes filtering and sorting based on default or previously set criteria.
	 * If no recipient is found in the session, it redirects to the login page.
	 */
	public void loadAppointments() {
		try {
			recipient = ((Recipient) FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
					.get("loggedInRecipient"));
			if (recipient == null) {
				LOGGER.warn("No logged-in recipient found in session. Redirecting to login.");
				ExternalContext e = FacesContext.getCurrentInstance().getExternalContext();
				e.redirect(e.getRequestContextPath() + "/recipient/Login.jsf");
				return;
			}
			hId = recipient.gethId();
			upcomingAppointments = appointmentDao.getUpcomingAppointmentsByRecipient(hId);
			pastAppointments = appointmentDao.getPastAppointmentsByRecipient(hId);
			LOGGER.info("Loaded " + upcomingAppointments.size() + " upcoming and " + pastAppointments.size()
					+ " past appointments");
			updateFilteredAndSortedAppointments();
			LOGGER.info("After filtering: " + filteredAppointments.size() + " appointments");
		} catch (Exception e) {
			LOGGER.error("Error loading appointments: " + e.getMessage(), e);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Error loading appointments. Please try again.", null));
			upcomingAppointments.clear();
			pastAppointments.clear();
			filteredAppointments.clear();
			paginatedAppointments.clear();
			currentPage = 0;
		}
	}

	/**
	 * Updates the list of {@code filteredAppointments} based on the current {@code timeFilterType},
	 * {@code statusFilterType}, {@code fromDate}, and {@code toDate}. After filtering, it applies
	 * the current sorting criteria and resets pagination if necessary. This method is called
	 * whenever a filter or sort option changes.
	 */
	public void updateFilteredAndSortedAppointments() {
		LOGGER.info("updateFilteredAndSortedAppointments: Time Filter=" + timeFilterType + ", Status Filter="
				+ statusFilterType + ", From Date=" + fromDate + ", To Date=" + toDate);
		List<Appointment> baseList = "past".equalsIgnoreCase(timeFilterType) ? pastAppointments : upcomingAppointments;

		filteredAppointments = new ArrayList<>();
		cancellableMap.clear();

		for (Appointment appt : baseList) {
			boolean matchStatus = "ALL".equalsIgnoreCase(statusFilterType)
					|| (appt.getStatus() != null && appt.getStatus().name().equalsIgnoreCase(statusFilterType));

			// NEW: Date range filtering logic
			boolean matchDateRange = true;
			if (fromDate != null && appt.getStart() != null) {
				// Check if appointment start date is ON OR AFTER fromDate
				matchDateRange = !appt.getStart().before(new Timestamp(fromDate.getTime()));
			}
			if (toDate != null && appt.getStart() != null) {
				// Check if appointment start date is ON OR BEFORE toDate (end of day)
				Calendar c = Calendar.getInstance();
				c.setTime(toDate);
				c.set(Calendar.HOUR_OF_DAY, 23);
				c.set(Calendar.MINUTE, 59);
				c.set(Calendar.SECOND, 59);
				c.set(Calendar.MILLISECOND, 999);
				matchDateRange = matchDateRange && !appt.getStart().after(new Timestamp(c.getTimeInMillis()));
			}

			if (matchStatus && matchDateRange) { // Combine all filters
				filteredAppointments.add(appt);
				cancellableMap.put(appt.getAppointmentId(), isCancellable(appt));
			}
		}

		sortFilteredAppointments();

		// Adjust current page if current page index is out of bounds after filtering
		if (currentPage * pageSize >= filteredAppointments.size() && filteredAppointments.size() > 0) {
			currentPage = (int) Math.floor((double) (filteredAppointments.size() - 1) / pageSize);
		} else if (filteredAppointments.isEmpty()) {
			currentPage = 0;
		}

		updatePaginatedAppointments();
	}

	/**
	 * Sorts the {@code filteredAppointments} list based on the current {@code sortField}
	 * and {@code ascending} order. Supports sorting by doctor name, status, specialization,
	 * appointment ID, and start time.
	 */
	private void sortFilteredAppointments() {
		try {
			if (filteredAppointments == null || filteredAppointments.isEmpty()) {
				return;
			}

			Comparator<Appointment> comparator;
			switch (sortField) {
			case "doctorName":
				comparator = Comparator.comparing(
						appt -> appt.getDoctor() != null ? appt.getDoctor().getDoctorName() : "",
						Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
				break;
			case "status":
				comparator = Comparator.comparing(appt -> appt.getStatus() != null ? appt.getStatus().toString() : "",
						Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
				break;
			case "specialization":
				comparator = Comparator.comparing(
						appt -> appt.getDoctor() != null ? appt.getDoctor().getSpecialization() : "",
						Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
				break;
			case "appointmentId":
				comparator = Comparator.comparing(Appointment::getAppointmentId,
						Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
				break;
			case "start":
			default:
				comparator = Comparator.comparing(Appointment::getStart,
						Comparator.nullsLast(Comparator.naturalOrder()));
			}

			if (!ascending) {
				comparator = comparator.reversed();
			}

			filteredAppointments.sort(comparator);
			LOGGER.info("Appointments sorted by " + sortField + " in " + (ascending ? "ascending" : "descending")
					+ " order.");

		} catch (Exception e) {
			LOGGER.warn("sortFilteredAppointments: Sorting exception - " + e.getMessage(), e);
		}
	}

	/**
	 * Sets the sorting field and order to ascending, then triggers a re-filter
	 * and re-pagination of appointments.
	 *
	 * @param field The field name to sort by (e.g., "doctorName", "start").
	 */
	public void sortByAsc(String field) {
		this.sortField = field;
		this.ascending = true;
		this.currentSortColumn = field;
		this.currentSortOrder = "asc";
		currentPage = 0; // Reset pagination on new sort
		updateFilteredAndSortedAppointments();
		LOGGER.info("Sorting by " + field + " (Ascending)");
	}

	/**
	 * Sets the sorting field and order to descending, then triggers a re-filter
	 * and re-pagination of appointments.
	 *
	 * @param field The field name to sort by (e.g., "doctorName", "start").
	 */
	public void sortByDesc(String field) {
		this.sortField = field;
		this.ascending = false;
		this.currentSortColumn = field;
		this.currentSortOrder = "desc";
		currentPage = 0; // Reset pagination on new sort
		updateFilteredAndSortedAppointments();
		LOGGER.info("Sorting by " + field + " (Descending)");
	}

	/**
	 * Determines whether a sort button for a specific column and order should be rendered.
	 * This helps to hide the active sort button and show only the alternative sort option.
	 *
	 * @param column The name of the column.
	 * @param order The sort order ("asc" or "desc").
	 * @return {@code true} if the sort button should be rendered, {@code false} otherwise.
	 */
	public boolean renderSortButton(String column, String order) {
		if (currentSortColumn == null || !currentSortColumn.equals(column)) {
			return true; // Render if it's not the currently sorted column
		}
		return !currentSortOrder.equals(order); // Render if it's the current column but opposite order
	}

	/**
	 * Updates the {@code paginatedAppointments} list based on the current {@code currentPage}
	 * and {@code pageSize}. This method is called after filtering/sorting or pagination changes.
	 */
	public void updatePaginatedAppointments() {
		if (filteredAppointments == null || filteredAppointments.isEmpty()) {
			paginatedAppointments = new ArrayList<>();
			return;
		}

		int fromIndex = currentPage * pageSize;
		int toIndex = Math.min(fromIndex + pageSize, filteredAppointments.size());

		// Ensure fromIndex does not exceed list size if filtered list becomes smaller
		fromIndex = Math.min(fromIndex, filteredAppointments.size());

		paginatedAppointments = new ArrayList<>(filteredAppointments.subList(fromIndex, toIndex));

		LOGGER.debug("Updated pagination - Page: " + (currentPage + 1) + ", Items: " + paginatedAppointments.size()
				+ ", Total: " + filteredAppointments.size());
	}

	/**
	 * Navigates to the next page of appointments if available.
	 */
	public void nextPage() {
		if (isHasNextPage()) {
			currentPage++;
			updatePaginatedAppointments();
			LOGGER.info("Navigated to page " + (currentPage + 1));
		}
	}

	/**
	 * Navigates to the previous page of appointments if available.
	 */
	public void prevPage() {
		if (isHasPrevPage()) {
			currentPage--;
			updatePaginatedAppointments();
			LOGGER.info("Navigated to page " + (currentPage + 1));
		}
	}

	/**
	 * Resets the pagination to the first page.
	 */
	public void resetPagination() {
		LOGGER.info("reset method ");
		currentPage = 0;
		updatePaginatedAppointments();
	}

	/**
	 * Navigates directly to a specified page number.
	 *
	 * @param page The 1-based page number to navigate to.
	 */
	public void goToPage(int page) {
		int zeroBasedPage = page - 1;
		int maxPage = getTotalPages() - 1;
		currentPage = Math.max(0, Math.min(zeroBasedPage, maxPage));
		updatePaginatedAppointments();
	}

	/**
	 * Checks if a given appointment is cancellable. An appointment is cancellable
	 * if its start time is in the future and its status is either BOOKED or PENDING.
	 *
	 * @param appt The {@code Appointment} object to check.
	 * @return {@code true} if the appointment can be cancelled, {@code false} otherwise.
	 */
	public boolean isCancellable(Appointment appt) {
		if (appt == null || appt.getStart() == null)
			return false;
		boolean isFuture = appt.getStart().after(new Timestamp(System.currentTimeMillis()));
		return isFuture
				&& (appt.getStatus() == AppointmentStatus.BOOKED || appt.getStatus() == AppointmentStatus.PENDING);
	}

	/**
	 * Provides a list of {@code SelectItem} options for the status filter dropdown in the UI.
	 * Includes "All", "Pending", "Booked", "Cancelled", and "Completed" (for past appointments).
	 *
	 * @return A {@code List} of {@code SelectItem} objects for status filtering.
	 */
	public List<SelectItem> getStatusFilterOptions() {
		List<SelectItem> options = new ArrayList<>();
		options.add(new SelectItem("ALL", "All"));
		options.add(new SelectItem("PENDING", "Pending"));
		options.add(new SelectItem("BOOKED", "Booked"));
		options.add(new SelectItem("CANCELLED", "Cancelled"));
		if ("past".equalsIgnoreCase(timeFilterType)) {
			options.add(new SelectItem("COMPLETED", "Completed"));
		}
		return options;
	}

	/**
	 * Handles the cancellation of the {@code selectedAppointment}. It updates the appointment status
	 * in the database, sends a cancellation email to the recipient, and refreshes the appointment lists.
	 * Displays appropriate success or error messages to the user.
	 *
	 * @return A navigation outcome string to the recipient appointments page if successful, or {@code null} on failure.
	 */
	public String cancelAppointment() {
		if (selectedAppointment == null) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_WARN, "No appointment selected for cancellation.", null));
			LOGGER.warn("Attempted to cancel appointment without selecting one.");
			return null;
		}

		try {
			boolean success = appointmentDao.cancelAppointment(selectedAppointment.getAppointmentId());
			if (!success) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"Failed to cancel appointment in the database.", null));
				LOGGER.error("Failed to cancel appointment in DB for ID: " + selectedAppointment.getAppointmentId());
				return null;
			}

			// Retrieve recipient and doctor details for email
			recipient = (Recipient) FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
					.get("loggedInRecipient");
			Doctors doctor = new DoctorDaoImpl().searchADoctorById(selectedAppointment.getDoctor().getDoctorId());

			ServletContext context = (ServletContext) FacesContext.getCurrentInstance().getExternalContext()
					.getContext();

			String subject = "Appointment Cancelled – Infinite HealthSure";

			// Format dates and times for the email slip
			String date = (selectedAppointment.getStart() != null)
					? selectedAppointment.getStart().toString().split(" ")[0]
					: "N/A";
			String startTime = (selectedAppointment.getStart() != null)
					? selectedAppointment.getStart().toString().split(" ")[1]
					: "N/A";
			String endTime = (selectedAppointment.getEnd() != null)
					? selectedAppointment.getEnd().toString().split(" ")[1]
					: "N/A";

			AppointmentSlip slip = new AppointmentSlip(recipient.getFirstName() + " " + recipient.getLastName(),
					selectedAppointment.getAppointmentId(), "Infinite HealthSure Hospital",
					context.getInitParameter("providerEmail"), context.getInitParameter("contact"),
					doctor.getDoctorName(), doctor.getSpecialization(), date, selectedAppointment.getSlotNo(),
					startTime + " - " + endTime);

			try {
				MailSend.sendMail(recipient.getEmail(), subject, MailSend.appointmentCancellation(slip));
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Appointment cancelled successfully. A confirmation email has been sent.", null));
				LOGGER.info("Appointment ID " + selectedAppointment.getAppointmentId() + " cancelled and email sent to "
						+ recipient.getEmail());
			} catch (Exception emailEx) {
				LOGGER.error("Error sending cancellation email for appointment ID "
						+ selectedAppointment.getAppointmentId() + ": " + emailEx.getMessage(), emailEx);
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Appointment cancelled, but failed to send confirmation email.", null));
			}

			loadAppointments(); // Refresh the list after cancellation
			return "recipient-appointments?faces-redirect=true";

		} catch (Exception e) {
			LOGGER.error("An unexpected error occurred during cancellation for appointment ID "
					+ (selectedAppointment != null ? selectedAppointment.getAppointmentId() : "N/A") + ": "
					+ e.getMessage(), e);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"An unexpected error occurred during cancellation.", null));
			return null;
		}
	}

	/**
	 * Resets all filters (time, status, date range) to their default values
	 * and re-applies filtering and sorting to refresh the displayed appointments.
	 */
	public void resetData() {
		this.timeFilterType = "future";
		this.statusFilterType = "ALL";
		this.fromDate = null;
		this.toDate = null;
		updateFilteredAndSortedAppointments();
	}
	// ======================= GETTERS & SETTERS ========================

	/**
	 * Gets the list of appointments currently displayed on the active page after filtering and sorting.
	 * Note: This method calls {@code loadAppointments()} which might be inefficient if called frequently
	 * by the JSF lifecycle. Ensure it's called appropriately.
	 *
	 * @return A {@code List} of {@code Appointment} objects for the current page.
	 */
	public List<Appointment> getPaginatedAppointments() {
		// Calling loadAppointments() here might lead to redundant data loads if this getter is invoked multiple times
		// during a single JSF request cycle (e.g., by multiple components).
		// Consider if loadAppointments() should only be called once in init() or on explicit user actions.
		// For now, it's left as is based on the original structure.
		loadAppointments();
		return paginatedAppointments;
	}

	/**
	 * Gets the current time filter type ("future" or "past").
	 *
	 * @return The time filter type {@code String}.
	 */
	public String getTimeFilterType() {
		return timeFilterType;
	}

	/**
	 * Sets the time filter type and triggers a re-filter and re-pagination of appointments.
	 *
	 * @param timeFilterType The time filter type to set ("future" or "past").
	 */
	public void setTimeFilterType(String timeFilterType) {
		this.timeFilterType = timeFilterType;
		this.currentPage = 0; // Reset page on filter change
		LOGGER.info("Time filter changed to: " + timeFilterType);
		updateFilteredAndSortedAppointments(); // Call update after filter change
	}

	/**
	 * Sets the status filter type and triggers a re-filter and re-pagination of appointments.
	 *
	 * @param statusFilterType The status filter type to set (e.g., "ALL", "PENDING", "BOOKED").
	 */
	public void setStatusFilterType(String statusFilterType) {
		this.statusFilterType = statusFilterType;
		this.currentPage = 0; // Reset page on filter change
		LOGGER.info("Status filter changed to: " + statusFilterType);
		updateFilteredAndSortedAppointments(); // Call update after filter change
	}

	/**
	 * Gets the current status filter type.
	 *
	 * @return The status filter type {@code String}.
	 */
	public String getStatusFilterType() {
		return statusFilterType;
	}

	/**
	 * Gets the currently selected appointment, typically used for detail viewing or cancellation.
	 *
	 * @return The {@code Appointment} object that is selected.
	 */
	public Appointment getSelectedAppointment() {
		return selectedAppointment;
	}

	/**
	 * Sets the currently selected appointment.
	 *
	 * @param selectedAppointment The {@code Appointment} object to set as selected.
	 */
	public void setSelectedAppointment(Appointment selectedAppointment) {
		this.selectedAppointment = selectedAppointment;
	}

	/**
	 * Gets the recipient's Health ID.
	 *
	 * @return The recipient's Health ID {@code String}.
	 */
	public String getHId() {
		return hId;
	}

	/**
	 * Sets the recipient's Health ID.
	 *
	 * @param hId The recipient's Health ID {@code String}.
	 */
	public void setHId(String hId) {
		this.hId = hId;
	}

	/**
	 * Gets a map indicating whether each appointment (by ID) is cancellable.
	 *
	 * @return A {@code Map} where the key is appointment ID and value is a {@code Boolean} indicating cancellability.
	 */
	public Map<String, Boolean> getCancellableMap() {
		return cancellableMap;
	}

	/**
	 * Gets the current page number (1-based for UI display).
	 *
	 * @return The current page number.
	 */
	public int getCurrentPage() {
		return currentPage + 1; // Return 1-based page number for display
	}

	/**
	 * Sets the current page number (expects 1-based input from UI and converts to 0-based for internal use).
	 *
	 * @param currentPage The current page number (1-based).
	 */
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage - 1; // Convert 1-based page to 0-based for internal use
	}

	/**
	 * Calculates and returns the total number of pages required for the filtered appointments.
	 *
	 * @return The total number of pages.
	 */
	public int getTotalPages() {
		if (filteredAppointments.isEmpty()) {
			return 1;
		}
		return (int) Math.ceil((double) filteredAppointments.size() / pageSize);
	}

	/**
	 * Gets the number of appointments to display per page.
	 *
	 * @return The page size.
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * Checks if there is a next page of appointments.
	 *
	 * @return {@code true} if a next page exists, {@code false} otherwise.
	 */
	public boolean isHasNextPage() {
		return (currentPage + 1) * pageSize < filteredAppointments.size();
	}

	/**
	 * Checks if there is a previous page of appointments.
	 *
	 * @return {@code true} if a previous page exists, {@code false} otherwise.
	 */
	public boolean isHasPrevPage() {
		return currentPage > 0;
	}

	/**
	 * Gets the current field by which the appointments are sorted.
	 *
	 * @return The sort field {@code String}.
	 */
	public String getSortField() {
		return sortField;
	}

	/**
	 * Checks if the current sort order is ascending.
	 *
	 * @return {@code true} if sorting is ascending, {@code false} if descending.
	 */
	public boolean isAscending() {
		return ascending;
	}

	/**
	 * Gets the name of the column currently used for sorting.
	 *
	 * @return The current sort column {@code String}.
	 */
	public String getCurrentSortColumn() {
		return currentSortColumn;
	}

	/**
	 * Gets the current sort order ("asc" or "desc").
	 *
	 * @return The current sort order {@code String}.
	 */
	public String getCurrentSortOrder() {
		return currentSortOrder;
	}

	/**
	 * Gets the start date for filtering appointments.
	 *
	 * @return The {@code Date} representing the 'from' date.
	 */
	public Date getFromDate() {
		return fromDate;
	}

	/**
	 * Sets the start date for filtering appointments and triggers a re-filter and re-pagination.
	 *
	 * @param fromDate The {@code Date} to set as the 'from' date.
	 */
	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
		this.currentPage = 0; // Reset page on filter change
		LOGGER.info("From Date filter changed to: " + fromDate);
		updateFilteredAndSortedAppointments(); // Re-filter and re-paginate
	}

	/**
	 * Gets the end date for filtering appointments.
	 *
	 * @return The {@code Date} representing the 'to' date.
	 */
	public Date getToDate() {
		return toDate;
	}

	/**
	 * Sets the end date for filtering appointments and triggers a re-filter and re-pagination.
	 *
	 * @param toDate The {@code Date} to set as the 'to' date.
	 */
	public void setToDate(Date toDate) {
		this.toDate = toDate;
		this.currentPage = 0; // Reset page on filter change
		LOGGER.info("To Date filter changed to: " + toDate);
		updateFilteredAndSortedAppointments(); // Re-filter and re-paginate
	}
}







