/*
 * -----------------------------------------------------------------------------
 * Copyright © 2025 Infinite Computer Solution. All rights reserved.
 * -----------------------------------------------------------------------------
 *
 * @Author   : Sandhan Meher
 * @Purpose  : This class provides the data access object (DAO) implementation
 * for managing appointments. It handles CRUD operations for appointments,
 * including booking, cancellation, and retrieval based on various criteria.
 * It also incorporates business logic validations related to appointment scheduling,
 * such as checking for overlaps, doctor/recipient status, and availability capacity.
 *
 * -----------------------------------------------------------------------------
 */
package com.infinite.jsf.provider.daoImpl;

import java.math.BigInteger;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.New;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.infinite.jsf.constant.AppointmentConstantMessage; // Import the constants class
import com.infinite.jsf.provider.dao.AppointmentDao;
import com.infinite.jsf.provider.model.Appointment;
import com.infinite.jsf.provider.model.AppointmentStatus;
import com.infinite.jsf.provider.model.DoctorAvailability;
import com.infinite.jsf.provider.model.DoctorStatus;
import com.infinite.jsf.recipient.model.RecipientStatus;
import com.infinite.jsf.util.SessionHelper;

public class AppointmentDaoImpl implements AppointmentDao {

	/**
	 * Generates the next unique appointment ID in "APPTXXXXXX" format (e.g., APPT000101).
	 * It queries the database to find the highest existing numeric suffix and increments it.
	 * If no existing appointments are found, it starts from "APPT000101".
	 *
	 * @param session The Hibernate session to use for querying.
	 * @return A {@code String} representing the newly generated appointment ID.
	 */
	public static String generateNextAppointmentId(Session session) {
		String prefix = "APPT";

		try {
			// Native SQL for extracting numeric part
			String sql = "SELECT MAX(CAST(SUBSTRING(appointment_id, 5) AS UNSIGNED)) FROM appointment";
			Query query = session.createSQLQuery(sql);

			Number result = (Number) query.uniqueResult(); // It returns Long or BigInteger
			int nextNumber = (result != null) ? result.intValue() + 1 : 101;

			// Format to 6-digit padded number: APPT000101, APPT000124
			return prefix + String.format("%06d", nextNumber);

		} catch (Exception e) {
			e.printStackTrace();
			// In case of any error, fallback to APPT000001
			return prefix + "000001";
		}
	}

