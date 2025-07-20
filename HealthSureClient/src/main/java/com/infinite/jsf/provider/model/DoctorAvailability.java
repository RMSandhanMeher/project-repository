package com.infinite.jsf.provider.model;

import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Date;

public class DoctorAvailability {
	private String availabilityId;
	private Doctors doctor;
	private Date availableDate;
	private Time startTime;
	private Time endTime;
	private SlotType slotType;
	private int maxCapacity;
	private Boolean isRecurring;
	private String notes;
	private Timestamp createdAt;
	private int patientWindow;

	public int getPatientWindow() {
		return patientWindow;
	}

	public void setPatientWindow(int patientWindow) {
		this.patientWindow = patientWindow;
	}

	// Constructors
	public DoctorAvailability() {
	}

	public DoctorAvailability(String availabilityId, Doctors doctor, Date availableDate, Time startTime, Time endTime,
			SlotType slotType) {
		this.availabilityId = availabilityId;
		this.doctor = doctor;
		this.availableDate = availableDate;
		this.startTime = startTime;
		this.endTime = endTime;
		this.slotType = slotType;
		this.maxCapacity = 15; // Default value
		this.isRecurring = false;
	}

	// Getters and Setters
	public String getAvailabilityId() {
		return availabilityId;
	}

	public void setAvailabilityId(String availabilityId) {
		this.availabilityId = availabilityId;
	}

	public Doctors getDoctor() {
		return doctor;
	}

	public void setDoctor(Doctors doctor) {
		this.doctor = doctor;
	}

	public Date getAvailableDate() {
		return availableDate;
	}

	public void setAvailableDate(Date availableDate) {
		this.availableDate = availableDate;
	}

	public Time getStartTime() {
		return startTime;
	}

	public void setStartTime(Time startTime) {
		this.startTime = startTime;
	}

	public Time getEndTime() {
		return endTime;
	}

	public void setEndTime(Time endTime) {
		this.endTime = endTime;
	}

	public SlotType getSlotType() {
		return slotType;
	}

	public void setSlotType(SlotType slotType) {
		this.slotType = slotType;
	}

	public Integer getMaxCapacity() {
		return maxCapacity;
	}

	public void setMaxCapacity(Integer maxCapacity) {
		this.maxCapacity = maxCapacity;
	}

	public Boolean getIsRecurring() {
		return isRecurring;
	}

	public void setIsRecurring(Boolean isRecurring) {
		this.isRecurring = isRecurring;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public String toString() {
		return "DoctorAvailability [availabilityId=" + availabilityId + ", doctor=" + doctor + ", availableDate="
				+ availableDate + ", startTime=" + startTime + ", endTime=" + endTime + ", slotType=" + slotType
				+ ", maxCapacity=" + maxCapacity + ", isRecurring=" + isRecurring + ", notes=" + notes + ", createdAt="
				+ createdAt + ", patientWindow=" + patientWindow + "]";
	}

}