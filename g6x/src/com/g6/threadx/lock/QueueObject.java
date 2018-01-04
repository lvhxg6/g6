package com.g6.threadx.lock;

/**
 * Created by huixiaolv on 04/01/2018.
 */
public class QueueObject {
    private boolean isNotified = false;

    public synchronized void doWait() throws InterruptedException {
        while (!isNotified)
            this.wait();
        this.isNotified = false;
    }

    public synchronized void doNotify(){
        this.isNotified = true;
        this.notify();
    }

    public boolean equals(Object o){
        return this == o;
    }

}
