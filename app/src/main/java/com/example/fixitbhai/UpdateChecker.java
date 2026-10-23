package com.example.fixitbhai;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
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
                .setMessage("A new version of FixitBhai is ready with performance improvements and new features!\n\nWould you like to download and install the update now?")
                .setPositiveButton("Download & Install", (dialog, which) -> {
                    downloadAndInstallApk(context, downloadUrl, versionTag);
                })
                .setNegativeButton("Later", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public static void downloadAndInstallApk(Context context, String apkUrl, String versionTag) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.getPackageManager().canRequestPackageInstalls()) {
                    Toast.makeText(context, "Please allow FixitBhai to install app updates.", Toast.LENGTH_LONG).show();
                    Intent settingsIntent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                    settingsIntent.setData(Uri.parse("package:" + context.getPackageName()));
                    settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(settingsIntent);
                    return;
                }
            }

            Toast.makeText(context, "Downloading FixitBhai v" + versionTag + " update... Please grant installation permission when prompted. Download may take a few seconds.", Toast.LENGTH_LONG).show();

            String fileName = "FixitBhai-v" + versionTag + ".apk";
            File destinationFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName);
            if (destinationFile.exists()) {
                destinationFile.delete();
            }

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));
            request.setTitle("Downloading FixitBhai Update v" + versionTag);
            request.setDescription("Downloading latest release from GitHub...");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationUri(Uri.fromFile(destinationFile));
            request.setMimeType("application/vnd.android.package-archive");

            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (downloadManager == null) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl));
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(browserIntent);
                return;
            }

            long downloadId = downloadManager.enqueue(request);

            BroadcastReceiver onCompleteReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context ctx, Intent intent) {
                    long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    if (id == downloadId) {
                        try {
                            ctx.unregisterReceiver(this);
                        } catch (Exception ignored) {}

                        installApkFile(ctx, destinationFile);
                    }
                }
            };

            androidx.core.content.ContextCompat.registerReceiver(
                    context,
                    onCompleteReceiver,
                    new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                    androidx.core.content.ContextCompat.RECEIVER_EXPORTED
            );

        } catch (Exception e) {
            Log.e(TAG, "In-app download failed: " + e.getMessage(), e);
            try {
                Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl));
                fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(fallbackIntent);
            } catch (Exception ignored) {}
        }
    }

    public static void installApkFile(Context context, File apkFile) {
        try {
            if (!apkFile.exists()) {
                Toast.makeText(context, "Downloaded update file not found.", Toast.LENGTH_SHORT).show();
                return;
            }

            Uri apkUri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                apkUri = FileProvider.getUriForFile(
                        context,
                        context.getPackageName() + ".fileprovider",
                        apkFile
                );
            } else {
                apkUri = Uri.fromFile(apkFile);
            }

            Intent installIntent = new Intent(Intent.ACTION_VIEW);
            installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(installIntent);
            Toast.makeText(context, "Opening installer... Note: If package conflict occurs, please uninstall previous version once.", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Log.e(TAG, "Package installation failed", e);
            Toast.makeText(context, "Installation failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}