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
<style>
/* Basic Reset & Body Styling */
body {
	font-family: 'Poppins', sans-serif; /* Modern font */
	background-color: #eef2f7; /* Light, calming background */
	margin: 0;
	padding: 0;
	display: flex;
	flex-direction: column;
	align-items: center;
	min-height: 100vh;
	color: #333;
}

/* Page Title */
h2 {
	text-align: center;
	color: #0056b3; /* Deeper blue for headings */
	margin-top: 100px; /* Adjust for navbar */
	margin-bottom: 25px; /* More space below title */
	font-size: 32px; /* Larger title */
	font-weight: 600; /* Slightly bolder */
	letter-spacing: -0.5px;
}

/* Main Content Panel */
.main-content-panel {
	width: 85%; /* Cover more page width */
	max-width: 1800px; /* Increased max-width */
	margin-bottom: 30px;
	background-color: #ffffff;
	border-radius: 16px; /* More rounded corners */
	box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
	/* Softer, larger shadow */
	padding: 40px; /* Increased padding */
	box-sizing: border-box;
	display: flex;
	flex-direction: column;
	align-items: center;
}

/* Form Elements (Labels, Inputs, Selects) */
.search-inputs-grid {
	width: 40%; /* Wider input section for better readability */
	margin: 15px auto 25px auto; /* Centered with more vertical margin */
	border-collapse: separate;
	padding-left: 5px;
	margin-left: 280px;
	/* Allows border-radius on cells if needed */
	border-spacing: 0 15px; /* Space between rows */
}

.search-inputs-grid tr td:first-child {
	width: 40%; /* Adjust label width */
	padding-right: 10px;
	vertical-align: middle;
	text-align: right;
	font-weight: 500; /* Medium weight for labels */
	color: #555;
	font-size: 15px;
}

.search-inputs-grid tr td:last-child {
	width: 70%; /* Adjust input width */
	vertical-align: middle;
}

/* Targeting JSF rendered select and input for better styling */
.search-inputs-grid select, .search-inputs-grid input[type="text"] {
	width: 100%;
	padding: 12px 18px; /* Generous padding */
	font-size: 16px; /* Readable font size */
	border: 1px solid #dcdfe6; /* Light, subtle border */
	border-radius: 8px; /* Nicely rounded */
	transition: border-color 0.3s ease, box-shadow 0.3s ease;
	outline: none;
	background-color: #f9fbff; /* Slightly off-white background */
	-webkit-appearance: none;
	/* Remove default browser styling for select */
	-moz-appearance: none;
	appearance: none;
}

.search-inputs-grid select:focus, .search-inputs-grid input[type="text"]:focus
	{
	border-color: #4a90e2; /* Clear blue on focus */
	box-shadow: 0 0 0 4px rgba(74, 144, 226, 0.2); /* Soft glow */
	background-color: #ffffff;
}

/* Custom arrow for select elements */
.search-inputs-grid .select-wrapper {
	position: relative;
}

.search-inputs-grid .select-wrapper::after {
	content: '▾'; /* Modern down arrow */
	position: absolute;
	right: 15px;
	top: 50%;
	transform: translateY(-50%);
	font-size: 16px; /* Larger arrow */
	color: #888;
	pointer-events: none;
}

/* Button Styles */
.btn {
	padding: 12px 25px; /* Larger buttons */
	border: none; /* No border for modern look */
	border-radius: 8px; /* Match input styling */
	cursor: pointer;
	font-size: 16px;
	font-weight: 500;
	text-decoration: none;
	display: inline-block;
	transition: background-color 0.3s ease, transform 0.2s ease;
	/* Add transform for subtle press effect */
	letter-spacing: 0.5px;
	outline: none;
	box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1); /* Subtle button shadow */
}

