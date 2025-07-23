<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>

<f:view>
<!DOCTYPE html>
<html>
<head>
    <title>Recipient Appointments</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/appointment/recipientAppointment.css" />
    <style>
        .slots-table-component > tbody > tr > td {
            text-align: center;
            position: relative;
            padding: 20px 1px;
        }
        .clickable-row {
            cursor: pointer;
        }
        .hidden-command-button {
            display: none;
        }
    </style>
</head>
<body class="body-bg min-h-screen-full page-padding">
    <jsp:include page="../../navbar/NavRecipient.jsp" />
    <div class="header-spacing"></div>
    <jsp:include page="NavBar.jsp" />
    <div class="header-spacing"></div>

    <div class="main-container">
        <h1 class="main-title">My Appointments</h1>

        <h:form id="appointmentForm">
            <div id="loadingOverlay" class="loading-overlay">
                <div class="loading-overlay-content">
                    <svg class="loading-spinner" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                        <circle class="opacity-25-svg" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
                        <path class="opacity-75-svg" fill="currentColor" d="M4 12a8 8 0 018-8v4a4 0 00-4 4H4z" />
                    </svg>
                    <p class="loading-text">Loading details...</p>
                </div>
            </div>

            <!-- Filter Bar -->
            <div class="filter-grid-container">
                <div class="filter-item">
                    <label class="filter-label">Time Filter:</label>
                    <h:selectOneMenu value="#{recipientAppointmentController.timeFilterType}" styleClass="filter-select" onchange="this.form.submit();">
                        <f:selectItem itemLabel="Future" itemValue="future" />
                        <f:selectItem itemLabel="Past" itemValue="past" />
                    </h:selectOneMenu>
                </div>
                <div class="filter-item">
                    <label class="filter-label">Status Filter:</label>
                    <h:selectOneMenu value="#{recipientAppointmentController.statusFilterType}" styleClass="filter-select" onchange="this.form.submit();">
                        <f:selectItems value="#{recipientAppointmentController.statusFilterOptions}" />
                    </h:selectOneMenu>
                </div>
                <div class="filter-item">
                    <label class="filter-label">Items Per Page:</label>
                    <h:selectOneMenu value="#{recipientAppointmentController.pageSize}" styleClass="filter-select" onchange="this.form.submit();">
                        <f:selectItem itemLabel="5" itemValue="5" />
                        <f:selectItem itemLabel="10" itemValue="10" />
                        <f:selectItem itemLabel="20" itemValue="20" />
                    </h:selectOneMenu>
                </div>
            </div>

            <!-- Appointments Table -->
            <h:dataTable value="#{recipientAppointmentController.paginatedAppointments}" var="appt"
                         styleClass="slots-table-component appointment-table"
                         rowClasses="table-row-odd clickable-row,table-row-even clickable-row"
                         columnClasses="table-cell">

                <h:column>
                    <f:facet name="header">ID</f:facet>
                    <h:panelGroup styleClass="cell-content #{appt.status.name().toLowerCase()}-status-background">
                        <h:outputText value="#{appt.appointmentId}" />
                        <h:commandButton value="View" styleClass="hidden-command-button"
                                         action="#{recipientAppointmentController.loadAppointmentDetailsForDisplay}"
                                         onclick="document.getElementById('loadingOverlay').style.display='flex'; return true;">
                            <f:setPropertyActionListener target="#{recipientAppointmentController.selectedAppointmentIdForDetail}" value="#{appt.appointmentId}" />
                        </h:commandButton>
                    </h:panelGroup>
                </h:column>

                <h:column>
                    <f:facet name="header">Doctor</f:facet>
                    <h:outputText value="#{appt.doctor.doctorName}" />
                </h:column>

                <h:column>
                    <f:facet name="header">Date</f:facet>
                    <h:outputText value="#{appt.start}">
                        <f:convertDateTime pattern="yyyy-MM-dd HH:mm" />
                    </h:outputText>
                </h:column>

                <h:column>
                    <f:facet name="header">Status</f:facet>
                    <h:outputText value="#{appt.status}" />
                </h:column>

                <h:column>
                    <f:facet name="header">Notes</f:facet>
                    <h:outputText value="#{empty appt.notes ? 'None' : appt.notes}" />
                </h:column>

                <h:column>
                    <f:facet name="header">Actions</f:facet>
                    <h:commandButton value="Cancel"
                                     rendered="#{recipientAppointmentController.cancellableMap[appt.appointmentId]}"
                                     onclick="event.stopPropagation(); return showLoadingAndConfirm();"
                                     action="#{recipientAppointmentController.cancelAppointment}"
                                     styleClass="cancel-button">
                        <f:setPropertyActionListener target="#{recipientAppointmentController.selectedAppointment}" value="#{appt}" />
                    </h:commandButton>
                </h:column>
            </h:dataTable>

            <h:panelGroup rendered="#{empty recipientAppointmentController.paginatedAppointments}">
                <div class="no-appointments-message">No appointments found for this filter.</div>
            </h:panelGroup>

            <!-- Pagination Controls -->
            <div class="pagination-container">
                <h:commandButton value="Previous" action="#{recipientAppointmentController.prevPage}"
                                 disabled="#{recipientAppointmentController.currentPage == 1}" styleClass="pagination-button" />
                <span class="pagination-info">Page #{recipientAppointmentController.currentPage} of #{recipientAppointmentController.totalPages}</span>
                <h:commandButton value="Next" action="#{recipientAppointmentController.nextPage}"
                                 disabled="#{recipientAppointmentController.currentPage == recipientAppointmentController.totalPages}" styleClass="pagination-button" />
            </div>

            <h:messages globalOnly="true" styleClass="global-message"
                        infoClass="info-message" errorClass="error-message" warnClass="warn-message" />
        </h:form>
    </div>

    <script>
        document.addEventListener('DOMContentLoaded', function () {
            const rows = document.querySelectorAll('.slots-table-component tbody tr.clickable-row');
            rows.forEach(row => {
                row.addEventListener('click', function (event) {
                    if (event.target.closest('.cancel-button') || event.target.closest('.pagination-button')) return;
                    const detailButton = row.querySelector('.hidden-command-button');
                    if (detailButton) detailButton.click();
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
