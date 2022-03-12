package com.mt.mtuser;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Created by gyh on 2020/12/3
 */

class MyNewVector {

    public static void main(String args[]) {
        d5();
    }

    public static void d5() {
        String encodedString = "TWpBPQ==";
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
        String decodedString = new String(decodedBytes);
        decodedBytes = Base64.getDecoder().decode(decodedString);
        decodedString = new String(decodedBytes);
        System.out.println(decodedString);
        int n = Integer.parseInt(decodedString);
        for (int a1 = 0; a1 < 10; a1++) {
            for (int a2 = 0; a2 < 10; a2++) {
                for (int a3 = 0; a3 < 10; a3++) {
                    for (int a4 = 0; a4 < 10; a4++) {
                        for (int a5 = 0; a5 < 10; a5++) {
                            for (int a6 = 0; a6 < 10; a6++) {
                                if ((a1 + a3 + a5) * (a2 + a4 + a6) == n) {
                                    System.out.println("" + a4 + a5 + a6 + a1 + a2 + a3);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    static void permu(char[] data, int cur, int x) {
        if (cur == data.length - 1) {
            String s = new String(data);
            int a1 = s.charAt(4);
            int a2 = s.charAt(5);
            int a3 = s.charAt(6);
            int a4 = s.charAt(1);
            int a5 = s.charAt(2);
            int a6 = s.charAt(3);
            if ((a1 + a3 + a5) * (a2 + a4 + a6) == x) {
                System.out.println(s);
            }
            return;
        }

        for (int i = cur; i < data.length; i++) {
            char tmp = data[i];
            for (int j = i - 1; j >= cur; j--) data[j + 1] = data[j];
            data[cur] = tmp;

            permu(data, cur + 1, x);

            tmp = data[cur];
            for (int j = cur; j < i; j++) data[j] = data[j + 1];
            data[i] = tmp;
        }
    }

    public static void d3() {
        double a = 15.5;
        double b = 10;
        double c = 8.8;
        System.out.println(Math.asin((b * b + c * c - a * a) / (2 * b * c)));
        System.out.println(Math.asin((c * c + a * a - b * b) / (2 * c * a)));
        System.out.println(Math.asin((a * a + b * b - c * c) / (2 * a * b)));
    }

    public static void D2() {
        Integer[] score = {78, 60, 55, 32, 90, 84};
        ArrayList<Integer> pass = new ArrayList<>();
        ArrayList<Integer> flunk = new ArrayList<>();
        for (int i : score) {
            if (i >= 60) {
                pass.add(i);
            } else {
                flunk.add(i);
            }
        }
        pass.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2.compareTo(o1);
            }
        });
        flunk.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2.compareTo(o1);
            }
        });
        System.out.println("及格数组");
        pass.add(2, 80);
        for (int i : pass) {
            System.out.println(i);
        }
        System.out.println("不及格数组");
        for (int i : flunk) {
            System.out.println(i);
        }
    }


}



