package com.mt.mtuser;

import com.mt.mtuser.common.Util;
import com.mt.mtuser.entity.Stockholder;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Properties;
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
        sh.getRealName();
        int i = 0;
        i = i + + + + +10;
        System.out.println(i);
    }
    @Test

    public void nmka() throws FileNotFoundException {
        Properties p = System.getProperties();
        p.forEach((k, v) -> System.out.println(k + "  " + v));
        System.out.println("1");
    }

}
