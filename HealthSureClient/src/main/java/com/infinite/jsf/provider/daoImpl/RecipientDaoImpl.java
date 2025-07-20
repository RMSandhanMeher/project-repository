package com.infinite.jsf.provider.daoImpl;

import org.hibernate.Session;

import com.infinite.jsf.provider.dao.RecipientDao;
import com.infinite.jsf.recipient.model.Recipient;
import com.infinite.jsf.util.SessionHelper;

public class RecipientDaoImpl implements RecipientDao {
	@Override
	public Recipient searchRecipientById(String hId) {
		Session session = SessionHelper.getSessionFactory().openSession();
		try {
			return (Recipient) session.get(Recipient.class, hId);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}
}
