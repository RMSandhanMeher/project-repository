<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html"%>

<f:view>
	<html>
<head>
<meta charset="UTF-8">
<title>Appointment Details</title>
</head>
<body>
	<h1>Appointment Details</h1>

	<p>
		<strong>Doctor Name:</strong>
		<h:outputText
			value="#{recipientAppointmentController.appointmentDetailsForDisplay.doctorName}" />
	</p>
	<p>
		<strong>Doctor Specialization:</strong>
		<h:outputText
			value="#{recipientAppointmentController.appointmentDetailsForDisplay.doctorSpecialization}" />
	</p>
	<p>
		<strong>Doctor Gender:</strong>
		<h:outputText
			value="#{recipientAppointmentController.appointmentDetailsForDisplay.doctorGender}" />
	</p>
	<p>
		<strong>Doctor Availability Timing:</strong>
		<h:outputText
			value="#{recipientAppointmentController.appointmentDetailsForDisplay.doctorAvailabilityTiming}" />
	</p>
	<p>
		<strong>Appointment ID:</strong>
		<h:outputText
			value="#{recipientAppointmentController.appointmentDetailsForDisplay.appointmentId}" />
	</p>
	<p>
		<strong>Requested At:</strong>
		<h:outputText
			value="#{recipientAppointmentController.appointmentDetailsForDisplay.requestedAt}" />
	</p>
	<p>
		<strong>Booked At:</strong>
		<h:outputText
			value="#{recipientAppointmentController.appointmentDetailsForDisplay.bookedAt}" />
	</p>
	<p>
		<strong>Cancelled At:</strong>
		<h:outputText
			value="#{recipientAppointmentController.appointmentDetailsForDisplay.cancelledAt}" />
	</p>
	<p>
		<strong>Completed At:</strong>
		<h:outputText
			value="#{recipientAppointmentController.appointmentDetailsForDisplay.completedAt}" />
	</p>
	<p>
		<strong>Status:</strong>
		<h:outputText
			value="#{recipientAppointmentController.appointmentDetailsForDisplay.status}" />
	</p>
	<p>
		<strong>Slot No:</strong>
		<h:outputText
			value="#{recipientAppointmentController.appointmentDetailsForDisplay.slotNo}" />
	</p>
	<p>
		<strong>Start Time:</strong>
		<h:outputText
			value="#{recipientAppointmentController.appointmentDetailsForDisplay.start}" />
	</p>
	<p>
		<strong>End Time:</strong>
		<h:outputText
			value="#{recipientAppointmentController.appointmentDetailsForDisplay.end}" />
	</p>
	<h:outputLink value="./recipient-appointments.jsf"> back </h:outputLink>
</body>
	</html>
</f:view>