package com.g6.threadx.lock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huixiaolv on 04/01/2018.
 *
 * 描述：简单实现的一个公平锁
 *      公平锁实现原理：
 *          在FairLock内部维护一个队列(数组)，按照FIFO的原则，依次获取队列的头QueueObject；
 *          没有加锁的时候，加上锁，加锁的时候调用线程QueueObject的doWait方法，使线程阻塞，FairLock内部维护一个有序队列，存放进程调用的顺序
 *          获取锁的线程执行结束之后，获取有序队列头部的QueueObject，调用doNotify方法，唤醒阻塞的线程继续执行
 *
 *
 *     首先注意到lock()方法不在声明为synchronized，取而代之的是对必需同步的代码，在synchronized中进行嵌套。
 *     FairLock新创建了一个QueueObject的实例，并对每个调用lock()的线程进行入队列。调用unlock()的线程将从
 * 队列头部获取QueueObject，并对其调用doNotify()，以唤醒在该对象上等待的线程。通过这种方式，在同一时间
 * 仅有一个等待线程获得唤醒，而不是所有的等待线程。这也是实现FairLock公平性的核心所在。
 *     请注意，在同一个同步块中，锁状态依然被检查和设置，以避免出现滑漏条件。
 *     还需注意到，QueueObject实际是一个semaphore。doWait()和doNotify()方法在QueueObject中保存着信号。
 *  这样做以避免一个线程在调用queueObject.doWait()之前被另一个调用unlock()并随之调用queueObject.doNotify()的线程重入，
 *  从而导致信号丢失。queueObject.doWait()调用放置在synchronized(this)块之外，以避免被monitor嵌套锁死，所以另外的线程
 *  可以解锁，只要当没有线程在lock方法的synchronized(this)块中执行即可。
 *
 *     最后，注意到queueObject.doWait()在try – catch块中是怎样调用的。在InterruptedException抛出的情况下，
 *  线程得以离开lock()，并需让它从队列中移除。
 *
 */
public class FailLock {
    private boolean isLocked = false;
    private Thread lockingThread = null;
    private List<QueueObject> waitingThreads = new ArrayList<QueueObject>();
    private Map<String,String> map = new HashMap<String,String>();

    public void lock() throws InterruptedException {
        QueueObject object = new QueueObject();
        boolean isLockedForThisThread = true;
        synchronized (this){
            waitingThreads.add(object);
            String tName = Thread.currentThread().getName();
            map.put(object.toString(),tName);
        }

        while(isLockedForThisThread){
            synchronized (this){
                isLockedForThisThread = isLocked || waitingThreads.get(0)!=object;
                if(!isLockedForThisThread){
                    String tName = Thread.currentThread().getName();
                    System.out.println("线程:"+tName+" 获得锁---------->");
                    isLocked = true;
                    waitingThreads.remove(object);
                    lockingThread = Thread.currentThread();
                    return;
                }
            }
            try{
                String tName = Thread.currentThread().getName();
                System.out.println("线程:"+tName+" doWait......");
                object.doWait();
            }catch (InterruptedException e){
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
        if(waitingThreads.size()>0){
            String key = waitingThreads.get(0).toString();
            waitingThreads.get(0).doNotify();
            System.out.println("notify 线程:"+map.get(key));
        }
    }

    public static void main(String[] args) throws InterruptedException {
        final Counter counter = new Counter();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                for (int i=0;i<1000;i++){
                    counter.inc();
                }
//                System.out.println(counter.getCounter());
            }
        };

        Runnable run1 = new Runnable() {
            @Override
            public void run() {
                for (int i=0;i<1000;i++){
                    counter.inc();
                }
//                System.out.println(counter.getCounter());
            }
        };

        Runnable run2 = new Runnable() {
            @Override
            public void run() {
                for (int i=0;i<1000;i++){
                    counter.inc();
                }
//                System.out.println(counter.getCounter());
            }
        };

        Thread t1 = new Thread(run, "T-1");
        Thread t2 = new Thread(run1, "T-2");
        Thread t3 = new Thread(run2, "T-3");
        t1.start();
        t2.start();
        t3.start();
//        t1.join();
//        t2.join();
//        t3.join();

//        System.out.println(counter.getCounter());

        Thread.currentThread().sleep(2000);

        System.out.println(counter.getCounter());

    }


}
