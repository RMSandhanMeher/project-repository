<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html"%>

<f:view>
	<html>
<head>
<title>Find Your Doctor</title>
<link
	href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap"
	rel="stylesheet">

<%-- Link to external CSS file --%>
<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/resources/css/searchDoctor.css" />

<%-- Link to external Java Script file --%>
<script type="text/javascript"
	src="${pageContext.request.contextPath}/resources/js/searchdocFilter.js"></script>

</head>
<body>

	<jsp:include page="/navbar/NavRecipient.jsp" />

	<jsp:include page="NavBar.jsp" />

	<h:form id="searchForm" styleClass="main-content-panel" style="margin-top:125px;padding-bottom:10px">
		<h2 style=" font-size: 32px;font-weight: 600;letter-spacing: -0.5px;  text-align: center;color: #0056b3;">Search for Providers🩺</h2>
		<%-- Global messages display --%>
		<h:messages globalOnly="true" style="color:red" />


		<div class="search-form-container">

			<div class="form-row">
				<h:outputLabel for="searchBy" value="Search By:" />
				<div class="input-wrapper">
					<h:selectOneMenu id="searchBy"
						value="#{doctorSearchController.searchBy}"
						onchange="this.form.submit()">
						<f:selectItems value="#{doctorSearchController.searchOptions}" />
					</h:selectOneMenu>
				</div>
			</div>


			<div class="form-row">
				<h:outputLabel escape="false"
					value="<span style='color:red; font-size:20px;'>*</span>Search Criteria:" />
				<div class="input-wrapper">
					<%-- Input Text for Doctor Name / Address --%>
					<h:panelGroup id="searchValueInputDiv" layout="block"
						styleClass="#{(doctorSearchController.searchBy eq 'specialization') ? 'hidden' : ''}">
						<h:inputText id="searchValueInput"
							value="#{doctorSearchController.searchValue}" />
						<br>
						<h:message for="searchValueInput" style="color:red" />
					</h:panelGroup>

					<%-- Dropdown for Specialization --%>
					<h:panelGroup id="specializationDropdownDiv" layout="block"
						styleClass="#{(doctorSearchController.searchBy eq 'specialization') ? '' : 'hidden'}">
						<h:selectOneMenu id="specializationDropdown"
							value="#{doctorSearchController.selectedSpecialization}">
							<f:selectItems
								value="#{doctorSearchController.specializationOptions}" />
						</h:selectOneMenu>
						<br>
						<h:message for="specializationDropdown" style="color:red" />
					</h:panelGroup>
				</div>
			</div>

			<%-- Row for Search Mode Radios --%>
			<div class="form-row">
				<h:outputLabel id="searchModeLabel" for="searchMode"
					value="Search Mode:"
					styleClass="#{(doctorSearchController.searchBy eq 'doctorName' or doctorSearchController.searchBy eq 'address') ? '' : 'hidden'}" />

				<h:panelGroup id="searchModeRadiosDiv" layout="block"
					styleClass="#{(doctorSearchController.searchBy eq 'doctorName' or doctorSearchController.searchBy eq 'address') ? 'search-mode-radios-container' : 'hidden search-mode-radios-container'}">
					<h:selectOneRadio id="searchMode"
						value="#{doctorSearchController.searchMode}"
						styleClass="search-mode-radios">
						<f:selectItem itemValue="exact" itemLabel="Exact Match" />
						<br>
						<f:selectItem itemValue="startsWith" itemLabel="StartsWith" />
						<f:selectItem itemValue="contains" itemLabel="Contains" />
						<%-- Important: If doctorSearchController.searchMode is null or not matching any itemValue, none will be selected --%>
					</h:selectOneRadio>
				</h:panelGroup>
			</div>
		</div>


		<div class="search-buttons">
			<h:commandButton value="Search Doctor"
				action="#{doctorSearchController.executeSearch}"
				styleClass="btn btn-primary"
				onclick="setTimeout(function() { window.location.hash='results'; }, 10);" />


			<h:commandButton value="Clear Search"
				action="#{doctorSearchController.resetSearch}"
				styleClass="btn btn-secondary" />
		</div>


		<%-- Conditional rendering for the table and its scrollable wrapper --%>
		<h:panelGroup id="scrollableTableWrapper"
			rendered="#{doctorSearchController.searchPerformed and not empty doctorSearchController.paginatedDoctors}"
			layout="block">
			<%-- NEW: Wrapper div for scrolling --%>

			<%-- Script to scroll only when the table and its wrapper are rendered --%>
			<a id="results"></a>
			<!-- ✅ Added anchor -->

			<script type="text/javascript">
				setTimeout(scrollToTable, 200);
			</script>

			<h:dataTable id="searchResultsTable"
				value="#{doctorSearchController.paginatedDoctors}" var="doc"
				styleClass="data-table">
				<%-- ID for the actual table --%>

				<h:column>
					<f:facet name="header">
						<h:panelGroup styleClass="h-panelgroup">
							<h:outputText value="Doctor Name" />
							<h:panelGroup layout="block" styleClass="sort-icons-container">
								<h:commandLink
									action="#{doctorSearchController.sortByAsc('doctorName')}"
									rendered="#{doctorSearchController.renderSortButton('doctorName', 'asc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/up-arrow.png"
										width="9" height="9" title="Sort Ascending" />
								</h:commandLink>

								<h:commandLink
									action="#{doctorSearchController.sortByDesc('doctorName')}"
									rendered="#{doctorSearchController.renderSortButton('doctorName', 'desc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/down-arrow.png"
										width="10" height="10" title="Sort Descending" />
								</h:commandLink>
							</h:panelGroup>
						</h:panelGroup>
					</f:facet>
					<h:outputText value="#{doc.doctorName}" />
				</h:column>


				<h:column>
					<f:facet name="header">
						<h:panelGroup styleClass="h-panelgroup">
							<h:outputText value="Specialization" />
							<h:panelGroup layout="block" styleClass="sort-icons-container">
								<h:commandLink
									action="#{doctorSearchController.sortByAsc('specialization')}"
									rendered="#{doctorSearchController.renderSortButton('specialization', 'asc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/up-arrow.png"
										width="9" height="9" title="Sort Ascending" />
								</h:commandLink>

								<h:commandLink
									action="#{doctorSearchController.sortByDesc('specialization')}"
									rendered="#{doctorSearchController.renderSortButton('specialization', 'desc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/down-arrow.png"
										width="10" height="10" title="Sort Descending" />
								</h:commandLink>
							</h:panelGroup>
						</h:panelGroup>
					</f:facet>
					<h:outputText value="#{doc.specialization}" />
				</h:column>


				<h:column>
					<f:facet name="header">
						<h:panelGroup styleClass="h-panelgroup">
							<h:outputText value="Status" />
							<h:panelGroup layout="block" styleClass="sort-icons-container">
								<h:commandLink
									action="#{doctorSearchController.sortByAsc('status')}"
									rendered="#{doctorSearchController.renderSortButton('status', 'asc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/up-arrow.png"
										width="9" height="9" title="Sort Ascending" />
								</h:commandLink>
								<h:commandLink
									action="#{doctorSearchController.sortByDesc('status')}"
									rendered="#{doctorSearchController.renderSortButton('status', 'desc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/down-arrow.png"
										width="10" height="10" title="Sort Descending" />
								</h:commandLink>
							</h:panelGroup>
						</h:panelGroup>
					</f:facet>
					<%-- THIS IS THE CRITICAL FIX --%>
					<h:outputText value="#{doc.status}"
						styleClass="#{doc.statusStyleClass}" />
				</h:column>

				<h:column>
					<f:facet name="header">
						<h:panelGroup styleClass="h-panelgroup">
							<h:outputText value="Address" />
							<h:panelGroup layout="block" styleClass="sort-icons-container">
								<h:commandLink
									action="#{doctorSearchController.sortByAsc('address')}"
									rendered="#{doctorSearchController.renderSortButton('address', 'asc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/up-arrow.png"
										width="9" height="9" title="Sort Ascending" />
								</h:commandLink>
								<h:commandLink
									action="#{doctorSearchController.sortByDesc('address')}"
									rendered="#{doctorSearchController.renderSortButton('address', 'desc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/down-arrow.png"
										width="10" height="10" title="Sort Descending" />
								</h:commandLink>
							</h:panelGroup>
						</h:panelGroup>
					</f:facet>
					<h:outputText value="#{doc.address}" />
				</h:column>

				<h:column>
					<f:facet name="header">
						<h:panelGroup styleClass="h-panelgroup">
							<h:outputText value="Type" />
							<h:panelGroup layout="block" styleClass="sort-icons-container">
								<h:commandLink
									action="#{doctorSearchController.sortByAsc('type')}"
									rendered="#{doctorSearchController.renderSortButton('type', 'asc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/up-arrow.png"
										width="9" height="9" title="Sort Ascending" />
								</h:commandLink>
								<h:commandLink
									action="#{doctorSearchController.sortByDesc('type')}"
									rendered="#{doctorSearchController.renderSortButton('type', 'desc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/down-arrow.png"
										width="10" height="10" title="Sort Descending" />
								</h:commandLink>
							</h:panelGroup>
						</h:panelGroup>
					</f:facet>
					<h:outputText value="#{doc.type}" />
				</h:column>

				<h:column>
					<f:facet name="header">
						<h:panelGroup styleClass="h-panelgroup">
							<h:outputText value="Email" />
							<h:panelGroup layout="block" styleClass="sort-icons-container">
								<h:commandLink
									action="#{doctorSearchController.sortByAsc('email')}"
									rendered="#{doctorSearchController.renderSortButton('email', 'asc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/up-arrow.png"
										width="9" height="9" title="Sort Ascending" />
								</h:commandLink>

								<h:commandLink
									action="#{doctorSearchController.sortByDesc('email')}"
									rendered="#{doctorSearchController.renderSortButton('email', 'desc')}"
									styleClass="sort-icons">
									<h:graphicImage value="/resources/media/images/down-arrow.png"
										width="10" height="10" title="Sort Descending" />
								</h:commandLink>
							</h:panelGroup>

						</h:panelGroup>
					</f:facet>
					<h:outputText value="#{doc.email}" />
				</h:column>

				<h:column>
					<f:facet name="header">
						<h:outputText value="Action" />
					</f:facet>
					<h:panelGroup rendered="#{doc.status eq 'ACTIVE'}">
						<h:commandButton value="Book Now"
							action="#{doctorAvailabilityController.chooseDoctor(doc.doctorId)}"
							styleClass="btn btn-success" />
					</h:panelGroup>
					<h:outputText rendered="#{doc.status ne 'ACTIVE'}"
						value="Unavailable Now" styleClass="unavailable-now" />
				</h:column>
			</h:dataTable>
		</h:panelGroup>

		<h:panelGroup id="doctorPaginationPanel"
			rendered="#{doctorSearchController.searchPerformed and not empty doctorSearchController.paginatedDoctors}"
			layout="block" styleClass="pagination">

			<h:outputText value="#{doctorSearchController.paginationDocSummary}"
				styleClass="pagination-label" />

			<div>
				<h:commandButton value="« Previous"
					action="#{doctorSearchController.prevPage}"
					disabled="#{not doctorSearchController.hasPrevPage}"
					styleClass="btn"
					onclick="setTimeout(function() { window.location.hash='results'; }, 10);" />


				<h:outputText styleClass="pagination-info"
					value="Page #{doctorSearchController.currentPage} of #{doctorSearchController.totalPages}" />

				<h:commandButton value="Next »"
					action="#{doctorSearchController.nextPage}"
					disabled="#{not doctorSearchController.hasNextPage}"
					styleClass="btn"
					onclick="setTimeout(function() { window.location.hash='results'; }, 10);" />

			</div>
		</h:panelGroup>


		<%-- Messages for no search or no results --%>
		<h:panelGroup rendered="#{not doctorSearchController.searchPerformed}">
			<div class="not-found">Search for doctors by name,
				specialization or address to find available appointments.</div>
		</h:panelGroup>

		<h:panelGroup
			rendered="#{doctorSearchController.searchPerformed and empty doctorSearchController.paginatedDoctors}">
			<div class="not-found">Kindly search properly to get desired
				result.</div>
		</h:panelGroup>

	</h:form>
</body>
<jsp:include page="/footer/Footer.jsp" />
	</html>
</f:view>