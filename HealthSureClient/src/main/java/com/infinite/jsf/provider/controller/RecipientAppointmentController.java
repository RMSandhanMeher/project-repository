package com.infinite.jsf.provider.controller;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.*;
import java.util.Comparator; // Import Comparator

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger; // Added for logging

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
	private static final Logger LOGGER = Logger.getLogger(RecipientAppointmentController.class.getName()); // Logger
																											// instance

	private final AppointmentDaoImpl appointmentDao = new AppointmentDaoImpl();

	private String hId;
	private Recipient recipient;
	private List<Appointment> upcomingAppointments = new ArrayList<>();
	private List<Appointment> pastAppointments = new ArrayList<>();
	private List<Appointment> filteredAppointments = new ArrayList<>();
	private List<Appointment> paginatedAppointments = new ArrayList<>();

	private Map<String, Boolean> cancellableMap = new HashMap<>();

	private Appointment selectedAppointment;

	private String timeFilterType = "future"; // "future" or "past"
	private String statusFilterType = "ALL"; // ALL, PENDING, BOOKED, CANCELLED, COMPLETED

	// Pagination: Fixed page size
	private final int pageSize = 5; // Fixed at 5 as per requirement
	private int currentPage = 0; // 0-based for sublist

	// Sorting
	private String sortField = "start"; // Default sort by appointment start date/time
	private boolean ascending = true;
	private String currentSortColumn = "start";
	private String currentSortOrder = "asc";

	@PostConstruct
	public void init() {
		LOGGER.info("RecipientAppointmentController initialized.");
		loadAppointments();
	}

	public void loadAppointments() {
		try {
			recipient = ((Recipient) FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
					.get("loggedInRecipient"));
			if (recipient == null) {
				LOGGER.warn("No logged-in recipient found in session. Redirecting to login.");
				ExternalContext e = FacesContext.getCurrentInstance().getExternalContext();
				e.redirect(e.getRequestContextPath() + "/recipient/Login.jsf");
				return; // Stop further execution if redirecting
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
			currentPage = 0; // Reset to first page
		}
	}

	public void updateFilteredAndSortedAppointments() {
		LOGGER.info("updateFilteredAndSortedAppointments: Time Filter=" + timeFilterType + ", Status Filter="
				+ statusFilterType);
		List<Appointment> baseList = "past".equalsIgnoreCase(timeFilterType) ? pastAppointments : upcomingAppointments;

		filteredAppointments = new ArrayList<>();
		cancellableMap.clear();

		for (Appointment appt : baseList) {
			boolean matchStatus = "ALL".equalsIgnoreCase(statusFilterType)
					|| (appt.getStatus() != null && appt.getStatus().name().equalsIgnoreCase(statusFilterType));
			if (matchStatus) {
				filteredAppointments.add(appt);
				cancellableMap.put(appt.getAppointmentId(), isCancellable(appt));
			}
		}

		sortFilteredAppointments(); // Apply sorting to the filtered list

		// Reset current page to 0 if the current page is out of bounds after
		// filtering/sorting
		if (currentPage * pageSize >= filteredAppointments.size() && filteredAppointments.size() > 0) {
			currentPage = (int) Math.floor((double) (filteredAppointments.size() - 1) / pageSize);
		} else if (filteredAppointments.isEmpty()) {
			currentPage = 0; // If no results, stay on page 0 (which is page 1 for display)
		}

		updatePaginatedAppointments();
	}

	/**
	 * Applies actual comparator-based sorting logic using the sortField.
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
			case "start": // Default and explicit sort for Timestamp
			default:
				comparator = Comparator.comparing(Appointment::getStart, Comparator.nullsLast(Comparator.naturalOrder()) // Natural
																															// order
																															// for
																															// Timestamps
				);
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
	 * Sort doctor result list ascending by field.
	 */
	public void sortByAsc(String field) {
		this.sortField = field;
		this.ascending = true;
		this.currentSortColumn = field;
		this.currentSortOrder = "asc";
		currentPage = 0; // Reset to first page on sort change
		updateFilteredAndSortedAppointments();
		LOGGER.info("Sorting by " + field + " (Ascending)");
	}

	/**
	 * Sort doctor result list descending by field.
	 */
	public void sortByDesc(String field) {
		this.sortField = field;
		this.ascending = false;
		this.currentSortColumn = field;
		this.currentSortOrder = "desc";
		currentPage = 0; // Reset to first page on sort change
		updateFilteredAndSortedAppointments();
		LOGGER.info("Sorting by " + field + " (Descending)");
	}

	/**
	 * Determines whether to show a specific sort button.
	 */
	public boolean renderSortButton(String column, String order) {
		if (currentSortColumn == null || !currentSortColumn.equals(column)) {
			return true; // Always show if not the current sort column
		}
		return !currentSortOrder.equals(order); // Show only if different from current order
	}

	public void updatePaginatedAppointments() {
		if (filteredAppointments == null || filteredAppointments.isEmpty()) {
			paginatedAppointments = new ArrayList<>();
			return;
		}

		int fromIndex = currentPage * pageSize;
		int toIndex = Math.min(fromIndex + pageSize, filteredAppointments.size());

		// Ensure fromIndex doesn't exceed list size
		fromIndex = Math.min(fromIndex, filteredAppointments.size());

		paginatedAppointments = new ArrayList<>(filteredAppointments.subList(fromIndex, toIndex));

		LOGGER.debug("Updated pagination - Page: " + (currentPage + 1) + ", Items: " + paginatedAppointments.size()
				+ ", Total: " + filteredAppointments.size());
	}

	public void nextPage() {
		if (isHasNextPage()) {
			currentPage++;
			updatePaginatedAppointments();
			LOGGER.info("Navigated to page " + (currentPage + 1));
		}
	}

	public void prevPage() {
		if (isHasPrevPage()) {
			currentPage--;
			updatePaginatedAppointments();
			LOGGER.info("Navigated to page " + (currentPage + 1));
		}
	}

	private void resetPagination() {
		currentPage = 0;
		updatePaginatedAppointments();
	}

	public void goToPage(int page) {
		// Convert 1-based page to 0-based for internal use
		int zeroBasedPage = page - 1;
		int maxPage = getTotalPages() - 1; // 0-based
		currentPage = Math.max(0, Math.min(zeroBasedPage, maxPage));
		updatePaginatedAppointments();
	}

	public boolean isCancellable(Appointment appt) {
		if (appt == null || appt.getStart() == null)
			return false;
		boolean isFuture = appt.getStart().after(new Timestamp(System.currentTimeMillis()));
		return isFuture
				&& (appt.getStatus() == AppointmentStatus.BOOKED || appt.getStatus() == AppointmentStatus.PENDING);
	}

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

			recipient = (Recipient) FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
					.get("loggedInRecipient");
			Doctors doctor = new DoctorDaoImpl().searchADoctorById(selectedAppointment.getDoctor().getDoctorId());

			ServletContext context = (ServletContext) FacesContext.getCurrentInstance().getExternalContext()
					.getContext();

			String subject = "Appointment Cancelled – Infinite HealthSure";

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

			loadAppointments(); // Refresh data and re-apply filters/pagination
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

	// ======================= GETTERS & SETTERS ========================

	public List<Appointment> getPaginatedAppointments() {
		return paginatedAppointments;
	}

	public String getTimeFilterType() {
		return timeFilterType;
	}

	public void setTimeFilterType(String timeFilterType) {
		this.timeFilterType = timeFilterType;
		this.currentPage = 0; // Reset to first page
		LOGGER.info("Time filter changed to: " + timeFilterType);
		loadAppointments(); // Reload all data
	}

	public void setStatusFilterType(String statusFilterType) {
		this.statusFilterType = statusFilterType;
		this.currentPage = 0; // Reset to first page
		LOGGER.info("Status filter changed to: " + statusFilterType);
		loadAppointments(); // Reload all data
	}

	public String getStatusFilterType() {
		return statusFilterType;
	}

	public Appointment getSelectedAppointment() {
		return selectedAppointment;
	}

	public void setSelectedAppointment(Appointment selectedAppointment) {
		this.selectedAppointment = selectedAppointment;
	}

	public String getHId() {
		return hId;
	}

	public void setHId(String hId) {
		this.hId = hId;
	}

	public Map<String, Boolean> getCancellableMap() {
		return cancellableMap;
	}

	public int getCurrentPage() {
		// Return 1-based page number for display
		return currentPage + 1;
	}

	public void setCurrentPage(int currentPage) {
		// Convert 1-based page to 0-based for internal use
		this.currentPage = currentPage - 1;
	}

	public int getTotalPages() {
		if (filteredAppointments.isEmpty()) {
			return 1;
		}
		return (int) Math.ceil((double) filteredAppointments.size() / pageSize);
	}

	public int getPageSize() {
		return pageSize; // Page size is now fixed
	}

	public boolean isHasNextPage() {
		return (currentPage + 1) * pageSize < filteredAppointments.size();
	}

	public boolean isHasPrevPage() {
		return currentPage > 0;
	}

	public String getSortField() {
		return sortField;
	}

	public boolean isAscending() {
		return ascending;
	}

	public String getCurrentSortColumn() {
		return currentSortColumn;
	}

	public String getCurrentSortOrder() {
		return currentSortOrder;
	}
}





