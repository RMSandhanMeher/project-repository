/// Copyright © 2025 Infinite Computer Solution. All rights reserved. 

package com.infinite.jsf.recipient.dao;

import java.util.List;
import com.infinite.jsf.provider.model.Doctors;

/**
 * Data Access Object interface for searching and retrieving doctor details
 * based on various criteria like name, address, and specialization.
 */
public interface SearchDoctorDao {

	/**
	 * Searches for doctors based on a specific field such as name, city, or
	 * specialization.
	 *
	 * @param searchBy the field to apply search on (e.g., "name", "city",
	 *                 "specialization")
	 * @param value    the value to search for in the specified field
	 * @return a list of doctors matching the search criteria
	 */
	List<Doctors> searchDoctors(String searchBy, String value);

	/**
	 * Finds doctors whose names start with the specified keyword.
	 *
	 * @param keyword the starting letters of the doctor's name
	 * @return a list of doctors whose names start with the keyword
	 */
	List<Doctors> findDoctorsByNameStartsWith(String keyword);

	/**
	 * Finds doctors whose names contain the specified keyword.
	 *
	 * @param keyword the fragment to search for in doctor names
	 * @return a list of doctors whose names contain the keyword
	 */
	List<Doctors> findDoctorsByNameContains(String keyword);

	/**
	 * Finds doctors whose addresses start with the specified keyword.
	 *
	 * @param keyword the starting letters of the address
	 * @return a list of doctors whose addresses start with the keyword
	 */
	List<Doctors> findDoctorsByAddressStartsWith(String keyword);

	/**
	 * Finds doctors whose addresses contain the specified keyword.
	 *
	 * @param keyword the fragment to search for in addresses
	 * @return a list of doctors whose addresses contain the keyword
	 */
	List<Doctors> findDoctorsByAddressContains(String keyword);

	/**
	 * Retrieves all available unique specializations from the system.
	 *
	 * @return a list of unique specialization names
	 */
	List<String> fetchAllSpecialization();
}