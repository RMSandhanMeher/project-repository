<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html"%>
<!DOCTYPE html>
<html lang="en">
<head>
<title>Recipient Profile</title>
<style>
body {
	font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
		"Helvetica Neue", Arial, sans-serif;
	background-color: #f0f2f5;
	margin: 0;
	padding: 0;
	display: flex;
	flex-direction: column;
	min-height: 100vh;
}

.container {
	max-width: 800px;
    margin: 100px auto 10px auto;
	padding: 24px;
	background-color: #ffffff;
	border-radius: 12px;
	box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
	flex-grow: 1;
}

.heading-primary {
	font-size: 2.5rem;
	color: #1e3a8a;
	text-align: center;
	margin-bottom: 24px;
	font-weight: 700;
}

.details-box {
	background-color: #f9fbfc;
	border: 1px solid #e2e8f0;
	border-radius: 8px;
	padding: 24px;
	margin-bottom: 24px;
}

.details-box p {
	font-size: 1.1rem;
	color: #4a5568;
	line-height: 1.6;
	margin: 12px 0;
}

.details-box strong {
	color: #1e3a8a;
	font-weight: 600;
	min-width: 150px;
	display: inline-block;
}

.button-group {
	display: flex;
	justify-content: center;
	gap: 16px;
}

.button-group .button-red, .button-group .button-yellow {
	padding: 12px 24px;
	font-size: 1rem;
	font-weight: 600;
	border: none;
	border-radius: 8px;
	cursor: pointer;
	transition: all 0.2s ease-in-out;
	text-transform: uppercase;
}

.button-group .button-red {
	background-color: #ef4444;
	color: #ffffff;
}

.button-group .button-red:hover {
	background-color: #dc2626;
	box-shadow: 0 4px 6px rgba(239, 68, 68, 0.3);
}

.button-group .button-yellow {
	background-color: #fcd34d;
	color: #1e3a8a;
}

.button-group .button-yellow:hover {
	background-color: #facc15;
	box-shadow: 0 4px 6px rgba(252, 211, 77, 0.3);
}

/* Basic Flexbox for details layout */
.details-box p {
	display: flex;
	align-items: center;
	gap: 10px;
}
</style>
</head>
<body>
	<f:view>
		<jsp:include page="/navbar/NavRecipient.jsp" />
		<div class="container">
			<h1 class="heading-primary">

				Welcome,
				<h:outputText value="#{sessionScope.provider_name}" />
			</h1>
			<div class="details-box">
				<p>
					<strong>Recipient Name:</strong>
					<h:outputText value="#{loginController.fullName}" />
				</p>
				<p>
					<strong>Email:</strong>
					<h:outputText value="#{sessionScope.loggedInRecipient.email}" />
				</p>
				<p>
					<strong>Gender:</strong>
					<h:outputText value="#{sessionScope.loggedInRecipient.gender}" />
				</p>
				<p>
					<strong>DOB:</strong>
					<h:outputText value="#{sessionScope.loggedInRecipient.dob}" />
				</p>
				<p>
					<strong>Address:</strong>
					<h:outputText value="#{sessionScope.loggedInRecipient.address}" />
				</p>
				<p>
					<strong>Mobile:</strong>
					<h:outputText value="#{sessionScope.loggedInRecipient.mobile}" />
				</p>
			</div>
			<div class="button-group">
				<h:form>
					<h:commandButton value="Logout" action="#{loginController.logout}"
						styleClass="button-red" />
				</h:form>
				<h:form>
					<h:commandButton value="Reset Password"
						action="#{authBean.resetPassword}" styleClass="button-yellow" />
				</h:form>
			</div>
		</div>
	</f:view>
	<jsp:include page="/footer/Footer.jsp" />
</body>
</html>