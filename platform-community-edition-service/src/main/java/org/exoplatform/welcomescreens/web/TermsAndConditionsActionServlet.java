/**
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.welcomescreens.web;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.welcomescreens.service.TermsAndConditionsService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author <a href="hzekri@exoplatform.com">hzekri</a>
 */
public class TermsAndConditionsActionServlet extends HttpServlet {

    private static final long serialVersionUID = 6467955354840693802L;
    private static final Log logger = ExoLogger.getLogger(TermsAndConditionsActionServlet.class);
    private final static String PARAM_CHECKTC = "checktc";
    private TermsAndConditionsService termsAndConditionsService;

    public TermsAndConditionsService getTermsAndConditionsService() {
        if (this.termsAndConditionsService == null) {
            termsAndConditionsService = (TermsAndConditionsService) PortalContainer.getInstance().getComponentInstanceOfType(TermsAndConditionsService.class);
        }
        return this.termsAndConditionsService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String remoteUserName = getRemoteUserName(httpServletRequest);
        // Get usefull parameters
        Boolean checkTc = false;
        try {
            checkTc = request.getParameter(PARAM_CHECKTC) != null;
        } catch (Exception e) {
            logger.error("Terms and conditions: impossible to get parameter " + PARAM_CHECKTC, e);
        }
        // Check tc with service
        if (checkTc) {
            getTermsAndConditionsService().checkTermsAndConditions();
        }
        if (remoteUserName != null) {
            getTermsAndConditionsService().accept(remoteUserName);
        }
        // Redirect to the account Setup
        String redirectURI = "/portal/";
        response.sendRedirect(redirectURI);
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
    private String getRemoteUserName(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getRemoteUser();
    }
}
