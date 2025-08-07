<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html"%>

<f:view>
	<html>
<head>
<title>Subscribed Family Members</title>
<style>
/* Universal Box-Sizing for consistent layout */
html {
	box-sizing: border-box;
}

*, *:before, *:after {
	box-sizing: inherit;
}

body {
	font-family: 'Segoe UI', sans-serif;
	background-color: #f8fcff; /* Light blue background */
	margin: 0;
	padding: 0;
	display: flex;
	flex-direction: column;
	/* Stack navbar, heading, and main content vertically */
	align-items: center; /* Center content horizontally on the page */
	min-height: 100vh; /* Ensure body takes full viewport height */
	color: #333; /* Default text color */
}

/* Main Heading (H2) Styling */
h2 {
	text-align: center;
	color: #0077b6; /* Professional blue */
	margin-top: 130px;
	/* Space from top (adjust if navbar pushes it down too much) */
	margin-bottom: 20px;
	/* Space between heading and the main content panel */
	font-size: 28px;
	font-weight: 600; /* Slightly bolder for headings */
	text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.1);
	width: 100%; /* Ensure it spans full width for centering */
}

/* The Main Content Panel (the shadowed box for table and pagination) */
.main-content-panel {
	width: 85%; /* Adjusted width for better aesthetics for table */
	max-width: 1100px; /* Maximum width for larger screens */
	margin-bottom: 40px; /* More space at the bottom */
	background-color: #ffffff; /* White background for the panel */
	border-radius: 12px; /* Rounded corners */
	box-shadow: 0px 8px 24px rgba(0, 0, 0, 0.15); /* Prominent shadow */
	padding: 35px 40px; /* Generous internal padding */
	display: flex;
	flex-direction: column; /* Stack table and pagination vertically */
	align-items: center;
	/* Center these items horizontally within the panel */
}

/* Data Table Styling */
.data-table {
	width: 100%;
	/* Table fills the width of its parent (.main-content-panel) */
	margin-top: 0; /* No top margin here as it's within the main panel */
	border-collapse: collapse; /* Remove double borders */
	background-color: #ffffff;
	border-radius: 10px;
	overflow: hidden; /* Ensures rounded corners apply to content */
	box-shadow: 0px 4px 16px rgba(0, 0, 0, 0.08);
	/* Subtle shadow for the table */
}

.data-table th, .data-table td {
	border: 1px solid #cce0eb; /* Lighter border for cells */
	padding: 12px 10px; /* More vertical padding for cells */
	text-align: left; /* Keep text aligned left */
	font-size: 15px; /* Slightly larger font size for table content */
	color: #444; /* Darker text for readability */
}

.data-table th {
	background-color: #e0f7fa; /* Very light blue for headers */
	text-align: center; /* Center header text */
	font-weight: 700; /* Bold headers */
	font-size: 16px; /* Larger font for headers */
	color: #0077b6; /* Blue text for headers */
}

/* Striped rows */
.data-table tr:nth-child(even) {
	background-color: #f5fafd; /* Very light blue for even rows */
}

/* Hover effect on rows */
.data-table tr:hover {
	background-color: #e8f7fd; /* Lighter blue on row hover */
}

/* Table Header Sort Icons (h:panelGroup for Flexbox) */
.data-table th .h-panelgroup {
	/* Target panelGroup inside table header */
	display: flex;
	align-items: center; /* Vertically aligns text and arrow container */
	justify-content: center;
	/* Horizontally centers content within the header cell */
	gap: 6px; /* Space between text and arrows */
	width: 100%; /* Ensure it takes full width of the th */
}

.sort-icons-container {
	display: flex;
	flex-direction: column; /* Stack arrows vertically */
}

.sort-icons { /* Applies to commandLink around graphicImage */
	display: block; /* Make links block to stack arrows */
	line-height: 1; /* Remove extra line height for tight stacking */
	padding: 2px 0; /* Small padding for click area */
}

.sort-icons img {
	vertical-align: middle; /* Align image nicely */
	/* Add the same filter and opacity for consistency with FindDoctor.jsp */
	filter: invert(50%) sepia(0%) saturate(10%) hue-rotate(0deg)
		brightness(50%) contrast(100%);
	opacity: 0.7;
	transition: opacity 0.2s ease;
}

.sort-icons:hover img {
	opacity: 1; /* Full opacity on hover */
	filter: invert(30%) sepia(0%) saturate(10%) hue-rotate(0deg)
		brightness(30%) contrast(100%); /* Even darker on hover */
}


/* Pagination Styling */
.pagination {
	display: flex; /* Use flexbox for alignment */
	align-items: center;
	/* Changed to space-between to push summary to left and buttons to right */
	justify-content: space-between; 
	gap: 15px; /* Space between elements */
	margin: 25px 0 0 0; /* Margin */
	width: 100%; /* Ensure it spans full width */
}

