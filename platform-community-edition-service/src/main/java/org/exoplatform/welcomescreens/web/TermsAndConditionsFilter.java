package org.exoplatform.welcomescreens.web;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.software.register.UnlockService;
import org.exoplatform.web.filter.Filter;
import org.exoplatform.welcomescreens.service.TermsAndConditionsService;

/**
 * Filter responsible of Terms and conditions displaying.
 * Call TermsAndConditions service to know if TermsAndConditions are checked, if not, forward to TermsAndConditions page
 * <p>
 * 2 conditions to forward to termes and conditions page:
 * <ul>
 * <li>Request URI is not a login URI. In this case we need to execute TermsAndConditions process after login process</li>
 * <li>TermsAndConditions is not checked</li>
 * </ul>
 * 
 * @author Clement
 *
 */
public class TermsAndConditionsFilter implements Filter {

  private static final String PLF_WELCOME_SCREENS_SERVLET_CTX = "/welcome-screens";
  private static final String TC_SERVLET_URL = "/terms-and-conditions";
  private static final String INITIAL_URI_PARAM_NAME = "initialURI";
  private static final String LOGIN_URI = "/login";
  private static final String DOLOGIN_URI = "/dologin";

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest httpServletRequest = (HttpServletRequest)request;
    HttpServletResponse httpServletResponse = (HttpServletResponse)response;

    TermsAndConditionsService termsAndConditionsService = PortalContainer.getInstance().getComponentInstanceOfType(TermsAndConditionsService.class);
    UnlockService unlockService = PortalContainer.getInstance().getComponentInstanceOfType(UnlockService.class);

    boolean tcChecked = termsAndConditionsService.isTermsAndConditionsChecked();
    String remoteUserName = getRemoteUserName(httpServletRequest);
    String requestUri = httpServletRequest.getRequestURI();
    String loginRequestUri = httpServletRequest.getContextPath() + LOGIN_URI;
    String dologinRequestUri = httpServletRequest.getContextPath() + DOLOGIN_URI;
    boolean isLoginUri = (requestUri.contains(loginRequestUri) || requestUri.contains(dologinRequestUri));
    boolean isRestUri = requestUri.contains(ExoContainerContext.getCurrentContainer().getContext().getRestContextName());
    boolean isDevMod = PropertyManager.isDevelopping();
    if(! isLoginUri && ! isRestUri && ! tcChecked && !isDevMod && unlockService.showTermsAndConditions()) {
      // Get full url
      String queryString = httpServletRequest.getQueryString();
      if (queryString != null) {
        requestUri =new StringBuffer(requestUri).append("?").append(queryString).toString();
      }

      // Get plf extension servlet context (because TermsAndConditionsFilter and terms-and-conditions servlet declaration are not on same context (webapp))
      ServletContext welcomrScreensContext = httpServletRequest.getSession().getServletContext().getContext(PLF_WELCOME_SCREENS_SERVLET_CTX);
      // Forward to resource from this context: 
      String uriTarget = (new StringBuilder()).append(TC_SERVLET_URL + "?" + INITIAL_URI_PARAM_NAME + "=").append(requestUri).toString();
      welcomrScreensContext.getRequestDispatcher(uriTarget).forward(httpServletRequest, httpServletResponse);
      return;
    } else if (StringUtils.isNotEmpty(remoteUserName)) {
      if (!termsAndConditionsService.isTermsAndConditionsAcceptedBy(remoteUserName) && termsAndConditionsService.getCurrentTermsAndConditionsVersion() != null) {
        // Get full url
        String queryString = httpServletRequest.getQueryString();
        if (queryString != null) {
          requestUri = new StringBuffer(requestUri).append("?").append(queryString).toString();
        }
        // Get plf extension servlet context (because TermsAndConditionsFilter and terms-and-conditions servlet declaration are not on same context (webapp))
        ServletContext welcomrScreensContext = httpServletRequest.getSession().getServletContext().getContext(PLF_WELCOME_SCREENS_SERVLET_CTX);
        // Forward to resource from this context:
        String uriTarget = (new StringBuilder()).append(TC_SERVLET_URL + "?" + INITIAL_URI_PARAM_NAME + "=").append(requestUri).toString();
        welcomrScreensContext.getRequestDispatcher(uriTarget).forward(httpServletRequest, httpServletResponse);
        return;
      }
    }
    chain.doFilter(request, response);
  }

  private String getRemoteUserName(HttpServletRequest httpServletRequest) {
    return httpServletRequest.getRemoteUser();
  }
}
