package com.infinite.jsf.provider.dto;

import java.io.Serializable;
import java.sql.Timestamp;
import com.infinite.jsf.provider.model.AppointmentStatus;
import com.infinite.jsf.provider.model.Gender; 

public class AppointmentDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    private String doctorName;
    private String doctorId;
    private String doctorSpecialization;
    private Gender doctorGender; // Assuming Gender enum exists
    private String doctorAvailabilityTiming; // e.g., "10:00 AM - 10:30 AM"
    private String appointmentId;
    private Timestamp requestedAt;
    private Timestamp bookedAt;
    private Timestamp cancelledAt;
    private Timestamp completedAt;
    private AppointmentStatus status; // Assuming AppointmentStatus enum exists
    private int slotNo;
    private Timestamp start; // Appointment start time
    private Timestamp end;   // Appointment end time

    // Constructors
    public AppointmentDetails() {
    }

    public AppointmentDetails(String doctorName, String doctorSpecialization, Gender doctorGender,
                              String doctorAvailabilityTiming, String appointmentId, Timestamp requestedAt,
                              Timestamp bookedAt, Timestamp cancelledAt, Timestamp completedAt,
                              AppointmentStatus status, int slotNo, Timestamp start, Timestamp end) {
        this.doctorName = doctorName;
        this.doctorSpecialization = doctorSpecialization;
        this.doctorGender = doctorGender;
        this.doctorAvailabilityTiming = doctorAvailabilityTiming;
        this.appointmentId = appointmentId;
        this.requestedAt = requestedAt;
        this.bookedAt = bookedAt;
        this.cancelledAt = cancelledAt;
        this.completedAt = completedAt;
        this.status = status;
        this.slotNo = slotNo;
        this.start = start;
        this.end = end;
    }

    // Getters and Setters
    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorSpecialization() {
        return doctorSpecialization;
    }

    public void setDoctorSpecialization(String doctorSpecialization) {
        this.doctorSpecialization = doctorSpecialization;
    }

    public Gender getDoctorGender() {
        return doctorGender;
    }

    public void setDoctorGender(Gender doctorGender) {
        this.doctorGender = doctorGender;
    }

    public String getDoctorAvailabilityTiming() {
        return doctorAvailabilityTiming;
    }

    public void setDoctorAvailabilityTiming(String doctorAvailabilityTiming) {
        this.doctorAvailabilityTiming = doctorAvailabilityTiming;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
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

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
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

    public String getDoctorId() {
		return doctorId;
	}

	public void setDoctorId(String doctorId) {
		this.doctorId = doctorId;
	}

	@Override
    public String toString() {
        return "AppointmentDetails{" +
               "doctorName='" + doctorName + '\'' +
               ", doctorSpecialization='" + doctorSpecialization + '\'' +
               ", doctorGender=" + doctorGender +
               ", doctorAvailabilityTiming='" + doctorAvailabilityTiming + '\'' +
               ", appointmentId='" + appointmentId + '\'' +
               ", requestedAt=" + requestedAt +
               ", bookedAt=" + bookedAt +
               ", cancelledAt=" + cancelledAt +
               ", completedAt=" + completedAt +
               ", status=" + status +
               ", slotNo=" + slotNo +
               ", start=" + start +
               ", end=" + end +
               '}';
    }
}