package org.exoplatform.software.settings;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.picocontainer.Startable;

import org.exoplatform.commons.info.PlatformInformationRESTService;
import org.exoplatform.commons.info.ProductInformations;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.software.register.Utils;

public class ExoProductInformationSetting implements Startable {
  private static final Log               LOG             = ExoLogger.getLogger(ExoProductInformationSetting.class);

  public static final String             USER_HOME       = System.getProperty("user.home");

  public static final String             EXO_HOME_FOLDER = USER_HOME + "/.eXo";

  private ProductInformations            productInformations;

  private PlatformInformationRESTService platformInformationRESTService;

  private String                         homeConfigFileLocation;

  private String                         homeConfigLocation;

  private String                         licensePath;

  public ExoProductInformationSetting(ProductInformations productInformations,
                                      PlatformInformationRESTService platformInformationRESTService,
                                      InitParams params) {
    this.productInformations = productInformations;
    this.platformInformationRESTService = platformInformationRESTService;
    this.licensePath = params != null
        && params.getValueParam("exo.license.path") != null ? params.getValueParam("exo.license.path").getValue() : null;
    this.homeConfigLocation = EXO_HOME_FOLDER + "/" + Utils.PRODUCT_NAME;
    this.homeConfigFileLocation = this.homeConfigLocation + "/" + Utils.LICENSE_FILE;
  }

  @Override
  public void start() {
    if (StringUtils.isNotBlank(licensePath) && !StringUtils.equals(licensePath, this.homeConfigFileLocation)) {
      checkCustomizeFolder(licensePath);
    }
  }

  @Override
  public void stop() {
    // NOP
  }

  public String getCurrentStatus() {
    return Utils.readFromFile(Utils.SW_REG_STATUS, this.homeConfigFileLocation);
  }

  public String getCurrentVersions() {
    String currentVersionKey = platformInformationRESTService.getPlatformEdition()
                                                             .concat("-")
                                                             .concat(Utils.SW_REG_PLF_VERSION);
    return Utils.readFromFile(currentVersionKey, this.homeConfigFileLocation);
  }

  public void addNewStatus(String status) {
    String currentRegStatus = getCurrentStatus();
    if (StringUtils.isEmpty(currentRegStatus)) {
      currentRegStatus = status.concat("-true");
    } else if (!currentRegStatus.contains(status.concat("-true"))) {
      currentRegStatus = currentRegStatus.concat(",").concat(status.concat("-true"));
    }
    Utils.writeToFile(Utils.SW_REG_STATUS, currentRegStatus, this.homeConfigFileLocation);
  }

  public void addNewVersion(String newVersion) {
    String currentVersionKey = platformInformationRESTService.getPlatformEdition()
                                                             .concat("-")
                                                             .concat(Utils.SW_REG_PLF_VERSION);

    String currentVersions = getCurrentVersions();
    String newVersions = StringUtils.isBlank(currentVersions) ? newVersion : (currentVersions + "," + newVersion);
    Utils.writeToFile(currentVersionKey, newVersions, this.homeConfigFileLocation);
  }

  public String getPlatformEdition() {
    try {
      String platformEdition = ExoContainer.hasProfile(Utils.COMMUNITY_EDITION) ? Utils.COMMUNITY_EDITION
                                                                                : Utils.ENTERPRISE_EDITION;
      if (platformEdition.equals(Utils.ENTERPRISE_EDITION) && StringUtils.isNotBlank(productInformations.getEdition())) {
        platformEdition = productInformations.getEdition();
      }
      return platformEdition;
    } catch (Exception e) {
      LOG.error("An error occurred while getting the platform edition information.", e);
    }
    return null;
  }

  public String getFileLocation() {
    String edition = getPlatformEdition();
    if (edition != null && edition.equals(PlatformInformationRESTService.COMMUNITY_EDITION)) {
      return this.homeConfigLocation + "/" + PlatformInformationRESTService.COMMUNITY_EDITION + ".xml";
    }
    return this.homeConfigFileLocation;
  }

  /**
   * Check and update customize path
   * 
   * @param lisensePath
   */
  private void checkCustomizeFolder(String lisensePath) {
    File lisenseFile = new File(lisensePath);
    if (!StringUtils.endsWith(lisensePath, Utils.LICENSE_FILE)) {
      if (lisenseFile.exists() && lisenseFile.mkdirs()) {
        LOG.error("The customize lisense.xml path cannot be use, default value will be applied.");
        return;
      }
      if (lisenseFile.isFile()) {
        if (lisenseFile.canWrite()) {
          this.homeConfigLocation = lisenseFile.getParent();
          this.homeConfigFileLocation = lisenseFile.getPath();
        }
      } else {
        this.homeConfigLocation = lisenseFile.getPath();
        this.homeConfigFileLocation = this.homeConfigLocation + "/" + Utils.LICENSE_FILE;
      }
    } else {
      if ((lisenseFile.getParentFile().exists() && lisenseFile.canWrite())
          || lisenseFile.getParentFile().mkdirs()) {
        this.homeConfigLocation = lisenseFile.getParent();
        this.homeConfigFileLocation = lisenseFile.getPath();
      }
    }
  }

}
