package com.infinite.jsf.provider.model;

import java.sql.Timestamp;
import java.util.Set;

import com.infinite.jsf.recipient.model.Recipient;

public class Appointment {
	private String appointmentId;
	private Doctors doctor;
	private Recipient recipient;
	private DoctorAvailability availability;
	private Provider provider;
	private Timestamp requestedAt;
	private Timestamp bookedAt;
	private Timestamp cancelledAt; // NEW
	private Timestamp completedAt; // NEW
	private AppointmentStatus status;
	private String notes;
	private int slotNo;
	private Timestamp start; // NEW
	private Timestamp end; // NEW

	public Set<MedicalProcedure> getProcedures() {
		return procedures;
	}

	public void setProcedures(Set<MedicalProcedure> procedures) {
		this.procedures = procedures;
	}

	private Set<MedicalProcedure> procedures;

	// Constructors
	public Appointment() {
	}

	public Appointment(String appointmentId, Doctors doctor, Recipient recipient, DoctorAvailability availability,
			Provider provider, Timestamp requestedAt, Timestamp bookedAt, Timestamp cancelledAt, Timestamp completedAt,
			AppointmentStatus status, String notes, int slotNo, Timestamp start, Timestamp end,
			Set<MedicalProcedure> procedures) {
		super();
		this.appointmentId = appointmentId;
		this.doctor = doctor;
		this.recipient = recipient;
		this.availability = availability;
		this.provider = provider;
		this.requestedAt = requestedAt;
		this.bookedAt = bookedAt;
		this.cancelledAt = cancelledAt;
		this.completedAt = completedAt;
		this.status = status;
		this.notes = notes;
		this.slotNo = slotNo;
		this.start = start;
		this.end = end;
		this.procedures = procedures;
	}

	public Timestamp getCancelledAt() {
		return cancelledAt;
	}

	public void setCancelledAt(Timestamp cancelledAt) {
		this.cancelledAt = cancelledAt;
	}

	public Timestamp getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(Timestamp completedAt) {
		this.completedAt = completedAt;
	}

	public int getSlotNo() {
		return slotNo;
	}

	public void setSlotNo(int slotNo) {
		this.slotNo = slotNo;
	}

	public Timestamp getStart() {
		return start;
	}

	public void setStart(Timestamp start) {
		this.start = start;
	}

	public Timestamp getEnd() {
		return end;
	}

	public void setEnd(Timestamp end) {
		this.end = end;
	}

	
	// Getters and Setters
	public String getAppointmentId() {
		return appointmentId;
	}

	public void setAppointmentId(String appointmentId) {
		this.appointmentId = appointmentId;
	}

	public Doctors getDoctor() {
		return doctor;
	}

	public void setDoctor(Doctors doctor) {
		this.doctor = doctor;
	}

	public Recipient getRecipient() {
		return recipient;
	}

	public void setRecipient(Recipient recipient) {
		this.recipient = recipient;
	}

	public DoctorAvailability getAvailability() {
		return availability;
	}

	public void setAvailability(DoctorAvailability availability) {
		this.availability = availability;
	}

	public Provider getProvider() {
		return provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}

	public Timestamp getRequestedAt() {
		return requestedAt;
	}

	public void setRequestedAt(Timestamp requestedAt) {
		this.requestedAt = requestedAt;
	}

	public Timestamp getBookedAt() {
		return bookedAt;
	}

	public void setBookedAt(Timestamp bookedAt) {
		this.bookedAt = bookedAt;
	}

	public AppointmentStatus getStatus() {
		return status;
	}

	public void setStatus(AppointmentStatus status) {
		this.status = status;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	@Override
	public String toString() {
		return "Appointment [appointmentId=" + appointmentId + ", doctor=" + doctor + ", recipient=" + recipient
				+ ", availability=" + availability + ", provider=" + provider + ", requestedAt=" + requestedAt
				+ ", bookedAt=" + bookedAt + ", status=" + status + ", notes=" + notes + ", procedures=" + procedures
				+ "]";
	}

}