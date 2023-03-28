package com.zegocloud.zimkit.components.album.browserimage.download;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;

import com.zegocloud.zimkit.common.glide.ZIMKitGlideLoader;
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
