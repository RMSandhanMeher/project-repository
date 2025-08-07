/// Copyright © 2025 Infinite Computer Solution. All rights reserved.

package com.infinite.jsf.recipient.daoImpl;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.transform.Transformers;

import com.infinite.jsf.insurance.model.PlanType;
import com.infinite.jsf.insurance.model.Subscribe;
import com.infinite.jsf.insurance.model.SubscribedMember;
import com.infinite.jsf.insurance.model.SubscriptionStatus;
import com.infinite.jsf.recipient.customException.InsuranceRetrivalException;
import com.infinite.jsf.recipient.dao.ShowInsuranceDao;
import com.infinite.jsf.recipient.model.RecipientInsuranceDTO;
import com.infinite.jsf.util.SessionHelper;

/**
 * Updated DAO: Uses Transformers.aliasToBean with enum post-processing.
 */
public class ShowInsuranceDaoImpl implements ShowInsuranceDao {

	private static final Logger LOGGER=LogManager.getLogger(ShowInsuranceDaoImpl.class);

	static SessionFactory sessionFactory;

	static {
		sessionFactory = SessionHelper.getSessionFactory();
	}

	@Override
	public List<RecipientInsuranceDTO> showInsuranceOfRecipient(String recipientId)
			throws HibernateException, IllegalArgumentException {

		List<RecipientInsuranceDTO> incdetailslist = new ArrayList<>();
		Session session = null;

		if (recipientId == null || recipientId.isEmpty()) {
			LOGGER.warn("showInsuranceOfRecipient: recipientId is null or empty. Returning empty list.");
			return incdetailslist;
		}

		try {
			session = sessionFactory.openSession();

			Query query = session.getNamedQuery("RecipientInsuranceDTO.findByRecipientId");
			query.setParameter("hId", recipientId);
			//Creates a resultTrsansformer that will inject aliased values into instances of class attribute via fields
			query.setResultTransformer(Transformers.aliasToBean(RecipientInsuranceDTO.class));

			@SuppressWarnings("unchecked")
			List<RecipientInsuranceDTO> results = query.list(); // Holds all the records

			for (RecipientInsuranceDTO incdetails : results) {
				
				// incdetails holds all records now as it is iterating
				// Enum conversion from raw String fields to enum fields
				String statusStr = incdetails.getCoverageStatusString();

				try {
					//Setting the String status to Enum
					incdetails.setCoverageStatus(SubscriptionStatus.valueOf(statusStr.trim().toUpperCase())); // Actual application enum
																												
				} catch (IllegalArgumentException e) {
					LOGGER.error("Invalid SubscriptionStatus value: " + statusStr, e);
					throw new InsuranceRetrivalException("Invalid Plan Type: "+statusStr, e);
				}

				String typeStr = incdetails.getCoverageTypeString();

				try {
					//Setting the String type to Enum
					incdetails.setCoverageType(PlanType.valueOf(typeStr.trim().toUpperCase())); // Actual application enums
																								
				} catch (IllegalArgumentException e) {
					LOGGER.error("Invalid PlanType value: " + typeStr, e);
					throw new InsuranceRetrivalException("Invalid Subscription Status: " + statusStr, e);
				}

				
				
				
				// Condition: Fetch Subscribed Members if coverageType is FAMILY
				if (incdetails.getCoverageType() == PlanType.FAMILY) {

					try {
						Query memberquery = session.getNamedQuery("SubscribedMember.findBySubscribeId");
						memberquery.setParameter("subscribeId", incdetails.getSubscribeId());

						@SuppressWarnings("unchecked")
						List<SubscribedMember> memberRows = memberquery.list(); // Holds all records of Members

						List<SubscribedMember> members = new ArrayList<>();

						for (SubscribedMember member : memberRows) {
							Subscribe subscribe = new Subscribe();
							subscribe.setSubscribeId(incdetails.getSubscribeId());
							member.setSubscribe(subscribe);
							members.add(member);
						}
						
						incdetails.setSubscribedMembers(members);
						
					} 
					
					catch (HibernateException | IllegalArgumentException e) {
						LOGGER.error("Database error while fetching subscribed members for subscribeId: "
								+ incdetails.getSubscribeId(), e);
						throw new InsuranceRetrivalException("A database error occured while retrieving your insurance details.",e);
					} catch (Exception e) {
						LOGGER.error(
								"Error processing subscribed members for subscribeId: " + incdetails.getSubscribeId(),
								e);
						throw new InsuranceRetrivalException(
								"An unexpected error occurred while retrieving your insurance details.", e);
					}
				} else {
					incdetails.setSubscribedMembers(null);
				}
				incdetailslist.add(incdetails); //Addition of all records in to the origin incdetailslist

			}
		} 
		
		 finally {
			if (session != null && session.isOpen()) {
				try {
					session.close();
				} catch (HibernateException closeEx) {
					LOGGER.warn("Error closing session in showInsuranceOfRecipient for recipientId: " + recipientId,
							closeEx);
					throw new InsuranceRetrivalException("Error occured while closing the Session");
				}
			}
		}

		return incdetailslist;
	}

}