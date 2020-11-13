package com.mt.mtuser;

import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.*;

/**
 * Created by gyh on 2020/4/2.
 */
public class Tenant {

    public static void main(String[] args) throws FileNotFoundException {
        nmka3();
        nmka2();
        ///975460423716
        //System.out.println(987654L * 987654);
    }

    private static void nmka3() {
        int n = 987654;
        BigInteger bi = new BigInteger("0");
        for (int i = 1; i <= n; i++) {
            bi = bi.add(new BigInteger("" + i).pow(8));
        }
        System.out.println(bi.toString());
        System.out.println(bi.mod(new BigInteger("123456789")));
    }

    private static void nmka2() {
        int n = 987654;
        long rul = 0;
        for (int i = 1; i <= n; i++) {
            rul += pow(i, 8);
            rul = rul % 123456789L;
        }
        System.out.println(rul);
        System.out.println(rul % 123456789);
    }

    public static long pow(long a, long b) {
        long temp = 1;
        for (int i = 0; i < b; i++) {
            temp = (temp % 1_000_000_000_000L) * a;
        }
        return temp;
    }

    public static int[] nmka(int[] data, int po) {
        int length = data.length;
        int pi = po % length;
        int[] temp = new int[length];
        for (int i = 0; i < length; i++) {
            temp[i] = data[(length + i - pi) % length];
        }
        return temp;
    }

    public static void mook(int n, char a, char b, char c) {
        if (n == 1) {
            System.out.println(" " + n + " " + a + " " + c + " ");
        } else {
            mook(n - 1, a, c, b);
            System.out.println(" " + n + " " + a + " " + c + " ");
            mook(n - 1, b, a, c);
        }
    }

    public static void nmak() {
        Calendar c = Calendar.getInstance();
        c.set(1921, Calendar.JUNE, 21, 12, 0);
        long lowTime = c.getTimeInMillis();
        Calendar n = Calendar.getInstance();
        n.set(2020, Calendar.JULY, 1, 12, 0);
        long nowTime = n.getTimeInMillis();
        System.out.println(lowTime);
        System.out.println((nowTime - lowTime) / 60_000);
    }


}