.btn:hover {
	transform: translateY(-2px); /* Slight lift on hover */
	box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.btn:active {
	transform: translateY(0); /* Press effect */
	box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.btn-primary {
	background-color: #007bff; /* Vibrant blue */
	color: white;
}

.btn-primary:hover {
	background-color: #0069d9; /* Darker blue on hover */
}

.btn-secondary {
	background-color: #6c757d; /* Muted gray for reset */
	color: white;
}

.btn-secondary:hover {
	background-color: #5a6268;
}

.btn-success {
	background-color: #28a745; /* Standard green for success */
	color: white;
}

.btn-success:hover {
	background-color: #218838;
}

.search-buttons {
	text-align: center;
	margin-top: 15px; /* Adjust spacing */
	margin-bottom: 40px; /* More space before table */
	width: 100%;
	display: flex;
	justify-content: center;
	gap: 25px; /* Increased gap between buttons */
}

/* Data Table Styling */
.data-table {
	width: 100%;
	margin-top: 20px; /* Space from search buttons */
	border-collapse: collapse;
	background-color: #ffffff;
	border-radius: 12px; /* Soft corners for table */
	overflow: hidden; /* Ensures rounded corners are visible */
	box-shadow: 0 6px 20px rgba(0, 0, 0, 0.07); /* Light table shadow */
}

.data-table th, .data-table td {
	border: 1px solid #e9ecef; /* Lighter borders */
	padding: 15px 20px; /* More padding in cells */
	text-align: left;
	font-size: 15px;
}

.data-table th {
	background-color: #f8f9fa; /* Very light header background */
	text-align: center;
	font-weight: 600; /* Semi-bold headers */
	color: #495057; /* Darker text for headers */
	font-size: 16px;
	position: sticky; /* Sticky headers for scrollable tables */
	top: 0;
	z-index: 10;
}

.data-table th .h-panelgroup {
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 8px; /* More space for sort icons */
}

.data-table tr:nth-child(even) {
	background-color: #fcfdff; /* Even rows slightly different */
}

.data-table tr:hover {
	background-color: #eef7ff; /* Gentle hover effect */
}

/* Doctor Status Styling */
.data-table td:nth-child(3) { /* Target the Status column */
	font-weight: 500;
	text-align: center; /* Center the status text */
}

.data-table td:nth-child(3) h\:outputText[value="ACTIVE"] {
	color: #28a745; /* Green for active */
	font-weight: 600;
}

.data-table td:nth-child(3) h\:outputText[value="INACTIVE"], .data-table td:nth-child(3) .unavailable-now
	{
	color: #6c757d; /* Muted grey for unavailable/inactive */
	font-weight: 500;
	font-style: italic; /* Italicize 'Unavailable Now' */
	font-size: 14px; /* Slightly smaller for 'Unavailable Now' */
}

.unavailable-now { /* Specific class for "Unavailable Now" */
	color: #6c757d; /* Muted grey */
	font-weight: 500;
	font-style: italic;
	font-size: 14px;
}

/* Pagination Styles */
.pagination {
	display: flex;
	align-items: center;
	/* Changed to space-between to push the first child (summary) to the left and the second (buttons group) to the right */
	justify-content: space-between; 
	gap: 15px; /* Increased gap */
	margin: 25px 0 0 0; /* More top margin */
	width: 100%;
}

.pagination .btn {
	padding: 8px 15px; /* Smaller pagination buttons */
	font-size: 13px;
	border-radius: 6px;
	box-shadow: none; /* No shadow for pagination buttons */
	background-color: #f0f4f8; /* Light background */
	color: #4a90e2; /* Blue text */
}

.pagination .btn:hover {
	background-color: #e0e7ed;
	transform: none; /* No lift */
	box-shadow: none;
}

.pagination .btn:active {
	background-color: #d0d6dd;
}

.pagination .btn:disabled { /* Used :disabled for JSF disabled attribute */
	opacity: 0.6;
	cursor: not-allowed;
	background-color: #f8f9fa;
	color: #99aab5;
}

.pagination-label { /* New class for the summary text */
	font-weight: 400; /* Normal weight */
	color: #777;
	font-size: 14px;
}

.pagination-info { /* Used for "Page X of Y" text */
	font-weight: 400; /* Normal weight */
	color: #777;
	font-size: 14px;
	margin: 0 10px; /* Adjust spacing around this label if needed */
}

/* Status Specific Styling - UPDATED TO MATCH UPPERCASE STATUS */
.status-ACTIVE { /* Specific class for active status */
	font-weight: 600;
	color: #28a745; /* Green for active */
}

.status-EXPIRED { /* Specific class for expired status */
	font-weight: 500;
	color: #dc3545; /* Red for expired */
}

/* Sort Icons */
.sort-icons-container {
	display: flex;
	flex-direction: column;
	margin-left: 5px; /* Space from text */
}

.sort-icons {
	display: block;
	line-height: 1; /* Tighter spacing */
}

.sort-icons+.sort-icons {
	margin-top: 4px; /* More space between arrows */
}

.sort-icons img {
	filter: invert(50%) sepia(0%) saturate(10%) hue-rotate(0deg)
		brightness(50%) contrast(100%);
	/* Darken image for better contrast on light background */
	opacity: 0.7;
	transition: opacity 0.2s ease;
}

.sort-icons:hover img {
	opacity: 1; /* Full opacity on hover */
	filter: invert(30%) sepia(0%) saturate(10%) hue-rotate(0deg)
		brightness(30%) contrast(100%); /* Even darker on hover */
}

/* Global Messages */
.global-messages {
	list-style: none;
	padding: 0;
	margin: 0 auto 30px auto; /* More margin */
	width: 90%;
	text-align: center;
}

.global-messages li {
	padding: 15px 20px; /* Larger messages */
	margin-bottom: 12px;
	border-radius: 10px; /* Softer corners */
	font-size: 15px;
	font-weight: 500;
	color: #333;
	border: 1px solid; /* Add border for definition */
}

.global-messages .ui-messages-info {
	background-color: #d4edda;
	color: #155724;
	border-color: #c3e6cb;
}

.global-messages .ui-messages-warn {
	background-color: #fff3cd;
	color: #856404;
	border-color: #ffeeba;
}

.global-messages .ui-messages-error, .global-messages .ui-messages-fatal
	{
	background-color: #f8d7da;
	color: #721c24;
	border-color: #f5c6cb;
}

/* No results / Initial message */
.not-found {
	text-align: center;
	font-weight: 500;
	color: green; /* Muted color */
	margin-top: 30px;
	font-size: 1em;
	width: 80%;
	padding: 20px;
	background-color: #f0f4f8; /* Light background */
	border-radius: 10px;
	border: 1px solid #dcdfe6;
}

/* Utility Class */
.hidden {
	display: none;
}


/* Status Specific Styling - UPDATED TO MATCH UPPERCASE STATUS */
.status-ACTIVE { /* Specific class for active status */
	font-weight: 600;
	color: #28a745; /* Green for active */
}

.status-INACTIVE { /* Specific class for expired status */
	font-weight: 500;
	color: #dc3545; /* Red for expired */
}


</style>
<script type="text/javascript">
        function toggleSearchInput() {
            let by = document.getElementById('searchForm:searchBy').value;
            let isSpecialization = by === 'specialization';

            // Toggle visibility of the containers
            let searchValueDiv = document.getElementById('searchForm:searchValueInputDiv');
            let specializationDiv = document.getElementById('searchForm:specializationDropdownDiv');

            searchValueDiv.classList.toggle('hidden', isSpecialization);
            specializationDiv.classList.toggle('hidden', !isSpecialization);

            // Clear the value of the component that is being hidden
            if (isSpecialization) {
                let input = document.getElementById('searchForm:searchValueInput');
                if (input) input.value = '';
            } else {
                let spec = document.getElementById('searchForm:specializationDropdown');
                // Set to the first option (placeholder) if selecting another searchBy type
                if (spec && spec.options.length > 0) {
                    spec.value = spec.options[0].value;
                }
            }
        }

        /* Return to the table content rather from top of page */
        function scrollToTable() {
            const table = document.querySelector('.data-table');
            if (table) {
                // Smooth scroll to table with offset for better visibility
                const offset = 80; // Adjust this value based on your navbar height
                const tablePosition = table.getBoundingClientRect().top + window.pageYOffset - offset;
                window.scrollTo({
                    top: tablePosition,
                    behavior: 'smooth'
                });
            }
        }

        // Modify the existing window.onload function
        window.onload = function() {
            // Ensure the correct input is shown/hidden on page load based on initial searchBy value
            toggleSearchInput();

            // Add click event listeners to pagination buttons
            const paginationButtons = document.querySelectorAll('.pagination .btn');
            paginationButtons.forEach(button => {
                button.addEventListener('click', function(e) {
                    // Small delay to ensure the table has updated
                    setTimeout(scrollToTable, 200);
                });
            });

            // Re-apply toggle on form submission for reset button immediate="true"
            // This is a common workaround for immediate="true" not triggering onchange
            const searchForm = document.getElementById('searchForm');
            if (searchForm) {
                searchForm.addEventListener('submit', function() {
                    // Give a small delay to allow JSF update to process, then re-toggle
                    setTimeout(toggleSearchInput, 50);
                });
            }
        }
        </script>

</head>
<body>

	<jsp:include page="/navbar/NavRecipient.jsp" />

	<h2>Find & Book a Doctor 🩺</h2>

	<h:form id="searchForm" styleClass="main-content-panel">
		<%-- Global messages display --%>
		<%-- Message for specific input components (if used) --%>
		<h:message id="searchFieldMessages" for="searchFieldContainer"
			styleClass="global-messages" />


		<h:panelGrid columns="2" styleClass="search-inputs-grid">
			<h:outputLabel for="searchBy" value="Search By:" />
			<h:selectOneMenu id="searchBy"
				value="#{doctorSearchController.searchBy}"
				onchange="toggleSearchInput()">
				<f:selectItems value="#{doctorSearchController.searchOptions}" />
			</h:selectOneMenu>

			<h:outputLabel escape="false"
				value=" <span style='color:red'>*</span>Search Criteria:" />
			<h:panelGroup>
				<h:panelGroup id="searchValueInputDiv" layout="block">
					<h:inputText id="searchValueInput"
						value="#{doctorSearchController.searchValue}" />
				</h:panelGroup>

				<h:panelGroup id="specializationDropdownDiv" layout="block"
					styleClass="hidden select-wrapper">
					<%-- Added select-wrapper for custom arrow --%>
					<h:selectOneMenu id="specializationDropdown"
						value="#{doctorSearchController.selectedSpecialization}">
						<f:selectItems
							value="#{doctorSearchController.specializationOptions}" />
					</h:selectOneMenu>
				</h:panelGroup>
				<h:message for="searchValueInput" style="color:red" />
			</h:panelGroup>
		</h:panelGrid>


		<div class="search-buttons">
			<h:commandButton value="Search Doctor"
				action="#{doctorSearchController.executeSearch}"
				styleClass="btn btn-primary" />

			<h:commandButton value="Clear Search"
				action="#{doctorSearchController.resetSearch}"
				styleClass="btn btn-secondary" />
		</div>


		<h:dataTable value="#{doctorSearchController.paginatedDoctors}"
			var="doc" styleClass="data-table"
			rendered="#{not empty doctorSearchController.paginatedDoctors}">

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
				<h:outputText value="#{doc.status}" 
				styleClass="status-#{doc.status}"/>
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

		<h:panelGroup id="doctorPaginationPanel"
			rendered="#{not empty doctorSearchController.paginatedDoctors}"
			layout="block" styleClass="pagination">

			<%-- This h:outputText will be aligned to the left --%>
			<h:outputText value="#{doctorSearchController.paginationDocSummary}"
				styleClass="pagination-label"
				rendered="#{not empty doctorSearchController.paginatedDoctors}" />

			<%-- This div will group the buttons and page numbers and be aligned to the right --%>
			<div>
				<h:commandButton value="« Previous"
					action="#{doctorSearchController.prevPage}"
					disabled="#{not doctorSearchController.hasPrevPage}"
					styleClass="btn" /> <%-- Changed styleClass to 'btn' --%>

				<h:outputText styleClass="pagination-info"
					value="Page #{doctorSearchController.currentPage} of #{doctorSearchController.totalPages}" />

				<h:commandButton value="Next »"
					action="#{doctorSearchController.nextPage}"
					disabled="#{not doctorSearchController.hasNextPage}"
					styleClass="btn" /> <%-- Changed styleClass to 'btn' --%>
			</div>
		</h:panelGroup>


		<h:panelGroup
			rendered="#{empty doctorSearchController.paginatedDoctors and not doctorSearchController.searchPerformed}">
			<div class="not-found">Search for doctors by name,
				specialization or and address to find available appointments.</div>
		</h:panelGroup>
		<h:panelGroup
			rendered="#{empty doctorSearchController.paginatedDoctors and doctorSearchController.searchPerformed}">
			<div class="not-found">Kindly Search properly to Book
				Appointment</div>
		</h:panelGroup>

		<h:panelGroup
			rendered="#{not empty doctorSearchController.paginatedDoctors}">


			<script type="text/javascript">
        window.addEventListener('load', function () {
            setTimeout(scrollToTable, 200);
        });
    </script>

		</h:panelGroup>


	</h:form>

</body>
	</html>
</f:view>