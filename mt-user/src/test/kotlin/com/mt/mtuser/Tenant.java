package com.mt.mtuser;

import com.mt.mtuser.common.Util;
import com.mt.mtuser.entity.Stockholder;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by gyh on 2020/4/2.
 */
public class Tenant {

    public static void main(String[] args) {
        String yer = Util.createDate("yyyy", System.currentTimeMillis());
        String m = Util.createDate("MM", System.currentTimeMillis());
        System.out.println(yer + " - " + m);
        Stockholder sh = new Stockholder();
        sh.getUserId();
        sh.getDepartment();
        sh.getRealName();
        int i = 0;
        i = i + + + + +10;
        System.out.println(i);
    }

}
