package org.exoplatform.welcomescreens.web;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.welcomescreens.service.utils.NodeUtils;

import javax.jcr.Node;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet responsible of the first display of TermsAndConditions
 * @author Clement
 *
 */
public class TermsAndConditionsViewServlet extends HttpServlet {
  private static final Log LOG = ExoLogger.getLogger(TermsAndConditionsViewServlet.class);
  private static final long serialVersionUID = 6467955354840693802L;
  private final static String TC_JSP_RESOURCE = "/WEB-INF/jsp/welcome-screens/termsandconditions.jsp";
  private final static String TC_JSP_RESOURCE_TERMS = "/WEB-INF/jsp/welcome-screens/UsageCharter.jsp";
  private static final String WEB_CONTENT_ATTRIBUTE_NAME = "WEB_CONTENT";
  private static final String WEB_CONTENT_STYLE_ATTRIBUTE_NAME = "WEB_CONTENT_STYLE";
  private static final String WEB_CONTENT_JS_ATTRIBUTE_NAME = "WEB_CONTENT_JS";
  private static final String WEB_CONTENT_URL = "/sites/shared/web contents/site artifacts/terms/terms";
  private static final String WEB_CONTENT_URL_STYLE = "/sites/shared/web contents/site artifacts/terms/terms/css";
  private static final String WEB_CONTENT_URL_JS = "/sites/shared/web contents/site artifacts/terms/terms/js";

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    HttpServletRequest httpServletRequest = (HttpServletRequest) request;
    String usageCharterStyle = "";
    String usageCharterJs = "";
    String remoteUserName = getRemoteUserName(httpServletRequest);
    Node nodeStyle = NodeUtils.findCollaborationFile(WEB_CONTENT_URL_STYLE);
    Node nodeScript = NodeUtils.findCollaborationFile(WEB_CONTENT_URL_JS);
    try {
      usageCharterStyle = nodeStyle.getNode("default.css/jcr:content").getProperty("jcr:data").getString();
    } catch (Exception e) {
      LOG.error("impossible to get the style of the web content", e);
    }
    try {
      usageCharterJs = nodeScript.getNode("default.js/jcr:content").getProperty("jcr:data").getString();
    } catch (Exception e) {
      LOG.error("impossible to get the scripts of the web content", e);
    }
    if (remoteUserName != null) {
      request.setAttribute(WEB_CONTENT_ATTRIBUTE_NAME, NodeUtils.getWebContentContentFromUrl(WEB_CONTENT_URL));
      request.setAttribute(WEB_CONTENT_STYLE_ATTRIBUTE_NAME, usageCharterStyle);
      request.setAttribute(WEB_CONTENT_JS_ATTRIBUTE_NAME, usageCharterJs);
      getServletContext().getRequestDispatcher(TC_JSP_RESOURCE_TERMS).forward(request, response);
    } else {
      getServletContext().getRequestDispatcher(TC_JSP_RESOURCE).forward(request, response);
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request, response);
  }

  private String getRemoteUserName(HttpServletRequest httpServletRequest) {
    return httpServletRequest.getRemoteUser();
  }
}
