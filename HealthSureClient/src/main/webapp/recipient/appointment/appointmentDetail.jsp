<%-- 
This page is to show all the details for the user 
appointment with a necessary informantion about the doctor
--%>


<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html"%>

<f:view>
	<!DOCTYPE html>
	<html>
<head>
<meta charset="UTF-8">
<title>Appointment Details</title>

<!-- Font Awesome & Google Fonts -->
<link rel="stylesheet"
	href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" />
<link
	href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600&display=swap"
	rel="stylesheet" />

<!-- Your custom CSS -->
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/resources/css/appointment/recipientAppointment.css" />
	

<style>
:root {
	--primary: #4f46e5;
	--success: #10b981;
	--warning: #f59e0b;
	--danger: #ef4444;
	--light: #f9fafb;
	--dark: #1f2937;
	--text: #374151;
	--border: #e5e7eb;
}

* {
	margin: 0;
	padding: 0;
	box-sizing: border-box;
	font-family: 'Poppins', sans-serif;
}

body {
	background: #f3f4f6;
	color: var(--text);
	padding: 20px;
	font-size: 14px;
}

.card {
	background: white;
	border-radius: 10px;
	box-shadow: 0 10px 20px rgba(0, 0, 0, 0.06);
	max-width: 800px;
	margin: 0 auto;
	overflow: hidden;
}

.card-header {
	background: var(--primary);
	color: white;
	padding: 16px 20px;
	display: flex;
	justify-content: space-between;
	align-items: center;
	font-size: 16px;
}

.card-header h2 {
	font-weight: 500;
	font-size: 16px;
}

.status {
	padding: 4px 12px;
	border-radius: 20px;
	font-size: 11px;
	font-weight: 600;
	text-transform: uppercase;
}

.status-booked {
	color: var(--success);
	background: rgba(16, 185, 129, 0.15);
}

.status-pending {
	color: var(--warning);
	background: rgba(245, 158, 11, 0.15);
}

.status-cancelled {
	color: var(--danger);
	background: rgba(239, 68, 68, 0.15);
}

.status-completed {
	color: var(--primary);
	background: rgba(79, 70, 229, 0.15);
}

.card-body {
	padding: 20px;
}

.detail-grid {
	display: grid;
	grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
	gap: 16px;
	margin-bottom: 16px;
}

.detail-item {
	margin-bottom: 8px;
	font-size: 13.5px;
}

.detail-label {
	font-weight: 600;
	color: #6b7280;
	display: block;
}

.detail-value {
	font-weight: 400;
	color: #111827;
}

.timeline {
	margin-top: 16px;
	padding-left: 16px;
	border-left: 2px solid var(--border);
}

.timeline-item {
	position: relative;
	padding-left: 18px;
	margin-bottom: 14px;
}

.timeline-dot {
	position: absolute;
	left: -7px;
	top: 4px;
	width: 12px;
	height: 12px;
	border-radius: 50%;
	background: var(--primary);
	border: 2px solid white;
	box-shadow: 0 0 0 2px var(--primary);
}

.timeline-date {
	font-size: 12px;
	color: #6b7280;
	margin-bottom: 2px;
}

.actions {
	display: flex;
	justify-content: flex-end;
	gap: 10px;
	padding: 14px 20px;
	background-color: #f9fafb;
	border-top: 1px solid var(--border);
}

.btn {
	padding: 7px 14px;
	border-radius: 6px;
	font-size: 13px;
	font-weight: 500;
	cursor: pointer;
	display: inline-block;
	text-decoration: none;
}

.btn-back {
	background: white;
	border: 1px solid var(--border);
	color: var(--text);
}

.btn-cancel {
	background: white;
	border: 1px solid var(--danger);
	color: var(--danger);
}

.btn-reschedule {
	background: var(--primary);
	color: white;
	border: 1px solid var(--primary);
}

