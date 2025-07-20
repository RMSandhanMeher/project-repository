package com.infinite.jsf.provider.daoImpl;

import java.sql.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query; // Deprecated in Hibernate 5.2+, consider using org.hibernate.query.Query
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction; // Import Transaction

import com.infinite.jsf.provider.dao.DoctorAvailabilityDao;
import com.infinite.jsf.provider.model.DoctorAvailability;
import com.infinite.jsf.util.SessionHelper;

public class DoctorAvailabilityDaoImpl implements DoctorAvailabilityDao {

	private SessionFactory sessionFactory;

	public DoctorAvailabilityDaoImpl() {
		// It's generally better to inject SessionFactory if using Spring or similar
		// frameworks.
		// For a standalone app, this direct access is common.
		this.sessionFactory = SessionHelper.getSessionFactory();
	}

	@Override
	public List<DoctorAvailability> getAvailableSlotsByDoctorAndDate(String doctorId, Date date) {
		Session session = null;
		List<DoctorAvailability> slots = null;
		try {
			session = sessionFactory.openSession();
			// HQL should use property names (e.g., doctor.doctorId), not column names
			// (doctor_id)
			// Order by availableDate is implicitly handled by the exact date match, but
			// good for consistency.
			// Replaced 'start_time' with 'startTime' for HQL property access.
			Query query = session.createQuery(
					"from DoctorAvailability where doctor.doctorId = :doctorId and availableDate = :date order by startTime asc");
			query.setParameter("doctorId", doctorId);
			query.setParameter("date", date);
			slots = query.list();
		} catch (HibernateException e) { // Catch more specific HibernateException
			System.err.println("Error fetching available slots by doctor and date: " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return slots;
	}

	@Override
	public DoctorAvailability getAvailabilityById(String availabilityId) {
		Session session = null;
		DoctorAvailability availability = null;
		try {
			session = sessionFactory.openSession();
			availability = (DoctorAvailability) session.get(DoctorAvailability.class, availabilityId); // No need for explicit cast with
																					// generics
		} catch (HibernateException e) {
			System.err.println("Error fetching availability by ID: " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return availability;
	}

	@Override
	public List<DoctorAvailability> getUpcomingAvailabilitiesForDoctor(String doctorId, Date fromDate) {
		Session session = null;
		List<DoctorAvailability> slots = null;
		try {
			session = sessionFactory.openSession();
			// HQL should use object properties, not database column names.
			// 'doctor.doctor_id' should be 'doctor.doctorId'.
			// 'available_date' should be 'availableDate'.
			Query query = session.createQuery(
					"from DoctorAvailability where doctor.doctorId = :doctorId and availableDate >= :fromDate order by availableDate asc, startTime asc");
			query.setParameter("doctorId", doctorId);
			query.setParameter("fromDate", fromDate);
			slots = query.list();
		} catch (HibernateException e) {
			System.err.println("Error fetching upcoming availabilities for doctor: " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return slots;
	}

	@Override
	public List<DoctorAvailability> getAvailabilityByDoctorAndDate(String doctorId, Date date) {
		Session session = null;
		List<DoctorAvailability> slots = null;
		try {
			session = sessionFactory.openSession();
			// HQL property names: 'doctor.doctor_id' -> 'doctor.doctorId', 'available_date'
			// -> 'availableDate', 'start_time' -> 'startTime'.
			Query query = session.createQuery(
					"from DoctorAvailability where doctor.doctorId = :doctorId and availableDate = :date order by startTime asc");
			query.setParameter("doctorId", doctorId);
			query.setParameter("date", date);
			slots = query.list();
		} catch (HibernateException e) {
			System.err.println("Error fetching availability by doctor and date: " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return slots;
	}

	@Override
	public List<DoctorAvailability> getAllAvailabilitiesByProvider(String providerId) {
		Session session = null;
		List<DoctorAvailability> list = null;
		try {
			session = sessionFactory.openSession();
			// HQL property names: 'doctor.provider.provider_id' ->
			// 'doctor.provider.providerId'.
			// Also, consider using 'availableDate' and 'startTime' for ordering.
			Query query = session.createQuery(
					"from DoctorAvailability where doctor.provider.providerId = :providerId order by availableDate desc, startTime desc");
			query.setParameter("providerId", providerId);
			list = query.list();
		} catch (HibernateException e) {
			System.err.println("Error fetching all availabilities by provider: " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return list;
	}

	@Override
	public void addDoctorAvailability(DoctorAvailability availability) {
		Session session = null;
		Transaction transaction = null; // Use Transaction object
		try {
			session = sessionFactory.openSession();
			transaction = session.beginTransaction();
			session.save(availability);
			transaction.commit(); // Commit the transaction
		} catch (HibernateException e) {
			System.err.println("Error adding doctor availability: " + e.getMessage());
			e.printStackTrace();
			if (transaction != null) { // Check if transaction was started before rollback
				transaction.rollback();
			}
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}

	@Override
	public void updateDoctorAvailability(DoctorAvailability availability) {
		Session session = null;
		Transaction transaction = null;
		try {
			session = sessionFactory.openSession();
			transaction = session.beginTransaction();
			session.update(availability);
			transaction.commit();
		} catch (HibernateException e) {
			System.err.println("Error updating doctor availability: " + e.getMessage());
			e.printStackTrace();
			if (transaction != null) {
				transaction.rollback();
			}
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}

	@Override
	public boolean deleteAvailabilityIfNoAppointments(String availabilityId) {
		Session session = null;
		Transaction transaction = null;
		boolean deleted = false;
		try {
			session = sessionFactory.openSession();
			transaction = session.beginTransaction();

			// Use 'availability.availabilityId' for HQL property access
			Long count = (Long) session
					.createQuery("select count(*) from Appointment where availability.availabilityId = :availabilityId")
					.setParameter("availabilityId", availabilityId).uniqueResult();

			if (count == 0) {
				DoctorAvailability availability = (DoctorAvailability) session.get(DoctorAvailability.class, availabilityId);
				if (availability != null) {
					session.delete(availability);
					deleted = true;
				}
			}

			transaction.commit();
		} catch (HibernateException e) {
			System.err.println("Error deleting availability: " + e.getMessage());
			e.printStackTrace();
			if (transaction != null) {
				transaction.rollback();
			}
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return deleted;
	}

	@Override
	public List<DoctorAvailability> getTodayAvailabilities() {
		Session session = null;
		List<DoctorAvailability> list = null;
		try {
			session = sessionFactory.openSession();
			// Use HQL function CURRENT_DATE(), not current_date() (case-sensitive in HQL
			// for functions)
			// HQL property 'available_date' -> 'availableDate', 'start_time' -> 'startTime'
			Query query = session
					.createQuery("from DoctorAvailability where availableDate = CURRENT_DATE() order by startTime asc");
			list = query.list();
		} catch (HibernateException e) {
			System.err.println("Error fetching today's availabilities: " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return list;
	}

	@Override
	public int getRemainingSlotsForAvailability(String availabilityId) {
		Session session = null;
		int remainingSlots = 0;
		try {
			session = sessionFactory.openSession();

			DoctorAvailability availability = (DoctorAvailability) session.get(DoctorAvailability.class, availabilityId);
			if (availability != null) {
				// Use 'availability.availabilityId' for HQL property access
				Long bookedSlotsLong = (Long) session.createQuery(
						"select count(*) from Appointment where availability.availabilityId = :availabilityId and status = 'BOOKED'")
						.setParameter("availabilityId", availabilityId).uniqueResult();

				int bookedSlots = (bookedSlotsLong != null) ? bookedSlotsLong.intValue() : 0;
				remainingSlots = availability.getMaxCapacity() - bookedSlots;
			}
		} catch (HibernateException e) {
			System.err.println("Error getting remaining slots for availability: " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return remainingSlots;
	}
}