/// Copyright © 2025 Infinite Computer Solution. All rights reserved. 

package com.infinite.jsf.recipient.dao;

import java.util.List;

import org.hibernate.HibernateException; // Import HibernateException

import com.infinite.jsf.recipient.customException.InsuranceRetrivalException;
import com.infinite.jsf.recipient.model.RecipientInsuranceDTO;

/**
 * Provides DAO methods for accessing insurance details of a recipient.
 */
public interface ShowInsuranceDao {

	/**
	 * Retrieves all insurance details associated with the specified recipient.
	 * @param recipientId the unique identifier of the recipient
	 * @return a list of PatientInsuranceDetails objects containing insurance information
	 * @throws InsuranceRetrivalException as a custom exception which extends RuntimeException
	 */
	List<RecipientInsuranceDTO> showInsuranceOfRecipient(String recipientId) throws InsuranceRetrivalException;
//            throws HibernateException, IllegalArgumentException; // Added throws clause here
	
}