/* Styles for h:commandButton within .pagination */
.pagination .btn {
	padding: 8px 15px; /* Smaller padding for pagination buttons */
	font-size: 13px; /* Smaller font size */
	border-radius: 6px; /* Slightly less rounded */
	box-shadow: none; /* No shadow */
	background-color: #f0f4f8; /* Light background */
	color: #4a90e2; /* Blue text */
	border: none;
	cursor: pointer;
	font-weight: 500;
	text-decoration: none;
	display: inline-block;
	transition: background-color 0.3s ease, transform 0.2s ease;
	letter-spacing: 0.5px;
	outline: none;
}

.backbtn {
	padding: 10px 15px; /* Smaller padding for back buttons */
	font-size: 13px; /* Smaller font size */
	border-radius: 6px; /* Slightly less rounded */
	box-shadow: none; /* No shadow */
	background-color: #f0f4f8; /* Light background */
	color: #4a90e2; /* Blue text */
	border: none;
	cursor: pointer;
	font-weight: 500;
	text-decoration: none;
	display: inline-block;
	transition: background-color 0.3s ease, transform 0.2s ease;
	letter-spacing: 0.5px;
	outline: none;
	padding-left: 12px;
	margin-left: 100px;
}


.pagination .btn:hover {
	background-color: #e0e7ed; /* Darker background on hover */
	transform: none; /* No transform on hover for pagination buttons */
	box-shadow: none; /* No shadow on hover */
}

.pagination .btn:active {
	background-color: #d0d6dd; /* Even darker on active */
}

/* Disabled state for pagination buttons */
.pagination .btn:disabled {
	opacity: 0.6;
	cursor: not-allowed;
	background-color: #f8f9fa;
	color: #99aab5;
}


.pagination-label {
	font-weight: 400; /* Regular weight */
	color: #777; /* Grayish color */
	font-size: 14px; /* Smaller font size */
	/* Remove specific width, text-align, margin-top if placed directly in flex container */
}

