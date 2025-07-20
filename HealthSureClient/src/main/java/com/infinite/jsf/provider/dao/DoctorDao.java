package com.infinite.jsf.provider.dao;


import java.util.List;

import com.infinite.jsf.provider.model.Doctors;


public interface DoctorDao {
	public List<Doctors> getAllApprovedDoctor();

	public List<Doctors> getApprovedDoctorsByProviderId(String providerId);

	public Doctors searchADoctorById(String doctorId);
}