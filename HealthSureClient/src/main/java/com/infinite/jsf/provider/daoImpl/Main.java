package com.infinite.jsf.provider.daoImpl;

import com.infinite.jsf.provider.dao.AppointmentDao;
import com.infinite.jsf.provider.dao.DoctorAvailabilityDao;
import com.infinite.jsf.provider.dao.DoctorDao;

import com.infinite.jsf.provider.model.Appointment;
import com.infinite.jsf.provider.model.DoctorAvailability;
import com.infinite.jsf.provider.model.Doctors;
import com.infinite.jsf.provider.model.Provider;
import com.infinite.jsf.recipient.model.Recipient;

import com.infinite.jsf.util.SessionHelper; // Assuming SessionHelper is in util package
import org.hibernate.Session; // Import Session

public class Main {

	public static void main(String[] args) {
		// Initialize DAOs
		AppointmentDao appointmentDao = new AppointmentDaoImpl();

		Session session = null; // Declare session here to ensure it's closed in finally block

		try {
			// Fetch existing entities from the database
			// You need a Hibernate session to fetch these, so open one.
			Provider provider = new Provider();
			provider.setProviderId("PROV001");

			Doctors doctor = new Doctors();
			doctor.setDoctorId("DOC001");

			Recipient recipient = new Recipient();
			recipient.sethId("REC001");

			session = SessionHelper.getSessionFactory().openSession();

			// Fetch Provider
			if (provider == null) {
				System.err.println("Error: Provider PROV001 not found.");
				return;
			}

			// Fetch Doctor
			if (doctor == null) {
				System.err.println("Error: Doctor DOC001 not found.");
				return;
			}

			// Fetch Recipient
			if (recipient == null) {
				System.err.println("Error: Recipient REC001 not found.");
				return;
			}

			// Fetch DoctorAvailability (choose a future one from your sample data, e.g.,
			// AVAIL004)
			DoctorAvailability doctorAvailability = new DoctorAvailability();
			doctorAvailability.setAvailabilityId("AVAIL005");
			if (doctorAvailability == null) {
				System.err.println("Error: DoctorAvailability AVAIL004 not found.");
				return;
			}

			// Create a new Appointment object
			Appointment newAppointment = new Appointment();
			newAppointment.setProvider(provider);
			newAppointment.setDoctor(doctor);
			newAppointment.setRecipient(recipient);
			newAppointment.setAvailability(doctorAvailability);
			newAppointment.setSlotNo(1); // Assuming slot 1 is desired for AVAIL004

			// Book the appointment
			System.out.println("Attempting to book a new appointment...");
			String bookingResult = appointmentDao.bookAnAppointment(newAppointment);
			System.out.println("Booking Result: " + bookingResult);

			// You can optionally try to retrieve the newly booked appointment if successful
			if (bookingResult.startsWith("Appointment booked successfully")) {
				// The ID is part of the result string, you might parse it or
				// if the Appointment object was updated by the DAO, you can get it directly.
				// For simplicity, we'll just print the result string.
				System.out.println("New Appointment ID: " + newAppointment.getAppointmentId());
			}

		} catch (Exception e) {
			System.err.println("An error occurred in Main: " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (session != null && session.isOpen()) {
				session.close(); // Close the session
			}

		}
	}
}
