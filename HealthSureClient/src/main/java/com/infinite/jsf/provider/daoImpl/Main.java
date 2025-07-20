package com.infinite.jsf.provider.daoImpl;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

import com.infinite.jsf.provider.dao.AppointmentDao;
import com.infinite.jsf.provider.model.Appointment;

public class Main {

	public static void main(String[] args) {
		AppointmentDao appointmentDao = new AppointmentDaoImpl();

		String availabilityId = "AVAIL006";  // Existing availability ID
		String recipientId = "REC001";      // Existing recipient ID
		String doctorId = "DOC001";         // Existing doctor ID
		String appointmentId = "APPT006";   // Appointment to cancel/update/test
		int slotNo = 2;

		// ✅ 1. getBookedCountForAvailability
		int count = appointmentDao.getBookedCountForAvailability(availabilityId);
		System.out.println("📦 Booked Count for " + availabilityId + ": " + count);

		// ✅ 2. hasOverlappingAppointment (not implemented in your code yet)
		System.out.println("⚠️ hasOverlappingAppointment is not implemented yet.");

		// ✅ 3. getAvailableSlotNumbers
		List<Integer> availableSlots = appointmentDao.getAvailableSlotNumbers(availabilityId);
		System.out.println("🪑 Available Slots: " + availableSlots);

		// ✅ 4. isSlotAlreadyBooked
		boolean isBooked = appointmentDao.isSlotAlreadyBooked(availabilityId, slotNo);
		System.out.println("🔐 Is Slot " + slotNo + " Already Booked: " + isBooked);

		// ✅ 5. getAppointmentsByAvailability
		List<Appointment> appList = appointmentDao.getAppointmentsByAvailability(availabilityId);
		System.out.println("📅 Appointments for Availability:");
		for (Appointment a : appList) {
			System.out.println("➤ ID: " + a.getAppointmentId() + ", Slot: " + a.getSlotNo());
		}

		// ✅ 6. isAppointmentInPast
		boolean isPast = appointmentDao.isAppointmentInPast(appointmentId);
		System.out.println("🕒 Is Appointment in Past: " + isPast);

		// ✅ 7. getAppointmentsByDoctorAndDate
		List<Appointment> doctorAppointments = appointmentDao.getAppointmentsByDoctorAndDate(doctorId, Date.valueOf(LocalDate.now()));
		System.out.println("👨‍⚕️ Appointments for Doctor " + doctorId + " today:");
		for (Appointment a : doctorAppointments) {
			System.out.println("➤ " + a.getAppointmentId() + ", Slot: " + a.getSlotNo());
		}

		// ✅ 8. isSlotTimeInFuture
		boolean futureSlot = appointmentDao.isSlotTimeInFuture(availabilityId, slotNo);
		System.out.println("⏳ Is Slot " + slotNo + " in Future: " + futureSlot);
	}
}
