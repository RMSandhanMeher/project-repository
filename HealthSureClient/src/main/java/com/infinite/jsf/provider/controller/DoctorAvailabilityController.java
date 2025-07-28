/*
* -----------------------------------------------------------------------------
* Copyright © 2025 Infinite Computer Solution. All rights reserved.
* -----------------------------------------------------------------------------
*
* @Author   : Sandhan Meher
* @Purpose  : This class serves as the controller for managing doctor availability
* and appointment booking/rescheduling for recipients. It handles fetching
* and displaying doctor availability slots, processing appointment bookings,
* and updating existing appointments. It interacts with DAO layers for
* availability, appointment, doctor, and recipient data, and sends
* confirmation emails.
*
* -----------------------------------------------------------------------------
*/
package com.infinite.jsf.provider.controller;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import com.infinite.jsf.provider.daoImpl.AppointmentDaoImpl;
import com.infinite.jsf.provider.daoImpl.DoctorAvailabilityDaoImpl;
import com.infinite.jsf.provider.daoImpl.DoctorDaoImpl;
import com.infinite.jsf.provider.daoImpl.RecipientDaoImpl;
import com.infinite.jsf.provider.dto.AppointmentSlip;
import com.infinite.jsf.provider.model.Appointment;
import com.infinite.jsf.provider.model.DoctorAvailability;
import com.infinite.jsf.provider.model.Doctors;
import com.infinite.jsf.provider.model.Provider;
import com.infinite.jsf.recipient.model.Recipient;
import com.infinite.jsf.util.MailSend;

public class DoctorAvailabilityController implements Serializable {
	private static final long serialVersionUID = 1L;

	private final DoctorAvailabilityDaoImpl availabilityDao = new DoctorAvailabilityDaoImpl();
	private final AppointmentDaoImpl appointmentDao = new AppointmentDaoImpl();

	private String doctorId;
	private List<DayAvailabilitySummary> groupedAvailabilityList;
	private Date selectedDate;
	private String selectedDateInput;
	private List<SlotDisplay> availableSlots;
	private Map<Date, List<DoctorAvailability>> dateMap;
	private String selectedAvailabilityId;
	private int selectedSlotNumber;
	private Doctors doctor;
	private String appointmentId; // Used for rescheduling existing appointments
	private List<String> availabilityTiming;

	/**
	 * Initializes the controller after its construction. This method is annotated with {@code @PostConstruct}
	 * and is called by the JSF runtime. It loads the initial set of upcoming doctor availabilities.
	 */
	@PostConstruct
	public void init() {
		loadAllUpcomingAvailability();
	}

	/**
	 * Loads all upcoming doctor availabilities for the {@code doctorId} set in this controller.
	 * It groups the availabilities by date and calculates the total remaining slots for each day,
	 * populating {@code groupedAvailabilityList} for display.
	 */
	public void loadAllUpcomingAvailability() {
		Date today = new Date(System.currentTimeMillis());
		List<DoctorAvailability> futureSlots = availabilityDao.getUpcomingAvailabilitiesForDoctor(doctorId, today);

		dateMap = futureSlots.stream().collect(
				Collectors.groupingBy(DoctorAvailability::getAvailableDate, TreeMap::new, Collectors.toList()));

		groupedAvailabilityList = dateMap.entrySet().stream()
				.map(entry -> new DayAvailabilitySummary(entry.getKey(), formatDisplayDate(entry.getKey()),
						entry.getValue().stream()
								.mapToInt(
										da -> availabilityDao.getRemainingSlotsForAvailability(da.getAvailabilityId()))
								.sum()))
				.collect(Collectors.toList());
	}

