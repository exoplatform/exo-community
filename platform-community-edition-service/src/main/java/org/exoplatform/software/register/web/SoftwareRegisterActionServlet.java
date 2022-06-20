package org.exoplatform.software.register.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.software.register.service.SoftwareRegistrationService;

/**
 * Created by The eXo Platform SEA Author : eXoPlatform toannh@exoplatform.com
 * On 9/28/15 Software register to Tribe
 */
public class SoftwareRegisterActionServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private static final Log  logger           = ExoLogger.getLogger(SoftwareRegisterActionServlet.class);

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String redirectURI = "/portal/";
    String value = request.getParameter("value");
    // make sure PortalContainer is created
    PortalContainer.getInstance();
    SoftwareRegistrationService softwareRegistrationService = CommonsUtils.getService(SoftwareRegistrationService.class);
    if (StringUtils.equals("skip", value)) {
      softwareRegistrationService.setSkipRequest(true);
      softwareRegistrationService.updateSkippedNumber();
    }
    if (StringUtils.equals("notReachable", value)) {
      request.getSession().setAttribute("notReachable", "true");
      redirectURI += "?" + SoftwareRegisterFilter.NOT_REACHABLE;
    }
    response.sendRedirect(redirectURI);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String registrationULR = SoftwareRegisterAuthViewServlet.getRegistrationURL(request);
    request.setAttribute("registrationURL", registrationULR);
    request.getSession().setAttribute("registrationURL", registrationULR);
    doPost(request, response);
  }
}
