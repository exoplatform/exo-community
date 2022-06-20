package org.exoplatform.software.register.web;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.software.register.UnlockService;
import org.exoplatform.software.register.service.SoftwareRegistrationService;
import org.exoplatform.web.filter.Filter;

/**
 * Filter platform registration screen displaying.
 * <p>
 * Conditions to forward to platform registration page:
 * <ul>
 * <li>eXo Community is not reachable</li>
 * <li>User can skip registration</li>
 * <li>User is not register local PLF with community</li>
 * </ul>
 *
 * @author ToanNH
 */
public class SoftwareRegisterFilter implements Filter {

  public static final String          NOT_REACHABLE             = "NOT_REACHABLE";

  private static final String         PLF_COMMUNITY_SERVLET_CTX = "/welcome-screens";

  private static final String         SR_SERVLET_URL            = "/software-register";

  private static final String         INITIAL_URI_PARAM_NAME    = "initialURI";

  public SoftwareRegisterFilter() {
  }

  private boolean checkRequest(boolean requestSkip) {
    SoftwareRegistrationService plfRegisterService = PortalContainer.getInstance().getComponentInstanceOfType(SoftwareRegistrationService.class);
    UnlockService unlockService = PortalContainer.getInstance().getComponentInstanceOfType(UnlockService.class);
    if (!requestSkip) {
      return true;
    }
    if (plfRegisterService.canSkipRegister() || (unlockService.isUnlocked())) {
      return false;
    }
    return !plfRegisterService.canSkipRegister();
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest httpServletRequest = (HttpServletRequest) request;
    HttpServletResponse httpServletResponse = (HttpServletResponse) response;
    SoftwareRegistrationService plfRegisterService = PortalContainer.getInstance().getComponentInstanceOfType(SoftwareRegistrationService.class);

    String notReachable = (String) httpServletRequest.getSession().getAttribute("notReachable");
    boolean isDevMod = PropertyManager.isDevelopping();
    if (notReachable == null) {
      notReachable = httpServletRequest.getQueryString();
      if (StringUtils.equals(notReachable, this.NOT_REACHABLE)) {
        notReachable = "true";
        httpServletRequest.getSession().setAttribute("notReachable", notReachable);
      }
    }

    String requestUri = httpServletRequest.getRequestURI();
    boolean isRestUri = requestUri.contains(ExoContainerContext.getCurrentContainer().getContext().getRestContextName());
    if (!isRestUri && !plfRegisterService.isSoftwareRegistered() && !isDevMod
        && !StringUtils.equals(notReachable, "true") && checkRequest(plfRegisterService.isSkipRequest())) {
      // Get full url
      String reqUri = httpServletRequest.getRequestURI().toString();
      String queryString = httpServletRequest.getQueryString();
      if (queryString != null) {
        reqUri = new StringBuffer(reqUri).append("?").append(queryString).toString();
      }
      ServletContext platformRegisterContext = httpServletRequest.getSession()
                                                                 .getServletContext()
                                                                 .getContext("/welcome-screens");
      String uriTarget = (new StringBuilder()).append(SR_SERVLET_URL)
                                              .append("?")
                                              .append(INITIAL_URI_PARAM_NAME)
                                              .append("=")
                                              .append(reqUri)
                                              .toString();
      platformRegisterContext.getRequestDispatcher(uriTarget).forward(httpServletRequest, httpServletResponse);
      return;
    }
    chain.doFilter(request, response);
  }
}
