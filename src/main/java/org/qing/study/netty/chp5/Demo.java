package org.qing.study.netty.chp5;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;

/**
 * Created by ganqin on 14-5-7.
 */
public class Demo {
    public static void main(String[] args) {
        System.out.println("start!");

        while(true) {
            try {
                System.out.println("loop");
                throw new Exception("err");
            } catch (Exception e) {
                System.out.println("exception happend");
                break;
            }
        }

        System.out.println("end!");
    }
}
