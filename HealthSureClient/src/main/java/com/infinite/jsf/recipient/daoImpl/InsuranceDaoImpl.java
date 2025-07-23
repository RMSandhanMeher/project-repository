/// Copyright © 2025 Infinite Computer Solution. All rights reserved.
///By: Purnendu Shekhar Achary 
///EmpId: 1806521

package com.infinite.jsf.recipient.daoImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.infinite.jsf.insurance.model.PlanType;
import com.infinite.jsf.insurance.model.Subscribe;
import com.infinite.jsf.insurance.model.SubscribedMember;
import com.infinite.jsf.insurance.model.SubscriptionStatus;
import com.infinite.jsf.recipient.dao.InsuranceDao;
import com.infinite.jsf.recipient.model.PatientInsuranceDetails;
import com.infinite.jsf.util.SessionHelper;

/**
 * Implementation of InsuranceDao interface. Retrieves insurance details of a
 * recipient including plan, coverage and subscribed members.
 */
public class InsuranceDaoImpl implements InsuranceDao {

    private static final Logger LOGGER = Logger.getLogger(InsuranceDaoImpl.class.getName());

	static SessionFactory sessionFactory;

	static {
		sessionFactory = SessionHelper.getSessionFactory();
	}

	/**
	 * Retrieves all insurance coverage details for a specific recipient ID. If the
	 * plan type is FAMILY, also fetches family member details.
	 *
	 * @param recipientId the unique identifier of the recipient
	 * @return list of PatientInsuranceDetails containing insurance and subscriber
	 *         data
	 */
	@Override
	public List<PatientInsuranceDetails> showInsuranceOfRecipient(String recipientId) {
		List<PatientInsuranceDetails> detailsList = new ArrayList<>();

		if (recipientId == null || recipientId.trim().isEmpty()) {
			LOGGER.warn("showInsuranceOfRecipient: recipientId is null or empty");
			return detailsList;
		}

		Session session = null;

		try {
			session = sessionFactory.openSession();
			session.clear();

			Query query = session.getNamedQuery("PatientInsuranceDetails.findByRecipientId");
			query.setParameter("hId", recipientId);
			List<Object[]> results = query.list();

			for (Object[] row : results) {
				try {
					PatientInsuranceDetails details = new PatientInsuranceDetails();
					details.setSubscribeId((String) row[0]);
					details.sethId((String) row[1]);
					details.setPatientName((String) row[2]);
					details.setCompanyName((String) row[3]);
					details.setPlanName((String) row[4]);
					details.setEnrollmentDate((Date) row[5]);
					details.setCoverageStartDate((Date) row[6]);
					details.setCoverageEndDate((Date) row[7]);

					SubscriptionStatus status = SubscriptionStatus.valueOf(((String) row[8]).toUpperCase());
					PlanType type = PlanType.valueOf(((String) row[9]).toUpperCase());

					details.setCoverageStatus(status);
					details.setCoverageType(type);
					details.setCoverageLimit((Double) row[10]);
					details.setRemaining((Double) row[11]);
					details.setClaimed((Double) row[12]);
					details.setLastClaimDate((Date) row[13]);

					if (type == PlanType.FAMILY) {
						try {
							Query memberQuery = session.getNamedQuery("SubscribedMember.findBySubscribeId");
							memberQuery.setParameter("subscribeId", details.getSubscribeId());

							List<SubscribedMember> memberRows = memberQuery.list();
							List<SubscribedMember> members = new ArrayList<>();

							for (SubscribedMember member : memberRows) {
								Subscribe subscribe = new Subscribe();
								subscribe.setSubscribeId(details.getSubscribeId());
								member.setSubscribe(subscribe);
								members.add(member);
							}

							details.setSubscribedMembers(members);
						} catch (Exception e) {
							LOGGER.error("Error fetching subscribed members for subscribeId: {}",
									e);
							details.setSubscribedMembers(new ArrayList<>());
						}
					} else {
						details.setSubscribedMembers(new ArrayList<>());
					}

					detailsList.add(details);
				} catch (Exception rowEx) {
					LOGGER.error("Error processing insurance record row for recipientId: {}", rowEx);
				}
			}

		} catch (Exception e) {
			LOGGER.error("Error in showInsuranceOfRecipient for recipientId: {}", e);
		} finally {
			if (session != null) {
				try {
					session.close();
				} catch (Exception closeEx) {
					LOGGER.warn("Error closing session in showInsuranceOfRecipient", closeEx);
				}
			}
		}

		return detailsList;
	}
}