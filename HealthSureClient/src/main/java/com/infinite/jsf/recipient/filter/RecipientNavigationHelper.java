package com.infinite.jsf.recipient.filter;

import java.io.IOException;
import java.io.Serializable;

import javax.faces.context.FacesContext;

@SuppressWarnings("serial")
public class RecipientNavigationHelper implements Serializable {

    // ... (rest of the class code)

    /**
     * Clears insurance-related session state and forces a browser redirect.
     *
     * @param path The relative path to the page to redirect to (e.g., "recipient/RecipientDashBoard.jsf").
     */
    public void clearInsuranceSessionStateAndRedirect(String path) throws IOException {
        // Clear the session attributes first.
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("subscribedMembers");
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("selectedItem");
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("selectedStatus");
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("fromDate");
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("toDate");
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("planNameSearchInput");

        // Perform the programmatic redirect.
        FacesContext.getCurrentInstance().getExternalContext().redirect(
            FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/" + path
        );
    }
}