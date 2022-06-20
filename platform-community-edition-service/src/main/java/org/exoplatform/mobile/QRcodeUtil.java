package org.exoplatform.mobile;

public class QRcodeUtil {

  public static String androidDownloadUrl = "https://play.google.com/store/apps/details?id=org.exoplatform";

  public static String appleDownloadUrl   = "https://apps.apple.com/tn/app/exo/id410476273";

  public static boolean isAndroid(String userAgent) {
    String agent = userAgent.toLowerCase();

    if (agent.indexOf("android") != -1)
      return true;
    return false;
  }

  public static boolean isIphone(String userAgent) {
    String agent = userAgent.toLowerCase();

    if (agent.indexOf("iphone") != -1)
      return true;
    return false;
  }

  public boolean isMobile(String userAgent) {
    String agent = userAgent.toLowerCase();

    if (agent.indexOf("mobile") != -1)
      return true;
    return false;
  }

}
