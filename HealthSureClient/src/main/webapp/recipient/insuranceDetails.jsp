<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html"%>

<f:view>
	<html>
<head>
<title>Insurance Policy Details</title>
<style>
body {
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    background: #f0f2f5;
    color: #212529; /* Changed to a darker color for better readability */
    margin: 0;
    padding: 40px;
    line-height: 1.6;
}

h2 {
    color: #1a237e;
    font-size: 2.5em;
    font-weight: 300;
    text-align: center;
    margin-bottom: 40px;
    position: relative;
    letter-spacing: 1px;
}

h2::after {
    content: '';
    display: block;
    width: 60px;
    height: 4px;
    background-color: #007bff;
    margin: 10px auto 0;
    border-radius: 2px;
}

.card {
    width: 80%;
    max-width: 1000px;
    background-color: #fff;
    border-radius: 12px;
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
    padding: 35px;
    margin: 30px auto 0 auto;
    transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.section-title {
    font-size: 1.5em;
    font-weight: 600;
    margin-top: 30px;
    margin-bottom: 20px;
    color: #343a40;
    border-left: 5px solid #007bff;
    padding-left: 15px;
    line-height: 1;
}

.detail-table {
    width: 100%;
    border-collapse: separate;
    border-spacing: 0 8px;
    margin-top: 15px;
}

.detail-table th, .detail-table td {
    padding: 15px;
    border: none;
}

.detail-table th {
    background-color: #e9ecef;
    color: #495057; /* Retained this as it's already a good dark shade */
    text-align: left;
    font-weight: 600;
    width: 35%;
    border-radius: 6px 0 0 6px;
}

.detail-table td {
    background-color: #f8f9fa;
    color: #343a40; /* Changed to a much darker shade */
    border-radius: 0 6px 6px 0;
}

.members-table {
    width: 100%;
    border-collapse: collapse;
    margin-top: 20px;
}

.members-table th, .members-table td {
    padding: 15px 20px;
    border-bottom: 1px solid #e0e0e0;
    text-align: left;
}

.members-table th {
    background-color: #f1f3f5;
    font-weight: 600;
    color: #343a40; /* Changed to a darker shade */
}

.members-table tr:nth-child(even) {
    background-color: #fafafa;
}

.back-button {
    display: block;
    margin: 40px auto 0 auto;
    padding: 12px 25px;
    background-color: #007bff;
    color: white;
    font-weight: bold;
    text-decoration: none;
    border-radius: 5px;
    transition: background-color 0.3s ease;
    text-align: center;
    width: fit-content;
}

.no-data {
    text-align: center;
    padding: 50px;
    font-size: 1.2em;
    color: #888;
    background-color: #fff;
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}
</style>
</head>

<body>

	<h2>Insurance Policy Details</h2>

	<h:panelGroup rendered="#{showincController.selectedItem != null}">
		<div class="card">

			<div class="section-title">Policy Information</div>

			<h:panelGrid columns="2" styleClass="detail-table">
				<h:outputText value="Subscription ID:" />
				<h:outputText value="#{showincController.selectedItem.subscribeId}" />

				<h:outputText value="Patient Name:" />
				<h:outputText value="#{showincController.selectedItem.patientName}" />

				<h:outputText value="Patient ID:" />
				<h:outputText value="#{showincController.selectedItem.HId}" />

				<h:outputText value="Company Name:" />
				<h:outputText value="#{showincController.selectedItem.companyName}" />

				<h:outputText value="Coverage ID:" />
				<h:outputText value="#{showincController.selectedItem.coverageid}" />

				<h:outputText value="Plan Name:" />
				<h:outputText value="#{showincController.selectedItem.planName}" />

				<h:outputText value="Coverage Start Date:" />
				<h:outputText
					value="#{showincController.selectedItem.coverageStartDate}">
					<f:convertDateTime pattern="dd-MM-yyyy" />
				</h:outputText>

				<h:outputText value="Coverage End Date:" />
				<h:outputText
					value="#{showincController.selectedItem.coverageEndDate}">
					<f:convertDateTime pattern="dd-MM-yyyy" />
				</h:outputText>

				<h:outputText value="Coverage Status:" />
				<h:outputText
					value="#{showincController.selectedItem.coverageStatus}" />

				<h:outputText value="Coverage Type:" />
				<h:outputText value="#{showincController.selectedItem.coverageType}" />

				<h:outputText value="Coverage Limit:" />
				<h:outputText
					value="#{showincController.selectedItem.coverageLimit}" />

				<h:outputText value="Remaining Coverage:" />
				<h:outputText value="#{showincController.selectedItem.remaining}" />

				<h:outputText value="Claimed Amount:" />
				<h:outputText value="#{showincController.selectedItem.claimed}" />
			</h:panelGrid>
		</div>

		<h:panelGroup
			rendered="#{!empty showincController.selectedItem.subscribedMembers}">
			<div class="card">
				<div class="section-title">Subscribed Members</div>

				<h:dataTable
					value="#{showincController.selectedItem.subscribedMembers}"
					var="member" styleClass="members-table">
					<h:column>
						<f:facet name="header">Member Name: </f:facet>
						<h:outputText value="#{member.fullName}" />
					</h:column>
					<h:column>
						<f:facet name="header">Relation: </f:facet>
						<h:outputText value="#{member.relationWithProposer}" />
					</h:column>
					<h:column>
						<f:facet name="header">Age: </f:facet>
						<h:outputText value="#{member.age}" />
					</h:column>
				</h:dataTable>
			</div>
		</h:panelGroup>
		<h:form>
			<h:commandButton value="«  Back"
				action="#{showincController.goBackinc}" styleClass="back-button" />
		</h:form>

	</h:panelGroup>

	<h:panelGroup rendered="#{showincController.selectedItem == null}">
		<p class="no-data">No insurance details found.</p>

	</h:panelGroup>



</body>
<%-- <a href="${pageContext.request.contextPath}/recipient/ShowInsurance.jsf" class="back-button">Back</a>--%>
	</html>
</f:view>