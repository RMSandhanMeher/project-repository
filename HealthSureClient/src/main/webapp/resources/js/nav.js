
      function setActiveLink(clicked) {
        document.querySelectorAll('.nav-link').forEach(link => {
          link.classList.remove('bg-blue-100', 'font-semibold');
        });
        clicked.classList.add('bg-blue-100', 'font-semibold');
      }
