// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   TermsAndConditionsServiceImpl.java

package org.exoplatform.welcomescreens.service.impl;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.welcomescreens.service.TermsAndConditionsService;
import org.exoplatform.welcomescreens.service.utils.NodeUtils;

import javax.jcr.Node;

import org.apache.commons.lang3.StringUtils;

import static java.util.Objects.isNull;

/**
 * This service is used to manage Terms and conditions
 * 
 * @author Clement
 */
public class TermsAndConditionsServiceImpl implements TermsAndConditionsService {

  private static final Log     LOG                       = ExoLogger.getLogger(TermsAndConditionsServiceImpl.class);

  private static final String  TERMS_AND_CONDITIONS      = "TermsAndConditions";

  private static final Context CONTEXT                   = Context.GLOBAL.id(TERMS_AND_CONDITIONS);

  private static final Scope   SCOPE                     = Scope.APPLICATION.id(TERMS_AND_CONDITIONS);

  private PortalContainer      container;

  private SettingService       settingService;

  private boolean              hasTermsAndConditionsNode = false;

  private OrganizationService organizationService;

  private UserACL userACL;

  private static final String TERMS_AND_CONDITONS_PROPERTY = "acceptedTermsAndConditions";

  private static final String WEB_CONTENT_URL = "/sites/shared/web contents/site artifacts/terms/terms";

  public TermsAndConditionsServiceImpl(PortalContainer container, SettingService settingService,OrganizationService organizationService,
                                       UserACL userACL) {
    this.container = container;
    this.settingService = settingService;
    this.organizationService = organizationService;
    this.userACL = userACL;
  }

  /*
   * ======================================================================= API
   * public methods
   * ======================================================================
   */
  public boolean isTermsAndConditionsChecked() {
    boolean isChecked = false;
    if (hasTermsAndConditions()) {
      isChecked = true;
    }
    return isChecked;
  }

  public void checkTermsAndConditions() {
    if (!hasTermsAndConditions()) {
      createTermsAndConditions();
    } else {
      LOG.debug("Terms and conditions: yet checked");
    }
  }

  @Override
  public boolean isTermsAndConditionsAcceptedBy(String userName) {
    if(userACL.isSuperUser()) {
      return true;
    }
    try {
      UserProfile userProfile = findUserProfileByUserName(userName);
      String acceptedTermsVersion = userProfile.getAttribute(TERMS_AND_CONDITONS_PROPERTY);
      String currentVersion = getCurrentTermsAndConditionsVersion();
      return StringUtils.equals(currentVersion, acceptedTermsVersion);
    } catch (Exception e) {
      LOG.warn("Error while checking Terms and conditions accepted for user: " + userName + " - considered as not accepted", e);
      return false;
    }
  }

  @Override
  public String getCurrentTermsAndConditionsVersion() {
    final String DEFAULT_DRAFT_VERSION_NAME = "jcr:rootVersion";
    Node termsAndConditionsNode = NodeUtils.findCollaborationFile(WEB_CONTENT_URL);
    try {
      String name = termsAndConditionsNode.getBaseVersion().getName();
      if (DEFAULT_DRAFT_VERSION_NAME.equals(name)) {
        return null;
      }
      return name;
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public void accept(String userName) {
    RequestLifeCycle.begin(PortalContainer.getInstance());
    try {
      UserProfile userProfile = findUserProfileByUserName(userName);
      String currentVersionName = getCurrentTermsAndConditionsVersion();
      userProfile.setAttribute(TERMS_AND_CONDITONS_PROPERTY, currentVersionName);
      organizationService.getUserProfileHandler().saveUserProfile(userProfile, false);
    } catch (Exception e) {
      LOG.error("Cannot update user profile to store terms and conditions acceptation", e);
    } finally {
      RequestLifeCycle.end();
    }
  }

  @Override
  public UserProfile findUserProfileByUserName(String userName) throws Exception {
    User user = organizationService.getUserHandler().findUserByName(userName);
    if (isNull(user)) {
      return null;
    }
    UserProfile userProfile = organizationService.getUserProfileHandler().findUserProfileByName(userName);
    if(userProfile == null) {
      userProfile = organizationService.getUserProfileHandler().createUserProfileInstance(userName);
    }
    return userProfile;
  }
  /*
   * ======================================================================= API
   * private methods
   * ======================================================================
   */
  private void createTermsAndConditions() {
    RequestLifeCycle.begin(container);
    try {
      settingService.set(CONTEXT, SCOPE, TERMS_AND_CONDITIONS, SettingValue.create(true));
    } catch (Exception e) {
      LOG.error("Terms and conditions: cannot save information", e);
    } finally {
      RequestLifeCycle.end();
    }
  }

  private boolean hasTermsAndConditions() {
      // --- Initial hasTermsAndConditionsNode is false we need to get flag from store
      if (hasTermsAndConditionsNode) {
        return true;
      } else {
        RequestLifeCycle.begin(container);
        try {
          // --- Get The session Provider
          SettingValue<?> value = settingService.get(CONTEXT, SCOPE, TERMS_AND_CONDITIONS);
          hasTermsAndConditionsNode = value != null && value.getValue() != null;
        } catch (Exception E) {
          LOG.error("Terms and conditions: connot get information from store", E);
          hasTermsAndConditionsNode = false;
        } finally {
          RequestLifeCycle.end();
        }
        return hasTermsAndConditionsNode;
      }
  }
}
