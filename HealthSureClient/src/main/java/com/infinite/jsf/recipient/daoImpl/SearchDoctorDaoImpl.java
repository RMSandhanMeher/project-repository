/// Copyright © 2025 Infinite Computer Solution. All rights reserved.

package com.infinite.jsf.recipient.daoImpl;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.apache.log4j.Logger;


import com.infinite.jsf.provider.model.Doctors;
import com.infinite.jsf.recipient.dao.SearchDoctorDao;
import com.infinite.jsf.util.SessionHelper;

/**
 * Implementation of SearchDoctorDao interface for searching doctors based on
 * name, address, specialization, and fetching all specializations. Uses
 * Hibernate for database operations.
 */
public class SearchDoctorDaoImpl implements SearchDoctorDao {

    private static final Logger LOGGER = Logger.getLogger(SearchDoctorDaoImpl.class.getName());

	static SessionFactory sessionFactory;

	static {
		sessionFactory = SessionHelper.getSessionFactory();
	}

	/**
	 * Utility method to perform doctor search using criteria.
	 *
	 * @param fieldName      the field to search (e.g., "doctorName", "address")
	 * @param keywordOrValue the value to match against
	 * @param matchMode      match mode (EXACT, START, ANYWHERE)
	 * @return list of matching Doctor objects
	 */
	@SuppressWarnings("unchecked")
	private List<Doctors> searchByCriteria(String fieldName, String keywordOrValue, MatchMode matchMode) {
		if (fieldName == null || keywordOrValue == null || fieldName.trim().isEmpty()) {
			LOGGER.warn("searchByCriteria: Required parameter is null or empty. fieldName={}, keyword={}");
			return new ArrayList<>();
		}

		List<Doctors> result = new ArrayList<>();
		Session session = null;

		try {
			session = sessionFactory.openSession();

			if ("doctorName".equalsIgnoreCase(fieldName) && matchMode == MatchMode.EXACT) {
				String normalizedValue = keywordOrValue.trim().toLowerCase();
				String hql = "FROM Doctors d WHERE lower(d.doctorName) = :name";
				result = session.createQuery(hql).setParameter("name", normalizedValue).list();
			} else {
				Criteria criteria = session.createCriteria(Doctors.class);

				if (matchMode != null) {
					criteria.add(Restrictions.ilike(fieldName, keywordOrValue.trim(), matchMode));
				} else {
					criteria.add(Restrictions.eq(fieldName, keywordOrValue));
				}

				result = criteria.list();
			}

		} catch (Exception e) {
			LOGGER.error("searchByCriteria: Error while searching. field={}, keyword={}, mode={}", e);
		} finally {
			if (session != null) {
				try {
					session.close();
				} catch (Exception closeEx) {
					LOGGER.warn("searchByCriteria: Error closing session", closeEx);
				}
			}
		}

		return result != null ? result : new ArrayList<>();
	}

	/**
	 * Searches for doctors based on selected criteria (e.g., name, specialization,
	 * address).
	 *
	 * @param searchBy the field to search on
	 * @param value    the value to match
	 * @return list of matching Doctors
	 */
	@Override
	public List<Doctors> searchDoctors(String searchBy, String value) {
		if (searchBy == null || value == null || searchBy.trim().isEmpty()) {
			LOGGER.warn("searchDoctors: searchBy or value is null or empty. searchBy={}, value={}");
			return new ArrayList<>();
		}

		try {
			String searchField = searchBy.trim().toLowerCase();

			switch (searchField) {
			case "doctorname":
				return searchByCriteria("doctorName", value, MatchMode.EXACT);
			case "specialization":
				return searchByCriteria("specialization", value, null);
			case "address":
				return searchByCriteria("address", value, MatchMode.ANYWHERE);
			default:
				LOGGER.warn("searchDoctors: Unsupported searchBy value '{}'");
			}
		} catch (Exception e) {
			LOGGER.error("searchDoctors: Exception occurred. searchBy={}, value={}", e);
		}

		return new ArrayList<>();
	}

	/**
	 * Finds doctors whose names start with the given keyword.
	 *
	 * @param keyword the name prefix to match
	 * @return list of matching Doctors
	 */
	@Override
	public List<Doctors> findDoctorsByNameStartsWith(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			LOGGER.warn("findDoctorsByNameStartsWith: keyword is null or empty");
			return new ArrayList<>();
		}

		try {
			String searchKeyword = keyword.trim();
			if (!searchKeyword.toLowerCase().startsWith("dr.")) {
				searchKeyword = "Dr. " + searchKeyword;
			}
			return searchByCriteria("doctorName", searchKeyword, MatchMode.START);
		} catch (Exception e) {
			LOGGER.error("findDoctorsByNameStartsWith: Exception occurred. keyword={}", e);
		}

		return new ArrayList<>();
	}

	/**
	 * Finds doctors whose names contain the keyword.
	 *
	 * @param keyword the keyword fragment
	 * @return list of matching Doctors
	 */
	@Override
	public List<Doctors> findDoctorsByNameContains(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			LOGGER.warn("findDoctorsByNameContains: keyword is null or empty");
			return new ArrayList<>();
		}

		try {
			return searchByCriteria("doctorName", keyword, MatchMode.ANYWHERE);
		} catch (Exception e) {
			LOGGER.error("findDoctorsByNameContains: Exception occurred. keyword={}", e);
		}

		return new ArrayList<>();
	}

	/**
	 * Finds doctors by address prefix.
	 *
	 * @param keyword the beginning of the address
	 * @return list of matching Doctors
	 */
	@Override
	public List<Doctors> findDoctorsByAddressStartsWith(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			LOGGER.warn("findDoctorsByAddressStartsWith: keyword is null or empty");
			return new ArrayList<>();
		}

		try {
			return searchByCriteria("address", keyword, MatchMode.START);
		} catch (Exception e) {
			LOGGER.error("findDoctorsByAddressStartsWith: Exception occurred. keyword={}", e);
		}

		return new ArrayList<>();
	}

	/**
	 * Finds doctors by address containing the keyword.
	 *
	 * @param keyword the substring of the address
	 * @return list of matching Doctors
	 */
	@Override
	public List<Doctors> findDoctorsByAddressContains(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			LOGGER.warn("findDoctorsByAddressContains: keyword is null or empty");
			return new ArrayList<>();
		}

		try {
			return searchByCriteria("address", keyword, MatchMode.ANYWHERE);
		} catch (Exception e) {
			LOGGER.error("findDoctorsByAddressContains: Exception occurred. keyword={}", e);
		}

		return new ArrayList<>();
	}

	/**
	 * Retrieves all doctor specializations from the database.
	 *
	 * @return list of specialization names
	 */
	@Override
	public List<String> fetchAllSpecialization() {
		Session session = null;
		List<String> specializations = new ArrayList<>();

		try {
			session = sessionFactory.openSession();
			Query query = session.getNamedQuery("fetchAllSpecializations");

			if (query != null) {
				specializations = query.list();
			} else {
				LOGGER.warn("fetchAllSpecialization: Named query 'fetchAllSpecializations' not found");
			}

		} catch (Exception e) {
			LOGGER.error("fetchAllSpecialization: Exception occurred", e);
		} finally {
			if (session != null) {
				try {
					session.close();
				} catch (Exception closeEx) {
					LOGGER.warn("fetchAllSpecialization: Error closing session", closeEx);
				}
			}
		}

		return specializations != null ? specializations : new ArrayList<>();
	}
}