	/**
	 * Books a new appointment in the system. This method performs extensive validations
	 * before persisting the appointment, including checks for past dates, doctor/recipient
	 * active status, overlapping appointments for recipient and doctor, slot availability,
	 * max capacity, number of upcoming appointments, pending appointments with the same doctor,
	 * valid slot numbers, future availability, and adherence to booking time limits and working hours.
	 *
	 * @param appointment The {@code Appointment} object containing details for the new booking.
	 * @return A {@code String} message indicating the success or failure of the booking operation,
	 * along with the new appointment ID if successful.
	 */
	public String bookAnAppointment(Appointment appointment) {
		Transaction tx = null;
		String result = null;
		Session session = null;

		try {
			// 1. Load the availability details
			DoctorAvailabilityDaoImpl availabilityDao = new DoctorAvailabilityDaoImpl();
			DoctorAvailability doctoravail = availabilityDao
					.getAvailabilityById(appointment.getAvailability().getAvailabilityId());

			if (doctoravail == null) {
				return AppointmentConstantMessage.INVALID_AVAILABILITY_SLOT;
			}

			// 2. Set calculated start and end times for the appointment based on availability and slot number
			appointment.setAvailability(doctoravail);
			long slotSt = Timestamp
					.valueOf(doctoravail.getStartTime().toLocalTime()
							.atDate(((Date) doctoravail.getAvailableDate()).toLocalDate()))
					.getTime() + (appointment.getSlotNo() - 1) * doctoravail.getPatientWindow() * 60 * 1000;
			long slotEn = slotSt + doctoravail.getPatientWindow() * 60 * 1000;
			appointment.setStart(new Timestamp(slotSt));
			appointment.setEnd(new Timestamp(slotEn));

			session = SessionHelper.getSessionFactory().openSession();
			tx = session.beginTransaction();

			String availabilityId = appointment.getAvailability().getAvailabilityId();
			String recipientId = appointment.getRecipient().gethId();
			String doctorId = appointment.getDoctor().getDoctorId();
			int slotNo = appointment.getSlotNo();
			Timestamp now = new Timestamp(System.currentTimeMillis());

			// VALIDATION 1: Prevent booking in the past
			if (appointment.getStart().before(now)) {
				return AppointmentConstantMessage.CANNOT_BOOK_PAST_APPOINTMENT;
			}

			// VALIDATION 2: Check if doctor is active
			Query doctorStatusQuery = session
					.createQuery("SELECT d.status FROM Doctors d WHERE d.doctorId = :doctorId");
			doctorStatusQuery.setParameter("doctorId", doctorId);
			DoctorStatus doctorStatus = (DoctorStatus) doctorStatusQuery.uniqueResult();

			if (doctorStatus == null || !"ACTIVE".equals(doctorStatus.name())) {
				return AppointmentConstantMessage.DOCTOR_NOT_ACTIVE;
			}

			// VALIDATION 3: Check if recipient is active
			Query recipientStatusQuery = session
					.createQuery("SELECT r.status FROM Recipient r WHERE r.hId = :recipientId");
			recipientStatusQuery.setParameter("recipientId", recipientId);
			RecipientStatus recipientStatus = (RecipientStatus) recipientStatusQuery.uniqueResult();

			if (recipientStatus == null || !"ACTIVE".equals(recipientStatus.name())) {
				return AppointmentConstantMessage.RECIPIENT_NOT_ACTIVE;
			}

			// VALIDATION 4: Check if recipient already has overlapping appointment
			Query overlapQuery = session.createQuery(
					"FROM Appointment a WHERE a.recipient.hId = :recipientId AND a.status IN ('BOOKED', 'PENDING') "
							+ "AND ((a.start < :endTime AND a.end > :startTime) OR "
							+ "(a.start = :startTime AND a.end = :endTime))");
			overlapQuery.setParameter("recipientId", recipientId);
			overlapQuery.setParameter("startTime", appointment.getStart());
			overlapQuery.setParameter("endTime", appointment.getEnd());

			if (!overlapQuery.list().isEmpty()) {
				return AppointmentConstantMessage.RECIPIENT_OVERLAPPING_APPOINTMENT;
			}

			// VALIDATION 5: Check if the slot number is already booked in this availability
			Query slotQuery = session
					.createQuery("FROM Appointment WHERE availability.availabilityId = :availabilityId "
							+ "AND slotNo = :slotNo AND status IN ('BOOKED', 'PENDING')");
			slotQuery.setParameter("availabilityId", availabilityId);
			slotQuery.setParameter("slotNo", slotNo);

			if (!slotQuery.list().isEmpty()) {
				return AppointmentConstantMessage.SLOT_ALREADY_BOOKED;
			}

			// VALIDATION 6: Check if max capacity is reached for this availability
			Query bookedCountQuery = session
					.createQuery("SELECT COUNT(*) FROM Appointment WHERE availability.availabilityId = :availabilityId "
							+ "AND status IN ('BOOKED', 'PENDING')");
			bookedCountQuery.setParameter("availabilityId", availabilityId);
			long bookedCount = (Long) bookedCountQuery.uniqueResult();

			int maxCapacity = appointment.getAvailability().getMaxCapacity();
			if (bookedCount >= maxCapacity) {
				return AppointmentConstantMessage.AVAILABILITY_FULL;
			}

			// VALIDATION 7: Check if recipient already has 10 upcoming appointments
			Query upcomingQuery = session
					.createQuery("SELECT COUNT(*) FROM Appointment WHERE recipient.hId = :recipientId "
							+ "AND status IN ('BOOKED', 'PENDING') AND start > :now");
			upcomingQuery.setParameter("recipientId", recipientId);
			upcomingQuery.setParameter("now", now);

			long upcomingCount = (Long) upcomingQuery.uniqueResult();
			if (upcomingCount >= 10) {
				return AppointmentConstantMessage.RECIPIENT_MAX_UPCOMING_APPOINTMENTS_REACHED;
			}

			// VALIDATION 8: Check if recipient has any pending/booked appointments with same doctor
			Query pendingWithDoctorQuery = session
					.createQuery("FROM Appointment a WHERE a.recipient.hId = :recipientId "
							+ "AND a.doctor.doctorId = :doctorId AND a.status IN( 'PENDING','BOOKED' ) AND a.start > :currentTime");
			pendingWithDoctorQuery.setParameter("recipientId", recipientId);
			pendingWithDoctorQuery.setParameter("doctorId", doctorId);
			pendingWithDoctorQuery.setParameter("currentTime", new Timestamp(System.currentTimeMillis()));

			List<Appointment> pendingAppointments = pendingWithDoctorQuery.list();
			if (!pendingAppointments.isEmpty()) {
				Appointment existingAppointment = pendingAppointments.get(0);
				DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
				DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

				String formattedDate = existingAppointment.getStart().toLocalDateTime().format(dateFormatter);
				String formattedTime = existingAppointment.getStart().toLocalDateTime().format(timeFormatter);

				return AppointmentConstantMessage.PENDING_APPOINTMENT_WITH_DOCTOR_PREFIX + formattedDate
						+ AppointmentConstantMessage.PENDING_APPOINTMENT_WITH_DOCTOR_AT + formattedTime
						+ AppointmentConstantMessage.PENDING_APPOINTMENT_WITH_DOCTOR_SUFFIX;
			}

			// VALIDATION 9: Check if slot number is within valid range
			if (slotNo < 1 || slotNo > maxCapacity) {
				return AppointmentConstantMessage.INVALID_SLOT_NUMBER;
			}

			// VALIDATION 10: Check if availability date is in the future (or today)
			if (doctoravail.getAvailableDate().before(Date.valueOf(LocalDate.now()))) {
				return AppointmentConstantMessage.CANNOT_BOOK_PAST_DATE;
			}

			// VALIDATION 11: Check if doctor has any scheduling conflicts
			// This checks if the doctor already has another booking overlapping with the new requested time
			Query doctorOverlapQuery = session.createQuery("FROM Appointment a WHERE a.doctor.doctorId = :doctorId "
					+ "AND a.status IN ('BOOKED', 'PENDING') " + "AND ((a.start < :endTime AND a.end > :startTime))");
			doctorOverlapQuery.setParameter("doctorId", doctorId);
			doctorOverlapQuery.setParameter("startTime", appointment.getStart());
			doctorOverlapQuery.setParameter("endTime", appointment.getEnd());

			if (!doctorOverlapQuery.list().isEmpty()) {
				return AppointmentConstantMessage.DOCTOR_SCHEDULING_CONFLICT;
			}

			// VALIDATION 12: Check if the appointment is too far in the future (e.g., 6 months)
			LocalDate maxFutureDate = LocalDate.now().plusMonths(6);
			if (doctoravail.getAvailableDate().after(Date.valueOf(maxFutureDate))) {
				return AppointmentConstantMessage.APPOINTMENT_TOO_FAR_IN_FUTURE;
			}

			// VALIDATION 13: Check if the appointment is within working hours (8 AM to 8 PM)
			if (appointment.getStart().toLocalDateTime().toLocalTime().isBefore(LocalTime.of(8, 0))
					|| appointment.getEnd().toLocalDateTime().toLocalTime().isAfter(LocalTime.of(20, 0))) {
				return AppointmentConstantMessage.APPOINTMENT_OUTSIDE_WORKING_HOURS;
			}

			// VALIDATION 14: Check minimum notice period (e.g., 2 hours before appointment)
			LocalDateTime minNoticeTime = LocalDateTime.now().plusHours(2);
			if (appointment.getStart().toLocalDateTime().isBefore(minNoticeTime)) {
				return AppointmentConstantMessage.MINIMUM_NOTICE_PERIOD_REQUIRED;
			}

			// All validations passed - save the appointment
			appointment.setRequestedAt(now);
			appointment.setStatus(AppointmentStatus.PENDING); // New appointments are PENDING initially
			appointment.setAppointmentId(generateNextAppointmentId(session)); // Generate unique ID

			session.save(appointment);
			tx.commit();

			result = AppointmentConstantMessage.APPOINTMENT_REQUESTED_SUCCESS + appointment.getAppointmentId();
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			result = AppointmentConstantMessage.ERROR_BOOKING_APPOINTMENT + e.getMessage();
		} finally {
			if (session != null) {
				session.close();
			}
		}

		return result;
	}

