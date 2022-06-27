package org.exoplatform.platform.edition;

import java.util.*;

import org.exoplatform.container.ExoProfileExtension;

public class EXOCommunityEdition implements ExoProfileExtension {
  @Override
  public Set<String> getProfiles() {
    return Collections.singleton("exo_community");
  }
}
