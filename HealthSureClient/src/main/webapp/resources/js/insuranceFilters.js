// insuranceFilters.js

// Script to ensure type="date" for date inputs
/**
* Date picker
*/
window.addEventListener('DOMContentLoaded', () => {
    const today = new Date().toISOString().split('T')[0];
 
    const fromDateInput = document.querySelector('input[id$="fromDate"]');
    const toDateInput = document.querySelector('input[id$="toDate"]');
 
    if (fromDateInput && toDateInput) {
        [fromDateInput, toDateInput].forEach(input => {
            input.setAttribute('type', 'date');
            input.setAttribute('max', today);
        });
 
        // When "From Date" changes, update "To Date" min
        fromDateInput.addEventListener('change', () => {
            const fromDate = fromDateInput.value;
            toDateInput.setAttribute('min', fromDate);
        });
 
        // When "To Date" changes, update "From Date" max
        toDateInput.addEventListener('change', () => {
            const toDate = toDateInput.value;
            fromDateInput.setAttribute('max', toDate > today ? today : toDate);
        });
    } else {
        console.warn("Date inputs not found. Check rendered HTML IDs.");
    }
});






// Function to scroll to the table
function scrollToTable() {
    // Corrected selector to match JSF's generated ID for the table
    // Assuming your form ID is 'insuranceForm' and table ID is 'insuranceTable'
    const table = document.getElementById('insuranceForm:insuranceTable'); 
    if (table) {
        const offset = 80; // Adjust for navbar height so that table doesn't go underneath of it
		//adds the current scroll position to get the true position relative to the document.
        const tablePosition = table.getBoundingClientRect().top + window.pageYOffset - offset;
		
		//Scrolls the page to bring the table into view.
        window.scrollTo({
            top: tablePosition,
            behavior: 'smooth'
        });
    }
}
// Call scroll to table on page load (after DOM is ready)
window.addEventListener('DOMContentLoaded', function() {
    // Call scroll to table. It will scroll if the table is rendered.
    const table = document.getElementById('insuranceForm:insuranceTable');
    if (table) {
        scrollToTable();
    }
});









function resetActiveFilter() {
    // Clear selected status dropdown
    var statusDropdown = document.getElementById('insuranceForm:statusFilter');
    if (statusDropdown) {
        statusDropdown.value = 'ALL'; // Set to the default 'ALL'
    }
    // Clear date inputs
    var fromDateInput = document.getElementById('insuranceForm:fromDate');
    if (fromDateInput) {
        fromDateInput.value = '';
    }
    var toDateInput = document.getElementById('insuranceForm:toDate');
    if (toDateInput) {
        toDateInput.value = '';
    }
    
}