	/**
	 * Checks if all slots for a given doctor availability ID are currently booked or pending.
	 *
	 * @param availabilityId The ID of the doctor's availability.
	 * @return {@code true} if the slot is full (booked up to max capacity), {@code false} otherwise.
	 */
	@Override
	public boolean isAvailabilitySlotFull(String availabilityId) {
		try {
			Session session = SessionHelper.getSessionFactory().openSession();
			// Step 1: Get total booked/pending appointments for the availability
			Query countQuery = session
					.createQuery("SELECT COUNT(*) FROM Appointment WHERE availability.availabilityId = :availabilityId "
							+ "AND status IN ('BOOKED', 'PENDING')");
			countQuery.setParameter("availabilityId", availabilityId);
			long bookedCount = (Long) countQuery.uniqueResult();

			// Step 2: Get max capacity from DoctorAvailability
			Query capacityQuery = session.createQuery(
					"SELECT a.maxCapacity FROM DoctorAvailability a WHERE a.availabilityId = :availabilityId");
			capacityQuery.setParameter("availabilityId", availabilityId);
			Integer maxCapacity = (Integer) capacityQuery.uniqueResult();

			if (maxCapacity == null) {
				// If availability not found, it can't be full in a meaningful way in this context
				return false;
			}

			return bookedCount >= maxCapacity;
		} catch (Exception e) {
			e.printStackTrace();
			// On error, conservatively assume not full to avoid preventing bookings
			return false;
		}
	}