	/**
	 * Loads and populates the available time slots for the {@code selectedDate}.
	 * It calculates individual slot timings based on availability's start/end times and max capacity,
	 * and filters out already booked slots. The results are stored in {@code availableSlots}.
	 */
	public void loadAvailableSlots() {
		availabilityTiming = new ArrayList<String>();
		for (DoctorAvailability a : new DoctorAvailabilityDaoImpl().getAvailabilityByDoctorAndDate(doctorId,
				selectedDate)) {
			availabilityTiming.add(
					a.getStartTime().toString().substring(0, 5) + " - " + a.getEndTime().toString().substring(0, 5));
		}
		availableSlots = new ArrayList<>();
		if (selectedDate == null)
			return;

		List<DoctorAvailability> dailyAvailabilities = dateMap.get(selectedDate);
		if (dailyAvailabilities == null)
			return;

		for (DoctorAvailability availability : dailyAvailabilities) {
			String availabilityId = availability.getAvailabilityId();
			List<Integer> availableSlotNumbers = appointmentDao.getAvailableSlotNumbers(availabilityId);

			Timestamp start = Timestamp.valueOf(availability.getAvailableDate() + " " + availability.getStartTime());
			Timestamp end = Timestamp.valueOf(availability.getAvailableDate() + " " + availability.getEndTime());
			int maxCapacity = availability.getMaxCapacity();
			long slotDuration = (end.getTime() - start.getTime()) / (maxCapacity * 60000); // Duration in minutes

			availableSlotNumbers.forEach(slotNo -> {
				long slotStartMillis = start.getTime() + (slotNo - 1) * slotDuration * 60000;
				LocalTime slotStartTime = new Timestamp(slotStartMillis).toLocalDateTime().toLocalTime();
				LocalTime slotEndTime = new Timestamp(slotStartMillis + slotDuration * 60000).toLocalDateTime()
						.toLocalTime();

				availableSlots.add(
						new SlotDisplay(availabilityId, slotNo, formatTime(slotStartTime), formatTime(slotEndTime)));
			});
		}
	}

	/**
	 * Sets the doctor ID and an existing appointment ID (for rescheduling context),
	 * then loads the doctor's details and their upcoming availability.
	 * This method is typically used when navigating from an existing appointment to reschedule.
	 *
	 * @param doctorId The ID of the doctor whose availability is to be viewed.
	 * @param appointmentId The ID of the appointment being rescheduled.
	 * @return A navigation outcome string to the doctor availability list page, or {@code null} if validation fails.
	 */
	public String chooseDoctor(String doctorId,String appointmentId) {
		this.doctorId = doctorId;
		this.appointmentId=appointmentId;
		System.out.println(doctorId+"        "+appointmentId);
		if (doctorId == null || doctorId.isEmpty()) {
			FacesContext.getCurrentInstance().addMessage("searchForm:searchFieldMessages",
					new FacesMessage(FacesMessage.SEVERITY_WARN, "Please select a doctor.", null));
			return null;
		}
		doctor = new DoctorDaoImpl().searchADoctorById(doctorId);
		loadAllUpcomingAvailability();
		return "/recipient/appointment/doctorAvailabilityList.jsf?faces-redirect=true";
	}

	/**
	 * Sets the doctor ID and then loads the doctor's details and their upcoming availability.
	 * This method is typically used when selecting a doctor for a new appointment.
	 *
	 * @param doctorId The ID of the doctor whose availability is to be viewed.
	 * @return A navigation outcome string to the doctor availability list page, or {@code null} if validation fails.
	 */
	public String chooseDoctor(String doctorId) {
		this.doctorId = doctorId;

		if (doctorId == null || doctorId.isEmpty()) {
			FacesContext.getCurrentInstance().addMessage("searchForm:searchFieldMessages",
					new FacesMessage(FacesMessage.SEVERITY_WARN, "Please select a doctor.", null));
			return null;
		}
		doctor = new DoctorDaoImpl().searchADoctorById(doctorId);
		loadAllUpcomingAvailability();
		return "/recipient/appointment/doctorAvailabilityList.jsf?faces-redirect=true";
	}

	/**
	 * Handles the selection of a date from the UI. It parses the {@code selectedDateInput}
	 * and, if valid, sets it as {@code selectedDate} and triggers the loading of available slots
	 * for that date. Displays an error message for invalid date formats.
	 */
	public void handleDateSelection() {
		try {
			if (selectedDateInput != null && !selectedDateInput.isEmpty()) {
				selectedDate = Date.valueOf(selectedDateInput);
				loadAvailableSlots();
			}
		} catch (IllegalArgumentException e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid date format. Please use YYYY-MM-DD", null));
		}
	}

