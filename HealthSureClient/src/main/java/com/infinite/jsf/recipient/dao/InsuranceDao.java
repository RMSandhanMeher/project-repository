/// Copyright © 2025 Infinite Computer Solution. All rights reserved. 

package com.infinite.jsf.recipient.dao;

import java.util.List;

import com.infinite.jsf.recipient.model.PatientInsuranceDetails;

/**
 * Provides DAO methods for accessing insurance details of a recipient.
 */
public interface InsuranceDao {

	/**
	 * Retrieves all insurance details associated with the specified recipient.
	 * 
	 * @param recipientId the unique identifier of the recipient
	 * @return a list of PatientInsuranceDetails objects containing insurance information   
	 */
	List<PatientInsuranceDetails> showInsuranceOfRecipient(String recipientId);
}