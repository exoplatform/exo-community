package org.exoplatform.software.register.service;

import org.exoplatform.software.register.model.SoftwareRegistration;

/**
 * Created by The eXo Platform SEA Author : eXoPlatform toannh@exoplatform.com
 * On 9/30/15 Software register to Tribe service
 */
public interface SoftwareRegistrationService {

  public static final String SOFTWARE_REGISTRATION_NODE          = "softwareRegistrationNode";

  public static final String SOFTWARE_REGISTRATION_SKIPPED       = "softwareRegistrationSkipped";

  public static final String SOFTWARE_REGISTRATION_HOST_DEFAULT  = "https://community.exoplatform.com";

  public static final String SOFTWARE_REGISTRATION_PATH          = "/portal/authorize";

  public static final String SOFTWARE_REGISTRATION_RETURN_URL    = "http://{0}:{1}/welcome-screens/software-register-auth";

  public static final String SOFTWARE_REGISTRATION_CLIENT_ID     = "client_id=x6iCo6YWmw";

  public static final String SOFTWARE_REGISTRATION_RESPONSE_TYPE = "response_type=code";

  public static final String SOFTWARE_REGISTRATION_HOST          = "registration.host";

  public static final String SOFTWARE_REGISTRATION_SKIP          = "registration.skip";

  public static final String SOFTWARE_REGISTRATION_SKIP_ALLOW    = "registration.skipAllow";

  /**
   * Check has your software registered to Tribe
   * 
   * @return boolean value
   */
  public boolean isSoftwareRegistered();

  /**
   * Check is registered and create if not exist
   */
  public void checkSoftwareRegistration();

  /**
   * get Skipped number max is 2
   * 
   * @return
   */
  public boolean canSkipRegister();

  public boolean canShowSkipBtn();

  public void updateSkippedNumber();

  /**
   * Get access token from community side
   * 
   * @param code
   * @return
   */
  public SoftwareRegistration registrationPLF(String code, String returnURL);

  /**
   * Check configuration allow skip platform register
   * 
   * @return
   */
  public boolean isSkipRequest();

  public void setSkipRequest(boolean isRequestSkip);

  public String getSoftwareRegistrationHost();
}
