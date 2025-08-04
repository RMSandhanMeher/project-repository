<%-- 
Appointment page navbar which will navigate among the pages like doctor pick for appointment , select a perticular date for the appointment ,
see all the future and past appointment 
--%>

<script src="https://cdn.tailwindcss.com"></script>
<script src="${pageContext.request.contextPath}/resources/js/nav.js"></script>
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/appointment/navBar.css">

<nav
	class="bg-white backdrop-blur-md text-gray-800 px-6 py-2 rounded-2xl shadow-lg mb-6 border border-gray-200 fixed top-16 left-1/2 -translate-x-1/2 z-30 w-fit">
	<div
		class="px-10 max-w-screen-xl mx-auto flex justify-center items-center">
		<div class="space-x-4 flex">
			<%-- The page for doctor selection for appointment request link  --%>
			<a
				href="${pageContext.request.contextPath}/recipient/appointment/SearchProviders.jsf"
				class="nav-link text-nowrap px-4 py-2 rounded-xl transition duration-200 border border-gray-300"
				onclick="setActiveLink(this)"> Select Doctor </a> 
				
				<%-- Page for date and timing select for appointment  --%>
				<a
				href="${pageContext.request.contextPath}/recipient/appointment/doctorAvailabilityList.jsf"
				class="nav-link text-nowrap px-4 py-2 rounded-xl transition duration-200 border border-gray-300"
				onclick="setActiveLink(this)"> Book Appointment </a> 
				
				<%-- Page for see past and future appointment  --%>
				<a
				href="${pageContext.request.contextPath}/recipient/appointment/recipient-appointments.jsf"
				class="nav-link text-nowrap px-4 py-2 rounded-xl transition duration-200 border border-gray-300"
				onclick="setActiveLink(this)"> My Appointments </a>
		</div>
	</div>
</nav>

<script>
  window.addEventListener('DOMContentLoaded', () => {
    const currentPath = window.location.pathname;
    document.querySelectorAll('.nav-link').forEach(link => {
      const linkPath = new URL(link.href).pathname;
      if (linkPath === currentPath) {
        link.classList.add('bg-blue-100', 'font-semibold');
      }
    });
  });
</script>

