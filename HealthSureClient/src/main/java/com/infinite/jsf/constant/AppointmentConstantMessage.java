/*
 * -----------------------------------------------------------------------------
 * Copyright © 2025 Infinite Computer Solution. All rights reserved.
 * -----------------------------------------------------------------------------
 *
 * @Author   : Sandhan Meher
 * @Purpose  : This class provides a centralized location for all constant
 * messages related to appointment operations. This helps in maintaining
 * consistency, reducing hardcoded strings, and simplifying future updates
 * to messages displayed to the user.
 *
 * -----------------------------------------------------------------------------
 */
package com.infinite.jsf.constant;

public class AppointmentConstantMessage {

    // General Validation Messages
    public static final String INVALID_AVAILABILITY_SLOT = "Invalid availability slot. Please select a valid time slot.";
    public static final String CANNOT_BOOK_PAST_APPOINTMENT = "Cannot book an appointment in the past.";
    public static final String DOCTOR_NOT_ACTIVE = "Doctor is not currently active. Please select another doctor.";
    public static final String RECIPIENT_NOT_ACTIVE = "Your account is not active. Please contact support.";
    public static final String RECIPIENT_OVERLAPPING_APPOINTMENT = "You already have an appointment scheduled during this time.";
    public static final String SLOT_ALREADY_BOOKED = "This time slot is already booked. Please choose another time.";
    public static final String AVAILABILITY_FULL = "All slots for this availability are already full.";
    public static final String RECIPIENT_MAX_UPCOMING_APPOINTMENTS_REACHED = "You can only have 10 upcoming appointments at a time.";
    public static final String INVALID_SLOT_NUMBER = "Invalid slot number. Please select a valid slot.";
    public static final String CANNOT_BOOK_PAST_DATE = "Cannot book appointments for past dates.";
    public static final String DOCTOR_SCHEDULING_CONFLICT = "Doctor has a scheduling conflict during this time.";
    public static final String APPOINTMENT_TOO_FAR_IN_FUTURE = "Appointments can only be booked up to 6 months in advance.";
    public static final String APPOINTMENT_OUTSIDE_WORKING_HOURS = "Appointments must be between 8:00 AM and 8:00 PM.";
    public static final String MINIMUM_NOTICE_PERIOD_REQUIRED = "Appointments must be booked at least 2 hours in advance.";

    // Success Messages
    public static final String APPOINTMENT_REQUESTED_SUCCESS = "Appointment requested successfully with ID: ";
    public static final String APPOINTMENT_UPDATED_SUCCESS = "Appointment updated successfully.";
    public static final String APPOINTMENT_CONFIRMATION_NAVIGATION = "appointmentConfirmation?faces-redirect=true";


    // Error Messages
    public static final String ERROR_BOOKING_APPOINTMENT = "Error booking appointment: ";
    public static final String APPOINTMENT_NOT_FOUND = "Appointment not found.";
    public static final String CANNOT_UPDATE_PAST_APPOINTMENTS = "Cannot update past appointments.";
    public static final String NEW_SLOT_OVERLAPS_EXISTING = "You already have an appointment scheduled during this new time.";
    public static final String NEW_SLOT_ALREADY_BOOKED_BY_SOMEONE_ELSE = "This new time slot is already booked by someone else. Please choose another slot.";
    public static final String CANNOT_RESCHEDULE_TO_PAST = "Cannot reschedule an appointment to a past time.";
    public static final String ERROR_UPDATING_APPOINTMENT = "Error occurred while updating the appointment: ";


    // UI/Controller Specific Messages for Appointment Details
    public static final String APPOINTMENT_DETAILS_NOT_FOUND_UI = "Appointment details not found.";
    public static final String ERROR_LOADING_APPOINTMENT_DETAILS_UI = "An error occurred while loading appointment details.";
    public static final String NO_APPOINTMENT_ID_PROVIDED_UI = "No appointment ID provided for detail view.";
    public static final String INVALID_APPOINTMENT_REQUEST_UI = "Invalid appointment request.";

    // UI/Controller Specific Messages for Doctor Availability/Booking
    public static final String PLEASE_SELECT_DOCTOR = "Please select a doctor.";
    public static final String INVALID_DATE_FORMAT = "Invalid date format. Please use YYYY-MM-DD";
    public static final String TIME_SLOT_NO_LONGER_AVAILABLE = "Time slot no longer available";
    public static final String ERROR_GENERIC_PREFIX = "Error: "; // Generic prefix for exceptions
    public static final String ERROR_SENDING_MAIL = "error while sending the mail here ";


    // Dynamic Message Parts
    public static final String PENDING_APPOINTMENT_WITH_DOCTOR_PREFIX = "You already have a pending / booked appointment with this doctor on ";
    public static final String PENDING_APPOINTMENT_WITH_DOCTOR_AT = " at ";
    public static final String PENDING_APPOINTMENT_WITH_DOCTOR_SUFFIX = ". Please complete or cancel that appointment first.";

    // For debugging/logging (not typically displayed to user directly, but good to centralize)
    public static final String LOG_APPOINTMENT_NOT_FOUND_PREFIX = "Appointment with ID ";
    public static final String LOG_APPOINTMENT_NOT_FOUND_SUFFIX = " not found.";
    public static final String LOG_ERROR_FETCHING_APPOINTMENT_DETAILS_PREFIX = "Error fetching appointment details for ID ";

}