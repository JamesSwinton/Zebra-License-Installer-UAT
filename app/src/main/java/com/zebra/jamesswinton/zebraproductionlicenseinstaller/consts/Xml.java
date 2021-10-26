package com.zebra.jamesswinton.zebraproductionlicenseinstaller.consts;

public class Xml {

  public enum LicenceCloud { UAT, Production };

  public static final String PreactivatedLicenceXml =
      "<wap-provisioningdoc>\n" +
          "  <characteristic version=\"8.1\" type=\"LicenseMgr\">\n" +
          "    <parm name=\"LicenseChoice\" value=\"zebra\" />\n" +
          "    <characteristic type=\"LicenseZebra\">\n" +
          "      <parm name=\"LicenseActionZebra\" value=\"activate\" />\n" +
          "      <characteristic type=\"NewLicenseZebra\">\n" +
          "        <characteristic type=\"LicenseSourceURL\">\n" +
          "          <parm name=\"LicenseSource\" value=\"Preactivated\" />\n" +
          "          <parm name=\"PreactivatedLicenseSource\" value=\"1\" />\n" +
          "          <parm name=\"PreactivatedLicenseMethod\" value=\"reference\" />\n" +
          "        </characteristic>\n" +
          "        <parm name=\"PreactivatedLicensePathAndFileName\" value=\"/sdcard/Download/licence.bin\" />\n" +
          "      </characteristic>\n" +
          "    </characteristic>\n" +
          "  </characteristic>\n" +
          "</wap-provisioningdoc>";

  public static String getActivationIdXml(LicenceCloud licenceCloud, String activationID) {
    return
        "<wap-provisioningdoc>\n" +
        "  <characteristic version=\"8.1\" type=\"LicenseMgr\">\n" +
        "    <parm name=\"LicenseChoice\" value=\"zebra\" />\n" +
        "    <characteristic type=\"LicenseZebra\">\n" +
        "      <parm name=\"LicenseActionZebra\" value=\"activate\" />\n" +
        "      <characteristic type=\"NewLicenseZebra\">\n" +
        "        <characteristic type=\"LicenseSourceURL\">\n" +
        "          <parm name=\"LicenseSource\" value=\"Zebra Cloud\" />\n" +
        "          <parm name=\"LicenseCloudURL\" value=" + '"' + (licenceCloud == LicenceCloud.UAT ? 1 : 2) + '"' + "  />\n" +
        "        </characteristic>\n" +
        "        <parm name=\"ActivationID\" value=" + '"' + activationID + '"' + "  />\n" +
        "        <parm name=\"ActivationQuantity\" value=\"1\" />\n" +
        "      </characteristic>\n" +
        "    </characteristic>\n" +
        "  </characteristic>\n" +
        "</wap-provisioningdoc>\n";
  }

}
