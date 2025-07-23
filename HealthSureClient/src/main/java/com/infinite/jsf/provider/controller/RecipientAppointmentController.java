package com.infinite.jsf.provider.controller;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.*;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.ServletContext;

import com.infinite.jsf.provider.daoImpl.AppointmentDaoImpl;
import com.infinite.jsf.provider.daoImpl.DoctorDaoImpl;
import com.infinite.jsf.provider.dto.AppointmentDetails;
import com.infinite.jsf.provider.dto.AppointmentSlip;
import com.infinite.jsf.provider.model.Appointment;
import com.infinite.jsf.provider.model.AppointmentStatus;
import com.infinite.jsf.provider.model.Doctors;
import com.infinite.jsf.recipient.model.Recipient;
import com.infinite.jsf.util.MailSend;

public class RecipientAppointmentController implements Serializable {

	private static final long serialVersionUID = 1L;

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

	private String selectedAppointmentIdForDetail; // New property to hold the ID
	private AppointmentDetails appointmentDetailsForDisplay;

	// Pagination
	private int pageSize = 5; // Default to 5
	private int currentPage = 1;
	private int totalPages = 1;

	@PostConstruct
	public void init() {
		loadAppointments();
	}

	public void loadAppointments() {
		try {
			recipient = ((Recipient) FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
					.get("loggedInRecipient"));
			if (recipient == null) {
				ExternalContext e = FacesContext.getCurrentInstance().getExternalContext();
				e.redirect(e.getRequestContextPath() + "/recipient/Login.jsf");
			}
			hId = recipient.gethId();
			upcomingAppointments = appointmentDao.getUpcomingAppointmentsByRecipient(hId);
			pastAppointments = appointmentDao.getPastAppointmentsByRecipient(hId);
			updateFilteredAppointments(); // This will also handle pagination
		} catch (Exception e) {
			System.err.println("Error loading appointments: " + e.getMessage());
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Error loading appointments. Please try again.", null));
			upcomingAppointments.clear();
			pastAppointments.clear();
			filteredAppointments.clear();
			paginatedAppointments.clear();
			totalPages = 1; // Reset to 1 page if no data
			currentPage = 1;
		}
	}

	public void updateFilteredAppointments() {
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

		// --- Crucial Pagination Recalculation ---
		// Calculate totalPages based on the *new* filtered list size
		if (filteredAppointments.isEmpty()) {
			totalPages = 1; // If no items, still show 1 page
		} else {
			totalPages = (int) Math.ceil((double) filteredAppointments.size() / pageSize);
		}

		// Adjust currentPage if it's now out of bounds for the new totalPages
		if (currentPage > totalPages) {
			currentPage = totalPages;
		}
		if (currentPage < 1 && totalPages >= 1) { // Ensure current page is never less than 1
			currentPage = 1;
		} else if (currentPage < 1 && totalPages == 0) { // Edge case: if totalPages somehow becomes 0
			currentPage = 1;
		}

		// Now update the paginated list
		updatePaginatedAppointments();
	}

	public void updatePaginatedAppointments() {
		int fromIndex = (currentPage - 1) * pageSize;
		int toIndex = Math.min(fromIndex + pageSize, filteredAppointments.size());

		if (fromIndex < 0 || fromIndex >= filteredAppointments.size()) {
			// If fromIndex is out of bounds, return an empty list.
			// This can happen if filters dramatically reduce the list size.
			paginatedAppointments = new ArrayList<>();
		} else {
			// Ensure toIndex is not less than fromIndex, which can happen if filtered list
			// becomes very small
			// and fromIndex is valid but toIndex calculation makes it smaller or equal.
			if (toIndex < fromIndex) {
				toIndex = fromIndex; // Or just set toIndex = filteredAppointments.size();
			}
			paginatedAppointments = filteredAppointments.subList(fromIndex, toIndex);
		}
	}

	public void nextPage() {
		if (currentPage < totalPages) {
			currentPage++;
			updatePaginatedAppointments();
		}
	}