.btn:hover {
	opacity: 0.85;
}
</style>
</head>
<body class="body-bg min-h-screen-full page-padding">
	<jsp:include page="../../navbar/NavRecipient.jsp" />
	<div class="header-spacing"></div>
	<jsp:include page="NavBar.jsp" />
	<div class="header-spacing"></div>

	<div class="card">
		<div class="card-header">
			<%-- Appoinemtnt Id of the recipient --%>
			<h2>
				<i class="fas fa-calendar-check"></i> Appointment #
				<h:outputText
					value="#{appointmentDetailController.appointmentDetailsForDisplay.appointmentId}" />
			</h2>
			<%-- Show the status of the appointment --%>
			<!-- Dynamic Status Rendering -->
			<h:panelGroup
				rendered="#{appointmentDetailController.appointmentDetailsForDisplay.status == 'BOOKED'}">
				<span class="status status-booked">BOOKED</span>
			</h:panelGroup>
			<h:panelGroup
				rendered="#{appointmentDetailController.appointmentDetailsForDisplay.status == 'PENDING'}">
				<span class="status status-pending">PENDING</span>
			</h:panelGroup>
			<h:panelGroup
				rendered="#{appointmentDetailController.appointmentDetailsForDisplay.status == 'CANCELLED'}">
				<span class="status status-cancelled">CANCELLED</span>
			</h:panelGroup>
			<h:panelGroup
				rendered="#{appointmentDetailController.appointmentDetailsForDisplay.status == 'COMPLETED'}">
				<span class="status status-completed">COMPLETED</span>
			</h:panelGroup>
		</div>

		<div class="card-body">
			<div class="detail-grid">
				<div>
					<div class="detail-item">
					<%-- Show the Doctor Name to whom the recipient request for the appointment --%>
						<span class="detail-label">Doctor:</span> <span
							class="detail-value"><h:outputText
								value="#{appointmentDetailController.appointmentDetailsForDisplay.doctorName}" /></span>
					</div>
					<div class="detail-item">
					<%-- Show the doctor specialization of the doctor --%>
						<span class="detail-label">Specialization:</span> <span
							class="detail-value"><h:outputText
								value="#{appointmentDetailController.appointmentDetailsForDisplay.doctorSpecialization}" /></span>
					</div>
					<div class="detail-item">
					<%-- Doctor gender --%>
						<span class="detail-label">Gender:</span> <span
							class="detail-value"><h:outputText
								value="#{appointmentDetailController.appointmentDetailsForDisplay.doctorGender}" /></span>
					</div>
					<div class="detail-item">
					<%-- Hospital ( Provider) name --%>
						<span class="detail-label">Provider:</span> <span
							class="detail-value">HealthSure Hospital</span>
					</div>
				</div>
				<div>
					<div class="detail-item">
					<%-- Appointment Date --%>
						<span class="detail-label">Date:</span> <span class="detail-value">
							<h:outputText
								value="#{appointmentDetailController.appointmentDetailsForDisplay.start}">
								<f:convertDateTime pattern="dd MMM yyyy" />
							</h:outputText>
						</span>
					</div>
					<div class="detail-item">
					<%-- Appointment timing --%>
						<span class="detail-label">Time:</span> <span class="detail-value">
							<h:outputText
								value="#{appointmentDetailController.appointmentDetailsForDisplay.start}">
								<f:convertDateTime pattern="hh:mm a" />
							</h:outputText> - <h:outputText
								value="#{appointmentDetailController.appointmentDetailsForDisplay.end}">
								<f:convertDateTime pattern="hh:mm a" />
							</h:outputText>
						</span>
					</div>
					<%-- Show the slot number of that appointment of the recipient --%>
					<div class="detail-item">
						<span class="detail-label">Slot:</span> <span class="detail-value">#<h:outputText
								value="#{appointmentDetailController.appointmentDetailsForDisplay.slotNo}" /></span>
					</div>
				</div>
			</div>

			<div class="timeline">
				<%-- Appointment request date --%>
				<h:panelGroup
					rendered="#{not empty appointmentDetailController.appointmentDetailsForDisplay.requestedAt}">
					<div class="timeline-item">
						<div class="timeline-dot"></div>
						<div class="timeline-date">
							<h:outputText
								value="#{appointmentDetailController.appointmentDetailsForDisplay.requestedAt}">
								<f:convertDateTime pattern="dd MMM yyyy - hh:mm a" />
							</h:outputText>
						</div>
						<div>Appointment requested</div>
					</div>
				</h:panelGroup>
				<%-- Appointment book timing --%>
				<h:panelGroup
					rendered="#{not empty appointmentDetailController.appointmentDetailsForDisplay.bookedAt}">
					<div class="timeline-item">
						<div class="timeline-dot"></div>
						<div class="timeline-date">
							<h:outputText
								value="#{appointmentDetailController.appointmentDetailsForDisplay.bookedAt}">
								<f:convertDateTime pattern="dd MMM yyyy - hh:mm a" />
							</h:outputText>
						</div>
						<div>Appointment confirmed</div>
					</div>
				</h:panelGroup>
				<%-- Appointment cancel timing --%>
				<h:panelGroup
					rendered="#{not empty appointmentDetailController.appointmentDetailsForDisplay.cancelledAt}">
					<div class="timeline-item">
						<div class="timeline-dot"></div>
						<div class="timeline-date">
							<h:outputText
								value="#{appointmentDetailController.appointmentDetailsForDisplay.cancelledAt}">
								<f:convertDateTime pattern="dd MMM yyyy - hh:mm a" />
							</h:outputText>
						</div>
						<div>Appointment cancelled</div>
					</div>
				</h:panelGroup>
				<%-- Appointment complete timing --%>
				<h:panelGroup
					rendered="#{not empty appointmentDetailController.appointmentDetailsForDisplay.completedAt}">
					<div class="timeline-item">
						<div class="timeline-dot"></div>
						<div class="timeline-date">
							<h:outputText
								value="#{appointmentDetailController.appointmentDetailsForDisplay.completedAt}">
								<f:convertDateTime pattern="dd MMM yyyy - hh:mm a" />
							</h:outputText>
						</div>
						<div>Appointment completed</div>
					</div>
				</h:panelGroup>
			</div>
		</div>

		<div class="actions">
			<%-- Back button --%>
			<a href="recipient-appointments.jsf" class="btn btn-back"> <i
				class="fas fa-arrow-left"></i> Back
			</a>

			<h:panelGroup
				rendered="#{appointmentDetailController.appointmentDetailsForDisplay.status == 'BOOKED' || appointmentDetailController.appointmentDetailsForDisplay.status == 'PENDING'}">
			<%-- Appointment reschedule button --%>
				<h:form style="display:inline;">
					<h:commandButton value="Reschedule" styleClass="btn btn-reschedule"
						action="#{doctorAvailabilityController.rescheduleAppointment(appointmentDetailController.appointmentDetailsForDisplay.doctorId,appointmentDetailController.appointmentDetailsForDisplay.appointmentId)}">
						<f:param name="appointmentId"
							value="#{appointmentDetailController.appointmentDetailsForDisplay.appointmentId}" />

					</h:commandButton>
				</h:form>
			</h:panelGroup>
		</div>
	</div>
</body>
	</html>
</f:view>
