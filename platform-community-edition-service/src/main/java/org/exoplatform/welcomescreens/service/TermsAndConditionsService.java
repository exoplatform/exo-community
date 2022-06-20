package org.exoplatform.welcomescreens.service;

import org.exoplatform.services.organization.UserProfile;

/**
 * Service used to manage Terms And Conditions
 * 
 * @author Clement
 */
public interface TermsAndConditionsService {
  public abstract boolean isTermsAndConditionsChecked();

  public abstract void checkTermsAndConditions();

  public abstract boolean isTermsAndConditionsAcceptedBy(String userName);

  public abstract String getCurrentTermsAndConditionsVersion();

  public abstract void accept(String userName);

  public abstract UserProfile findUserProfileByUserName(String userName) throws Exception;
}