	public void prevPage() {
		if (currentPage > 1) {
			currentPage--;
			updatePaginatedAppointments();
		}
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

	public String loadAppointmentDetailsForDisplay() {
		System.out.println("loadappointmentdetailsfordisplay");
		if (selectedAppointmentIdForDetail != null && !selectedAppointmentIdForDetail.isEmpty()) {
			try {
				Appointment appointment = appointmentDao.getAppointmentById(selectedAppointmentIdForDetail);
				if (appointment != null) {
					// Populate the DTO
					appointmentDetailsForDisplay = new AppointmentDetails();
					appointmentDetailsForDisplay.setAppointmentId(appointment.getAppointmentId());
					appointmentDetailsForDisplay.setRequestedAt(appointment.getRequestedAt());
					appointmentDetailsForDisplay.setBookedAt(appointment.getBookedAt());
					appointmentDetailsForDisplay.setCancelledAt(appointment.getCancelledAt());
					appointmentDetailsForDisplay.setCompletedAt(appointment.getCompletedAt());
					appointmentDetailsForDisplay.setStatus(appointment.getStatus());
					appointmentDetailsForDisplay.setSlotNo(appointment.getSlotNo());
					appointmentDetailsForDisplay.setStart(appointment.getStart());
					appointmentDetailsForDisplay.setEnd(appointment.getEnd());

					// Doctor details
					if (appointment.getDoctor() != null) {
						appointmentDetailsForDisplay.setDoctorName(appointment.getDoctor().getDoctorName());
						appointmentDetailsForDisplay
								.setDoctorSpecialization(appointment.getDoctor().getSpecialization());
						appointmentDetailsForDisplay.setDoctorGender(appointment.getDoctor().getGender());
					} else {
						appointmentDetailsForDisplay.setDoctorName("N/A");
						appointmentDetailsForDisplay.setDoctorSpecialization("N/A");
					}

					// Doctor Availability Timing (format as a string)
					if (appointment.getStart() != null && appointment.getEnd() != null) {
						// Using SimpleDateFormat to format the Timestamp to HH:mm
						java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
						String startTimeStr = sdf.format(new java.util.Date(appointment.getStart().getTime()));
						String endTimeStr = sdf.format(new java.util.Date(appointment.getEnd().getTime()));
						appointmentDetailsForDisplay.setDoctorAvailabilityTiming(startTimeStr + " - " + endTimeStr);
					} else {
						appointmentDetailsForDisplay.setDoctorAvailabilityTiming("N/A");
					}

					// For debugging:
					System.out.println("Loaded AppointmentDetails DTO: " + appointmentDetailsForDisplay);
					return "/recipient/appointment/appointmentDetail.jsf?faces-redirect=true";
				} else {
					System.err.println("Appointment with ID " + selectedAppointmentIdForDetail + " not found.");
					FacesContext.getCurrentInstance().addMessage(null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Appointment details not found."));
					appointmentDetailsForDisplay = null; // Ensure it's null if not found
				}
			} catch (Exception e) {
				System.err.println("Error fetching appointment details for ID " + selectedAppointmentIdForDetail + ": "
						+ e.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"Error", "An error occurred while loading appointment details."));
				appointmentDetailsForDisplay = null;
			}
		} else {
			System.err.println("No appointment ID provided for detail view.");
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Invalid appointment request."));
			appointmentDetailsForDisplay = null;
		}
		return null;
	}

	public String cancelAppointment() {
		if (selectedAppointment == null) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_WARN, "No appointment selected for cancellation.", null));
			return null;
		}

		try {
			boolean success = appointmentDao.cancelAppointment(selectedAppointment.getAppointmentId());
			if (!success) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"Failed to cancel appointment in the database.", null));
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
			} catch (Exception emailEx) {
				System.err.println("Error sending cancellation email: " + emailEx.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Appointment cancelled, but failed to send confirmation email.", null));
			}

			loadAppointments(); // Refresh data and re-apply filters/pagination
			return "recipient-appointments?faces-redirect=true";

		} catch (Exception e) {
			System.err.println("Error cancelling appointment: " + e.getMessage());
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
		this.currentPage = 1; // Reset to first page on filter change
		updateFilteredAppointments();
	}

	public String getStatusFilterType() {
		return statusFilterType;
	}

	public void setStatusFilterType(String statusFilterType) {
		this.statusFilterType = statusFilterType;
		this.currentPage = 1; // Reset to first page on filter change
		updateFilteredAppointments();
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
		return currentPage;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
		this.currentPage = 1; // Reset to first page if page size changes
		updateFilteredAppointments();
	}

	public AppointmentDetails getAppointmentDetailsForDisplay() {
		return appointmentDetailsForDisplay;
	}

	public void setAppointmentDetailsForDisplay(AppointmentDetails appointmentDetailsForDisplay) {
		this.appointmentDetailsForDisplay = appointmentDetailsForDisplay;
	}

	public String getSelectedAppointmentIdForDetail() {
		return selectedAppointmentIdForDetail;
	}

	public void setSelectedAppointmentIdForDetail(String selectedAppointmentIdForDetail) {
		this.selectedAppointmentIdForDetail = selectedAppointmentIdForDetail;
	}

}