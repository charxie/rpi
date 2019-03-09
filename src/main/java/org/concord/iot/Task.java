package org.concord.iot;

/**
 * @author Charles Xie
 */

public class Task {

    private String name;
    private volatile boolean stopped = true;
    private volatile int index;
    private IoTWorkbench workbench;
    private Runnable runnable;

    public Task(String name, IoTWorkbench workbench) {
        this.name = name;
        this.workbench = workbench;
    }

    public String getName() {
        return name;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public void addToIndex(int x) {
        index += x;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    public void run() {
        workbench.submitTask(runnable);
    }

}
