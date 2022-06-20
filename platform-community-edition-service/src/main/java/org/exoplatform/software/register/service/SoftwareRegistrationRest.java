/**
 * Copyright (C) 2009 eXo Platform SAS.
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
package org.exoplatform.software.register.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;

@Path("/plf")
public class SoftwareRegistrationRest implements ResourceContainer {
  private static final Log LOG = ExoLogger.getExoLogger(SoftwareRegistrationRest.class);

  @GET
  @Path("checkConnection")
  @Produces("html/text")
  public Response checkConnection() {
    SoftwareRegistrationService registrationService = CommonsUtils.getService(SoftwareRegistrationService.class);
    String pingServerURL = registrationService.getSoftwareRegistrationHost();
    try {
      URL url = new URL(pingServerURL);
      HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
      urlConn.connect();
      return Response.ok(String.valueOf(HttpURLConnection.HTTP_OK == urlConn.getResponseCode())).build();
    } catch (IOException e) {
      LOG.error("Error checking internet HTTP connection");
    }
    return Response.ok(String.valueOf(false)).build();
  }
}
