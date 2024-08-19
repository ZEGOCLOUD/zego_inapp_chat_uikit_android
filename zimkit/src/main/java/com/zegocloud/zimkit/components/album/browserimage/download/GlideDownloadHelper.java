package com.zegocloud.zimkit.components.album.browserimage.download;

import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;

import android.provider.MediaStore;
import com.zegocloud.zimkit.common.glide.ZIMKitGlideLoader;
import com.zegocloud.zimkit.components.album.MimeType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class GlideDownloadHelper {

    //Create a new folder of your own in the picture directory
    public static final String rootPath =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/zim";

    private static Context appContext;
    private static final String dir_name = "/zim";
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

    private static String getCacheDir() {
        File dir = new File(rootPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir.getAbsolutePath();
    }

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
            String fileName = System.currentTimeMillis() + "_zimkit." + suffix;
            File originFile = startDownload();
//            saveToSystem(fileName,suffix);
            File targetFile = new File(getCacheDir(), fileName);
            FileUtils.copy(originFile, targetFile);
            FileUtils.deleteFile(originFile);
            if (isCancel) {
                return;
            }
            mainThread.post(() -> {
                if (getListener() != null) {
                    getListener().success(url, targetFile.getAbsolutePath(), fileName);
                }
            });
        }

        //  https://mp.weixin.qq.com/s?__biz=MzA5MzI3NjE2MA==&mid=2650249029&idx=1&sn=6ab18477950e5f4e1a14dc47ecc4f763&chksm=8863662abf14ef3c1500d64c106ab2e5a6c95e716ff6e57ba379e2aabca7b6046060ccb78af2&scene=21#wechat_redirect
        //  https://developer.android.google.cn/training/data-storage/shared/media?hl=zh-cn#add-item

        private void saveToSystem(String fileName,String extension) {
            // Add a media item that other apps don't see until the item is
            // fully written to the media store.
            ContentResolver resolver = context.getApplicationContext().getContentResolver();

            ContentValues imageDetails = new ContentValues();
            imageDetails.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            imageDetails.put(MediaStore.Images.Media.IS_PENDING, 1);

            String mimeType = null;
            if ("jpg".equals(extension) || "jpeg".equals(extension)) {
                mimeType = "image/jpeg";
            } else if ("png".equals(extension)) {
                mimeType = "image/png";
            }
            imageDetails.put(MediaStore.Images.Media.MIME_TYPE, mimeType);

            Uri imageCollection;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                imageDetails.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
            } else {
                imageCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                imageDetails.put(MediaStore.MediaColumns.DATA,
                    Environment.getExternalStorageDirectory().getPath() + "/" + Environment.DIRECTORY_PICTURES + "/"
                        + fileName);
            }

            Uri songContentUri = resolver.insert(imageCollection, imageDetails);

            // "w" for write.
//            try (ParcelFileDescriptor pfd = resolver.openFileDescriptor(songContentUri, "w", null)) {
//                // Write data into the pending audio file.
//            }

            // Now that you're finished, release the "pending" status and let other apps
            // play the audio track.
            imageDetails.clear();
            imageDetails.put(MediaStore.Audio.Media.IS_PENDING, 0);
            resolver.update(songContentUri, imageDetails, null, null);

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

    private static class FileUtils {
        public static void copy(File source, File target) throws IOException {
            FileInputStream fileInputStream = null;
            FileOutputStream fileOutputStream = null;
            try {
                fileInputStream = new FileInputStream(source);
                fileOutputStream = new FileOutputStream(target);
                byte[] buffer = new byte[1024];
                while (fileInputStream.read(buffer) > 0) {
                    fileOutputStream.write(buffer);
                }
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
