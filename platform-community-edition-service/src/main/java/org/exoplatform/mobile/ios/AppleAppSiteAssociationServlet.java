/*
 * Copyright (C) 2021 eXo Platform SAS
 *
 *  This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <gnu.org/licenses>.
 */
package org.exoplatform.mobile.ios;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.manager.IdentityManagerImpl;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;

public class AppleAppSiteAssociationServlet extends HttpServlet {

  private static final Log LOG                 = ExoLogger.getExoLogger(AppleAppSiteAssociationServlet.class);

  private static String    DEFAULT_IOS_APP_IDS =
                                               "FEM998T42U.com.exoplatform.mob.eXoPlatformiPHone,FEM998T42U.org.exoplatform.exo-snapshot,FEM998T42U.org.exoplatform.exo-beta";

  private static String    DEFAULT_APP_URLS    = "/,/portal/*";

  @Override
  public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {

    JSONObject appleAppSiteAssociationJson = new JSONObject();
    JSONObject webCredentials = new JSONObject();
    JSONObject appLinks = new JSONObject();
    try {
      String appIds = PropertyManager.getProperty("exo.mobile.ios.appIds");
      if (appIds != null) {
        appIds = DEFAULT_IOS_APP_IDS + "," + appIds;
      } else {
        appIds = DEFAULT_IOS_APP_IDS;
      }

      String appURLs = PropertyManager.getProperty("exo.mobile.ios.appURLs");
      if(appURLs != null) {
        appURLs = DEFAULT_APP_URLS + "," + appURLs;
      } else {
        appURLs = DEFAULT_APP_URLS;
      }
      String[] appIdsArray = new String[] {};
      String[] appURLsArray = new String[] {};
      if (!StringUtils.isBlank(appIds)) {
        appIdsArray = appIds.split(",");
      }
      if (!StringUtils.isBlank(appURLs)) {
        appURLsArray = appURLs.split(",");
      }
      webCredentials.put("apps", appIdsArray);
      JSONArray appsDetails = new JSONArray();
      for (String appId : appIdsArray) {
        JSONObject app = new JSONObject();
        app.put("appId", appId);
        app.put("paths", appURLsArray);
        appsDetails.put(app);
      }
      appLinks.put("details", appsDetails);
      appLinks.put("apps", List.of());
      appleAppSiteAssociationJson.put("appLinks", appLinks);
      appleAppSiteAssociationJson.put("webcredentials", webCredentials);

      PrintWriter out = res.getWriter();
      res.setContentType("application/json");
      res.setCharacterEncoding("UTF-8");
      out.print(appleAppSiteAssociationJson);
      out.flush();
    } catch (JSONException e) {
      LOG.error("Problem when creating JSON for Apple Application & site association file !", e);
    }
  }
}
