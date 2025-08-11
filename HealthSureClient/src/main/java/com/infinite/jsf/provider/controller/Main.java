package com.infinite.jsf.provider.controller;

import com.infinite.jsf.provider.daoImpl.AppointmentDaoImpl;
import com.infinite.jsf.provider.model.Appointment;

public class Main {
	public static void main(String[] args) {

		Appointment ap = new AppointmentDaoImpl().getAppointmentById("APPT000013");
		if (ap == null) {
			System.out.println("ap null");
		}
		else {
			System.out.println(ap.getAppointmentId());
		}
	}
	public void method(String a) {
		if(a.equals("name")) {
			System.out.println("name ");
		}
	}

}