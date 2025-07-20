<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<link rel="stylesheet" href="/HealthSureClient/resources/css/navProvider.css" />

<nav id="navbar" class="navbar navbar-scrolled"> <!-- Removed navbar-transparent -->
    <div class="navbar-container">

        <!-- LOGO + NAME -->
        <div class="navbar-logo">
        	<a href="/HealthSureClient/home/Home.jsf" class="logo-link">
            <h:graphicImage library="media" name="images/Logo.jpg" alt="HealthSure Logo" styleClass="logo-img" />
            </a>
            <span class="brand-name">HealthSure</span>
        </div>

        <!-- PROVIDER NAV LINKS -->
        <ul class="nav-links">
        
            <li><a href="RecipientDashBoard.jsf" class="nav-link">DashBoard</a></li>
            <li><a href="${pageContext.request.contextPath}/recipient/appointment/doctorAvailabilityList.jsf" class="nav-link">My Appointments</a></li>
            <li><a href="ShowInsurance.jsf" class="nav-link">My Insurance</a></li>
            <li><a href="SearchProviders.jsf" class="nav-link">Find Doctors</a></li>
			<li><a href="Reports.jsf" class="nav-link">Reports</a></li>
			<li><a href="Home.jsf" class="nav-link">About Us</a></li>
        </ul>

        <!-- LOGOUT BUTTON -->
        <div class="auth-buttons">
			<h:form>
				<h:commandButton value="Logout" action="#{showincController.logout}"
					styleClass="logout-btn" />
			</h:form>

		</div>
    </div>
</nav>