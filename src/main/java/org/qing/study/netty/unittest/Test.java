package org.qing.study.netty.unittest;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.List;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-3 下午4:59
 */
class Decode extends ByteToMessageDecoder {
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelInactive");
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        System.out.println("decode");
        //System.out.println("size " + in.readableBytes());
        System.out.println(new String(in.readBytes(in.readableBytes()).array()));
    }
}

class Encode extends MessageToByteEncoder<String> {
    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) throws Exception {
        System.out.println(msg);
        out.writeBytes(msg.getBytes());
    }
}

public class Test {
    public static void main(String[] args) {
        //EmbeddedChannel channel = new EmbeddedChannel(new Decode());


        //channel.writeInbound(Unpooled.wrappedBuffer("hell0".getBytes()));

        EmbeddedChannel channel = new EmbeddedChannel(new Encode());
        //channel.readOutbound();
        channel.writeOutbound("hh");
        ByteBuf  s = (ByteBuf)channel.readOutbound();
        System.out.println(s.readableBytes());
        //System.out.println(channel.finish());
        /*try {
            Thread.sleep(99999999);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }*/

    }
}
