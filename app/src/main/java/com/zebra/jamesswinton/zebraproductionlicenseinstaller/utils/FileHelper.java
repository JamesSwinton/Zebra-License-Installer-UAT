package com.zebra.jamesswinton.zebraproductionlicenseinstaller.utils;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class FileHelper {
  private static final String LicenceFilePath = "/enterprise/usr/licence.txt";

  public static boolean licenceFileAvailable() {
    return new File("/enterprise/usr/licence.txt").exists();
  }

  public static boolean binFileAvailable() {
    return new File("/sdcard/Download/licence.bin").exists();
  }

  @Nullable
  public static ArrayList<String> getActivationIdsFromLicenceFile(Context cx) {
    try {
      return readFileLines(cx, LicenceFilePath);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  @NonNull
  private static ArrayList<String> readFileLines(Context cx, String filePath) throws IOException {
    ArrayList<String> contents = new ArrayList<>();
    File inputFile = new File(filePath);
    if (inputFile.exists()) {
      InputStream inputStream = new FileInputStream(inputFile);
      InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
      BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
      String receiveString = "";
      while ((receiveString = bufferedReader.readLine()) != null) {
        contents.add(receiveString);
      } inputStream.close();
    } else {
      throw new FileNotFoundException("Could not get intput stream for path: " + filePath);
    }
    if (contents.isEmpty()) {
      throw new IOException("No data found in file: " + filePath);
    }
    return contents;
  }
}
