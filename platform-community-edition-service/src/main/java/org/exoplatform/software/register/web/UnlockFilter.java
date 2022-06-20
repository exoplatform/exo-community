package org.exoplatform.software.register.web;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.software.register.UnlockService;
import org.exoplatform.web.filter.Filter;

public class UnlockFilter implements Filter {
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest httpServletRequest = (HttpServletRequest) request;

    boolean isIgnoringRequest = isIgnoredRequest(httpServletRequest.getSession(true).getServletContext(),
                                                 httpServletRequest.getRequestURI());
    if (!isIgnoringRequest) {
      UnlockService unlockService = PortalContainer.getInstance().getComponentInstanceOfType(UnlockService.class);
      unlockService.setCalledUrl(httpServletRequest.getRequestURI());
    }
    chain.doFilter(request, response);
  }

  private boolean isIgnoredRequest(ServletContext context, String url) {
    String fileName = url.substring(url.indexOf("/"));
    String mimeType = context.getMimeType(fileName);
    return (mimeType != null || url.contains(CommonsUtils.getRestContextName()));
  }
}