	/**
	 * Books a new appointment for the logged-in recipient based on the {@code selectedAvailabilityId}
	 * and {@code selectedSlotNumber}. It validates the availability, creates an {@code Appointment} object,
	 * persists it via the DAO, and sends a confirmation email. Resets form fields and navigates to a confirmation page on success.
	 *
	 * @return A navigation outcome string to the appointment confirmation page if successful, or {@code null} if an error occurs.
	 */
	public String bookAppointment() {
		try {
			System.out.println("selectedAvailabilityId" + selectedAvailabilityId);
			FacesContext context = FacesContext.getCurrentInstance();
			DoctorAvailability availability = availabilityDao.getAvailabilityById(selectedAvailabilityId);

			if (availability == null) {
				context.addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Time slot no longer available", null));
				this.selectedAvailabilityId = null;
				this.selectedSlotNumber = 0;
				return null;
			}

			Appointment appointment = new Appointment();
			appointment.setAvailability(availability);
			appointment.setDoctor(availability.getDoctor());
			appointment.setSlotNo(selectedSlotNumber);

			// Set current user as recipient (should come from session)
			
			Recipient recipient = (Recipient) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("loggedInRecipient");
			appointment.setRecipient(recipient);

			Provider provider = new Provider();
			provider.setProviderId("PROV001"); // Assuming a fixed provider ID for now
			appointment.setProvider(provider);

			String result = appointmentDao.bookAnAppointment(appointment);
			if (result.startsWith("Appointment requested successfully")) {

				HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext()
						.getSession(true);
				session.setAttribute("confirmationMessage", result);
				this.selectedAvailabilityId = null;
				this.selectedSlotNumber = 0;
				this.selectedDateInput = null;
				this.selectedDate = null;
				this.loadAvailableSlots(); // Refresh available slots
				Recipient res = new RecipientDaoImpl().searchRecipientById(recipient.gethId());
				Doctors doctor = new DoctorDaoImpl().searchADoctorById(doctorId);
				ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext()
						.getContext();
				Appointment ap = new AppointmentDaoImpl()
						.getAppointmentById(result.split(" ")[result.split(" ").length - 1]);
				String subject = "Appointment Request Received – Awaiting Confirmation";
				AppointmentSlip apSli = new AppointmentSlip(res.getFirstName() + " " + res.getLastName(),
						ap.getAppointmentId(), "Infinite HealthSure Hospital",
						servletContext.getInitParameter("providerEmail"), servletContext.getInitParameter("contact"),
						doctor.getDoctorName(), doctor.getSpecialization(), ap.getStart().toString().split(" ")[0],
						appointment.getSlotNo(),
						ap.getStart().toString().split(" ")[1] + " - " + ap.getEnd().toString().split(" ")[1]);
				try {
					MailSend.sendMail(res.getEmail(), subject, MailSend.appointmentRequest(apSli));
				} catch (Exception e) {
					System.out.println("error while sending the mail here ");
				}
				return "appointmentConfirmation?faces-redirect=true";
			} else {
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, result, null));
				this.selectedAvailabilityId = null;
				this.selectedSlotNumber = 0;
				this.selectedDateInput = null;
				this.selectedDate = null;
				this.loadAvailableSlots(); // Refresh available slots
			}
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: " + e.getMessage(), null));
		}
		return null;
	}

	/**
	 * Updates an existing appointment with a new time slot. It retrieves the existing appointment
	 * using {@code appointmentId}, sets the new availability and slot number, and then persists
	 * the changes via the DAO. Resets form fields and navigates to a confirmation page on success.
	 *
	 * @return A navigation outcome string to the appointment confirmation page if successful, or {@code null} if an error occurs.
	 */
	public String updateBookedAppointment() {
		System.out.println("method ");
		try {
			System.out.println("selectedAvailabilityId" + selectedAvailabilityId);
			FacesContext context = FacesContext.getCurrentInstance();
			DoctorAvailability availability = availabilityDao.getAvailabilityById(selectedAvailabilityId);

			if (availability == null) {
				context.addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Time slot no longer available", null));
				this.selectedAvailabilityId = null;
				this.selectedSlotNumber = 0;
				return null;
			}

			Appointment appointment = new AppointmentDaoImpl().getAppointmentById(appointmentId);
			appointment.setAvailability(availability);
			appointment.setDoctor(availability.getDoctor());
			appointment.setSlotNo(selectedSlotNumber);
			
			// Set current user as recipient (should come from session)
			
			Recipient recipient = (Recipient) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("loggedInRecipient");
			appointment.setRecipient(recipient);

			Provider provider = new Provider();
			provider.setProviderId("PROV001"); // Assuming a fixed provider ID for now
			appointment.setProvider(provider);
			
			System.out.println(appointment.getAppointmentId());
			String result = appointmentDao.updateAppointment(appointment);
			if (result.startsWith("Appointment updated successfully")) {

				HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext()
						.getSession(true);
				session.setAttribute("confirmationMessage", result);
				this.selectedAvailabilityId = null;
				this.selectedSlotNumber = 0;
				this.selectedDateInput = null;
				this.selectedDate = null;
				this.loadAvailableSlots(); // Refresh available slots
//				Recipient res = new RecipientDaoImpl().searchRecipientById(recipient.gethId()); // Commented out mail sending for update for now, can be enabled if needed
//				Doctors doctor = new DoctorDaoImpl().searchADoctorById(doctorId);
//				ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext()
//						.getContext();
//				Appointment ap = new AppointmentDaoImpl()
//						.getAppointmentById(result.split(" ")[result.split(" ").length - 1]);
//				String subject = "Appointment update request Received – Awaiting Confirmation";
//				AppointmentSlip apSli = new AppointmentSlip(res.getFirstName() + " " + res.getLastName(),
//						ap.getAppointmentId(), "Infinite HealthSure Hospital",
//						servletContext.getInitParameter("providerEmail"), servletContext.getInitParameter("contact"),
//						doctor.getDoctorName(), doctor.getSpecialization(), ap.getStart().toString().split(" ")[0],
//						appointment.getSlotNo(),
//						ap.getStart().toString().split(" ")[1] + " - " + ap.getEnd().toString().split(" ")[1]);
//				try {
//					MailSend.sendMail(res.getEmail(), subject, MailSend.appointmentRequest(apSli));
//				} catch (Exception e) {
//					System.out.println("error while sending the mail here ");
//				}
				return "appointmentConfirmation?faces-redirect=true";
			} else {
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, result, null));
				this.selectedAvailabilityId = null;
				this.selectedSlotNumber = 0;
				this.selectedDateInput = null;
				this.selectedDate = null;
				this.loadAvailableSlots(); // Refresh available slots
			}
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: " + e.getMessage(), null));
		}
		return null;
	}
	
	/**
	 * Prepares the controller for rescheduling an existing appointment by setting the doctor ID
	 * and the appointment ID, then navigates to the update appointment page.
	 *
	 * @param doctorId The ID of the doctor for the appointment to be rescheduled.
	 * @param appointmentId The ID of the appointment to be rescheduled.
	 * @return A navigation outcome string to the update appointment page with a faces-redirect.
	 */
	public String rescheduleAppointment(String doctorId,String appointmentId) {
		chooseDoctor(doctorId,appointmentId);
		return "/recipient/appointment/updateAppointment?faces-redirect=true";
	}

	/**
	 * Returns a filtered list of available slots that fall within the morning time period (midnight to noon).
	 *
	 * @return A {@code List} of {@code SlotDisplay} objects for morning slots.
	 */
	public List<SlotDisplay> getMorningSlots() {
		return filterSlotsByTime(LocalTime.MIN, LocalTime.NOON);
	}

	/**
	 * Returns a filtered list of available slots that fall within the afternoon time period (noon to 5 PM).
	 *
	 * @return A {@code List} of {@code SlotDisplay} objects for afternoon slots.
	 */
	public List<SlotDisplay> getAfternoonSlots() {
		return filterSlotsByTime(LocalTime.NOON, LocalTime.of(17, 0));
	}

	/**
	 * Returns a filtered list of available slots that fall within the evening time period (5 PM to midnight).
	 *
	 * @return A {@code List} of {@code SlotDisplay} objects for evening slots.
	 */
	public List<SlotDisplay> getEveningSlots() {
		return filterSlotsByTime(LocalTime.of(17, 0), LocalTime.MAX);
	}

	/**
	 * Helper method to filter the {@code availableSlots} list based on a specified time range.
	 *
	 * @param start The start {@code LocalTime} for filtering (inclusive).
	 * @param end The end {@code LocalTime} for filtering (exclusive).
	 * @return A {@code List} of {@code SlotDisplay} objects that fall within the given time range.
	 */
	private List<SlotDisplay> filterSlotsByTime(LocalTime start, LocalTime end) {
		if (availableSlots == null)
			return Collections.emptyList();

		return availableSlots.stream().filter(slot -> {
			LocalTime time = LocalTime.parse(slot.getStartTime());
			return !time.isBefore(start) && time.isBefore(end);
		}).collect(Collectors.toList());
	}

	/**
	 * Formats a {@code java.sql.Date} object into a display-friendly string (e.g., "Mon 1, Jan").
	 *
	 * @param date The {@code Date} to format.
	 * @return A formatted date string.
	 */
	private String formatDisplayDate(Date date) {
		return new SimpleDateFormat("E d, MMM", Locale.ENGLISH).format(date);
	}

	/**
	 * Formats a {@code java.time.LocalTime} object into a "HH:mm" string format.
	 *
	 * @param time The {@code LocalTime} to format.
	 * @return A formatted time string.
	 */
	private String formatTime(LocalTime time) {
		return String.format("%02d:%02d", time.getHour(), time.getMinute());
	}

	// Getters and Setters

	/**
	 * Gets the list of {@code DayAvailabilitySummary} objects, providing a summary
	 * of daily availabilities grouped by date.
	 *
	 * @return A {@code List} of {@code DayAvailabilitySummary}.
	 */
	public List<DayAvailabilitySummary> getGroupedAvailabilityList() {
		return groupedAvailabilityList;
	}

	/**
	 * Gets the currently selected date for viewing slots.
	 *
	 * @return The selected {@code java.sql.Date}.
	 */
	public Date getSelectedDate() {
		return selectedDate;
	}

	/**
	 * Sets the currently selected date.
	 *
	 * @param selectedDate The {@code java.sql.Date} to set.
	 */
	public void setSelectedDate(Date selectedDate) {
		this.selectedDate = selectedDate;
	}

	/**
	 * Gets the input string representing the selected date (e.g., "YYYY-MM-DD").
	 *
	 * @return The selected date input string.
	 */
	public String getSelectedDateInput() {
		return selectedDateInput;
	}

	/**
	 * Sets the input string for the selected date. This property is often bound to a text input field in the UI.
	 *
	 * @param selectedDateInput The date string to set.
	 */
	public void setSelectedDateInput(String selectedDateInput) {
		this.selectedDateInput = selectedDateInput;
	}

	/**
	 * Gets the list of available {@code SlotDisplay} objects for the {@code selectedDate}.
	 *
	 * @return A {@code List} of {@code SlotDisplay}.
	 */
	public List<SlotDisplay> getAvailableSlots() {
		return availableSlots;
	}

	/**
	 * Gets the count of morning slots for the selected date.
	 *
	 * @return The number of morning slots.
	 */
	public int getMorningSlotCount() {
		return getMorningSlots().size();
	}

	/**
	 * Gets the count of afternoon slots for the selected date.
	 *
	 * @return The number of afternoon slots.
	 */
	public int getAfternoonSlotCount() {
		return getAfternoonSlots().size();
	}

	/**
	 * Gets the count of evening slots for the selected date.
	 *
	 * @return The number of evening slots.
	 */
	public int getEveningSlotCount() {
		return getEveningSlots().size();
	}

	/**
	 * Gets the ID of the selected doctor availability record, representing a specific date and time range of a doctor's availability.
	 *
	 * @return The selected availability ID as a {@code String}.
	 */
	public String getSelectedAvailabilityId() {
		return selectedAvailabilityId;
	}

	/**
	 * Sets the ID of the selected doctor availability record.
	 *
	 * @param selectedAvailabilityId The availability ID to set.
	 */
	public void setSelectedAvailabilityId(String selectedAvailabilityId) {
		this.selectedAvailabilityId = selectedAvailabilityId;
	}

	/**
	 * Gets the selected slot number within a chosen availability.
	 *
	 * @return The selected slot number as an {@code int}.
	 */
	public int getSelectedSlotNumber() {
		return selectedSlotNumber;
	}

	/**
	 * Sets the selected slot number within a chosen availability.
	 *
	 * @param selectedSlotNumber The slot number to set.
	 */
	public void setSelectedSlotNumber(int selectedSlotNumber) {
		this.selectedSlotNumber = selectedSlotNumber;
	}

	// Inner classes

	/**
	 * Inner class representing a summary of doctor availability for a specific day.
	 * It includes the date, a formatted display date, and the total number of available slots for that day.
	 */
	public static class DayAvailabilitySummary {
		private final Date date;
		private final String displayDate;
		private final int totalSlots;

		/**
		 * Constructs a new {@code DayAvailabilitySummary}.
		 * @param date The {@code java.sql.Date} of the availability.
		 * @param displayDate A formatted {@code String} representation of the date.
		 * @param totalSlots The total number of available slots for this date.
		 */
		public DayAvailabilitySummary(Date date, String displayDate, int totalSlots) {
			this.date = date;
			this.displayDate = displayDate;
			this.totalSlots = totalSlots;
		}

		/**
		 * Gets the raw date object.
		 * @return The {@code java.sql.Date}.
		 */
		public Date getDate() {
			return date;
		}

		/**
		 * Gets the formatted display date string.
		 * @return The display date {@code String}.
		 */
		public String getDisplayDate() {
			return displayDate;
		}

		/**
		 * Gets the total number of slots available for this day.
		 * @return The total slots as an {@code int}.
		 */
		public int getTotalSlots() {
			return totalSlots;
		}
	}

	/**
	 * Inner class representing a specific time slot within a doctor's availability.
	 * It includes the availability ID, slot number, and formatted start and end times for the slot.
	 */
	public static class SlotDisplay implements Serializable { // Add Serializable to inner classes if they are part of controller state
		private static final long serialVersionUID = 1L; // Add serialVersionUID
		private final String availabilityId;
		private final int slotNumber;
		private final String startTime;
		private final String endTime;

		/**
		 * Constructs a new {@code SlotDisplay} object.
		 * @param availabilityId The ID of the parent {@code DoctorAvailability} record.
		 * @param slotNumber The specific slot number within that availability.
		 * @param startTime The formatted start time of the slot (e.g., "09:00").
		 * @param endTime The formatted end time of the slot (e.g., "09:30").
		 */
		public SlotDisplay(String availabilityId, int slotNumber, String startTime, String endTime) {
			this.availabilityId = availabilityId;
			this.slotNumber = slotNumber;
			this.startTime = startTime;
			this.endTime = endTime;
		}

		/**
		 * Gets the availability ID associated with this slot.
		 * @return The availability ID {@code String}.
		 */
		public String getAvailabilityId() {
			return availabilityId;
		}

		/**
		 * Gets the slot number.
		 * @return The slot number as an {@code int}.
		 */
		public int getSlotNumber() {
			return slotNumber;
		}

		/**
		 * Gets the formatted start time of the slot.
		 * @return The start time {@code String}.
		 */
		public String getStartTime() {
			return startTime;
		}

		/**
		 * Gets the formatted end time of the slot.
		 * @return The end time {@code String}.
		 */
		public String getEndTime() {
			return endTime;
		}

		/**
		 * Gets a formatted string representing the time range of the slot (e.g., "09:00 - 09:30").
		 * @return The formatted time range {@code String}.
		 */
		public String getFormattedTimeRange() {
			return startTime + " - " + endTime;
		}
	}

	/**
	 * Gets the {@code Doctors} object currently associated with this controller (the selected doctor).
	 *
	 * @return The {@code Doctors} object.
	 */
	public Doctors getDoctor() {
		return doctor;
	}

	/**
	 * Sets the {@code Doctors} object for this controller.
	 *
	 * @param doctor The {@code Doctors} object to set.
	 */
	public void setDoctor(Doctors doctor) {
		this.doctor = doctor;
	}

	/**
	 * Gets a list of strings representing the overall availability timings for the selected date.
	 * This typically includes the broader time blocks a doctor is available (e.g., "09:00 - 13:00").
	 *
	 * @return A {@code List} of {@code String} representing availability timings.
	 */
	public List<String> getAvailabilityTiming() {
		return availabilityTiming;
	}

	/**
	 * Sets the list of strings representing the overall availability timings.
	 *
	 * @param availabilityTiming The {@code List} of {@code String} to set.
	 */
	public void setAvailabilityTiming(List<String> availabilityTiming) {
		this.availabilityTiming = availabilityTiming;
	}

}