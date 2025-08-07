/// Copyright © 2025 Infinite Computer Solution. All rights reserved.

package com.infinite.jsf.recipient.daoImpl;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import com.infinite.jsf.provider.model.Doctors;
import com.infinite.jsf.recipient.customException.ProviderSearchException;
import com.infinite.jsf.recipient.dao.SearchDoctorDao;
import com.infinite.jsf.util.SessionHelper;

/**
 * Implementation of SearchDoctorDao interface for searching doctors based on
 * name, address, specialization, and fetching all specializations. Uses
 * Hibernate for database operations.
 */
public class SearchDoctorDaoImpl implements SearchDoctorDao {

	private static final Logger LOGGER = LogManager.getLogger(SearchDoctorDaoImpl.class);

	static SessionFactory sessionFactory;

	static {
		sessionFactory = SessionHelper.getSessionFactory();
	}

	/**
	 * Utility method to perform doctor search using criteria.
	 *
	 * @param fieldName the field to search (e.g., "doctorName", "address", Specialization)
	 * @param keywordOrValue the value to match against
	 * @param matchMode match mode (EXACT, START, ANYWHERE)
	 * @return list of matching Doctor objects
	 * @throws HibernateException if there is an issue with database access.
	 */
	@SuppressWarnings("unchecked")
	private List<Doctors> searchByCriteria(String fieldName, String keywordOrValue, MatchMode matchMode)
			throws HibernateException, IllegalArgumentException {
		if (fieldName == null || keywordOrValue == null || fieldName.trim().isEmpty()) {
			LOGGER.warn("searchByCriteria: Required parameter is null or empty. fieldName=" + fieldName + ", keyword="
					+ keywordOrValue);
			return new ArrayList<>();
		}

		List<Doctors> result = new ArrayList<>();
		Session session = null;

		try {
			session = sessionFactory.openSession();

			if ("doctorName".equalsIgnoreCase(fieldName)) {

				if (matchMode == MatchMode.EXACT) {
					String nameValue = keywordOrValue.trim().toLowerCase();
					String hql = "FROM Doctors d WHERE lower(d.doctorName) = :name ORDER BY d.doctorName ASC";

					result = session.createQuery(hql).setParameter("name", nameValue).list();
				}

				else if ((matchMode == MatchMode.START || matchMode == MatchMode.ANYWHERE)) {

					String cleanedKeyword = keywordOrValue.replaceAll("[.\\s]", "").toLowerCase();

					String hql = "FROM Doctors d "
							+ "WHERE lower(replace(replace(d.doctorName, '.',''), ' ','')) LIKE :keyword "
							+ "ORDER BY d.doctorName ASC";

					if (matchMode == MatchMode.START) {
						cleanedKeyword += "%";
					} else {
						cleanedKeyword = "%" + cleanedKeyword + "%";
					}

					result = session.createQuery(hql).setParameter("keyword", cleanedKeyword).list();
				}
				return result;
			}

			if ("address".equalsIgnoreCase(fieldName) && matchMode == MatchMode.EXACT) {
				String addressValue = keywordOrValue.trim().toLowerCase();
				String hql = "FROM Doctors d WHERE lower(trim(d.address)) = :address";
				result = session.createQuery(hql).setParameter("address", addressValue).list();
				return result;
			}

			// Main calling method for all types the Criteria
			Criteria criteria = session.createCriteria(Doctors.class);

			if (matchMode != null) {
				criteria.add(Restrictions.ilike(fieldName, keywordOrValue.trim(), matchMode));
			} else {
				criteria.add(Restrictions.eq(fieldName, keywordOrValue)); // Specifically for specilization as matchMode
																			// is null (DropDown)
			}

			result = criteria.list();

		} catch (HibernateException | IllegalArgumentException e) {
			LOGGER.error("searchByCriteria: Database error while searching. field=" + fieldName + ", keyword="
					+ keywordOrValue + ", mode=" + matchMode, e);
			throw new ProviderSearchException(String.format("An unexpected error occured while searching via: "+" "+keywordOrValue), e);	
		} 
		catch(Exception e) {
			LOGGER.error("Database error occured while using searchBy");
			throw new ProviderSearchException(String.format("An unexpected error occured while searching via: "+" "+keywordOrValue+ "Please try again after sometime"), e);	
		}
		
		finally {
			if (session != null && session.isOpen()) {
				try {
					session.close();
				} catch (HibernateException closeEx) {
					LOGGER.warn("searchByCriteria: Error closing session", closeEx);
				} catch (Exception closeEx) {
					LOGGER.warn("searchByCriteria: Generic error closing session", closeEx);
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
	 * @throws HibernateException       if there is an issue with database access.
	 * @throws IllegalArgumentException if an invalid 'searchBy' field is provided.
	 */
	@Override
	public List<Doctors> searchDoctors(String searchBy, String value) throws HibernateException, IllegalArgumentException {
		if (searchBy == null || value == null || searchBy.trim().isEmpty()) {
			LOGGER.warn("searchDoctors: searchBy or value is null or empty. searchBy=" + searchBy + ", value=" + value);
			return new ArrayList<>();
		}

		String searchField = searchBy.trim().toLowerCase();

		try {
			switch (searchField) {
			case "doctorname":
				return searchByCriteria("doctorName", value, MatchMode.EXACT);
			case "specialization":
				return searchByCriteria("specialization", value, null);
			case "address":
				return searchByCriteria("address", value, MatchMode.EXACT);
			default:
				LOGGER.warn("searchDoctors: Unsupported searchBy value '" + searchBy
						+ "'. Throwing IllegalArgumentException.");
				throw new IllegalArgumentException("Unsupported search criteria: " + searchBy);
			}
		} catch (HibernateException | IllegalArgumentException e) {
			LOGGER.error("searchDoctors: An unexpected error occurred. searchBy=" + searchBy + ", value=" + value, e);
			throw new ProviderSearchException("An Unexcepted error occured while using searchBy", e);
		} catch (Exception e) {
			LOGGER.error("searchDoctors: An unexpected error occurred. searchBy=" + searchBy + ", value=" + value, e);
			throw new ProviderSearchException("An unexpected error occurred during the search.", e);
		}
	}

	/**
	 * Finds doctors whose names start with the given keyword.
	 *
	 * @param keyword the name prefix to match
	 * @return list of matching Doctors
	 * @throws HibernateException if there is an issue with database access.
	 */
	@Override
	public List<Doctors> findDoctorsByNameStartsWith(String keyword) throws HibernateException, IllegalArgumentException {
		if (keyword == null || keyword.trim().isEmpty()) {
			LOGGER.warn("findDoctorsByNameStartsWith: keyword is null or empty");
			return new ArrayList<>();
		}

		try {
			String searchKeyword = keyword.trim();
			if (!searchKeyword.toLowerCase().startsWith("dr")) {
				searchKeyword = "Dr. " + searchKeyword;
			}
			return searchByCriteria("doctorName", searchKeyword, MatchMode.START);
		} catch (HibernateException | IllegalArgumentException e) {
		    LOGGER.error(String.format("Failed to retrieve doctors whose names start with the given prefix: '%s'. Possible query or mapping issue.", keyword), e);
		    throw new ProviderSearchException(String.format("Failed to retrieve doctors whose names start with the given prefix: '%s'. Possible query or mapping issue.", keyword), e);
		} catch (Exception e) {
		    LOGGER.error(String.format("findDoctorsByNameStartsWith: An unexpected error occurred. keyword='%s'", keyword), e);
		    throw new ProviderSearchException("An unexpected error occurred while searching for doctors by name prefix.", e);
		}

	}

	/**
	 * Finds doctors whose names contain the keyword.
	 *
	 * @param keyword the keyword fragment
	 * @return list of matching Doctors
	 * @throws HibernateException if there is an issue with database access.
	 */
	@Override
	public List<Doctors> findDoctorsByNameContains(String keyword) throws HibernateException, IllegalArgumentException {
		if (keyword == null || keyword.trim().isEmpty()) {
			LOGGER.warn("findDoctorsByNameContains: keyword is null or empty");
			return new ArrayList<>();
		}

		try {
			return searchByCriteria("doctorName", keyword, MatchMode.ANYWHERE);
		} catch (HibernateException | IllegalArgumentException e) {
		    LOGGER.error(String.format("Failed to search doctors by name containing keyword: '%s'. Possible query or mapping issue.", keyword), e);
		    throw new ProviderSearchException(String.format("Failed to search doctors by name containing keyword: '%s'.", keyword), e);
		} catch (Exception e) {
		    LOGGER.error(String.format("findDoctorsByNameContains: An unexpected error occurred. keyword='%s'", keyword), e);
		    throw new ProviderSearchException("An unexpected error occurred while searching for doctors by name.", e);
		}


	}

	/**
	 * Finds doctors by address prefix.
	 *
	 * @param keyword the beginning of the address
	 * @return list of matching Doctors
	 * @throws HibernateException if there is an issue with database access.
	 */
	@Override
	public List<Doctors> findDoctorsByAddressStartsWith(String keyword) throws HibernateException, IllegalArgumentException {
		if (keyword == null || keyword.trim().isEmpty()) {
			LOGGER.warn("findDoctorsByAddressStartsWith: keyword is null or empty");
			return new ArrayList<>();
		}

		try {
			return searchByCriteria("address", keyword, MatchMode.START);
		} catch (HibernateException | IllegalArgumentException e) {
		    LOGGER.error(String.format("Failed to retrieve address whose names start with the given prefix: '%s'. Possible query or mapping issue.", keyword), e);
		    throw new ProviderSearchException(String.format("Failed to retrieve address whose names start with the given prefix: '%s'. Possible query or mapping issue.", keyword), e);
		} catch (Exception e) {
			LOGGER.error(String.format("findDoctorsByAddressStartsWith: An unexpected error occurred. keyword='%s'", keyword), e);
		    throw new ProviderSearchException("An unexpected error occurred while searching for address by name prefix.", e);
		}
	}

	/**
	 * Finds doctors by address containing the keyword.
	 *
	 * @param keyword the substring of the address
	 * @return list of matching Doctors
	 * @throws HibernateException if there is an issue with database access.
	 */

	@Override
	public List<Doctors> findDoctorsByAddressContains(String keyword) throws HibernateException, IllegalArgumentException {
		if (keyword == null || keyword.trim().isEmpty()) {
			LOGGER.warn("findDoctorsByAddressContains: keyword is null or empty");
			return new ArrayList<>();
		}

		try {
			return searchByCriteria("address", keyword, MatchMode.ANYWHERE);
		} catch (HibernateException | IllegalArgumentException e) {
			LOGGER.error(String.format("Failed to search address by name containing keyword: '%s'. Possible query or mapping issue.", keyword), e);
		    throw new ProviderSearchException(String.format("Failed to search address by name containing keyword: '%s'.", keyword), e);
		} catch (Exception e) {
			LOGGER.error(String.format("findDoctorsByAddressContains: An unexpected error occurred. keyword='%s'", keyword), e);
		    throw new ProviderSearchException("An unexpected error occurred while searching for address by name.", e);
		}
	}

	/**
	 * Retrieves all doctor specializations from the database.
	 *
	 * @return list of specialization names
	 * @throws HibernateException if there is an issue with database access.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<String> fetchAllSpecialization() throws HibernateException  {
		Session session = null;
		List<String> specializations = new ArrayList<>();

		try {
			session = sessionFactory.openSession();
			Query query = session.getNamedQuery("fetchAllSpecializations");

			if (query != null) {
				specializations = query.list();
			} else {
				LOGGER.warn(
						"fetchAllSpecialization: Named query 'fetchAllSpecializations' not found. Returning empty list.");
			}

		} catch (HibernateException e) {
			LOGGER.error("fetchAllSpecialization: Database error occurred", e);
			throw new ProviderSearchException("An unexpected error occured while retriving the specialization. PLease try again later", e);
		} catch (Exception e) {
			LOGGER.error("fetchAllSpecialization: An unexpected error occurred", e);
			throw new ProviderSearchException("An unexpected error occurred fetching specializations. PLease try again later", e);
		} finally {
			if (session != null && session.isOpen()) {
				try {
					session.close();
				} catch (HibernateException closeEx) {
					LOGGER.warn("fetchAllSpecialization: Error closing session. Please try after sometime", closeEx);
				} catch (Exception closeEx) {
					LOGGER.warn("fetchAllSpecialization: Generic error closing session", closeEx);
				}
			}
		}

		return specializations != null ? specializations : new ArrayList<>();
	}
}