/* General button styles for other buttons if needed, kept for reference */
.btn {
	padding: 10px 20px;
	border: none;
	border-radius: 8px;
	cursor: pointer;
	font-size: 15px;
	font-weight: 500;
	text-decoration: none;
	display: inline-block;
	transition: background-color 0.3s ease, transform 0.2s ease;
	letter-spacing: 0.5px;
	outline: none;
	box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.btn-primary {
	background-color: #007bff;
	color: white;
}
</style>
</head>
<body>

	<jsp:include page="/navbar/NavRecipient.jsp" />


	<h2>Subscribed Family Members</h2>

	<h:form styleClass="main-content-panel">
		<h:dataTable value="#{showincController.paginatedMemberList}"
			var="member" styleClass="data-table"
			rendered="#{not empty showincController.paginatedMemberList}">

			<h:column>
				<f:facet name="header">
					<h:panelGroup styleClass="h-panelgroup">
						<h:outputText value="Member Id" />
						<h:panelGroup layout="block" styleClass="sort-icons-container">
							<h:commandLink
								action="#{showincController.sortByAscMem('memberId')}"
								rendered="#{showincController.renderSortButtonMem('memberId', 'asc')}"
								styleClass="sort-icons">
								<h:graphicImage value="/resources/media/images/up-arrow.png"
									width="10" height="10" title="sort-ascending"/>
							</h:commandLink>

							<h:commandLink
								action="#{showincController.sortByDescMem('memberId')}"
								rendered="#{showincController.renderSortButtonMem('memberId', 'desc')}"
								styleClass="sort-icons">
								<h:graphicImage value="/resources/media/images/down-arrow.png"
									width="10" height="10" title="sort-descending"/>
							</h:commandLink>
						</h:panelGroup>
					</h:panelGroup>
				</f:facet>
				<h:outputText value="#{member.memberId }" />
			</h:column>


			<h:column>
				<f:facet name="header">
					<h:panelGroup styleClass="h-panelgroup">
						<h:outputText value="Full Name" />
						<h:panelGroup layout="block" styleClass="sort-icons-container">
							<h:commandLink
								action="#{showincController.sortByAscMem('fullName')}"
								rendered="#{showincController.renderSortButtonMem('fullName', 'asc')}"
								styleClass="sort-icons">
								<h:graphicImage value="/resources/media/images/up-arrow.png"
									width="10" height="10" title="sort-ascending"/>
							</h:commandLink>

							<h:commandLink
								action="#{showincController.sortByDescMem('fullName')}"
								rendered="#{showincController.renderSortButtonMem('fullName', 'desc')}"
								styleClass="sort-icons">
								<h:graphicImage value="/resources/media/images/down-arrow.png"
									width="10" height="10" title="sort-descending"/>
							</h:commandLink>
						</h:panelGroup>
					</h:panelGroup>
				</f:facet>
				<h:outputText value="#{member.fullName}" />
			</h:column>


			<h:column>
				<f:facet name="header">
					<h:panelGroup styleClass="h-panelgroup">
						<h:outputText value="Age" />
						<h:panelGroup layout="block" styleClass="sort-icons-container">
							<h:commandLink action="#{showincController.sortByAscMem('age')}"
								rendered="#{showincController.renderSortButtonMem('age', 'asc')}"
								styleClass="sort-icons">
								<h:graphicImage value="/resources/media/images/up-arrow.png"
									width="10" height="10" title="sort-ascending"/>
							</h:commandLink>

							<h:commandLink action="#{showincController.sortByDescMem('age')}"
								rendered="#{showincController.renderSortButtonMem('age', 'desc')}"
								styleClass="sort-icons">
								<h:graphicImage value="/resources/media/images/down-arrow.png"
									width="10" height="10" title="sort-descending"/>
							</h:commandLink>
						</h:panelGroup>
					</h:panelGroup>
				</f:facet>
				<h:outputText value="#{member.age}" />
			</h:column>


			<h:column>
				<f:facet name="header">
					<h:panelGroup styleClass="h-panelgroup">
						<h:outputText value="Gender" />
						<h:panelGroup layout="block" styleClass="sort-icons-container">
							<h:commandLink
								action="#{showincController.sortByAscMem('gender')}"
								rendered="#{showincController.renderSortButtonMem('gender', 'asc')}"
								styleClass="sort-icons">
								<h:graphicImage value="/resources/media/images/up-arrow.png"
									width="10" height="10" title="sort-ascending"/>
							</h:commandLink>

							<h:commandLink
								action="#{showincController.sortByDescMem('gender')}"
								rendered="#{showincController.renderSortButtonMem('gender', 'desc')}"
								styleClass="sort-icons">
								<h:graphicImage value="/resources/media/images/down-arrow.png"
									width="10" height="10" title="sort-descending"/>
							</h:commandLink>
						</h:panelGroup>
					</h:panelGroup>
				</f:facet>
				<h:outputText value="#{member.gender}" />
			</h:column>


			<h:column>
				<f:facet name="header">
					<h:panelGroup styleClass="h-panelgroup">
						<h:outputText value="Relation" />
						<h:panelGroup layout="block" styleClass="sort-icons-container">
							<h:commandLink
								action="#{showincController.sortByAscMem('relationWithProposer')}"
								rendered="#{showincController.renderSortButtonMem('relationWithProposer', 'asc')}"
								styleClass="sort-icons">
								<h:graphicImage value="/resources/media/images/up-arrow.png"
									width="10" height="10" title="sort-ascending"/>
							</h:commandLink>

							<h:commandLink
								action="#{showincController.sortByDescMem('relationWithProposer')}"
								rendered="#{showincController.renderSortButtonMem('relationWithProposer', 'desc')}"
								styleClass="sort-icons">
								<h:graphicImage value="/resources/media/images/down-arrow.png"
									width="10" height="10" title="sort-descending"/>
							</h:commandLink>
						</h:panelGroup>
					</h:panelGroup>
				</f:facet>
				<h:outputText value="#{member.relationWithProposer}" />
			</h:column>


			<h:column>
				<f:facet name="header">
					<h:panelGroup styleClass="h-panelgroup">
						<h:outputText value="Aadhar No" />
						<h:panelGroup layout="block" styleClass="sort-icons-container">
							<h:commandLink
								action="#{showincController.sortByAscMem('aadharNo')}"
								rendered="#{showincController.renderSortButtonMem('aadharNo', 'asc')}"
								styleClass="sort-icons">
								<h:graphicImage value="/resources/media/images/up-arrow.png"
									width="10" height="10" title="sort-ascending"/>
							</h:commandLink>

							<h:commandLink
								action="#{showincController.sortByDescMem('aadharNo')}"
								rendered="#{showincController.renderSortButtonMem('aadharNo', 'desc')}"
								styleClass="sort-icons">
								<h:graphicImage value="/resources/media/images/down-arrow.png"
									width="10" height="10" title="sort-descending"/>
							</h:commandLink>
						</h:panelGroup>
					</h:panelGroup>
				</f:facet>
				<h:outputText value="#{member.aadharNo}" />
			</h:column>

		</h:dataTable>

		<h:panelGroup id="paginationPanel"
			rendered="#{not empty showincController.paginatedMemberList}"
			layout="block" styleClass="pagination">

			<h:outputText value="#{showincController.paginationMemSummary}"
				styleClass="pagination-label"
				rendered="#{not empty showincController.paginatedMemberList}" />


			<h:commandButton value="«  Back"
				action="#{showincController.goBackinc}"
				styleClass="backbtn" />


			<div> <%-- This div groups the pagination buttons together --%>
				<h:commandButton value="« Previous"
					action="#{showincController.previousMemberPage}"
					disabled="#{not showincController.hasPreviousMemberPage}"
					styleClass="btn" />

				<h:outputText styleClass="pagination-label"
					value="Page #{showincController.currentMemberPage} of #{showincController.totalMemberPages}" />

				<h:commandButton value="Next »"
					action="#{showincController.nextMemberPage}"
					disabled="#{not showincController.hasNextMemberPage}"
					styleClass="btn" />
			</div>
		</h:panelGroup>
	</h:form>

</body>
	</html>
</f:view>
