<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html"%>

<link rel="stylesheet"
	href="/HealthSureClient/resources/css/navStyle.css" />

<nav id="navbar" class="navbar navbar-transparent">
	<div class="navbar-container">

		<!-- LOGO + NAME -->
		<div class="navbar-logo">
			<a href="/HealthSureClient/home/Home.jsf" class="logo-link"> <img
				src="/HealthSureClient/resources/media/images/Logo.jpg"
				alt="Apollo Logo" class="logo-img" />
			</a> <span class="brand-name">HealthSure</span>

		</div>

		<!-- NAV LINKS -->
		<ul class="nav-links">
			<li><a href="#home" class="nav-link">Home</a></li>
			<li><a href="#departments" class="nav-link">Departments</a></li>
			<li><a href="#doctors" class="nav-link">Doctors</a></li>
			<li><a href="#aboutus" class="nav-link">About Us</a></li>
			<li><a href="#contact" class="nav-link">Contact</a></li>
		</ul>

		<!-- LOGIN + SIGNUP -->


		<div class="auth-buttons">
			<h:panelGroup rendered="#{empty sessionScope.loggedInRecipient }">
				<!-- LOGIN -->
				<div class="dropdown">
					<button class="dropdown-button login-btn">Login ▾</button>
					<ul class="dropdown-menu">
						<li><a href="adminLogin.jsp">Admin</a></li>
						<li><a href="providerLogin.jsp">Provider</a></li>
						<li><a href="/HealthSureClient/recipient/Login.jsf">Recipient</a></li>
						<li><a href="pharmacyLogin.jsp">Pharmacy</a></li>
						<li><a href="doctorLogin.jsp">Doctor</a></li>
					</ul>
				</div>

				<!-- SIGNUP -->
				<div class="dropdown">
					<button class="dropdown-button signup-btn">Signup ▾</button>
					<ul class="dropdown-menu">
						<li><a href="adminSignup.jsp">Admin</a></li>
						<li><a href="providerSignup.jsp">Provider</a></li>
						<li><a href="recipientSignup.jsp">Recipient</a></li>
						<li><a href="pharmacySignup.jsp">Pharmacy</a></li>
						<li><a href="doctorSignup.jsp">Doctor</a></li>
					</ul>
				</div>
			</h:panelGroup>
			<h:panelGroup rendered="#{not empty sessionScope.loggedInRecipient }"
				layout="block"
				style="display: flex; align-items: center; justify-content: flex-end; gap: 10px; padding-right: 15px;">

				<li style="list-style: none;"><a
					href="${pageContext.request.contextPath}/recipient/RecipientDashBoard.jsf"
					style="background-color: #3da1b9; padding: 5px 10px; border-radius: 5px; color: white; text-decoration: none;">
						DashBoard </a></li>

				<h:form style="margin: 0;">
					<h:commandButton value="Logout" action="#{loginController.logout}"
						style="background-color: #e74c3c; color: white; border: none; padding: 5px 10px; border-radius: 5px; cursor: pointer;" />
				</h:form>

			</h:panelGroup>

		</div>
	</div>
</nav>

<script>
    window.addEventListener("scroll", () => {
        const navbar = document.getElementById("navbar");
        if (window.scrollY > 20) {
            navbar.classList.remove("navbar-transparent");
            navbar.classList.add("navbar-scrolled");
        } else {
            navbar.classList.remove("navbar-scrolled");
            navbar.classList.add("navbar-transparent");
        }
    });
</script>
