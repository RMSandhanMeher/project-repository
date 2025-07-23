<script src="https://cdn.tailwindcss.com"></script>
<nav
	class="bg-white backdrop-blur-md text-gray-800 px-6 py-2 rounded-2xl shadow-lg mb-6 border border-gray-200 fixed top-24 left-1/2 -translate-x-1/2 z-30 w-fit">
	<div
		class="px-10 max-w-screen-xl mx-auto flex justify-center items-center">
		<div class="space-x-4 flex">
			<a
				href="${pageContext.request.contextPath}/recipient/SearchProviders.jsf"
				class="nav-link text-nowrap px-4 py-2 rounded-xl transition duration-200 border border-gray-300"
				onclick="setActiveLink(this)"> Select Doctor </a> <a
				href="${pageContext.request.contextPath}/recipient/appointment/doctorAvailabilityList.jsf"
				class="nav-link text-nowrap px-4 py-2 rounded-xl transition duration-200 border border-gray-300"
				onclick="setActiveLink(this)"> Book Appointment </a> <a
				href="${pageContext.request.contextPath}/recipient/appointment/recipient-appointments.jsf"
				class="nav-link text-nowrap px-4 py-2 rounded-xl transition duration-200 border border-gray-300"
				onclick="setActiveLink(this)"> My Appointments </a>
		</div>
	</div>
</nav>

<script>
      function setActiveLink(clicked) {
        document.querySelectorAll('.nav-link').forEach(link => {
          link.classList.remove('bg-blue-100', 'font-semibold');
        });
        clicked.classList.add('bg-blue-100', 'font-semibold');
      }
    </script>
