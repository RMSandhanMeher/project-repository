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

//	getter and setter 

	public String getSelectedAppointmentIdForDetail() {
		return selectedAppointmentIdForDetail;
	}

	public void setSelectedAppointmentIdForDetail(String selectedAppointmentIdForDetail) {
		this.selectedAppointmentIdForDetail = selectedAppointmentIdForDetail;
	}

	public AppointmentDetails getAppointmentDetailsForDisplay() {
		return appointmentDetailsForDisplay;
	}

	public void setAppointmentDetailsForDisplay(AppointmentDetails appointmentDetailsForDisplay) {
		this.appointmentDetailsForDisplay = appointmentDetailsForDisplay;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
