package cn.linked.router.api;

import android.util.Log;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RouterThreadPoolExecutor extends ThreadPoolExecutor {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final long THREAD_LIFE = 30L;

    private static volatile RouterThreadPoolExecutor instance;
    public static ThreadPoolExecutor getInstance(){
        if(instance!=null){
            synchronized (RouterThreadPoolExecutor.class){
                if(instance==null){
                    instance=new RouterThreadPoolExecutor(
                            CPU_COUNT+1,
                            CPU_COUNT+1,
                            THREAD_LIFE,
                            TimeUnit.SECONDS,
                            new ArrayBlockingQueue<Runnable>(64));
                    return instance;
                }
            }
        }
        return instance;
    }
    private RouterThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, Executors.defaultThreadFactory(),(r,executor)->{
            Log.w("Router","Task rejected, too many task!");
        });
    }
}
