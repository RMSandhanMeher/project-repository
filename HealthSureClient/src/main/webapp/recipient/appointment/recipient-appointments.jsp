<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core"%>

<f:view>
	<html>
<head>
<title>My Appointments</title>
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

/* Styles for sort icons - you might want to move these to your external CSS file */
.h-panelgroup {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 5px; /* Space between text and icons */
    /* Ensure the header text itself doesn't wrap oddly */
    white-space: nowrap;
}

.sort-icons-container {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
}

.sort-icons img {
    vertical-align: middle;
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
					<p class="loading-text">Loading details...</p>
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
				<%-- REMOVED Page Size filter as it's now fixed in the controller --%>
				<%-- The following section was removed:
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
				--%>
			</div>

			<h:dataTable
				value="#{recipientAppointmentController.paginatedAppointments}"
				var="appt" styleClass="slots-table-component appointment-table"
				rowClasses="table-row-odd clickable-row,table-row-even clickable-row"
				columnClasses="table-cell">

				<h:column>
					<f:facet name="header">
						<h:panelGroup styleClass="h-panelgroup">
							<h:outputText value="Appointment ID" />
							<h:panelGroup layout="block" styleClass="sort-icons-container">
								<h:commandLink
									action="#{recipientAppointmentController.sortByAsc('appointmentId')}"
									rendered="#{recipientAppointmentController.renderSortButton('appointmentId', 'asc')}"
									styleClass="sort-icons">
									<%-- Corrected image path: Use f:facet for graphicImage value --%>
									<h:graphicImage value="/resources/media/images/up-arrow.png"
										width="9" height="9" title="Sort Ascending" />
								</h:commandLink>
								<h:commandLink
									action="#{recipientAppointmentController.sortByDesc('appointmentId')}"
									rendered="#{recipientAppointmentController.renderSortButton('appointmentId', 'desc')}"
									styleClass="sort-icons">
									<%-- Corrected image path: Use f:facet for graphicImage value --%>
									<h:graphicImage value="/resources/media/images/down-arrow.png"
										width="10" height="10" title="Sort Descending" />
								</h:commandLink>
							</h:panelGroup>
						</h:panelGroup>
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
						<h:panelGroup styleClass="h-panelgroup">
							<h:outputText value="Doctor Name" />
							<h:panelGroup layout="block" styleClass="sort-icons-container">
								<h:commandLink
									action="#{recipientAppointmentController.sortByAsc('doctorName')}"
									rendered="#{recipientAppointmentController.renderSortButton('doctorName', 'asc')}"
									styleClass="sort-icons">
									<%-- Corrected image path: Use f:facet for graphicImage value --%>
									<h:graphicImage value="/resources/media/images/up-arrow.png"
										width="9" height="9" title="Sort Ascending" />
								</h:commandLink>
								<h:commandLink
									action="#{recipientAppointmentController.sortByDesc('doctorName')}"
									rendered="#{recipientAppointmentController.renderSortButton('doctorName', 'desc')}"
									styleClass="sort-icons">
									<%-- Corrected image path: Use f:facet for graphicImage value --%>
									<h:graphicImage value="/resources/media/images/down-arrow.png"
										width="10" height="10" title="Sort Descending" />
								</h:commandLink>
							</h:panelGroup>
						</h:panelGroup>
					</f:facet>
					<h:panelGroup
						styleClass="cell-content #{appt.status != null ? appt.status.name().toLowerCase() : ''}-status-background">
						<h:outputText
							value="#{appt.doctor != null ? appt.doctor.doctorName : 'N/A'}" />
					</h:panelGroup>
				</h:column>

				<h:column>
					<f:facet name="header">
						<h:panelGroup styleClass="h-panelgroup">
							<h:outputText value="Appointment Date" />
							<h:panelGroup layout="block" styleClass="sort-icons-container">
								<h:commandLink
									action="#{recipientAppointmentController.sortByAsc('start')}"
									rendered="#{recipientAppointmentController.renderSortButton('start', 'asc')}"
									styleClass="sort-icons">
									<%-- Corrected image path: Use f:facet for graphicImage value --%>
									<h:graphicImage value="/resources/media/images/up-arrow.png"
										width="9" height="9" title="Sort Ascending" />
								</h:commandLink>
								<h:commandLink
									action="#{recipientAppointmentController.sortByDesc('start')}"
									rendered="#{recipientAppointmentController.renderSortButton('start', 'desc')}"
									styleClass="sort-icons">
									<%-- Corrected image path: Use f:facet for graphicImage value --%>
									<h:graphicImage value="/resources/media/images/down-arrow.png"
										width="10" height="10" title="Sort Descending" />
								</h:commandLink>
							</h:panelGroup>
						</h:panelGroup>
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
						<h:panelGroup styleClass="h-panelgroup">
							<h:outputText value="Status" />
							<h:panelGroup layout="block" styleClass="sort-icons-container">
								<h:commandLink
									action="#{recipientAppointmentController.sortByAsc('status')}"
									rendered="#{recipientAppointmentController.renderSortButton('status', 'asc')}"
									styleClass="sort-icons">
									<%-- Corrected image path: Use f:facet for graphicImage value --%>
									<h:graphicImage value="/resources/media/images/up-arrow.png"
										width="9" height="9" title="Sort Ascending" />
								</h:commandLink>
								<h:commandLink
									action="#{recipientAppointmentController.sortByDesc('status')}"
									rendered="#{recipientAppointmentController.renderSortButton('status', 'desc')}"
									styleClass="sort-icons">
									<%-- Corrected image path: Use f:facet for graphicImage value --%>
									<h:graphicImage value="/resources/media/images/down-arrow.png"
										width="10" height="10" title="Sort Descending" />
								</h:commandLink>
							</h:panelGroup>
						</h:panelGroup>
					</f:facet>
					<h:panelGroup
						styleClass="cell-content #{appt.status != null ? appt.status.name().toLowerCase() : ''}-status-background">
						<h:outputText value="#{appt.status}" />
					</h:panelGroup>
				</h:column>

				<h:column>
					<f:facet name="header">
						<h:panelGroup styleClass="h-panelgroup">
							<h:outputText value="Specialization" />
							<h:panelGroup layout="block" styleClass="sort-icons-container">
								<h:commandLink
									action="#{recipientAppointmentController.sortByAsc('specialization')}"
									rendered="#{recipientAppointmentController.renderSortButton('specialization', 'asc')}"
									styleClass="sort-icons">
									<%-- Corrected image path: Use f:facet for graphicImage value --%>
									<h:graphicImage value="/resources/media/images/up-arrow.png"
										width="9" height="9" title="Sort Ascending" />
								</h:commandLink>
								<h:commandLink
									action="#{recipientAppointmentController.sortByDesc('specialization')}"
									rendered="#{recipientAppointmentController.renderSortButton('specialization', 'desc')}"
									styleClass="sort-icons">
									<%-- Corrected image path: Use f:facet for graphicImage value --%>
									<h:graphicImage value="/resources/media/images/down-arrow.png"
										width="10" height="10" title="Sort Descending" />
								</h:commandLink>
							</h:panelGroup>
						</h:panelGroup>
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
					disabled="#{not recipientAppointmentController.hasPrevPage}"
					styleClass="pagination-button" />

				<span class="pagination-info"> <h:outputText
						value="Page #{recipientAppointmentController.currentPage} of #{recipientAppointmentController.totalPages}" />
				</span>

				<h:commandButton value="Next"
					action="#{recipientAppointmentController.nextPage}"
					disabled="#{not recipientAppointmentController.hasNextPage}"
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
                        event.target.closest('.sort-icons') || // Added to prevent row click on sort icons
                        event.target.closest('.filter-select') || // Added to prevent row click on filter dropdowns
                        event.target.closest('.no-row-click')) {
                        return; // Do nothing if a specific button or element was clicked
                    }

                    // Find the hidden commandButton within the clicked row
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