package com.zebra.jamesswinton.zebraproductionlicenseinstaller;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKManager.EMDKListener;
import com.symbol.emdk.EMDKManager.FEATURE_TYPE;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.ProfileManager;
import com.zebra.jamesswinton.zebraproductionlicenseinstaller.consts.Xml;
import com.zebra.jamesswinton.zebraproductionlicenseinstaller.consts.Xml.LicenceCloud;
import com.zebra.jamesswinton.zebraproductionlicenseinstaller.profilemanager.ProcessProfileAsync;
import com.zebra.jamesswinton.zebraproductionlicenseinstaller.profilemanager.ProcessProfileAsync.OnProfileApplied;
import com.zebra.jamesswinton.zebraproductionlicenseinstaller.profilemanager.XmlParsingError;
import com.zebra.jamesswinton.zebraproductionlicenseinstaller.utils.ActivationMethod;
import com.zebra.jamesswinton.zebraproductionlicenseinstaller.utils.FileHelper;
import com.zebra.jamesswinton.zebraproductionlicenseinstaller.utils.PermissionsHelper;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements EMDKListener {

  // Profile Name
  private static final String ProfileName = "ProductionLicenseInstaller";

  // Permissions
  private PermissionsHelper mPermissionsHelper;

  // Activation ID & Mode
  private Pair<ActivationMethod, ArrayList<String>> mActivationDetails;
  private int currentLicenceIndex = 0;
  private boolean attemptedBinLicence = false;

  // EMDK
  private EMDKManager mEmdkManager;
  private ProfileManager mProfileManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Get Permissions (EMDK Initialised when granted)
    mPermissionsHelper = new PermissionsHelper(this, () -> {
      // Get Method & Keys
      mActivationDetails = getActivationDetails();

      // Init EMDK -> Profile applied when ready
      if (mActivationDetails.first != ActivationMethod.UNK) {
        initEmdk();
      } else {
        finish();
      }
    });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mEmdkManager != null) {
      mEmdkManager.release();
      mEmdkManager = null;
    }
  }

  @NonNull
  private Pair<ActivationMethod, ArrayList<String>> getActivationDetails() {
    String msg;
    ActivationMethod activationMethod;
    ArrayList<String> activationIds = FileHelper.getActivationIdsFromLicenceFile(this);

    if (activationIds == null || activationIds.isEmpty()) {
      if (FileHelper.binFileAvailable()) {
        activationMethod = ActivationMethod.BIN;
        msg = "licence.bin file found";
      } else {
        activationMethod = ActivationMethod.UNK;
        msg = "No license files found - exiting";
      }
    } else if (activationIds.size() >= 2) {
      activationMethod = ActivationMethod.TXT_MULTIPLE;
      msg = String.format("Multiple Activation IDs Found - Applying 1st Licence: %1$s",
          trimActivationId(activationIds.get(0)));
    } else {
      activationMethod = ActivationMethod.TXT_SINGLE;
      msg = String.format("Single Activation ID Found - Applying Licence: %1$s",
          trimActivationId(activationIds.get(0)));
    }

    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    return new Pair<>(activationMethod, activationIds);
  }

  private void initEmdk() {
    EMDKResults emdkManagerResults = EMDKManager.getEMDKManager(this, this);
    if (emdkManagerResults == null || emdkManagerResults.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
      Toast.makeText(MainActivity.this,"Could not obtain EMDKManager",
          Toast.LENGTH_LONG).show();
      finish();
    }
  }

  private String trimActivationId(String id) {
    return id.subSequence(0, 4).toString();
  }

  /**
   * EMDK Callbacks
   */

  @Override
  public void onOpened(EMDKManager emdkManager) {
    this.mEmdkManager = emdkManager;
    this.mProfileManager = (ProfileManager) mEmdkManager.getInstance(FEATURE_TYPE.PROFILE);

    // Null checks
    if (mActivationDetails.first == null || mActivationDetails.second == null) {
      return;
    }

    // Check for Licence
    switch(mActivationDetails.first) {
      case TXT_SINGLE:
      case TXT_MULTIPLE:
        new ProcessProfileAsync(ProfileName, mProfileManager, OnProfileApplied)
            .execute(Xml.getActivationIdXml(LicenceCloud.Production, mActivationDetails.second.get(0)));
        break;
      case BIN:
        new ProcessProfileAsync(ProfileName, mProfileManager, OnProfileApplied)
            .execute(Xml.PreactivatedLicenceXml);
        break;
    }
  }

  @Override
  public void onClosed() {
    // Release EMDK Manager Instance
    if (mEmdkManager != null) {
      mEmdkManager.release();
      mEmdkManager = null;
    }
  }

  private void showXmlParsingError(XmlParsingError... parsingErrors) {
    StringBuilder errsConcat = new StringBuilder("Error Processing XML!\n\n");
    for (int i = 0; i < parsingErrors.length; i++) {
      XmlParsingError err = parsingErrors[i];
      errsConcat.append(String.format("Error %1$s/%2$s", i + 1, parsingErrors.length));
      errsConcat.append("\n\n");
      errsConcat.append(String.format("Type: %1$s", err.getType()));
      errsConcat.append("\n");
      errsConcat.append(String.format("Desc: %1$s", err.getDescription()));
      errsConcat.append("\n\n");
    } Toast.makeText(this, errsConcat, Toast.LENGTH_LONG).show();
  }

  /**
   * Result
   */

  private final OnProfileApplied OnProfileApplied = new OnProfileApplied() {
    @Override
    public void profileApplied(String xml, EMDKResults emdkResults) {
      Toast.makeText(MainActivity.this, "License Installed!", Toast.LENGTH_SHORT)
          .show();
      finish();
    }

    @Override
    public void profileError(XmlParsingError... parsingErrors) {
      showXmlParsingError(parsingErrors);
      if (moreLicensesToApply()) {
        String licence = mActivationDetails.second.get(++currentLicenceIndex);
        Toast.makeText(MainActivity.this, String.format("Trying next licence (%1$s)", trimActivationId(licence)), Toast.LENGTH_SHORT).show();
        new ProcessProfileAsync(ProfileName, mProfileManager, OnProfileApplied).execute(Xml.getActivationIdXml(LicenceCloud.Production, licence));
      } else if (FileHelper.binFileAvailable() && !attemptedBinLicence) {
        attemptedBinLicence = true;
        Toast.makeText(MainActivity.this, "Install failed with all licences - Attempting to use BIN file", Toast.LENGTH_SHORT).show();
        new ProcessProfileAsync(ProfileName, mProfileManager, OnProfileApplied).execute(Xml.PreactivatedLicenceXml);
      } else {
        Toast.makeText(MainActivity.this, "Licence install failed & no .bin file found - exiting", Toast.LENGTH_SHORT).show();
        finish();
      }
    }
  };

  private boolean moreLicensesToApply() {
    if (mActivationDetails.second != null && !mActivationDetails.second.isEmpty()) {
      return currentLicenceIndex < (mActivationDetails.second.size() - 1);
    } else {
      return false;
    }
  }

  /**
   * Permissions
   */

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    mPermissionsHelper.onRequestPermissionsResult();
  }
}