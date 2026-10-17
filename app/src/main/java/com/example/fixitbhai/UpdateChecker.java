package com.example.fixitbhai;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpdateChecker {

    private static final String TAG = "UpdateChecker";
    private static final String GITHUB_RELEASES_API = "https://api.github.com/repos/ShashankS1011/FixitBhai/releases/latest";

    public interface UpdateCheckCallback {
        void onCheckComplete(boolean hasUpdate, String latestTag, String downloadUrl);
    }

    public static void checkForUpdates(Context context, boolean showToastIfLatest, UpdateCheckCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            boolean hasUpdate = false;
            String latestTag = "";
            String downloadUrl = "";

            try {
                URL url = new URL(GITHUB_RELEASES_API);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
                conn.setRequestProperty("User-Agent", "FixitBhai-App");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                if (conn.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();

                    JSONObject responseJson = new JSONObject(sb.toString());
                    latestTag = responseJson.optString("tag_name", "").replace("v", "").trim();

                    JSONArray assets = responseJson.optJSONArray("assets");
                    if (assets != null && assets.length() > 0) {
                        for (int i = 0; i < assets.length(); i++) {
                            JSONObject asset = assets.getJSONObject(i);
                            if (asset.optString("name", "").contains(".apk")) {
                                downloadUrl = asset.optString("browser_download_url", "");
                                break;
                            }
                        }
                    }
                    if (downloadUrl.isEmpty()) {
                        downloadUrl = responseJson.optString("html_url", "https://github.com/ShashankS1011/FixitBhai/releases/latest");
                    }

                    String currentVersion = BuildConfig.VERSION_NAME.replace("v", "").trim();
                    hasUpdate = isVersionGreater(latestTag, currentVersion);
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Update check failed: " + e.getMessage());
            }

            final boolean finalHasUpdate = hasUpdate;
            final String finalLatestTag = latestTag;
            final String finalDownloadUrl = downloadUrl;

            handler.post(() -> {
                if (callback != null) {
                    callback.onCheckComplete(finalHasUpdate, finalLatestTag, finalDownloadUrl);
                }

                if (finalHasUpdate) {
                    showUpdateDialog(context, finalLatestTag, finalDownloadUrl);
                } else if (showToastIfLatest) {
                    Toast.makeText(context, "FixitBhai is up to date! (v" + BuildConfig.VERSION_NAME + ")", Toast.LENGTH_SHORT).show();
                }
            });

            executor.shutdown();
        });
    }

    private static boolean isVersionGreater(String remote, String local) {
        if (remote == null || remote.isEmpty() || local == null || local.isEmpty()) return false;
        try {
            String[] rParts = remote.split("\\.");
            String[] lParts = local.split("\\.");
            int length = Math.max(rParts.length, lParts.length);
            for (int i = 0; i < length; i++) {
                int rVal = i < rParts.length ? Integer.parseInt(rParts[i].replaceAll("[^0-9]", "")) : 0;
                int lVal = i < lParts.length ? Integer.parseInt(lParts[i].replaceAll("[^0-9]", "")) : 0;
                if (rVal > lVal) return true;
                if (rVal < lVal) return false;
            }
        } catch (Exception ignored) {}
        return false;
    }

    public static void showUpdateDialog(Context context, String versionTag, String downloadUrl) {
        new MaterialAlertDialogBuilder(context)
                .setTitle("New Update Available (v" + versionTag + ")")
                .setMessage("A new version of FixitBhai is available on GitHub with improvements and bug fixes!\n\nWould you like to download the update now?")
                .setPositiveButton("Download Update", (dialog, which) -> {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
                        context.startActivity(intent);
                    } catch (Exception ignored) {}
                })
                .setNegativeButton("Later", (dialog, which) -> dialog.dismiss())
                .show();
    }
}