<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>

<f:view>
	<!DOCTYPE html>
	<html>
<head>
<meta charset="UTF-8">
<title>Login - HealthSure</title>
<style>
body {
	font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
	background-color: #e6f2f1;
	margin: 0;
	padding: 0;
	display: flex;
	align-items: center;
	justify-content: center;
	height: 100vh;
}

.container {
	width: 100%;
	max-width: 600px;
	background-color: #ffffff;
	padding: 50px 50px;
	border-radius: 16px; /* slightly more rounded */
	box-shadow: 0 10px 30px rgba(0, 128, 128, 0.25);
	/* slightly deeper shadow */
}

h2 {
	text-align: center;
	color: #006d6a;
	margin-bottom: 30px;
	font-size: 26px;
}

label {
	display: block;
	margin-bottom: 6px;
	font-weight: 600;
	color: #444;
}

input[type="text"], input[type="password"] {
	width: 100%;
	padding: 12px 14px;
	margin-bottom: 8px;
	border: 1px solid #b0c4c4;
	border-radius: 6px;
	font-size: 15px;
	background-color: #f9fdfd;
	margin-right: 10px;
	padding-left: 10px;
}

.btn {
	background-color: #008080;
	color: white;
	padding: 14px;
	width: 100%;
	border: none;
	border-radius: 6px;
	font-size: 17px;
	cursor: pointer;
	margin-top: 12px;
}

.btn:hover {
	background-color: #006666;
}

.error-message {
	color: #d9534f;
	font-size: 13px;
	margin-top: -6px;
	margin-bottom: 14px;
	display: block;
}

.forgot-link {
	margin-top: 16px;
	text-align: center;
}

.forgot-link a {
	color: #008080;
	text-decoration: underline;
	font-weight: 600;
}

.error-messages {
	margin-top: 16px;
}
</style>
</head>
<body>

	<jsp:include page="/navbar/Navbar.jsp" />

	<h:form id="login">
		<div class="container">
			<h2>Login to HealthSure</h2>

			<!-- Username -->
			<label for="userName">User name</label>
			<h:inputText id="userName" value="#{loginController.userName}" />
			<h:message for="userName" style="color:red" />

			<!-- Password -->
			<label for="password">Password</label>
			<h:inputSecret id="password" value="#{loginController.password}" />
			<div style="min-height: 18px;">
				<h:message for="password" styleClass="error-message" />
			</div>


			<!-- Login Button -->
			<h:commandButton value="Login" action="#{loginController.login}"
				styleClass="btn" />

			<!-- Forgot Password Link -->
			<div class="forgot-link">
				<h:outputLink value="ForgotPassword.jsf">
                    Forgot Password?
                </h:outputLink>
			</div>



			<!-- Error Messages -->
			<div class="error-messages" style="min-height: 18px;">
				<h:messages globalOnly="true" layout="list"
					styleClass="error-message" />
			</div>

		</div>



	</h:form>
</body>
	</html>
</f:view>