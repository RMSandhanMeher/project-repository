<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html"%>
<f:view>
	<!DOCTYPE html>
	<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Doctor Availability</title>
<%-- <script src="https://cdn.tailwindcss.com"></script> --%>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/resources/css/appointment/doctorAvailability.css">
<link rel="stylesheet"
	href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
<link rel="stylesheet"
	href="https://cdn.jsdelivr.net/npm/flatpickr/dist/flatpickr.min.css">
</head>
<body class="body-bg-color min-h-screen-full p-4-spacing">
	<jsp:include page="./../../navbar/NavRecipient.jsp" />
	<jsp:include page="NavBar.jsp" />
	<div id="bookingOverlay" class="booking-overlay">
		<div class="booking-overlay-content">
			<svg class="spinner-icon mx-auto-spacing"
				xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
			<circle class="opacity-25-svg" cx="12" cy="12" r="10"
					stroke="currentColor" stroke-width="4"></circle>
			<path class="opacity-75-svg" fill="currentColor"
					d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z"></path>
		</svg>
			<p class="mt-2-spacing text-gray-700-color">Booking your
				appointment...</p>

		</div>
	</div>

	<div class="main-container-card" style=" margin-top: 165px">
		<div class="header-section-flex">
			<h1 class="header-title transform-hover">
				<i class="fas fa-calendar-alt mr-2-spacing"></i> Book Appointment
			</h1>
			<div class="doctor-info-text-right">
				<span class="doctor-name-text"> <h:outputText
						value="#{doctorAvailabilityController.doctor.doctorName}" />
				</span> <span class="doctor-specialization-text"> <h:outputText
						value="#{doctorAvailabilityController.doctor.specialization}" />
				</span>
			</div>
		</div>



		<h:form id="appointmentForm">
			<div class="date-picker-row-layout mb-4-spacing md:mb-6-spacing">
				<div class="flex-col-flex-1">
					<h2 class="section-title mb-2-spacing">Select Date</h2>
					<div class="date-picker-input-group">
						<h:inputText id="datePicker"
							value="#{doctorAvailabilityController.selectedDateInput}"
							styleClass="flatpickr-input date-input-field" />
						<h:commandButton id="datePickerSubmit" value="Go"
							styleClass="go-button"
							action="#{doctorAvailabilityController.handleDateSelection}" />
						<div class="message-container date-picker-message">
							<h:messages globalOnly="true" styleClass="message-full-width"
								infoClass="info-message-style" errorClass="error-message-style" />
						</div>
					</div>
				</div>

				<div class="doctor-availability-table-wrapper">
					<h6 class="section-title">Available Timings</h6>
					<h:dataTable
						value="#{doctorAvailabilityController.availabilityTiming}"
						var="tim" styleClass="availability-table-style">
						<h:column>
							<h:outputText value="#{tim}" styleClass="availability-item-text" />
						</h:column>
					</h:dataTable>
				</div>
			</div>




			<div class="mb-6-spacing md:mb-8-spacing">
				<div class="date-scroll-container">
					<h:dataTable
						value="#{doctorAvailabilityController.groupedAvailabilityList}"
						var="day" styleClass="date-display-table">
						<h:column>
							<div class="date-card-style ripple">
								<h:commandButton value="#{day.displayDate}"
									action="#{doctorAvailabilityController.loadAvailableSlots}"
									styleClass="date-card-button">
									<f:setPropertyActionListener
										target="#{doctorAvailabilityController.selectedDate}"
										value="#{day.date}" />
								</h:commandButton>
								<div class="date-card-slot-count">
									<h:outputText value="#{day.totalSlots}" />
									slots
								</div>
							</div>
						</h:column>
					</h:dataTable>
				</div>
			</div>

			<h:panelGroup
				rendered="#{not empty doctorAvailabilityController.selectedDate}">
				<div class="time-slots-header-flex mb-4-spacing gap-2-spacing">
					<h2 class="section-title">
						Available Time Slots for <span class="selected-date-highlight">
							<h:outputText
								value="#{doctorAvailabilityController.selectedDate}">
								<f:convertDateTime pattern="EEEE, MMMM d, yyyy" />
							</h:outputText>
						</span>
					</h2>
				</div>

				<h:panelGroup
					rendered="#{empty doctorAvailabilityController.morningSlots and empty doctorAvailabilityController.afternoonSlots and empty doctorAvailabilityController.eveningSlots}">
					<div class="empty-state p-6-spacing text-center mb-6-spacing">
						<i
							class="fas fa-calendar-times empty-state-icon transform-transition"></i>
						<h3 class="empty-state-title">No Available Slots</h3>
						<p class="empty-state-text">There are no available time slots
							for the selected date.</p>
						<p class="empty-state-text-small mt-2-spacing">Please try
							another date or check back later.</p>
					</div>
				</h:panelGroup>

				<h:panelGroup
					rendered="#{not empty doctorAvailabilityController.morningSlots}">
					<div class="time-group-style mb-3-spacing md:mb-4-spacing">
						<h3 class="time-group-title">
							<i class="fas fa-sun mr-2-spacing transform-transition-hover"></i>
							Morning Slots <span class="slot-count-text"> (<h:outputText
									value="#{doctorAvailabilityController.morningSlotCount}" />
								available)
							</span>
						</h3>
						<div class="time-slots-display-wrapper">
							<h:dataTable value="#{doctorAvailabilityController.morningSlots}"
								var="slot" styleClass="time-slots-table-component">
								<h:column>
									<h:commandButton value="#{slot.formattedTimeRange}"
										action="#{doctorAvailabilityController.bookAppointment}"
										onclick="return showBookingLoading();"
										styleClass="time-slot-button ripple">

										<f:setPropertyActionListener
											target="#{doctorAvailabilityController.selectedAvailabilityId}"
											value="#{slot.availabilityId}" />
										<f:setPropertyActionListener
											target="#{doctorAvailabilityController.selectedSlotNumber}"
											value="#{slot.slotNumber}" />
									</h:commandButton>
								</h:column>
							</h:dataTable>
						</div>
					</div>
				</h:panelGroup>

				<h:panelGroup
					rendered="#{not empty doctorAvailabilityController.afternoonSlots}">
					<div class="time-group-style mb-3-spacing md:mb-4-spacing">
						<h3 class="time-group-title">
							<i
								class="fas fa-cloud-sun mr-2-spacing transform-transition-hover"></i>
							Afternoon Slots <span class="slot-count-text"> (<h:outputText
									value="#{doctorAvailabilityController.afternoonSlotCount}" />
								available)
							</span>
						</h3>
						<div class="time-slots-display-wrapper">
							<h:dataTable
								value="#{doctorAvailabilityController.afternoonSlots}"
								var="slot" styleClass="time-slots-table-component">
								<h:column>
									<h:commandButton value="#{slot.formattedTimeRange}"
										action="#{doctorAvailabilityController.bookAppointment}"
										onclick="return showBookingLoading();"
										styleClass="time-slot-button ripple">

										<f:setPropertyActionListener
											target="#{doctorAvailabilityController.selectedAvailabilityId}"
											value="#{slot.availabilityId}" />
										<f:setPropertyActionListener
											target="#{doctorAvailabilityController.selectedSlotNumber}"
											value="#{slot.slotNumber}" />
									</h:commandButton>
								</h:column>
							</h:dataTable>
						</div>
					</div>
				</h:panelGroup>

				<h:panelGroup
					rendered="#{not empty doctorAvailabilityController.eveningSlots}">
					<div class="time-group-style mb-3-spacing md:mb-4-spacing">
						<h3 class="time-group-title">
							<i class="fas fa-moon mr-2-spacing transform-transition-hover"></i>
							Evening Slots <span class="slot-count-text"> (<h:outputText
									value="#{doctorAvailabilityController.eveningSlotCount}" />
								available)
							</span>
						</h3>
						<div class="time-slots-display-wrapper">
							<h:dataTable value="#{doctorAvailabilityController.eveningSlots}"
								var="slot" styleClass="time-slots-table-component">
								<h:column>
									<h:commandButton value="#{slot.formattedTimeRange}"
										action="#{doctorAvailabilityController.bookAppointment}"
										onclick="return showBookingLoading();"
										styleClass="time-slot-button ripple">

										<f:setPropertyActionListener
											target="#{doctorAvailabilityController.selectedAvailabilityId}"
											value="#{slot.availabilityId}" />
										<f:setPropertyActionListener
											target="#{doctorAvailabilityController.selectedSlotNumber}"
											value="#{slot.slotNumber}" />
									</h:commandButton>
								</h:column>
							</h:dataTable>
						</div>
					</div>
				</h:panelGroup>
			</h:panelGroup>
		</h:form>
	</div>

	<script src="https://cdn.jsdelivr.net/npm/flatpickr"></script>
	<script>
        document.addEventListener('DOMContentLoaded', function() {
            // Initialize date picker
            const datePickerElement = document.getElementById('appointmentForm:datePicker');
            
            // Set fixed width and placeholder
            datePickerElement.style.width = '120px';
            datePickerElement.setAttribute('placeholder', 'Select date');
            
            // Initialize flatpickr with placeholder
            flatpickr(datePickerElement, {
                dateFormat: "Y-m-d",
                minDate: "today",
                placeholder: "Select appointment date",
                disableMobile: false,
                onChange: function(selectedDates, dateStr) {
                    document.getElementById('appointmentForm:datePickerSubmit').click();
                }
            });

            // Add ripple effect to all elements with .ripple class
            document.querySelectorAll('.ripple').forEach(button => {
                button.addEventListener('click', function(e) {
                    const rect = this.getBoundingClientRect();
                    const x = e.clientX - rect.left;
                    const y = e.clientY - rect.top;
                    
                    const ripple = document.createElement('span');
                    ripple.classList.add('ripple-effect');
                    ripple.style.left = `${x}px`;
                    ripple.style.top = `${y}px`;
                    
                    this.appendChild(ripple);
                    
                    setTimeout(() => {
                        ripple.remove();
                    }, 600);
                });
            });
        });
        function showBookingLoading() {
    		document.getElementById("bookingOverlay").style.display = "flex";
    		return true;
    	}
    </script>
</body>
	</html>
</f:view>