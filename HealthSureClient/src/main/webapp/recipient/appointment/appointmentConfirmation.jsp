<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>

<f:view>
	<html>
<head>
<title>Appointment Confirmation</title>
<script src="https://cdn.tailwindcss.com"></script>
<!-- External CSS -->
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/resources/css/appointment/appointmentConfirmation.css">
<!-- Font Awesome for icons -->
<link rel="stylesheet"
	href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body class="confirmation-page">
	<jsp:include page="./../../navbar/NavRecipient.jsp" />
	<div class="spacer"></div>
	<jsp:include page="NavBar.jsp" />
	<div class="page-container">
		<div class="max-w-sm mx-auto">
			<div class="confirmation-card">
				<!-- Header with gradient background -->
				<div class="card-header">
					<div class="header-icon-container">
						<i class="fas fa-check-circle header-icon"></i>
					</div>
					<h1 class="card-title">Appointment Confirmed!</h1>
				</div>

				<!-- Content area -->
				<div class="card-content">
					<!-- Success message -->
					<div class="success-message">
						<div class="success-icon">
							<i class="fas fa-check-circle"></i>
						</div>
						<div>
							<p class="success-text">
								<h:outputText value="#{sessionScope.confirmationMessage}" />
							</p>
						</div>
					</div>

					<!-- Additional information cards -->
					<div class="info-grid">
						<div class="info-card info-card-blue">
							<h3 class="info-title info-title-blue">What's Next?</h3>
							<ul class="info-list">
								<li>You'll receive a confirmation email</li>
								<li>Arrive 15 minutes before your appointment</li>
								<li>Bring your ID and insurance card</li>
							</ul>
						</div>
						<div class="info-card info-card-gray">
							<h3 class="info-title info-title-gray">Need Help?</h3>
							<p class="info-text">
								Contact our support team at <span class="highlight-text"><h:outputText
										value="#{initParam['providerEmail']}" /></span>
								<h:outputText value="or call #{initParam['contact']}"
									styleClass="medium-text" />
							</p>
						</div>
					</div>

					<h:form>
						<!-- Action buttons -->
						<div class="button-group">
							<h:commandButton value="Book Another Appointment"
								action="doctorAvailabilityList"
								styleClass="btn btn-primary focus-ring" />

							<h:commandButton value="Back to Home" action="providerList"
								styleClass="btn btn-secondary focus-ring" />
						</div>
					</h:form>
				</div>
			</div>
		</div>
	</div>
</body>
	</html>
</f:view>