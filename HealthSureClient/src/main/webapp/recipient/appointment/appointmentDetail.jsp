<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

<f:view>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Appointment Details</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/appointment/recipientAppointment.css" />
    <style>
        .details-container {
            max-width: 800px;
            margin: auto;
            background-color: #ffffff;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
        }
        .detail-item {
            margin-bottom: 20px;
            font-size: 16px;
            line-height: 1.4;
        }
        .detail-label {
            font-weight: bold;
            display: block;
            margin-bottom: 5px;
        }
        .back-link {
            margin-top: 30px;
            display: inline-block;
            text-decoration: none;
            color: #0066cc;
            font-weight: bold;
        }
    </style>
</head>
<body class="body-bg min-h-screen-full page-padding">
    <jsp:include page="../../navbar/NavRecipient.jsp" />
    <div class="header-spacing"></div>
    <jsp:include page="NavBar.jsp" />
    <div class="header-spacing"></div>

    <div class="details-container">
        <h1 class="main-title">Appointment Details</h1>

        <div class="detail-item">
            <span class="detail-label">Doctor Name:</span>
            <h:outputText value="#{appointmentDetailController.appointmentDetailsForDisplay.doctorName}" />
        </div>

        <div class="detail-item">
            <span class="detail-label">Specialization:</span>
            <h:outputText value="#{appointmentDetailController.appointmentDetailsForDisplay.doctorSpecialization}" />
        </div>

        <div class="detail-item">
            <span class="detail-label">Gender:</span>
            <h:outputText value="#{appointmentDetailController.appointmentDetailsForDisplay.doctorGender}" />
        </div>

        <div class="detail-item">
            <span class="detail-label">Availability Timing:</span>
            <h:outputText value="#{appointmentDetailController.appointmentDetailsForDisplay.doctorAvailabilityTiming}" />
        </div>

        <div class="detail-item">
            <span class="detail-label">Appointment ID:</span>
            <h:outputText value="#{appointmentDetailController.appointmentDetailsForDisplay.appointmentId}" />
        </div>

        <div class="detail-item">
            <span class="detail-label">Requested At:</span>
            <h:outputText value="#{appointmentDetailController.appointmentDetailsForDisplay.requestedAt}" />
        </div>

        <div class="detail-item">
            <span class="detail-label">Booked At:</span>
            <h:outputText value="#{appointmentDetailController.appointmentDetailsForDisplay.bookedAt}" />
        </div>

        <div class="detail-item">
            <span class="detail-label">Cancelled At:</span>
            <h:outputText value="#{appointmentDetailController.appointmentDetailsForDisplay.cancelledAt}" />
        </div>

        <div class="detail-item">
            <span class="detail-label">Completed At:</span>
            <h:outputText value="#{appointmentDetailController.appointmentDetailsForDisplay.completedAt}" />
        </div>

        <div class="detail-item">
            <span class="detail-label">Status:</span>
            <h:outputText value="#{appointmentDetailController.appointmentDetailsForDisplay.status}" />
        </div>

        <div class="detail-item">
            <span class="detail-label">Slot No:</span>
            <h:outputText value="#{appointmentDetailController.appointmentDetailsForDisplay.slotNo}" />
        </div>

        <div class="detail-item">
            <span class="detail-label">Start Time:</span>
            <h:outputText value="#{appointmentDetailController.appointmentDetailsForDisplay.start}" />
        </div>

        <div class="detail-item">
            <span class="detail-label">End Time:</span>
            <h:outputText value="#{appointmentDetailController.appointmentDetailsForDisplay.end}" />
        </div>

        <h:outputLink value="./recipient-appointments.jsf" styleClass="back-link">← Back to Appointments</h:outputLink>
    </div>
</body>
</html>
</f:view>







