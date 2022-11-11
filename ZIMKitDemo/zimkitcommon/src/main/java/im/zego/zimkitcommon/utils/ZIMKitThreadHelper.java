package im.zego.zimkitcommon.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Threads tool for executing threads, etc.
 */
public class ZIMKitThreadHelper {

    public static final ZIMKitThreadHelper INST = new ZIMKitThreadHelper();

    private ExecutorService executors;

    private ZIMKitThreadHelper(){
    }

    /**
     * Execution in threads
     * @param runnable The runnable to be executed
     */
    public void execute(Runnable runnable) {
        ExecutorService executorService = getExecutorService();
        if (executorService != null) {
            executorService.execute(runnable);
        } else {
            new Thread(runnable).start();
        }
    }

    /**
     * Get the cache thread pool
     * @return Caching thread pool service
     */
    private ExecutorService getExecutorService(){
        if (executors == null) {
            executors = Executors.newCachedThreadPool();
        }

        return executors;
    }

}
