package com.mt.mtuser;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by gyh on 2020/4/2.
 */
public class Tenant {

    public static void main(String[] args) {
        Timer timer = new Timer();
        TimerTask task1 = new TimerTask() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName() + System.currentTimeMillis() + " nmka11111");
            }
        };
        TimerTask task2 = new TimerTask() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName() + System.currentTimeMillis() + " nmka222");
            }
        };
        System.out.println(Thread.currentThread().getName() + System.currentTimeMillis());
        timer.schedule(task1, 0);
        timer.schedule(task2, 100);
    }

}
