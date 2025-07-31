package com.infinite.jsf.recipient.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class RecipientFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// Optional: Initialization logic
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		HttpSession session = req.getSession(true);

		// Validate: Check if recipient is logged in
		Object recipientUser = (session != null) ? session.getAttribute("loggedInRecipient") : null;

		String loginPage = req.getContextPath() + "/recipient/Login.jsf";

		// Allow access to login page or resources without filtering
		String uri = req.getRequestURI();

		boolean isLoginRequest = uri.endsWith("/recipient/Login.jsf") || uri.contains("javax.faces.resource");

		if (recipientUser != null || isLoginRequest) {
			// Valid session or accessing login/resources: continue
			chain.doFilter(request, response);
		} else {
			// Not logged in, redirect to login
			uri=uri.substring(req.getContextPath().length()).split(";")[0];
			System.out.println(uri);
			session.setAttribute("requestUri", uri);
			res.sendRedirect(loginPage);
		}
	}

	@Override
	public void destroy() {
		// Optional: Cleanup
	}
}
