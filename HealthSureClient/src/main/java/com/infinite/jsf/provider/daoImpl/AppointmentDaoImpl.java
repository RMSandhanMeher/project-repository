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
		String hql = "SELECT a.appointmentId FROM Appointment a ORDER BY a.appointmentId DESC";

		Query query = session.createQuery(hql);
		query.setMaxResults(1);

		String lastId = (String) query.uniqueResult();

		int nextNumber = 101;
		if (lastId != null && lastId.startsWith(prefix)) {
			try {
				int lastNumber = Integer.parseInt(lastId.substring(prefix.length()));
				nextNumber = lastNumber + 1;
			} catch (NumberFormatException e) {
				System.err.println("Warning: Could not parse number from last appointment ID: " + lastId
						+ ". Starting from default " + nextNumber);
			}
		}

		return prefix + nextNumber;
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
			Query bookedCountQuery = session.createQuery(
					"SELECT COUNT(*) FROM Appointment WHERE availability.availabilityId = :availabilityId "
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
							+ "AND a.doctor.doctorId = :doctorId AND a.status = 'PENDING'  AND a.start > :currentTime");
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

				return "You already have a pending appointment with this doctor on " + formattedDate + " at "
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

			result = "Appointment booked successfully with ID: " + appointment.getAppointmentId();
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
			Query countQuery = session.createQuery(
					"SELECT COUNT(*) FROM Appointment WHERE availability.availability_id = :availabilityId "
							+ "AND status IN ('BOOKED', 'PENDING')");
			countQuery.setParameter("availabilityId", availabilityId);
			long bookedCount = (Long) countQuery.uniqueResult();

			// Step 2: Get max capacity from DoctorAvailability
			Query capacityQuery = session.createQuery(
					"SELECT a.max_capacity FROM DoctorAvailability a WHERE a.availability_id = :availabilityId");
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

			Query query = session.createQuery("FROM Appointment a WHERE a.recipient.h_id = :recipientId "
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Appointment getAppointmentById(String appointmentId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean cancelAppointment(String appointmentId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateAppointment(Appointment appointment) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getBookedCountForAvailability(String availabilityId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean hasOverlappingAppointment(String recipientId, String availabilityId, Timestamp start,
			Timestamp end) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Integer> getAvailableSlotNumbers(String availabilityId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSlotAlreadyBooked(String availabilityId, int slotNo) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Appointment> getAppointmentsByAvailability(String availabilityId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAppointmentInPast(String appointmentId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Appointment> getAppointmentsByDoctorAndDate(String doctorId, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSlotTimeInFuture(String availabilityId, int slotNo) {
		// TODO Auto-generated method stub
		return false;
	}

}