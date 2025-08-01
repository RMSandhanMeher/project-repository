<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core"%>

<f:view>
	<html>
<head>
<title>My Appointments</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/resources/css/appointment/recipientAppointment.css">

<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/flatpickr/dist/flatpickr.min.css">

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
	white-space: nowrap; /* Ensure the header text itself doesn't wrap oddly */
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

/* Styles for text inputs */
.filter-item input[type="text"] {
	padding: 8px 10px;
	border: 1px solid #ccc;
	border-radius: 4px;
	font-size: 1rem;
	width: 150px; /* Adjust as needed */
	box-sizing: border-box; /* Include padding and border in the element's total width and height */
}

/* Add a style for the reset button */
.reset-button {
    padding: 8px 15px;
    background-color: #6c757d; /* A neutral gray */
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 1rem;
    transition: background-color 0.2s ease;
    margin-top: 25px;
}

.reset-button:hover {
    background-color: #5a6268;
}

</style>
</head>
<body class="body-bg min-h-screen-full page-padding">
	<jsp:include page="./../../navbar/NavRecipient.jsp" />
	<div class="header-spacing"></div>
	<jsp:include page="NavBar.jsp" />
	<div class="main-container">
		<h1 class="main-title">My Appointments</h1>
		<%-- The loading animation --%>
		<h:form id="appointmentForm" prependId="false">
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
			<%-- time filter for the user appointment --%>
			<div class="filter-grid-container" style="display: flex; flex-wrap: wrap; gap: 15px;">
				<div class="filter-item">
					<label for="timeFilter" class="filter-label">Time Filter:</label>
					<h:selectOneMenu id="timeFilter"
						value="#{recipientAppointmentController.timeFilterType}"
						styleClass="filter-select" onchange="this.form.submit();">
						<f:selectItem itemLabel="Future" itemValue="future" />
						<f:selectItem itemLabel="Past" itemValue="past" />
					</h:selectOneMenu>
				</div>
				<%-- status filter for the user appointment the status are pending book complete and cancel  --%>
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
				<%-- show all the appointment after this date --%>
                <div class="filter-item">
                    <label for="fromDate" class="filter-label">From Date:</label>
                    <h:inputText id="fromDate"
                        value="#{recipientAppointmentController.fromDate}"
                        styleClass="filter-select date-input-field">
                        <f:convertDateTime pattern="yyyy-MM-dd" />
                    </h:inputText>
                </div>
                <%-- show all the appointment after this date --%>
                <div class="filter-item">
                    <label for="toDate" class="filter-label">To Date:</label>
                    <h:inputText id="toDate"
                        value="#{recipientAppointmentController.toDate}"
                        styleClass="filter-select date-input-field">
                        <f:convertDateTime pattern="yyyy-MM-dd" />
                    </h:inputText>
                </div>
				<%-- reset all the filter data  --%>
				<div class="filter-item">
					<%-- New Reset Button --%>
					<h:commandButton value="Reset Filters"
						action="#{recipientAppointmentController.resetData()}"
						styleClass="reset-button"
						onclick="showLoading(); return true;" />
				</div>

			</div>
			<%-- show filter appointments with button for sorting --%>
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
									<h:graphicImage value="/resources/media/images/up-arrow.png"
										width="9" height="9" title="Sort Ascending" />
								</h:commandLink>
								<h:commandLink
									action="#{recipientAppointmentController.sortByDesc('appointmentId')}"
									rendered="#{recipientAppointmentController.renderSortButton('appointmentId', 'desc')}"
									styleClass="sort-icons">
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
									<h:graphicImage value="/resources/media/images/up-arrow.png"
										width="9" height="9" title="Sort Ascending" />
								</h:commandLink>
								<h:commandLink
									action="#{recipientAppointmentController.sortByDesc('doctorName')}"
									rendered="#{recipientAppointmentController.renderSortButton('doctorName', 'desc')}"
									styleClass="sort-icons">
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
									<h:graphicImage value="/resources/media/images/up-arrow.png"
										width="9" height="9" title="Sort Ascending" />
								</h:commandLink>
								<h:commandLink
									action="#{recipientAppointmentController.sortByDesc('start')}"
									rendered="#{recipientAppointmentController.renderSortButton('start', 'desc')}"
									styleClass="sort-icons">
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
									<h:graphicImage value="/resources/media/images/up-arrow.png"
										width="9" height="9" title="Sort Ascending" />
								</h:commandLink>
								<h:commandLink
									action="#{recipientAppointmentController.sortByDesc('status')}"
									rendered="#{recipientAppointmentController.renderSortButton('status', 'desc')}"
									styleClass="sort-icons">
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
									<h:graphicImage value="/resources/media/images/up-arrow.png"
										width="9" height="9" title="Sort Ascending" />
								</h:commandLink>
								<h:commandLink
									action="#{recipientAppointmentController.sortByDesc('specialization')}"
									rendered="#{recipientAppointmentController.renderSortButton('specialization', 'desc')}"
									styleClass="sort-icons">
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
			<%-- Button for move to previous--%>
				<h:commandButton value="Previous"
					action="#{recipientAppointmentController.prevPage}"
					disabled="#{not recipientAppointmentController.hasPrevPage}"
					styleClass="pagination-button"
					onclick="document.getElementById('loadingOverlay').style.display = 'flex'; return true;" />
			<%-- Show current page with total number of pages --%>
				<span class="pagination-info"> <h:outputText
						value="Page #{recipientAppointmentController.currentPage} of #{recipientAppointmentController.totalPages}" />
						<h:inputHidden id="currentPage" value="#{recipientAppointmentController.currentPage}" />
				</span>
			<%-- Button for move to next --%>
				<h:commandButton value="Next"
					action="#{recipientAppointmentController.nextPage}"
					disabled="#{not recipientAppointmentController.hasNextPage}"
					styleClass="pagination-button"
					onclick="document.getElementById('loadingOverlay').style.display = 'flex'; return true;" />
			</div>

			<h:messages globalOnly="true" styleClass="global-message"
				infoClass="info-message" errorClass="error-message"
				warnClass="warn-message" />
		</h:form>
	</div>

    <script src="https://cdn.jsdelivr.net/npm/flatpickr"></script>

	<script>
        document.addEventListener('DOMContentLoaded', function() {
            // Initialize Flatpickr for "From Date"
            flatpickr("#fromDate", {
                dateFormat: "Y-m-d", // Matches f:convertDateTime pattern
                onClose: function(selectedDates, dateStr, instance) {
                    // When a date is selected and the calendar closes, submit the form
                    document.getElementById('appointmentForm').submit();
                }
            });

            // Initialize Flatpickr for "To Date"
            flatpickr("#toDate", {
                dateFormat: "Y-m-d", // Matches f:convertDateTime pattern
                onClose: function(selectedDates, dateStr, instance) {
                    // When a date is selected and the calendar closes, submit the form
                    document.getElementById('appointmentForm').submit();
                }
            });

            // Existing clickable row logic (modified to exclude date input clicks)
            const rows = document.querySelectorAll('.slots-table-component tbody tr.clickable-row');
            rows.forEach(row => {
                row.addEventListener('click', function(event) {
                    // Check if the click originated from elements that should prevent row click
                    if (event.target.closest('.cancel-button') ||
                        event.target.closest('.pagination-button') ||
                        event.target.closest('.sort-icons') ||
                        event.target.closest('.filter-select') ||
                        event.target.closest('.date-input-field') || // Exclude clicks on the date input fields
                        event.target.closest('.no-row-click') ||
                        event.target.closest('.flatpickr-calendar')) { // Exclude clicks within the flatpickr calendar itself
                        return; // Do nothing if a specific interactive element was clicked
                    }

                    const detailButton = row.querySelector('.hidden-command-button');
                    if (detailButton) {
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

		function showLoading() {
		    document.getElementById('loadingOverlay').style.display = 'flex';
		    return true;
		}
	</script>
</body>
	</html>
</f:view>