<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html"%>

<f:view>
	<html>
<head>
<title>HealthSure Insurance</title>
<link
	href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap"
	rel="stylesheet">

<%-- Link to external CSS file --%>
<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/resources/css/showInc.css" />

<%-- Link to external Java Script file --%>
<script type="text/javascript"
	src="${pageContext.request.contextPath}/resources/js/insuranceFilters.js"></script>

</head>
<body>

	<jsp:include page="/navbar/NavRecipient.jsp" />

	<h2>Health Insurance Details</h2>

	<h:form id="insuranceForm" styleClass="main-content-panel">

		<%-- Filter Buttons --%>
		<h:panelGroup layout="block" styleClass="filter-buttons-bar">
			<h:commandButton id="activeOnlyBtn" value="Show Active Only"
				action="#{showincController.filterByCoverageStatus('ACTIVE')}"
				styleClass="btn btn-primary"
				onclick="setActiveFilter('activeOnlyBtn');" />
			<h:commandButton id="expiredOnlyBtn" value="Show Expired Only"
				action="#{showincController.filterByCoverageStatus('EXPIRED')}"
				styleClass="btn btn-primary"
				onclick="setActiveFilter('expiredOnlyBtn');" />

			<h:panelGroup layout="block" styleClass="date-filter-group">
				<h:outputLabel for="fromDate" value="From:" />
				<h:inputText id="fromDate" value="#{showincController.fromDate}"
					styleClass="date-input">
					<f:convertDateTime pattern="yyyy-MM-dd" />
				</h:inputText>

				<h:outputLabel for="toDate" value="To:" />
				<h:inputText id="toDate" value="#{showincController.toDate}"
					styleClass="date-input">
					<f:convertDateTime pattern="yyyy-MM-dd" />
				</h:inputText>

				<h:commandButton id="filterDateBtn" value="Filter by Date"
					action="#{showincController.filterByDateRange}"
					styleClass="btn btn-primary"
					onclick="setActiveFilter('filterDateBtn');" />
			</h:panelGroup>

			<h:commandButton id="resetFilterBtn" value="Reset Filters"
				action="#{showincController.resetFilter}"
				styleClass="btn btn-secondary" onclick="resetActiveFilter();" />
		</h:panelGroup>




		<h:messages globalOnly="true" style="color:red" id="messages" />
		<%-- Added ID to messages --%>

		<h:panelGroup id="insuranceTablePanel">


			<h:dataTable id="insuranceTable"
				value="#{showincController.insuranceData}" var="insurance"
				styleClass="data-table"
				rendered="#{not empty showincController.insuranceData}">

				<h:column>
					<f:facet name="header">
						<h:panelGroup styleClass="h-panelgroup">
							<h:outputText value="Patient Name" />
							<h:panelGroup layout="block" styleClass="sort-icons-container">
								<h:commandLink
									action="#{showincController.sortByAsc('patientName')}"
									rendered="#{showincController.renderSortButton('patientName', 'asc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/up-arrow.png"
										width="10" height="10" />
								</h:commandLink>
								<h:commandLink
									action="#{showincController.sortByDesc('patientName')}"
									rendered="#{showincController.renderSortButton('patientName', 'desc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/down-arrow.png"
										width="10" height="10" />
								</h:commandLink>
							</h:panelGroup>
						</h:panelGroup>
					</f:facet>
					<h:panelGroup rendered="#{insurance.coverageType eq 'FAMILY'}">
						<h:commandLink
							action="#{showincController.viewMembers(insurance)}"
							style="display:block; text-decoration:none; color:inherit;">
							<h:outputText value="#{insurance.patientName}" />
						</h:commandLink>
					</h:panelGroup>
					<h:panelGroup rendered="#{insurance.coverageType ne 'FAMILY'}">
						<h:outputText value="#{insurance.patientName}" />
					</h:panelGroup>
				</h:column>

				<h:column>
					<f:facet name="header">
						<h:panelGroup styleClass="h-panelgroup">
							<h:outputText value="Company" />
							<h:panelGroup layout="block" styleClass="sort-icons-container">
								<h:commandLink
									action="#{showincController.sortByAsc('companyName')}"
									rendered="#{showincController.renderSortButton('companyName', 'asc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/up-arrow.png"
										width="10" height="10" />
								</h:commandLink>
								<h:commandLink
									action="#{showincController.sortByDesc('companyName')}"
									rendered="#{showincController.renderSortButton('companyName', 'desc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/down-arrow.png"
										width="10" height="10" />
								</h:commandLink>
							</h:panelGroup>
						</h:panelGroup>
					</f:facet>
					<h:panelGroup rendered="#{insurance.coverageType eq 'FAMILY'}">
						<h:commandLink
							action="#{showincController.viewMembers(insurance)}"
							style="display:block; text-decoration:none; color:inherit;">
							<h:outputText value="#{insurance.companyName}" />
						</h:commandLink>
					</h:panelGroup>
					<h:panelGroup rendered="#{insurance.coverageType ne 'FAMILY'}">
						<h:outputText value="#{insurance.companyName}" />
					</h:panelGroup>
				</h:column>

				<h:column>
					<f:facet name="header">
						<h:panelGroup styleClass="h-panelgroup">
							<h:outputText value="Plan" />
							<h:panelGroup layout="block" styleClass="sort-icons-container">
								<h:commandLink
									action="#{showincController.sortByAsc('planName')}"
									rendered="#{showincController.renderSortButton('planName', 'asc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/up-arrow.png"
										width="10" height="10" />
								</h:commandLink>
								<h:commandLink
									action="#{showincController.sortByDesc('planName')}"
									rendered="#{showincController.renderSortButton('planName', 'desc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/down-arrow.png"
										width="10" height="10" />
								</h:commandLink>
							</h:panelGroup>
						</h:panelGroup>
					</f:facet>
					<h:panelGroup rendered="#{insurance.coverageType eq 'FAMILY'}">
						<h:commandLink
							action="#{showincController.viewMembers(insurance)}"
							style="display:block; text-decoration:none; color:inherit;">
							<h:outputText value="#{insurance.planName}" />
						</h:commandLink>
					</h:panelGroup>
					<h:panelGroup rendered="#{insurance.coverageType ne 'FAMILY'}">
						<h:outputText value="#{insurance.planName}" />
					</h:panelGroup>
				</h:column>

				<h:column>
					<f:facet name="header">
						<h:panelGroup styleClass="h-panelgroup">
							<h:outputText value="Coverage Start" />
							<h:panelGroup layout="block" styleClass="sort-icons-container">
								<h:commandLink
									action="#{showincController.sortByAsc('coverageStartDate')}"
									rendered="#{showincController.renderSortButton('coverageStartDate', 'asc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/up-arrow.png"
										width="10" height="10" />
								</h:commandLink>
								<h:commandLink
									action="#{showincController.sortByDesc('coverageStartDate')}"
									rendered="#{showincController.renderSortButton('coverageStartDate', 'desc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/down-arrow.png"
										width="10" height="10" />
								</h:commandLink>
							</h:panelGroup>
						</h:panelGroup>
					</f:facet>
					<h:panelGroup rendered="#{insurance.coverageType eq 'FAMILY'}">
						<h:commandLink
							action="#{showincController.viewMembers(insurance)}"
							style="display:block; text-decoration:none; color:inherit;">
							<h:outputText value="#{insurance.coverageStartDate}">
								<f:convertDateTime pattern="MM-dd-yyyy" />
							</h:outputText>
						</h:commandLink>
					</h:panelGroup>
					<h:panelGroup rendered="#{insurance.coverageType ne 'FAMILY'}">
						<h:outputText value="#{insurance.coverageStartDate}">
							<f:convertDateTime pattern="MM-dd-yyyy" />
						</h:outputText>
					</h:panelGroup>
				</h:column>

				<h:column>
					<f:facet name="header">
						<h:panelGroup styleClass="h-panelgroup">
							<h:outputText value="Coverage End" />
							<h:panelGroup layout="block" styleClass="sort-icons-container">
								<h:commandLink
									action="#{showincController.sortByAsc('coverageEndDate')}"
									rendered="#{showincController.renderSortButton('coverageEndDate', 'asc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/up-arrow.png"
										width="10" height="10" />
								</h:commandLink>
								<h:commandLink
									action="#{showincController.sortByDesc('coverageEndDate')}"
									rendered="#{showincController.renderSortButton('coverageEndDate', 'desc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/down-arrow.png"
										width="10" height="10" />
								</h:commandLink>
							</h:panelGroup>
						</h:panelGroup>
					</f:facet>
					<h:panelGroup rendered="#{insurance.coverageType eq 'FAMILY'}">
						<h:commandLink
							action="#{showincController.viewMembers(insurance)}"
							style="display:block; text-decoration:none; color:inherit;">
							<h:outputText value="#{insurance.coverageEndDate}">
								<f:convertDateTime pattern="MM-dd-yyyy" />
							</h:outputText>
						</h:commandLink>
					</h:panelGroup>
					<h:panelGroup rendered="#{insurance.coverageType ne 'FAMILY'}">
						<h:outputText value="#{insurance.coverageEndDate}">
							<f:convertDateTime pattern="MM-dd-yyyy" />
						</h:outputText>
					</h:panelGroup>
				</h:column>

				<h:column>
					<f:facet name="header">
						<h:panelGroup styleClass="h-panelgroup">
							<h:outputText value="Type" />
							<h:panelGroup layout="block" styleClass="sort-icons-container">
								<h:commandLink
									action="#{showincController.sortByAsc('coverageType')}"
									rendered="#{showincController.renderSortButton('coverageType', 'asc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/up-arrow.png"
										width="10" height="10" />
								</h:commandLink>
								<h:commandLink
									action="#{showincController.sortByDesc('coverageType')}"
									rendered="#{showincController.renderSortButton('coverageType', 'desc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/down-arrow.png"
										width="10" height="10" />
								</h:commandLink>
							</h:panelGroup>
						</h:panelGroup>
					</f:facet>
					<h:panelGroup rendered="#{insurance.coverageType eq 'FAMILY'}">
						<h:commandLink
							action="#{showincController.viewMembers(insurance)}"
							style="display:block; text-decoration:none; color:inherit;">
							<h:outputText value="#{insurance.coverageType}" />
						</h:commandLink>
					</h:panelGroup>
					<h:panelGroup rendered="#{insurance.coverageType ne 'FAMILY'}">
						<h:outputText value="#{insurance.coverageType}" />
					</h:panelGroup>
				</h:column>

				<h:column>
					<f:facet name="header">
						<h:panelGroup styleClass="h-panelgroup">
							<h:outputText value="Status" />
							<h:panelGroup layout="block" styleClass="sort-icons-container">
								<h:commandLink
									action="#{showincController.sortByAsc('coverageStatus')}"
									rendered="#{showincController.renderSortButton('coverageStatus', 'asc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/up-arrow.png"
										width="10" height="10" />
								</h:commandLink>
								<h:commandLink
									action="#{showincController.sortByDesc('coverageStatus')}"
									rendered="#{showincController.renderSortButton('coverageStatus', 'desc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/down-arrow.png"
										width="10" height="10" />
								</h:commandLink>
							</h:panelGroup>
						</h:panelGroup>
					</f:facet>
					<h:panelGroup rendered="#{insurance.coverageType eq 'FAMILY'}">
						<h:commandLink
							action="#{showincController.viewMembers(insurance)}"
							style="display:block; text-decoration:none; color:inherit;">
							<h:outputText value="#{insurance.coverageStatus}"
								styleClass="status-#{insurance.coverageStatus}" />
						</h:commandLink>
					</h:panelGroup>
					<h:panelGroup rendered="#{insurance.coverageType ne 'FAMILY'}">
						<h:outputText value="#{insurance.coverageStatus}"
							styleClass="status-#{insurance.coverageStatus}" />
					</h:panelGroup>
				</h:column>

				<h:column>
					<f:facet name="header">
						<h:panelGroup styleClass="h-panelgroup">
							<h:outputText value="Limit" />
							<h:panelGroup layout="block" styleClass="sort-icons-container">
								<h:commandLink
									action="#{showincController.sortByAsc('coverageLimit')}"
									rendered="#{showincController.renderSortButton('coverageLimit', 'asc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/up-arrow.png"
										width="10" height="10" />
								</h:commandLink>
								<h:commandLink
									action="#{showincController.sortByDesc('coverageLimit')}"
									rendered="#{showincController.renderSortButton('coverageLimit', 'desc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/down-arrow.png"
										width="10" height="10" />
								</h:commandLink>
							</h:panelGroup>
						</h:panelGroup>
					</f:facet>

					<h:panelGroup rendered="#{insurance.coverageType eq 'FAMILY'}">
						<h:commandLink
							action="#{showincController.viewMembers(insurance)}"
							style="display:block; text-decoration:none; color:inherit;">
							<h:outputText value="#{insurance.coverageLimit}" />
						</h:commandLink>
					</h:panelGroup>

					<h:panelGroup rendered="#{insurance.coverageType ne 'FAMILY'}">
						<h:outputText value="#{insurance.coverageLimit}" />
					</h:panelGroup>
				</h:column>

				<h:column>
					<f:facet name="header">
						<h:panelGroup styleClass="h-panelgroup">
							<h:outputText value="Remaining" />
							<h:panelGroup layout="block" styleClass="sort-icons-container">
								<h:commandLink
									action="#{showincController.sortByAsc('remaining')}"
									rendered="#{showincController.renderSortButton('remaining', 'asc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/up-arrow.png"
										width="10" height="10" />
								</h:commandLink>
								<h:commandLink
									action="#{showincController.sortByDesc('remaining')}"
									rendered="#{showincController.renderSortButton('remaining', 'desc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/down-arrow.png"
										width="10" height="10" />
								</h:commandLink>
							</h:panelGroup>
						</h:panelGroup>
					</f:facet>
					<h:panelGroup rendered="#{insurance.coverageType eq 'FAMILY'}">
						<h:commandLink
							action="#{showincController.viewMembers(insurance)}"
							style="display:block; text-decoration:none; color:inherit;">
							<h:outputText value="#{insurance.remaining}" />
						</h:commandLink>
					</h:panelGroup>
					<h:panelGroup rendered="#{insurance.coverageType ne 'FAMILY'}">
						<h:outputText value="#{insurance.remaining}" />
					</h:panelGroup>
				</h:column>

				<h:column>
					<f:facet name="header">
						<h:panelGroup styleClass="h-panelgroup">
							<h:outputText value="Claimed" />
							<h:panelGroup layout="block" styleClass="sort-icons-container">
								<h:commandLink
									action="#{showincController.sortByAsc('claimed')}"
									rendered="#{showincController.renderSortButton('claimed', 'asc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/up-arrow.png"
										width="10" height="10" />
								</h:commandLink>
								<h:commandLink
									action="#{showincController.sortByDesc('claimed')}"
									rendered="#{showincController.renderSortButton('claimed', 'desc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/down-arrow.png"
										width="10" height="10" />
								</h:commandLink>
							</h:panelGroup>
						</h:panelGroup>
					</f:facet>
					<h:panelGroup rendered="#{insurance.coverageType eq 'FAMILY'}">
						<h:commandLink
							action="#{showincController.viewMembers(insurance)}"
							style="display:block; text-decoration:none; color:inherit;">
							<h:outputText value="#{insurance.claimed}" />
						</h:commandLink>
					</h:panelGroup>
					<h:panelGroup rendered="#{insurance.coverageType ne 'FAMILY'}">
						<h:outputText value="#{insurance.claimed}" />
					</h:panelGroup>
				</h:column>

				<h:column>
					<f:facet name="header">
						<h:panelGroup styleClass="h-panelgroup">
							<h:outputText value="Last Claim" />
							<h:panelGroup layout="block" styleClass="sort-icons-container">
								<h:commandLink
									action="#{showincController.sortByAsc('lastClaimDate')}"
									rendered="#{showincController.renderSortButton('lastClaimDate', 'asc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/up-arrow.png"
										width="10" height="10" />
								</h:commandLink>
								<h:commandLink
									action="#{showincController.sortByDesc('lastClaimDate')}"
									rendered="#{showincController.renderSortButton('lastClaimDate', 'desc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/down-arrow.png"
										width="10" height="10" />
								</h:commandLink>
							</h:panelGroup>
						</h:panelGroup>
					</f:facet>

					<h:panelGroup rendered="#{insurance.coverageType eq 'FAMILY'}">
						<h:commandLink
							action="#{showincController.viewMembers(insurance)}"
							style="display:block; text-decoration:none; color:inherit;">
							<h:outputText value="#{insurance.lastClaimDate}">
								<f:convertDateTime pattern="MM-dd-yyyy" />
							</h:outputText>
						</h:commandLink>
					</h:panelGroup>

					<h:panelGroup rendered="#{insurance.coverageType ne 'FAMILY'}">
						<h:outputText value="#{insurance.lastClaimDate}">
							<f:convertDateTime pattern="MM-dd-yyyy" />
						</h:outputText>
					</h:panelGroup>
				</h:column>
			</h:dataTable>
		</h:panelGroup>
		<%-- Closing panelGroup for insuranceTablePanel --%>

		<h:panelGroup id="paginationPanel"
			rendered="#{not empty showincController.insuranceData}"
			layout="block" styleClass="pagination">

			<h:outputText value="#{showincController.paginationIncSummary}"
				styleClass="pagination-label"
				rendered="#{not empty showincController.insuranceData}" />

			<div>
				<%-- This div groups the pagination buttons and current page label --%>
				<h:commandButton value="« Previous"
					action="#{showincController.previousPage}"
					disabled="#{not showincController.hasPreviousPage}"
					styleClass="btn" />

				<h:outputText styleClass="pagination-label"
					value="Page #{showincController.currentPage} of #{showincController.totalPages}" />

				<h:commandButton value="Next »"
					action="#{showincController.nextPage}"
					disabled="#{not showincController.hasNextPage}" styleClass="btn" />
			</div>
		</h:panelGroup>

		<h:panelGroup rendered="#{empty showincController.insuranceData}">
			<h:outputText value="Please subscribe to a plan now."
				styleClass="not-found" />
		</h:panelGroup>


	</h:form>
</body>
	</html>
</f:view>