	/**
	 * Retrieves a list of upcoming appointments for a specific recipient.
	 * Upcoming appointments are those whose start time is in the future,
	 * regardless of their current status (BOOKED, PENDING, or CANCELLED, though CANCELLED
	 * might be filtered out at the UI layer if not relevant). The list is ordered by start time ascending.
	 *
	 * @param recipientId The unique ID of the recipient.
	 * @return A {@code List} of {@code Appointment} objects, or an empty list if none are found or an error occurs.
	 */
	@Override
	public List<Appointment> getUpcomingAppointmentsByRecipient(String recipientId) {
		try {
			Session session = SessionHelper.getSessionFactory().openSession();
			Timestamp now = new Timestamp(System.currentTimeMillis());

			Query query = session.createQuery("FROM Appointment a WHERE a.recipient.hId = :recipientId "
					+ "AND a.status IN ('BOOKED', 'PENDING', 'CANCELLED') " // Include CANCELLED for history
					+ "AND a.start > :now ORDER BY a.start ASC");

			query.setParameter("recipientId", recipientId);
			query.setParameter("now", now);

			@SuppressWarnings("unchecked")
			List<Appointment> list = query.list();

			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>(); // Return empty list instead of null on error
		}
	}

	/**
	 * Retrieves a list of past appointments for a specific recipient.
	 * Past appointments are those whose start time is in the past.
	 * The list is ordered by start time descending.
	 *
	 * @param recipientId The unique ID of the recipient.
	 * @return A {@code List} of {@code Appointment} objects, or an empty list if none are found or an error occurs.
	 */
	@Override
	public List<Appointment> getPastAppointmentsByRecipient(String recipientId) {
		try {
			Session session = SessionHelper.getSessionFactory().openSession();
			Timestamp now = new Timestamp(System.currentTimeMillis());

			Query query = session.createQuery("FROM Appointment a WHERE a.recipient.hId = :recipientId "
					+ "AND a.start < :now ORDER BY a.start DESC"); // Order by latest first
			query.setParameter("recipientId", recipientId);
			query.setParameter("now", now);

			@SuppressWarnings("unchecked")
			List<Appointment> list = query.list();

			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>(); // Return empty list instead of null on error
		}
	}

