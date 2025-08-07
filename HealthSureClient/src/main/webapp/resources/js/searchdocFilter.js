// searchdocFilter.js

// This function clears the irrelevant input fields and then submits the form.
function clearCriteriaAndSubmit(selectElement) {
    const selectedValue = selectElement.value;
    const searchValueInput = document.getElementById('searchForm:searchValueInput');
    const specializationDropdown = document.getElementById('searchForm:specializationDropdown');
    const searchModeRadios = document.getElementsByName('searchForm:searchMode');

    // Always clear both fields before submitting to ensure a clean slate
    if (searchValueInput) {
        searchValueInput.value = '';
    }
    if (specializationDropdown) {
        specializationDropdown.value = '';
    }
    
    // Clear the radio button selection
    if (searchModeRadios) {
        for (let i = 0; i < searchModeRadios.length; i++) {
            searchModeRadios[i].checked = false;
        }
    }
    
    // Now submit the form
    selectElement.form.submit();
}

// Toggles visibility of form sections based on user selection
function toggleSearchInput() {
    const searchBySelect = document.getElementById('searchForm:searchBy');
    if (!searchBySelect) return;

    const selectedValue = searchBySelect.value;
    const searchValueInputDiv = document.getElementById('searchForm:searchValueInputDiv');
    const specializationDropdownDiv = document.getElementById('searchForm:specializationDropdownDiv');
    const searchModeLabel = document.getElementById('searchForm:searchModeLabel');
    const searchModeRadiosDiv = document.getElementById('searchForm:searchModeRadiosDiv');
    
    const isSpecialization = (selectedValue === 'specialization');
    const isDoctorNameOrAddress = (selectedValue === 'doctorName' || selectedValue === 'address');
    const isAddress = (selectedValue === 'address');

    if (searchValueInputDiv) {
        searchValueInputDiv.classList.toggle('hidden', isSpecialization);
    }
    if (specializationDropdownDiv) {
        specializationDropdownDiv.classList.toggle('hidden', !isSpecialization);
    }
    if (searchModeLabel) {
        searchModeLabel.classList.toggle('hidden', !isDoctorNameOrAddress);
    }
    if (searchModeRadiosDiv) {
        searchModeRadiosDiv.classList.toggle('hidden', !isDoctorNameOrAddress);
    }
    
    // NEW LOGIC: Hide "Exact" radio button when 'Address' is selected
    const searchModeRadios = document.getElementsByName('searchForm:searchMode');
    if (searchModeRadios && searchModeRadios.length > 0) {
        // Find the "Exact" radio button and its label
        for (let i = 0; i < searchModeRadios.length; i++) {
            if (searchModeRadios[i].value === 'exact') {
                const exactRadio = searchModeRadios[i];
                const exactLabel = document.querySelector(`label[for="${exactRadio.id}"]`);
                
                if (isAddress) {
                    exactRadio.style.display = 'none';
                    if (exactLabel) exactLabel.style.display = 'none';
                } else {
                    exactRadio.style.display = 'inline-block';
                    if (exactLabel) exactLabel.style.display = 'inline-block';
                }
                break;
            }
        }
    }
}

// Smoothly scrolls to the results table if present
function scrollToTable() {
    const anchor = document.getElementById('results');
    if (anchor) {
        const offset = 90; 
        const top = anchor.getBoundingClientRect().top + window.pageYOffset - offset;
        window.scrollTo({ top, behavior: 'smooth' });
    }
}

// Sets up form visibility when the page loads
window.onload = function () {
    toggleSearchInput();

    // Scroll only when coming back to #results
    if (window.location.hash === '#results') {
        setTimeout(scrollToTable, 100);
    }
};