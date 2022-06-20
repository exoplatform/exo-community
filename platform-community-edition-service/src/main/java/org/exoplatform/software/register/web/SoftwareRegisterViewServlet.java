package org.exoplatform.software.register.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.*;

/**
 * Created by The eXo Platform SEA Author : eXoPlatform toannh@exoplatform.com
 * On 9/28/15 Software register to Tribe
 */
public class SoftwareRegisterViewServlet extends HttpServlet {

  private static final long   serialVersionUID = 1L;

  private final static String SR_JSP_RESOURCE  = "/WEB-INF/jsp/software-registration/softwareregister.jsp";

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String registrationULR = SoftwareRegisterAuthViewServlet.getRegistrationURL(request);
    request.setAttribute("registrationURL", registrationULR);
    request.getSession().setAttribute("registrationURL", registrationULR);
    getServletContext().getRequestDispatcher(SR_JSP_RESOURCE).forward(request, response);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request, response);
  }
}
