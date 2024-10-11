package com.zegocloud.zimkit.components.album.browserimage.download;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.provider.MediaStore;
import androidx.annotation.RequiresApi;
import com.zegocloud.zimkit.common.glide.ZIMKitGlideLoader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class GlideDownloadHelper {

    private static Context appContext;
    private static DownloadQueue downloadQueue;

    /**
     * Initialization
     *
     * @param context
     */
    public static void init(Context context) {
        if (appContext == null) {
            appContext = context.getApplicationContext();
            downloadQueue = new DownloadQueue();
            downloadQueue.start();
        }
    }

    public static DownloadTask with(String url) {
        return new DownloadTaskImpl(appContext).setUrl(url);
    }

    //    private static String getCacheDir() {
    //        File dir = new File(rootPath);
    //        if (!dir.exists()) {
    //            dir.mkdirs();
    //        }
    //        return dir.getAbsolutePath();
    //    }

    public interface DownloadListener {

        void success(String url, String localFilePath, String localFileName);

        void error(String url, Exception e);
    }

    private static class DownloadTaskImpl implements WorkTask, DownloadTask {

        private static final Handler mainThread = new Handler(Looper.getMainLooper());
        private Context context;
        private volatile boolean isCancel;
        private String url;
        private String suffix = "png";
        private SoftReference<DownloadListener> softReference;

        public DownloadTaskImpl(Context context) {
            this.context = context;
        }

        @Override
        public void download() throws InterruptedException, ExecutionException, IOException {
            if (isCancel) {
                return;
            }
            PackageManager pm = context.getPackageManager();
            ApplicationInfo applicationInfo = context.getApplicationContext().getApplicationInfo();
            String appName = pm.getApplicationLabel(applicationInfo).toString();

            String fileName = System.currentTimeMillis() + "_zimkit." + suffix;
            File originFile = startDownload();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Api29Impl.saveToSystem(context, originFile, fileName, suffix, appName);
            } else {
                ApiBelow.saveToSystem(context, originFile, fileName, suffix, appName);
            }
            FileUtils.deleteFile(originFile);
            if (isCancel) {
                return;
            }
            mainThread.post(() -> {
                if (getListener() != null) {
                    getListener().success(url, "", fileName);
                }
            });
        }

        @Override
        public void deliverError(Exception e) {
            if (isCancel) {
                return;
            }
            mainThread.post(() -> {
                if (getListener() != null) {
                    getListener().error(url, e);
                }
            });
        }

        @Override
        public void cancel() {
            isCancel = true;
            softReference = null;
        }

        private DownloadListener getListener() {
            return softReference != null ? softReference.get() : null;
        }

        @Override
        public DownloadTask setUrl(String url) {
            this.url = url;
            return this;
        }

        @Override
        public DownloadTask setSuffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        @Override
        public DownloadTask setListener(DownloadListener listener) {
            this.softReference = new SoftReference<>(listener);
            return this;
        }

        @Override
        public DownloadTask execute() {
            downloadQueue.add(this);
            return this;
        }

        private File startDownload() throws InterruptedException, ExecutionException {
            return ZIMKitGlideLoader.downloadNetWorkResource(context, url);
        }
    }

    static class ApiBelow {

        public static void saveToSystem(Context context, File sourceFile, String fileName, String extension,
            String relativePath) {
            // Add a media item that other apps don't see until the item is
            // fully written to the media store.
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            String mimeType = null;
            if ("jpg".equals(extension) || "jpeg".equals(extension)) {
                mimeType = "image/jpeg";
            } else if ("png".equals(extension)) {
                mimeType = "image/png";
            }
            values.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator
                + relativePath;
            values.put(MediaStore.MediaColumns.DATA, path + File.separator + fileName);

            ContentResolver contentResolver = context.getApplicationContext().getContentResolver();
            Uri imageCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri imageUri = contentResolver.insert(imageCollection, values);

            //            "w" for write.
            try (ParcelFileDescriptor pfd = contentResolver.openFileDescriptor(imageUri, "w", null)) {
                // Write data into the pending audio file.
                FileUtils.copy(sourceFile, pfd.getFileDescriptor());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Now that you're finished, release the "pending" status and let other apps
            // play the audio track.
            values.clear();

            // Final notification of gallery update
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imageUri));
        }
    }

    @RequiresApi(29)
    static class Api29Impl {

        static void saveToSystem(Context context, File sourceFile, String fileName, String extension,
            String relativePath) {
            // Add a media item that other apps don't see until the item is
            // fully written to the media store.
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            String mimeType = null;
            if ("jpg".equals(extension) || "jpeg".equals(extension)) {
                mimeType = "image/jpeg";
            } else if ("png".equals(extension)) {
                mimeType = "image/png";
            }
            values.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
            values.put(MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + File.separator + relativePath);

            Uri imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            ContentResolver contentResolver = context.getApplicationContext().getContentResolver();
            Uri imageUri = contentResolver.insert(imageCollection, values);

            //            "w" for write.
            try (ParcelFileDescriptor pfd = contentResolver.openFileDescriptor(imageUri, "w", null)) {
                // Write data into the pending audio file.
                FileUtils.copy(sourceFile, pfd.getFileDescriptor());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Now that you're finished, release the "pending" status and let other apps
            // play the audio track.
            values.clear();

            values.put(MediaStore.Audio.Media.IS_PENDING, 0);
            contentResolver.update(imageUri, values, null, null);
        }
    }

    private static class FileUtils {

        public static void copy(File source, FileDescriptor target) {
            FileInputStream fileInputStream = null;
            FileOutputStream fileOutputStream = null;
            try {
                fileInputStream = new FileInputStream(source);
                fileOutputStream = new FileOutputStream(target);
                byte[] buffer = new byte[1024];
                while (fileInputStream.read(buffer) > 0) {
                    fileOutputStream.write(buffer);
                }
            } catch (Exception e) {

            } finally {
                try {
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public static void deleteFile(File file) {
            if (file == null || !file.exists()) {
                return;
            }
            if (file.isFile()) {
                file.delete();
            }
        }
    }

    /**
     * Download Task Queue
     */
    private static class DownloadQueue implements Runnable {

        private static final LinkedBlockingQueue<WorkTask> netQueue = new LinkedBlockingQueue<>();
        private static final int thread_size = 3;
        private ExecutorService threadPool = Executors.newFixedThreadPool(thread_size);
        private static volatile boolean isStop;

        public DownloadQueue start() {
            for (int i = 0; i < thread_size; ++i) {
                threadPool.execute(this);
            }
            return this;
        }

        @Override
        public void run() {
            //Set the thread priority, here is the background thread
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            WorkTask task;
            while (true) {
                try {
                    task = netQueue.take();
                } catch (InterruptedException e) {
                    if (isStop) {
                        return;
                    }
                    continue;
                }
                try {
                    task.download();
                } catch (Exception e) {
                    e.printStackTrace();
                    task.deliverError(e);
                }
            }
        }

        public void cancelAll() {
            isStop = true;
            try {
                threadPool.shutdownNow();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public DownloadQueue add(WorkTask downloadTask) {
            netQueue.add(downloadTask);
            return this;
        }
    }

    public interface WorkTask {

        void download() throws InterruptedException, ExecutionException, IOException;

        void deliverError(Exception e);
    }

    public interface DownloadTask {

        void cancel();

        DownloadTask setUrl(String url);

        DownloadTask setSuffix(String suffix);

        DownloadTask setListener(DownloadListener listener);

        DownloadTask execute();
    }

}
