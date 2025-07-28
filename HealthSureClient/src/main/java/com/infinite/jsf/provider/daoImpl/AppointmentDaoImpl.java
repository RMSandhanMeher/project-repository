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

import com.infinite.jsf.provider.dao.AppointmentDao;
import com.infinite.jsf.provider.model.Appointment;
import com.infinite.jsf.provider.model.AppointmentStatus;
import com.infinite.jsf.provider.model.DoctorAvailability;
import com.infinite.jsf.provider.model.DoctorStatus;
import com.infinite.jsf.recipient.model.RecipientStatus;
import com.infinite.jsf.util.SessionHelper;

public class AppointmentDaoImpl implements AppointmentDao {

	// Generates the next appointment ID in APPT### format
	public static String generateNextAppointmentId(Session session) {
		String prefix = "APPT";

		try {
			// Native SQL for extracting numeric part
			String sql = "SELECT MAX(CAST(SUBSTRING(appointment_id, 5) AS UNSIGNED)) FROM appointment";
			Query query = session.createSQLQuery(sql);

			Number result = (Number) query.uniqueResult(); // It returns Long or BigInteger
			int nextNumber = (result != null) ? result.intValue() + 1 : 101;

			// Format to 3-digit padded number: APPT001, APPT124
			return prefix + String.format("%06d", nextNumber);

		} catch (Exception e) {
			e.printStackTrace();
			// In case of any error, fallback to APPT101
			return prefix + "000001";
		}
	}

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
				return "Invalid availability slot. Please select a valid time slot.";
			}

			// 2. Set calculated start and end times for the appointment
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
				return "Cannot book an appointment in the past.";
			}

			// VALIDATION 2: Check if doctor is active
			Query doctorStatusQuery = session
					.createQuery("SELECT d.status FROM Doctors d WHERE d.doctorId = :doctorId");
			doctorStatusQuery.setParameter("doctorId", doctorId);
			DoctorStatus doctorStatus = (DoctorStatus) doctorStatusQuery.uniqueResult();

			if (!"ACTIVE".equals(doctorStatus.name())) {
				return "Doctor is not currently active. Please select another doctor.";
			}

			// VALIDATION 3: Check if recipient is active
			Query recipientStatusQuery = session
					.createQuery("SELECT r.status FROM Recipient r WHERE r.hId = :recipientId");
			recipientStatusQuery.setParameter("recipientId", recipientId);
			RecipientStatus recipientStatus = (RecipientStatus) recipientStatusQuery.uniqueResult();

			if (!"ACTIVE".equals(recipientStatus.name())) {
				return "Your account is not active. Please contact support.";
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
				return "You already have an appointment scheduled during this time.";
			}

			// VALIDATION 5: Check if the slot number is already booked in this availability
			Query slotQuery = session
					.createQuery("FROM Appointment WHERE availability.availabilityId = :availabilityId "
							+ "AND slotNo = :slotNo AND status IN ('BOOKED', 'PENDING')");
			slotQuery.setParameter("availabilityId", availabilityId);
			slotQuery.setParameter("slotNo", slotNo);

			if (!slotQuery.list().isEmpty()) {
				return "This time slot is already booked. Please choose another time.";
			}

			// VALIDATION 6: Check if max capacity is reached for this availability
			Query bookedCountQuery = session
					.createQuery("SELECT COUNT(*) FROM Appointment WHERE availability.availabilityId = :availabilityId "
							+ "AND status IN ('BOOKED', 'PENDING')");
			bookedCountQuery.setParameter("availabilityId", availabilityId);
			long bookedCount = (Long) bookedCountQuery.uniqueResult();

			int maxCapacity = appointment.getAvailability().getMaxCapacity();
			if (bookedCount >= maxCapacity) {
				return "All slots for this availability are already full.";
			}

			// VALIDATION 7: Check if recipient already has 10 upcoming appointments
			Query upcomingQuery = session
					.createQuery("SELECT COUNT(*) FROM Appointment WHERE recipient.hId = :recipientId "
							+ "AND status IN ('BOOKED', 'PENDING') AND start > :now");
			upcomingQuery.setParameter("recipientId", recipientId);
			upcomingQuery.setParameter("now", now);

			long upcomingCount = (Long) upcomingQuery.uniqueResult();
			if (upcomingCount >= 10) {
				return "You can only have 10 upcoming appointments at a time.";
			}

			// VALIDATION 8: Check if recipient has any pending appointments with same
			// doctor
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

				return "You already have a pending / booked appointment with this doctor on " + formattedDate + " at "
						+ formattedTime + ". Please complete or cancel that appointment first.";
			}

			// VALIDATION 9: Check if slot number is within valid range
			if (slotNo < 1 || slotNo > maxCapacity) {
				return "Invalid slot number. Please select a valid slot.";
			}

			// VALIDATION 10: Check if availability date is in the future
			if (doctoravail.getAvailableDate().before(Date.valueOf(LocalDate.now()))) {
				return "Cannot book appointments for past dates.";
			}

			// VALIDATION 11: Check if doctor has any scheduling conflicts
			Query doctorOverlapQuery = session.createQuery("FROM Appointment a WHERE a.doctor.doctorId = :doctorId "
					+ "AND a.status IN ('BOOKED', 'PENDING') " + "AND ((a.start < :endTime AND a.end > :startTime))");
			doctorOverlapQuery.setParameter("doctorId", doctorId);
			doctorOverlapQuery.setParameter("startTime", appointment.getStart());
			doctorOverlapQuery.setParameter("endTime", appointment.getEnd());

			if (!doctorOverlapQuery.list().isEmpty()) {
				return "Doctor has a scheduling conflict during this time.";
			}

			// VALIDATION 12: Check if the appointment is too far in the future (e.g., 6
			// months)
			LocalDate maxFutureDate = LocalDate.now().plusMonths(6);
			if (doctoravail.getAvailableDate().after(Date.valueOf(maxFutureDate))) {
				return "Appointments can only be booked up to 6 months in advance.";
			}

			// VALIDATION 13: Check if the appointment is within working hours
			if (appointment.getStart().toLocalDateTime().toLocalTime().isBefore(LocalTime.of(8, 0))
					|| appointment.getEnd().toLocalDateTime().toLocalTime().isAfter(LocalTime.of(20, 0))) {
				return "Appointments must be between 8:00 AM and 8:00 PM.";
			}

			// VALIDATION 14: Check minimum notice period (e.g., 2 hours before appointment)
			LocalDateTime minNoticeTime = LocalDateTime.now().plusHours(2);
			if (appointment.getStart().toLocalDateTime().isBefore(minNoticeTime)) {
				return "Appointments must be booked at least 2 hours in advance.";
			}

			// All validations passed - save the appointment
			appointment.setRequestedAt(now);
			appointment.setStatus(AppointmentStatus.PENDING);
			appointment.setAppointmentId(generateNextAppointmentId(session));

			session.save(appointment);
			tx.commit();

			result = "Appointment requested successfully with ID: " + appointment.getAppointmentId();
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			result = "Error booking appointment: " + e.getMessage();
		} finally {
			if (session != null) {
				session.close();
			}
		}

		return result;
	}

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
				return false; // availability not found, assume not full
			}

			return bookedCount >= maxCapacity;
		} catch (Exception e) {
			e.printStackTrace();
			return false; // on error, assume not full (or log as needed)
		}
	}

	@Override
	public List<Appointment> getUpcomingAppointmentsByRecipient(String recipientId) {
		try {
			Session session = SessionHelper.getSessionFactory().openSession();
			Timestamp now = new Timestamp(System.currentTimeMillis());

			Query query = session.createQuery("FROM Appointment a WHERE a.recipient.hId = :recipientId "
					+ "AND a.status IN ('BOOKED', 'PENDING', 'CANCELLED') "
					+ "AND a.start > :now ORDER BY a.start ASC");

			query.setParameter("recipientId", recipientId);
			query.setParameter("now", now);
//			query.setMaxResults(10); // Only next 10 appointments allowed

			@SuppressWarnings("unchecked")
			List<Appointment> list = query.list();

			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public List<Appointment> getPastAppointmentsByRecipient(String recipientId) {
		try {

			Session session = SessionHelper.getSessionFactory().openSession();
			Timestamp now = new Timestamp(System.currentTimeMillis());

			Query query = session.createQuery("FROM Appointment a WHERE a.recipient.hId = :recipientId "
					+ "AND a.start < :now ORDER BY a.start DESC");
			query.setParameter("recipientId", recipientId);
			query.setParameter("now", now);

			@SuppressWarnings("unchecked")
			List<Appointment> list = query.list();

			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

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
				return false; // Past appointment can't be cancelled
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
		}
	}

	@Override
	public String updateAppointment(Appointment updatedAppointment) {
	    Transaction tx = null;
	    try {
	        Session session = SessionHelper.getSessionFactory().openSession();
	        tx = session.beginTransaction();

	        // Load the original appointment
	        Appointment existing = (Appointment) session.get(Appointment.class, updatedAppointment.getAppointmentId());
	        if (existing == null) {
	            return "Appointment not found.";
	        }
	    	DoctorAvailabilityDaoImpl availabilityDao = new DoctorAvailabilityDaoImpl();
			DoctorAvailability doctoravail = availabilityDao
					.getAvailabilityById(updatedAppointment.getAvailability().getAvailabilityId());

			if (doctoravail == null) {
				return "Invalid availability slot. Please select a valid time slot.";
			}

			// 2. Set calculated start and end times for the appointment
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
	            return "Cannot update past appointments.";
	        }

	        String availabilityId = updatedAppointment.getAvailability().getAvailabilityId();
	        String recipientId = updatedAppointment.getRecipient().gethId();
	        int slotNo = updatedAppointment.getSlotNo();

	        // Check if new slot overlaps with another existing appointment of recipient
	        Query overlapQuery = session.createQuery(
	            "FROM Appointment a WHERE a.recipient.hId = :recipientId AND a.status IN ('BOOKED', 'PENDING') " +
	            "AND ((a.start <= :endTime AND a.end >= :startTime)) AND a.appointmentId != :currentId"
	        );
	        overlapQuery.setParameter("recipientId", recipientId);
	        overlapQuery.setParameter("startTime", updatedAppointment.getStart());
	        overlapQuery.setParameter("endTime", updatedAppointment.getEnd());
	        overlapQuery.setParameter("currentId", updatedAppointment.getAppointmentId());

	        if (!overlapQuery.list().isEmpty()) {
	            return "Overlapping appointment exists for the recipient.";
	        }

	        // Check if the new slot number is already taken in same availability
	        Query slotQuery = session.createQuery(
	            "FROM Appointment a WHERE a.availability.availabilityId = :availabilityId AND a.slotNo = :slotNo " +
	            "AND a.status IN ('BOOKED', 'PENDING') AND a.appointmentId != :currentId"
	        );
	        slotQuery.setParameter("availabilityId", availabilityId);
	        slotQuery.setParameter("slotNo", slotNo);
	        slotQuery.setParameter("currentId", updatedAppointment.getAppointmentId());

	        if (!slotQuery.list().isEmpty()) {
	            return "Slot already booked in the selected availability.";
	        }

	        // Update details
	        existing.setAvailability(updatedAppointment.getAvailability());
	        existing.setSlotNo(slotNo);
	        existing.setStart(updatedAppointment.getStart());
	        existing.setEnd(updatedAppointment.getEnd());
	        existing.setNotes(updatedAppointment.getNotes());

	        session.update(existing);
	        tx.commit();
	        return "Appointment updated successfully.";
	    } catch (Exception e) {
	        if (tx != null)
	            tx.rollback();
	        e.printStackTrace();
	        return "Error occurred while updating the appointment.";
	    }
	}


	@Override
	public int getBookedCountForAvailability(String availabilityId) {
		int count = 0;
		try {
			Session session = SessionHelper.getSessionFactory().openSession();
			Query query = session.createQuery(
					"SELECT COUNT(*) FROM Appointment a " + "WHERE a.availability.availabilityId = :availabilityId "
							+ "AND a.status IN ('BOOKED', 'PENDING')");
			query.setParameter("availabilityId", availabilityId);
			Long result = (Long) query.uniqueResult();
			count = result != null ? result.intValue() : 0;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return count;
	}

	@Override
	public boolean hasOverlappingAppointment(String recipientId, String availabilityId, Timestamp start,
			Timestamp end) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Integer> getAvailableSlotNumbers(String availabilityId) {
		List<Integer> availableSlots = new ArrayList<>();
		try {
			Session session = SessionHelper.getSessionFactory().openSession();
			// Step 1: Get max_capacity from DoctorAvailability
			Query capacityQuery = session.createQuery(
					"SELECT da.maxCapacity FROM DoctorAvailability da WHERE da.availabilityId = :availabilityId");
			capacityQuery.setParameter("availabilityId", availabilityId);
			Integer maxCapacity = (Integer) capacityQuery.uniqueResult();

			if (maxCapacity == null || maxCapacity <= 0) {
				return availableSlots; // return empty list if invalid
			}

			// Step 2: Get all booked slot numbers
			Query bookedQuery = session.createQuery(
					"SELECT a.slotNo FROM Appointment a " + "WHERE a.availability.availabilityId = :availabilityId "
							+ "AND a.status IN ('BOOKED', 'PENDING')");
			bookedQuery.setParameter("availabilityId", availabilityId);
			List<Integer> bookedSlots = bookedQuery.list();

			// Step 3: Prepare the full range and subtract booked slots
			for (int i = 1; i <= maxCapacity; i++) {
				if (!bookedSlots.contains(i)) {
					availableSlots.add(i);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return availableSlots;
	}

	@Override
	public boolean isSlotAlreadyBooked(String availabilityId, int slotNo) {
		try {
			Session session = SessionHelper.getSessionFactory().openSession();
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
		}
	}

	@Override
	public List<Appointment> getAppointmentsByAvailability(String availabilityId) {
		List<Appointment> appointments = null;

		try {
			Session session = SessionHelper.getSessionFactory().openSession();
			Query query = session.createQuery(
					"FROM Appointment a WHERE a.availability.availabilityId = :availabilityId ORDER BY a.slotNo ASC");
			query.setParameter("availabilityId", availabilityId);

			appointments = query.list();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return appointments;
	}

	@Override
	public boolean isAppointmentInPast(String appointmentId) {
		try {
			Session session = SessionHelper.getSessionFactory().openSession();
			Appointment appointment = (Appointment) session.get(Appointment.class, appointmentId);

			if (appointment != null && appointment.getStart() != null) {
				Timestamp now = new Timestamp(System.currentTimeMillis());
				return appointment.getStart().before(now);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override
	public List<Appointment> getAppointmentsByDoctorAndDate(String doctorId, Date date) {
		List<Appointment> appointments = null;

		try {
			Session session = SessionHelper.getSessionFactory().openSession();
			Query query = session.createQuery(
					"FROM Appointment a WHERE a.doctor.doctorId = :doctorId AND DATE(a.start) = :appointmentDate");
			query.setParameter("doctorId", doctorId);
			query.setParameter("appointmentDate", date);

			appointments = query.list();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return appointments;
	}

	@Override
	public boolean isSlotTimeInFuture(String availabilityId, int slotNo) {
		try {
			Session session = SessionHelper.getSessionFactory().openSession();
			DoctorAvailability availability = (DoctorAvailability) session.get(DoctorAvailability.class,
					availabilityId);
			if (availability == null)
				return false;

			// Calculate slot duration
			int windowMinutes = availability.getPatientWindow(); // e.g., 15
			if (windowMinutes <= 0)
				return false;

			// Calculate slot start time
			java.sql.Time startTime = availability.getStartTime();
			Timestamp availableDateTime = Timestamp
					.valueOf(availability.getAvailableDate().toString() + " " + startTime.toString());

			// Calculate the start time of the specific slot
			long slotStartMillis = availableDateTime.getTime() + (slotNo - 1) * windowMinutes * 60 * 1000L;
			Timestamp slotStart = new Timestamp(slotStartMillis);

			// Compare with current time
			return slotStart.after(new Timestamp(System.currentTimeMillis()));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}