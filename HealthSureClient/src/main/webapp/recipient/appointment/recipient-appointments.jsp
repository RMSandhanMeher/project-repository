<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core"%>

<f:view>
	<html>
<head>
<title>Recipient Appointments</title>
<%-- Removed Tailwind CDN script --%>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/resources/css/appointment/recipientAppointment.css">
<%-- Link to external CSS --%>
<style>
/* Specific table cell alignment, can also be moved to external CSS if preferred */
.slots-table-component>tbody>tr>td {
	text-align: center;
}
</style>
</head>
<body class="body-bg min-h-screen-full page-padding">
	<jsp:include page="./../../navbar/NavRecipient.jsp" />
	<div class="header-spacing"></div>
	<jsp:include page="NavBar.jsp" />
	<div class="header-spacing"></div>
	<div class="main-container">
		<h1 class="main-title">My Appointments</h1>

		<h:form id="appointmentForm">
			<div id="loadingOverlay" class="loading-overlay">
				<div class="loading-overlay-content">
					<svg class="loading-spinner" xmlns="http://www.w3.org/2000/svg"
						fill="none" viewBox="0 0 24 24">
						<circle class="opacity-25-svg" cx="12" cy="12" r="10"
							stroke="currentColor" stroke-width="4"></circle>
						<path class="opacity-75-svg" fill="currentColor"
							d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z"></path>
					</svg>
					<p class="loading-text">Cancelling your appointment...</p>
				</div>
			</div>

			<div class="filter-grid-container">
				<div class="filter-item">
					<label for="timeFilter" class="filter-label">Time Filter:</label>
					<h:selectOneMenu id="timeFilter"
						value="#{recipientAppointmentController.timeFilterType}"
						styleClass="filter-select" onchange="this.form.submit();">
						<f:selectItem itemLabel="Future" itemValue="future" />
						<f:selectItem itemLabel="Past" itemValue="past" />
					</h:selectOneMenu>
				</div>

				<div class="filter-item">
					<label for="statusFilter" class="filter-label">Status
						Filter:</label>
					<h:selectOneMenu id="statusFilter"
						value="#{recipientAppointmentController.statusFilterType}"
						styleClass="filter-select" onchange="this.form.submit();">
						<f:selectItems
							value="#{recipientAppointmentController.statusFilterOptions}" />
					</h:selectOneMenu>
				</div>
				<%-- Added Page Size filter --%>
				<div class="filter-item">
					<label for="pageSizeFilter" class="filter-label">Items Per
						Page:</label>
					<h:selectOneMenu id="pageSizeFilter"
						value="#{recipientAppointmentController.pageSize}"
						styleClass="filter-select" onchange="this.form.submit();">
						<f:selectItem itemLabel="5" itemValue="5" />
						<f:selectItem itemLabel="10" itemValue="10" />
						<f:selectItem itemLabel="20" itemValue="20" />
					</h:selectOneMenu>
				</div>
			</div>

			<h:dataTable
				value="#{recipientAppointmentController.paginatedAppointments}"
				var="appt" styleClass="slots-table-component appointment-table"
				rowClasses="table-row-odd,table-row-even" columnClasses="table-cell">

				<h:column>
					<f:facet name="header">
						<h:outputText value="Appointment ID" />
					</f:facet>
					<h:outputText value="#{appt.appointmentId}" />
				</h:column>

				<h:column>
					<f:facet name="header">
						<h:outputText value="Doctor Name" />
					</f:facet>
					<h:outputText
						value="#{appt.doctor != null ? appt.doctor.doctorName : 'N/A'}" />
				</h:column>

				<h:column>
					<f:facet name="header">
						<h:outputText value="Appointment Date" />
					</f:facet>
					<h:outputText value="#{appt.start}">
						<f:convertDateTime pattern="yyyy-MM-dd HH:mm" />
					</h:outputText>
				</h:column>

				<h:column>
					<f:facet name="header">
						<h:outputText value="Status" />
					</f:facet>
					<h:outputText value="#{appt.status}" />
				</h:column>

				<h:column>
					<f:facet name="header">
						<h:outputText value="Notes" />
					</f:facet>
					<h:outputText value="#{empty appt.notes ? 'None' : appt.notes}" />
				</h:column>
				<h:column>
					<f:facet name="header">
						<h:outputText value="Actions" />
					</f:facet>
					<h:commandButton value="Cancel"
						rendered="#{recipientAppointmentController.cancellableMap[appt.appointmentId]}"
						onclick="return showLoadingAndConfirm();"
						action="#{recipientAppointmentController.cancelAppointment}"
						styleClass="cancel-button">
						<f:setPropertyActionListener
							target="#{recipientAppointmentController.selectedAppointment}"
							value="#{appt}" />
					</h:commandButton>
				</h:column>
			</h:dataTable>
			<h:panelGroup
				rendered="#{empty recipientAppointmentController.paginatedAppointments}">
				<div class="no-appointments-message">No appointments found for
					this filter.</div>
			</h:panelGroup>
			<div class="pagination-container">
				<h:commandButton value="Previous"
					action="#{recipientAppointmentController.prevPage}"
					disabled="#{recipientAppointmentController.currentPage == 1}"
					styleClass="pagination-button" />

				<span class="pagination-info"> <h:outputText
						value="Page #{recipientAppointmentController.currentPage} of #{recipientAppointmentController.totalPages}" />
				</span>

				<h:commandButton value="Next"
					action="#{recipientAppointmentController.nextPage}"
					disabled="#{recipientAppointmentController.currentPage == recipientAppointmentController.totalPages}"
					styleClass="pagination-button" />
			</div>

			<h:messages globalOnly="true" styleClass="global-message"
				infoClass="info-message" errorClass="error-message"
				warnClass="warn-message" />
		</h:form>
	</div>
	<script>
		function showLoadingAndConfirm() {
			const confirmCancel = true;
			if (confirmCancel) {
				document.getElementById("loadingOverlay").style.display = "flex";
				return true;
			}
			return false;
		}
	</script>
</body>
	</html>
</f:view>