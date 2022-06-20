package org.exoplatform.software.register.service.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.info.PlatformInformationRESTService;
import org.exoplatform.commons.info.PlatformInformationRESTService.JsonPlatformInfo;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.software.register.UnlockService;
import org.exoplatform.software.register.model.SoftwareRegistration;
import org.exoplatform.software.register.service.SoftwareRegistrationService;
import org.exoplatform.software.settings.ExoProductInformationSetting;

/**
 * Created by The eXo Platform SEA Author : eXoPlatform toannh@exoplatform.com
 * On 9/30/15 Implement methods of SoftwareRegistrationService interface
 */
public class SoftwareRegistrationServiceImpl implements SoftwareRegistrationService {

  private static final Log               LOG                      = ExoLogger.getLogger(SoftwareRegistrationServiceImpl.class);

  private String                         softwareRegistrationHost = SOFTWARE_REGISTRATION_HOST_DEFAULT;

  private PlatformInformationRESTService platformInformationRESTService;

  private ExoProductInformationSetting   productInformationSetting;

  private SettingService                 settingService;

  private UnlockService                  unlockService;

  private boolean                        isSkipRequest;

  private int                            allowedSkips             = 0;

  public SoftwareRegistrationServiceImpl(PlatformInformationRESTService platformInformationRESTService,
                                         ExoProductInformationSetting productInformationSetting,
                                         SettingService settingService,
                                         InitParams initParams,
                                         UnlockService unlockService) {
    this.platformInformationRESTService = platformInformationRESTService;
    this.productInformationSetting = productInformationSetting;
    this.settingService = settingService;
    this.unlockService = unlockService;
    if (initParams != null) {
      if (initParams.getValueParam(SOFTWARE_REGISTRATION_HOST) != null) {
        this.softwareRegistrationHost = initParams.getValueParam(SOFTWARE_REGISTRATION_HOST).getValue();
      }
      if (initParams.getValueParam(SOFTWARE_REGISTRATION_SKIP) != null) {
        String skipPLFRegister = initParams.getValueParam(SOFTWARE_REGISTRATION_SKIP).getValue();
        isSkipRequest = skipPLFRegister != null && StringUtils.equals("true", skipPLFRegister.trim());
      }
      if (initParams.getValueParam(SOFTWARE_REGISTRATION_SKIP_ALLOW) != null) {
        allowedSkips = Integer.parseInt(initParams.getValueParam(SOFTWARE_REGISTRATION_SKIP_ALLOW).getValue());
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SoftwareRegistration registrationPLF(String code, String returnURL) {
    String url = softwareRegistrationHost + "/portal/accessToken";
    SoftwareRegistration softwareRegistration = new SoftwareRegistration();
    try {
      int responseCode = 0;
      StringBuilder result = new StringBuilder();
      try (DefaultHttpClient client = new DefaultHttpClient()) {
        HttpPost post = new HttpPost(url);
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("grant_type", "authorization_code"));
        urlParameters.add(new BasicNameValuePair("code", code));
        urlParameters.add(new BasicNameValuePair("redirect_uri", returnURL));
        urlParameters.add(new BasicNameValuePair("client_id", "x6iCo6YWmw"));
        urlParameters.add(new BasicNameValuePair("client_secret", "3XNzbpuTSx5HqJsBSwgl"));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        HttpResponse response = client.execute(post);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line = "";
        while ((line = rd.readLine()) != null) {
          result.append(line);
        }
        responseCode = response.getStatusLine().getStatusCode();
      }

      JSONObject responseData = new JSONObject(result.toString());
      if (responseCode == HTTPStatus.OK) {
        String accessToken = responseData.getString("access_token");
        softwareRegistration.setAccess_token(accessToken);
        boolean pushInfo = sendInformation(accessToken);
        softwareRegistration.setPushInfo(pushInfo);
      } else {
        String errorCode = responseData.getString("error");
        softwareRegistration.setError_code(errorCode);
      }

      return softwareRegistration;
    } catch (Exception ex) {
      softwareRegistration.setNotReachable(true);
    }
    return softwareRegistration;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void updateSkippedNumber() {
    settingService.set(Context.GLOBAL,
                       Scope.GLOBAL,
                       SOFTWARE_REGISTRATION_SKIPPED,
                       new SettingValue<Object>(String.valueOf(getSkippedNumber() + 1)));
  }

  @Override
  public boolean canSkipRegister() {
    return unlockService.isUnlocked() || getSkippedNumber() <= allowedSkips;
  }

  @Override
  public boolean canShowSkipBtn() {
    return unlockService.isUnlocked() || getSkippedNumber() < allowedSkips;
  }

  private int getSkippedNumber() {
    SettingValue<?> settingValue = settingService.get(Context.GLOBAL, Scope.GLOBAL, SOFTWARE_REGISTRATION_SKIPPED);
    if (settingValue != null) {
      return Integer.parseInt(settingValue.getValue().toString());
    } else {
      settingService.set(Context.GLOBAL, Scope.GLOBAL, SOFTWARE_REGISTRATION_SKIPPED, new SettingValue<Object>("0"));
    }
    return 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isSoftwareRegistered() {
    String currStatus = productInformationSetting.getCurrentStatus();
    boolean plfRegistrationStatus = currStatus != null
        && currStatus.contains(platformInformationRESTService.getPlatformEdition().concat("-true"));

    String currVersions = productInformationSetting.getCurrentVersions();
    boolean plfVersionRegistrationStatus = currVersions != null
        && currVersions.contains(platformInformationRESTService.getJsonPlatformInfo()
                                                               .getPlatformVersion());
    return plfRegistrationStatus && plfVersionRegistrationStatus;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void checkSoftwareRegistration() {
    // Persisted registration status on local
    productInformationSetting.addNewStatus(platformInformationRESTService.getPlatformEdition());
    productInformationSetting.addNewVersion(platformInformationRESTService.getJsonPlatformInfo().getPlatformVersion());
  }

  /**
   * {@inheritDoc}
   */
  private boolean sendInformation(String accessTokencode) {
    try {
      String url = softwareRegistrationHost + "/portal/rest/registerSoftware/register";
      HttpClient client = new DefaultHttpClient();
      HttpPost httpPost = new HttpPost(url);

      JsonPlatformInfo jsonPlatformInfo = platformInformationRESTService.getJsonPlatformInfo();
      JSONObject jsonObj = new JSONObject(jsonPlatformInfo);

      String input = jsonObj.toString();

      httpPost.setHeader("Accept", "application/json");
      httpPost.setHeader("Content-type", "application/json");
      httpPost.setHeader("Authorization", "Bearer " + accessTokencode);
      httpPost.setEntity(new StringEntity(input));

      HttpResponse response = client.execute(httpPost);

      if (response.getStatusLine().getStatusCode() != 200) {
        LOG.warn("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
        return false;
      }
      return true;
    } catch (Exception e) {
      LOG.warn("Can not send Platform information to eXo community", e);
      return false;
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getSoftwareRegistrationHost() {
    return softwareRegistrationHost;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isSkipRequest() {
    return isSkipRequest;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setSkipRequest(boolean isRequestSkip) {
    this.isSkipRequest = isRequestSkip;
  }

}
