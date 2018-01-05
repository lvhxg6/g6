package com.g6.threadx.lock;

/**
 * Created by huixiaolv on 04/01/2018.
 */
public class Counter {
    int counter = 0;
    FailLock lock = new FailLock();
    public void inc(){
        try {
            lock.lock();
            System.out.println(Thread.currentThread().getName()+" 开始执行......"+counter+"      "+(++counter));
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }

    }
    public int getCounter(){
        return counter;
    }
}
