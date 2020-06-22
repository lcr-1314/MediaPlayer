package com.rh.utilslib.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ThreadManagerUtil.java
 *
 * @author Wilson
 * @description
 * @date 2015/11/4
 * @modifier
 */
public class ThreadManagerUtil {

    public static ExecutorService sPool = Executors.newCachedThreadPool();

    /***
     *
     * @param task
     */
    public static void start(Runnable task){
        if(sPool != null && !sPool.isShutdown() && !sPool.isTerminated()){
            sPool.execute(task);
        }
    }


    /***
     * 关闭线程池
     */
    public static void threadPoolShutdown(){
        if(sPool != null){
            sPool.shutdown();
        }
    }

}
