package com.roiquery.analytics.taskqueue;

public class SynnDataModel {

    public  Object data = null;
    public int taskSeq = 0;

    public boolean isSucceed = false;

    public void waitDataCome(Long timeout) throws InterruptedException {
        synchronized (this) {
            this.wait(timeout);
        }
    }

    public void done() {
        synchronized (this) {
            isSucceed = true;
            notify();
        }
    }
}
