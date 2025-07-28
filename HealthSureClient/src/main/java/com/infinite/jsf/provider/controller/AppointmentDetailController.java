/*
* -----------------------------------------------------------------------------
* Copyright © 2025 Infinite Computer Solution. All rights reserved.
* -----------------------------------------------------------------------------
*
* @Author   : Sandhan Meher
* @Purpose  : This class serves as the controller for displaying detailed
* information about a specific appointment. It retrieves appointment data
* from the DAO layer, processes it, and populates an AppointmentDetails DTO
* for presentation in the JSF UI. It also handles navigation related to
* viewing and rescheduling appointments.
*
* -----------------------------------------------------------------------------
*/
package com.infinite.jsf.provider.controller;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import com.infinite.jsf.provider.dao.AppointmentDao;
import com.infinite.jsf.provider.daoImpl.AppointmentDaoImpl;
import com.infinite.jsf.provider.dto.AppointmentDetails;
import com.infinite.jsf.provider.model.Appointment;

public class AppointmentDetailController implements Serializable {
	private static final long serialVersionUID = 1L;
	private String selectedAppointmentIdForDetail; // New property to hold the ID
	private AppointmentDetails appointmentDetailsForDisplay;
	private AppointmentDao appointmentDao=new AppointmentDaoImpl();

	/**
	 * Loads the detailed information for a specific appointment based on its ID.
	 * This method retrieves the appointment from the database, transforms it into an
	 * {@code AppointmentDetails} DTO, and makes it available for display in the UI.
	 * It also handles error messages if the appointment is not found or if an exception occurs.
	 *
	 * @param selectedAppointmentIdForDetail The unique ID of the appointment to load details for.
	 * @return A navigation outcome string (e.g., "/recipient/appointment/appointmentDetail.jsf?faces-redirect=true")
	 * if successful, or {@code null} if there's an error or the appointment is not found.
	 */
	public String loadAppointmentDetailsForDisplay(String selectedAppointmentIdForDetail) {
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
					appointmentDetailsForDisplay.setDoctorId(appointment.getDoctor().getDoctorId());
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

	/**
	 * Navigates to the appointment rescheduling page.
	 *
	 * @return A navigation outcome string to the update appointment page with a faces-redirect.
	 */
	public String rescheduleAppointment() {
		return "/recipient/appointment/updateAppointment?faces-redirect=true";
	}

    /**
     * Gets the ID of the currently selected appointment for detail viewing.
     *
     * @return The selected appointment ID as a {@code String}.
     */
	public String getSelectedAppointmentIdForDetail() {
		return selectedAppointmentIdForDetail;
	}

    /**
     * Sets the ID of the appointment to be displayed in detail.
     *
     * @param selectedAppointmentIdForDetail The ID of the appointment.
     */
	public void setSelectedAppointmentIdForDetail(String selectedAppointmentIdForDetail) {
		this.selectedAppointmentIdForDetail = selectedAppointmentIdForDetail;
	}

    /**
     * Gets the {@code AppointmentDetails} DTO object populated with the details
     * of the currently displayed appointment.
     *
     * @return The {@code AppointmentDetails} object.
     */
	public AppointmentDetails getAppointmentDetailsForDisplay() {
		return appointmentDetailsForDisplay;
	}

    /**
     * Sets the {@code AppointmentDetails} DTO object for display.
     *
     * @param appointmentDetailsForDisplay The {@code AppointmentDetails} object to set.
     */
	public void setAppointmentDetailsForDisplay(AppointmentDetails appointmentDetailsForDisplay) {
		this.appointmentDetailsForDisplay = appointmentDetailsForDisplay;
	}

    /**
     * Gets the serial version UID for serialization.
     *
     * @return The serial version UID as a {@code long}.
     */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}