	/**
	 * Retrieves a single appointment by its unique ID.
	 *
	 * @param appointmentId The unique ID of the appointment.
	 * @return The {@code Appointment} object if found, otherwise {@code null}.
	 */
	@Override
	public Appointment getAppointmentById(String appointmentId) {
		try {
			Session session = SessionHelper.getSessionFactory().openSession();
			return (Appointment) session.get(Appointment.class, appointmentId);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Cancels an existing appointment. The appointment's status is updated to {@code CANCELLED}
	 * and the {@code cancelledAt} timestamp is set. Only future appointments can be cancelled.
	 *
	 * @param appointmentId The ID of the appointment to cancel.
	 * @return {@code true} if the appointment was successfully cancelled, {@code false} otherwise
	 * (e.g., appointment not found or already in the past).
	 */
	@Override
	public boolean cancelAppointment(String appointmentId) {
		Transaction tx = null;
		try {
			Session session = SessionHelper.getSessionFactory().openSession();
			tx = session.beginTransaction();

			Appointment appointment = (Appointment) session.get(Appointment.class, appointmentId);
			if (appointment == null) {
				return false; // No such appointment
			}

			// Check if appointment is in the future
			Timestamp now = new Timestamp(System.currentTimeMillis());
			if (appointment.getStart() != null && appointment.getStart().before(now)) {
				return false; // Past appointment cannot be cancelled
			}

			appointment.setStatus(AppointmentStatus.CANCELLED);
			appointment.setCancelledAt(now);

			session.update(appointment);
			tx.commit();
			return true;
		} catch (Exception e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
			return false;
		} finally {
			// Ensure session is closed even if an exception occurs
			// This might be managed by SessionHelper or a filter in a real application
			// if (session != null && session.isOpen()) {
			// session.close();
			// }
		}
	}

	/**
	 * Updates an existing appointment with new details, typically for rescheduling.
	 * Validations include checking if the original appointment exists, if it's a future appointment,
	 * if the new slot overlaps with other existing appointments for the recipient, and if the new slot
	 * is already taken in the selected availability.
	 *
	 * @param updatedAppointment The {@code Appointment} object containing the new details for the update.
	 * The {@code appointmentId} in this object must match an existing appointment.
	 * @return A {@code String} message indicating the success or failure of the update operation.
	 */
	@Override
	public String updateAppointment(Appointment updatedAppointment) {
	    Transaction tx = null;
	    Session session = null;
	    try {
	        session = SessionHelper.getSessionFactory().openSession();
	        tx = session.beginTransaction();

	        // Load the original appointment
	        Appointment existing = (Appointment) session.get(Appointment.class, updatedAppointment.getAppointmentId());
	        if (existing == null) {
	            return AppointmentConstantMessage.APPOINTMENT_NOT_FOUND;
	        }
	    	DoctorAvailabilityDaoImpl availabilityDao = new DoctorAvailabilityDaoImpl();
			DoctorAvailability doctoravail = availabilityDao
					.getAvailabilityById(updatedAppointment.getAvailability().getAvailabilityId());

			if (doctoravail == null) {
				return AppointmentConstantMessage.INVALID_AVAILABILITY_SLOT;
			}

			// Set calculated start and end times for the updated appointment
			updatedAppointment.setAvailability(doctoravail);
			long slotSt = Timestamp
					.valueOf(doctoravail.getStartTime().toLocalTime()
							.atDate(((Date) doctoravail.getAvailableDate()).toLocalDate()))
					.getTime() + (updatedAppointment.getSlotNo() - 1) * doctoravail.getPatientWindow() * 60 * 1000;
			long slotEn = slotSt + doctoravail.getPatientWindow() * 60 * 1000;
			updatedAppointment.setStart(new Timestamp(slotSt));
			updatedAppointment.setEnd(new Timestamp(slotEn));

	        // Allow update only if appointment is in the future
	        Timestamp now = new Timestamp(System.currentTimeMillis());
	        if (existing.getStart() != null && existing.getStart().before(now)) {
	            return AppointmentConstantMessage.CANNOT_UPDATE_PAST_APPOINTMENTS;
	        }

	        String availabilityId = updatedAppointment.getAvailability().getAvailabilityId();
	        String recipientId = updatedAppointment.getRecipient().gethId();
	        int slotNo = updatedAppointment.getSlotNo();

	        // Check if new slot overlaps with another existing appointment of recipient (excluding the current one being updated)
	        Query overlapQuery = session.createQuery(
	            "FROM Appointment a WHERE a.recipient.hId = :recipientId AND a.status IN ('BOOKED', 'PENDING') " +
	            "AND ((a.start < :endTime AND a.end > :startTime)) AND a.appointmentId != :currentId" // Corrected overlap logic for strict intersection and exclusion of current
	        );
	        overlapQuery.setParameter("recipientId", recipientId);
	        overlapQuery.setParameter("startTime", updatedAppointment.getStart());
	        overlapQuery.setParameter("endTime", updatedAppointment.getEnd());
	        overlapQuery.setParameter("currentId", updatedAppointment.getAppointmentId());

	        if (!overlapQuery.list().isEmpty()) {
	            return AppointmentConstantMessage.NEW_SLOT_OVERLAPS_EXISTING;
	        }

	        // Check if the new slot number is already taken in same availability (excluding the current one being updated)
	        Query slotQuery = session.createQuery(
	            "FROM Appointment a WHERE a.availability.availabilityId = :availabilityId AND a.slotNo = :slotNo " +
	            "AND a.status IN ('BOOKED', 'PENDING') AND a.appointmentId != :currentId"
	        );
	        slotQuery.setParameter("availabilityId", availabilityId);
	        slotQuery.setParameter("slotNo", slotNo);
	        slotQuery.setParameter("currentId", updatedAppointment.getAppointmentId());

	        if (!slotQuery.list().isEmpty()) {
	            return AppointmentConstantMessage.NEW_SLOT_ALREADY_BOOKED_BY_SOMEONE_ELSE;
	        }
	        
	        // VALIDATION: Check if the updated appointment time is in the past
	        if (updatedAppointment.getStart().before(now)) {
	            return AppointmentConstantMessage.CANNOT_RESCHEDULE_TO_PAST;
	        }

	        // Update details of the existing appointment object
	        existing.setAvailability(updatedAppointment.getAvailability());
	        existing.setSlotNo(slotNo);
	        existing.setStart(updatedAppointment.getStart());
	        existing.setEnd(updatedAppointment.getEnd());
	        existing.setNotes(updatedAppointment.getNotes());
	        existing.setBookedAt(now); // Update bookedAt to reflect reschedule time

	        session.update(existing);
	        tx.commit();
	        return AppointmentConstantMessage.APPOINTMENT_UPDATED_SUCCESS;
	    } catch (Exception e) {
	        if (tx != null)
	            tx.rollback();
	        e.printStackTrace();
	        return AppointmentConstantMessage.ERROR_UPDATING_APPOINTMENT + e.getMessage();
	    } finally {
	        if (session != null) {
	            session.close();
	        }
	    }
	}


	/**
	 * Retrieves the count of booked and pending appointments for a specific doctor availability.
	 *
	 * @param availabilityId The ID of the doctor's availability.
	 * @return The number of booked/pending appointments for that availability. Returns 0 on error.
	 */
	@Override
	public int getBookedCountForAvailability(String availabilityId) {
		int count = 0;
		Session session = null;
		try {
			session = SessionHelper.getSessionFactory().openSession();
			Query query = session.createQuery(
					"SELECT COUNT(*) FROM Appointment a " + "WHERE a.availability.availabilityId = :availabilityId "
							+ "AND a.status IN ('BOOKED', 'PENDING')");
			query.setParameter("availabilityId", availabilityId);
			Long result = (Long) query.uniqueResult();
			count = result != null ? result.intValue() : 0;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return count;
	}

	/**
	 * This method is not fully implemented in the provided code. It is intended to check
	 * if a recipient has an overlapping appointment within a specific availability slot.
	 *
	 * @param recipientId The ID of the recipient.
	 * @param availabilityId The ID of the doctor's availability.
	 * @param start The start timestamp of the proposed slot.
	 * @param end The end timestamp of the proposed slot.
	 * @return {@code true} if an overlapping appointment exists, {@code false} otherwise.
	 */
	@Override
	public boolean hasOverlappingAppointment(String recipientId, String availabilityId, Timestamp start,
			Timestamp end) {
		// TODO: Implement the actual logic for checking overlapping appointments if needed.
		// The current implementation in bookAnAppointment and updateAppointment already handles this for recipient.
		return false;
	}

	/**
	 * Retrieves a list of available slot numbers for a given doctor availability.
	 * It determines the total capacity and subtracts the already booked/pending slots.
	 *
	 * @param availabilityId The ID of the doctor's availability.
	 * @return A {@code List} of {@code Integer} representing the available slot numbers.
	 * Returns an empty list if no slots are available or on error.
	 */
	@Override
	public List<Integer> getAvailableSlotNumbers(String availabilityId) {
		List<Integer> availableSlots = new ArrayList<>();
		Session session = null;
		try {
			session = SessionHelper.getSessionFactory().openSession();
			// Step 1: Get max_capacity from DoctorAvailability
			Query capacityQuery = session.createQuery(
					"SELECT da.maxCapacity FROM DoctorAvailability da WHERE da.availabilityId = :availabilityId");
			capacityQuery.setParameter("availabilityId", availabilityId);
			Integer maxCapacity = (Integer) capacityQuery.uniqueResult();

			if (maxCapacity == null || maxCapacity <= 0) {
				return availableSlots; // return empty list if invalid capacity or availability not found
			}

			// Step 2: Get all booked and pending slot numbers for this availability
			Query bookedQuery = session.createQuery(
					"SELECT a.slotNo FROM Appointment a " + "WHERE a.availability.availabilityId = :availabilityId "
							+ "AND a.status IN ('BOOKED', 'PENDING')");
			bookedQuery.setParameter("availabilityId", availabilityId);
			@SuppressWarnings("unchecked")
			List<Integer> bookedSlots = bookedQuery.list();

			// Step 3: Prepare the full range of slots and subtract booked slots
			for (int i = 1; i <= maxCapacity; i++) {
				if (!bookedSlots.contains(i)) {
					availableSlots.add(i);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return availableSlots;
	}

	/**
	 * Checks if a specific slot number within a given doctor availability is already booked or pending.
	 *
	 * @param availabilityId The ID of the doctor's availability.
	 * @param slotNo The specific slot number to check.
	 * @return {@code true} if the slot is already booked or pending, {@code false} otherwise. Returns {@code false} on error.
	 */
	@Override
	public boolean isSlotAlreadyBooked(String availabilityId, int slotNo) {
		Session session = null;
		try {
			session = SessionHelper.getSessionFactory().openSession();
			Query query = session.createQuery(
					"SELECT count(*) FROM Appointment a " + "WHERE a.availability.availabilityId = :availabilityId "
							+ "AND a.slotNo = :slotNo AND a.status IN ('BOOKED', 'PENDING')");
			query.setParameter("availabilityId", availabilityId);
			query.setParameter("slotNo", slotNo);

			Long count = (Long) query.uniqueResult();
			return count != null && count > 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false; // return false on error to avoid false positives
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}

	/**
	 * Retrieves all appointments associated with a specific doctor availability.
	 * The appointments are ordered by slot number in ascending order.
	 *
	 * @param availabilityId The ID of the doctor's availability.
	 * @return A {@code List} of {@code Appointment} objects, or an empty list if none are found or an error occurs.
	 */
	@Override
	public List<Appointment> getAppointmentsByAvailability(String availabilityId) {
		List<Appointment> appointments = new ArrayList<>(); // Initialize to empty list
		Session session = null;
		try {
			session = SessionHelper.getSessionFactory().openSession();
			Query query = session.createQuery(
					"FROM Appointment a WHERE a.availability.availabilityId = :availabilityId ORDER BY a.slotNo ASC");
			query.setParameter("availabilityId", availabilityId);

			appointments = query.list();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return appointments;
	}

	/**
	 * Checks if a specific appointment's start time is in the past relative to the current time.
	 *
	 * @param appointmentId The ID of the appointment to check.
	 * @return {@code true} if the appointment is in the past, {@code false} otherwise.
	 * Returns {@code false} if the appointment is not found or on error.
	 */
	@Override
	public boolean isAppointmentInPast(String appointmentId) {
		Session session = null;
		try {
			session = SessionHelper.getSessionFactory().openSession();
			Appointment appointment = (Appointment) session.get(Appointment.class, appointmentId);

			if (appointment != null && appointment.getStart() != null) {
				Timestamp now = new Timestamp(System.currentTimeMillis());
				return appointment.getStart().before(now);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return false;
	}

	/**
	 * Retrieves a list of appointments for a specific doctor on a given date.
	 * The comparison is done based on the date part of the appointment's start timestamp.
	 *
	 * @param doctorId The ID of the doctor.
	 * @param date The specific {@code java.sql.Date} to filter appointments by.
	 * @return A {@code List} of {@code Appointment} objects, or an empty list if none are found or an error occurs.
	 */
	@Override
	public List<Appointment> getAppointmentsByDoctorAndDate(String doctorId, Date date) {
		List<Appointment> appointments = new ArrayList<>(); // Initialize to empty list
		Session session = null;
		try {
			session = SessionHelper.getSessionFactory().openSession();
			Query query = session.createQuery(
					"FROM Appointment a WHERE a.doctor.doctorId = :doctorId AND DATE(a.start) = :appointmentDate");
			query.setParameter("doctorId", doctorId);
			query.setParameter("appointmentDate", date);

			appointments = query.list();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return appointments;
	}

	/**
	 * Checks if a specific slot time within a given doctor availability is in the future relative to the current time.
	 * It calculates the exact start timestamp of the slot based on the availability's date, start time, patient window, and slot number.
	 *
	 * @param availabilityId The ID of the doctor's availability.
	 * @param slotNo The specific slot number within that availability.
	 * @return {@code true} if the calculated slot start time is in the future, {@code false} otherwise.
	 * Returns {@code false} if the availability is not found, patient window is invalid, or on error.
	 */
	@Override
	public boolean isSlotTimeInFuture(String availabilityId, int slotNo) {
		Session session = null;
		try {
			session = SessionHelper.getSessionFactory().openSession();
			DoctorAvailability availability = (DoctorAvailability) session.get(DoctorAvailability.class,
					availabilityId);
			if (availability == null)
				return false;

			// Calculate slot duration
			int windowMinutes = availability.getPatientWindow();
			if (windowMinutes <= 0)
				return false;

			// Combine availability date and start time into a single Timestamp
			java.sql.Time startTime = availability.getStartTime();
			// Using LocalDateTime to handle potential date-time conversions accurately
			LocalDateTime availableLocalDateTime = LocalDateTime.of(availability.getAvailableDate().toLocalDate(), startTime.toLocalTime());
			Timestamp availableDateTime = Timestamp.valueOf(availableLocalDateTime);

			// Calculate the start time of the specific slot
			long slotStartMillis = availableDateTime.getTime() + (long)(slotNo - 1) * windowMinutes * 60 * 1000L;
			Timestamp slotStart = new Timestamp(slotStartMillis);

			// Compare with current time
			return slotStart.after(new Timestamp(System.currentTimeMillis()));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}
}
