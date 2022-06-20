package org.exoplatform.software.register.web;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import org.exoplatform.commons.info.MissingProductInformationException;
import org.exoplatform.commons.info.ProductInformations;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.software.register.Utils;
import org.exoplatform.software.settings.ExoProductInformationSetting;

public class PingBackService {

  private static final Log             LOG                      = ExoLogger.getExoLogger(PingBackService.class);

  private static final String          LOOP_FUSE_FORM_DISPLAYED = "formDisplayed";

  private boolean                      loopfuseFormDisplayed    = false;

  private String                       pingBackUrl;

  private ExoProductInformationSetting productInformationSetting;

  private ProductInformations          productInformations;

  public PingBackService(ProductInformations productInformations,
                         ExoProductInformationSetting productInformationSetting,
                         InitParams initParams) {
    this.productInformationSetting = productInformationSetting;
    this.productInformations = productInformations;
    if (initParams != null) {
      ValueParam pingBackUrlValueParam = initParams.getValueParam("pingBackUrl");
      pingBackUrl = pingBackUrlValueParam != null ? pingBackUrlValueParam.getValue() : "";
    }
  }

  public boolean isConnectedToInternet() {
    String pingServerURL = pingBackUrl.substring(0, pingBackUrl.indexOf("/", "http://url".length()));
    try {
      URL url = new URL(pingServerURL);
      HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
      urlConn.connect();
      return (HttpURLConnection.HTTP_NOT_FOUND != urlConn.getResponseCode());
    } catch (IOException e) {
      LOG.warn("Error checking internet HTTP connection");
    }
    return false;
  }

  public boolean isLandingPageDisplayed() throws MissingProductInformationException {
    String loopfuseFormDisplayedString = readFromFile(LOOP_FUSE_FORM_DISPLAYED, productInformationSetting.getFileLocation());
    if (loopfuseFormDisplayedString != null && !loopfuseFormDisplayedString.isEmpty()) {
      loopfuseFormDisplayed = Boolean.parseBoolean(loopfuseFormDisplayedString);
      if (loopfuseFormDisplayed
          && Utils.ENTERPRISE_EDITION.equalsIgnoreCase(productInformationSetting.getPlatformEdition())) {
        return false;
      }
    }
    return loopfuseFormDisplayed;
  }

  private String readFromFile(String key, String fileLocation) throws MissingProductInformationException {
    if (fileLocation != null && !fileLocation.isEmpty() && !new File(fileLocation).exists()) {
      writePingBackFormDisplayed(false);
      return "false";
    }
    try {
      Properties properties = new Properties();
      InputStream inputStream = new FileInputStream(fileLocation);
      properties.loadFromXML(inputStream);
      inputStream.close();
      return (String) properties.get(key);
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  public void writePingBackFormDisplayed(boolean loopfuseFormDisplayed) throws MissingProductInformationException {
    this.loopfuseFormDisplayed = loopfuseFormDisplayed;
    writeToFile(LOOP_FUSE_FORM_DISPLAYED, Boolean.toString(loopfuseFormDisplayed), productInformationSetting.getFileLocation());
  }

  public String getPingBackUrl() {
    // --- Check the platform edition from systemfile then from internal
    // database
    String edition = productInformationSetting.getPlatformEdition();

    // --- If true then we are in the case of Enteprise license before first
    // start
    boolean enterpriseCheck = false;
    // --- Use ping back url corresponding to the current version of the server
    if (edition.equalsIgnoreCase(Utils.COMMUNITY_EDITION)) {
      // --- Concat the suffix "-ent"
      return pingBackUrl;
    } else {
      try {
        String loopfuseFormDisplayedString = readFromFile(LOOP_FUSE_FORM_DISPLAYED, productInformationSetting.getFileLocation());
        enterpriseCheck = Boolean.parseBoolean(loopfuseFormDisplayedString);
      } catch (Exception MissingProductInformationException) {
        LOG.error("Platform version detection : Error loading the version from FileSystem, the default value will be used");
      }
      if (enterpriseCheck) {
        return pingBackUrl = pingBackUrl.concat("-ent");
      } else {
        return pingBackUrl = pingBackUrl.concat("-ex");
      }
    }
  }

  private void writeToFile(String key, String value, String fileLocation) {
    if (fileLocation == null || fileLocation.isEmpty()) {
      throw new IllegalArgumentException("Illegal empty file Location parameter.");
    }
    InputStream inputStream = null;
    OutputStream outputStream = null;
    try {
      Properties properties = new Properties();
      File file = new File(fileLocation);
      if (file.exists()) {
        inputStream = new FileInputStream(fileLocation);
        properties.loadFromXML(inputStream);
        inputStream.close();
      } else {
        verifyAndCreateParentFolder(fileLocation);
      }
      properties.put(key, value);
      outputStream = new FileOutputStream(fileLocation);
      properties.storeToXML(outputStream, "");
      outputStream.close();
    } catch (Exception exception) {
      if (outputStream != null) {
        try {
          outputStream.close();
        } catch (IOException ioException) {
          LOG.error("Error during close outputStream ", ioException);
        }
      }
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException ioException) {
          LOG.error("Error during close inputStream ", ioException);
        }
      }
    }
  }

  private void verifyAndCreateParentFolder(String fileLocation) {
    String parentFolderPath = fileLocation.replace("\\", "/");
    int parentFolderPathEndIndex = fileLocation.lastIndexOf("/");
    if (parentFolderPathEndIndex >= 0) {
      parentFolderPath = fileLocation.substring(0, parentFolderPathEndIndex);
    }
    if (!new File(parentFolderPath).exists()) {
      new File(parentFolderPath).mkdirs();
    }
  }
}
