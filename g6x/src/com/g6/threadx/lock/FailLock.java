package com.g6.threadx.lock;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huixiaolv on 04/01/2018.
 *
 * 描述：简单实现的一个公平锁
 *
 */
public class FailLock {
    private boolean isLocked = false;
    private Thread lockingThread = null;
    private List<QueueObject> waitingThreads = new ArrayList<QueueObject>();

    public void lock() throws InterruptedException {
        QueueObject object = new QueueObject();
        boolean isLockedForThisThread = true;
        synchronized (this){
            waitingThreads.add(object);
        }

        while(isLockedForThisThread){
            synchronized (this){
                isLockedForThisThread = isLocked || waitingThreads.get(0)!=object;
                if(!isLockedForThisThread){
                    isLocked = true;
                    waitingThreads.remove(object);
                    lockingThread = Thread.currentThread();
                    return;
                }
            }
            try{
                object.doWait();
            }catch (Exception e){
                synchronized (this){
                    waitingThreads.remove(object);
                }
                throw e;
            }
        }

    }

    public synchronized void unlock(){
        if(this.lockingThread!=Thread.currentThread())
            throw new IllegalMonitorStateException("Calling thread has not locked this lock");
        isLocked = false;
        lockingThread = null;
        if(waitingThreads.size()>0)
            waitingThreads.get(0).doNotify();
    }

    public static class Counter{
        int counter = 0;
        public void inc(){
            counter++;
        }
        public int getCounter(){
            return counter;
        }
    }

    public static void main(String[] args){
        final Counter counter = new Counter();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                for (int i=0;i<1000;i++){
                    counter.inc();
                }
                System.out.println(counter.getCounter());
            }
        };

        Runnable run1 = new Runnable() {
            @Override
            public void run() {
                for (int i=0;i<1000;i++){
                    counter.inc();
                }
                System.out.println(counter.getCounter());
            }
        };

        new Thread(run,"T-1").start();
        new Thread(run1,"T-2").start();

    }


}
