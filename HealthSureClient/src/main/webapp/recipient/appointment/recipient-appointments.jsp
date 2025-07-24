<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core"%>

<f:view>
	<html>
<head>
<title>Recipient Appointments</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/resources/css/appointment/recipientAppointment.css">
<style>
/* Specific table cell alignment, can also be moved to external CSS if preferred */
.slots-table-component>tbody>tr>td {
	text-align: center;
	/* Important for absolute positioning of inner elements */
	position: relative;
	/* Ensure padding is defined here for the cell itself */
	padding: 20px 1px; /* px-4 py-2 from your original CSS */
}

/* New CSS for clickable rows */
.clickable-row {
    cursor: pointer;
}

/* Ensure the hidden button doesn't take up space or interfere with layout */
.hidden-command-button {
    display: none;
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
							d="M4 12a8 8 0 018-8v4a4 0 00-4 4H4z"></path>
					</svg>
					<p class="loading-text">Loading details...</p> </div>
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
				rowClasses="table-row-odd clickable-row,table-row-even clickable-row"
				columnClasses="table-cell">
				
				<h:column>
					<f:facet name="header">
						<h:outputText value="Appointment ID" />
					</f:facet>
					<h:panelGroup
						styleClass="cell-content #{appt.status != null ? appt.status.name().toLowerCase() : ''}-status-background">
						<h:outputText value="#{appt.appointmentId}" />
                        <h:commandButton id="detailButton" value="View"
                                       action="#{appointmentDetailController.loadAppointmentDetailsForDisplay(appt.appointmentId)}"
                                       styleClass="hidden-command-button"
                                       onclick="document.getElementById('loadingOverlay').style.display = 'flex'; return true;">
                            <f:setPropertyActionListener
                                target="#{appointmentDetailController.selectedAppointmentIdForDetail}"
                                value="#{appt.appointmentId}" />
                        </h:commandButton>
					</h:panelGroup>
				</h:column>

				<h:column>
					<f:facet name="header">
						<h:outputText value="Doctor Name" />
					</f:facet>
					<h:panelGroup
						styleClass="cell-content #{appt.status != null ? appt.status.name().toLowerCase() : ''}-status-background">
						<h:outputText
							value="#{appt.doctor != null ? appt.doctor.doctorName : 'N/A'}" />
					</h:panelGroup>
				</h:column>

				<h:column>
					<f:facet name="header">
						<h:outputText value="Appointment Date" />
					</f:facet>
					<h:panelGroup
						styleClass="cell-content #{appt.status != null ? appt.status.name().toLowerCase() : ''}-status-background">
						<h:outputText value="#{appt.start}">
							<f:convertDateTime pattern="yyyy-MM-dd HH:mm" />
						</h:outputText>
					</h:panelGroup>
				</h:column>

				<h:column>
					<f:facet name="header">
						<h:outputText value="Status" />
					</f:facet>
					<h:panelGroup
						styleClass="cell-content #{appt.status != null ? appt.status.name().toLowerCase() : ''}-status-background">
						<h:outputText value="#{appt.status}" />
					</h:panelGroup>
				</h:column>

				<h:column>
					<f:facet name="header">
						<h:outputText value="Specialization " />
					</f:facet>
					<h:panelGroup
						styleClass="cell-content #{appt.status != null ? appt.status.name().toLowerCase() : ''}-status-background">
						<h:outputText value="#{appt.doctor.specialization}" />
					</h:panelGroup>
				</h:column>

				<h:column>
					<f:facet name="header">
						<h:outputText value="Actions" />
					</f:facet>
					<h:panelGroup
						styleClass="cell-content #{appt.status != null ? appt.status.name().toLowerCase() : ''}-status-background">
						<h:commandButton value="Cancel"
							rendered="#{recipientAppointmentController.cancellableMap[appt.appointmentId]}"
							onclick="event.stopPropagation(); return showLoadingAndConfirm();"
							action="#{recipientAppointmentController.cancelAppointment}"
							styleClass="cancel-button">
							<f:setPropertyActionListener
								target="#{recipientAppointmentController.selectedAppointment}"
								value="#{appt}" />
						</h:commandButton>
					</h:panelGroup>
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
        // This function will be called once the DOM is fully loaded.
        // It ensures that elements exist before we try to attach listeners.
        document.addEventListener('DOMContentLoaded', function() {
            // Get all rows with the 'clickable-row' class within the table body
            const rows = document.querySelectorAll('.slots-table-component tbody tr.clickable-row');

            rows.forEach(row => {
                row.addEventListener('click', function(event) {
                    // Check if the click originated from the cancel button or its children
                    // or from any element with the 'no-row-click' class (for other future interactive elements)
                    if (event.target.closest('.cancel-button') || 
                        event.target.closest('.pagination-button') ||
                        event.target.closest('.no-row-click')) { // Add this class to any element that should NOT trigger row click
                        return; // Do nothing if a specific button or element was clicked
                    }

                    // Find the hidden commandButton within the clicked row
                    // Note: JSF generates complex IDs like formId:dataTableId:rowIndex:componentId
                    // querySelector works with just the class or attribute selectors
                    const detailButton = row.querySelector('.hidden-command-button');
                    
                    if (detailButton) {
                        // Programmatically click the hidden button
                        detailButton.click();
                    }
                });
            });
        });


		function showLoadingAndConfirm() {
			const confirmCancel = confirm('Are you sure you want to cancel this appointment?');
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