package org.qing.study.udptalk;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ganqin on 14-5-27.
 */

class UdpHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private Map<String, InetSocketAddress[]> room = new HashMap<String, InetSocketAddress[]>();

    UdpHandler(Map<String, InetSocketAddress[]> room) {
        this.room = room;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        ByteBuf byteBuf = msg.content();
        int cmd = byteBuf.readByte();
        if(cmd == 1) { // tune
            String roomId = new String(byteBuf.readBytes(byteBuf.readableBytes()).array());
            System.out.println(msg.sender() + " 进入房间：" + roomId);
            InetSocketAddress[] arr = room.get(roomId);
            if(arr == null) {
                arr = new InetSocketAddress[2];
                arr[0] = msg.sender();
                room.put(roomId, arr);
            } else {
                arr[1] = msg.sender();
                room.put(roomId, arr);
            }

            if(arr[1] != null) {
                // 两个用户都进入room
                ByteBuf buf = Unpooled.buffer();
                buf.writeByte(1);
                buf.writeBytes(roomId.getBytes());
                ByteBuf buf2 = buf.copy();

                ctx.write(new DatagramPacket(buf, arr[0]));
                ctx.write(new DatagramPacket(buf2, arr[1]));
                ctx.flush();
            }
        } else if(cmd == 2) {
            String roomId = new String(byteBuf.readBytes(36).array());
            System.out.println("room id " + roomId);
            InetSocketAddress[] arr = room.get(roomId);

            String content = byteBuf.readBytes(byteBuf.readableBytes()).toString();
            System.out.println("转发 " + msg.sender() +" 消息: " + content);

            if(arr[0].equals(msg.sender())) {
                System.out.println("转发给 " + arr[1]);
                ByteBuf buf = Unpooled.buffer();
                buf.writeByte(2);
                buf.writeBytes(content.getBytes());
                ctx.writeAndFlush(new DatagramPacket(buf, arr[1]));
            } else {
                System.out.println("转发给 " + arr[0]);
                ByteBuf buf = Unpooled.buffer();
                buf.writeByte(2);
                buf.writeBytes(content.getBytes());
                ctx.writeAndFlush(new DatagramPacket(buf, arr[0]));
            }
        }
    }
}
public class UdpServer {
    private Map<String, InetSocketAddress[]> room = new HashMap<String, InetSocketAddress[]>();

    public void start() throws Exception {
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();

        ChannelFuture future = bootstrap.group(group).channel(NioDatagramChannel.class).handler(new ChannelInitializer<DatagramChannel>() {
            @Override
            protected void initChannel(DatagramChannel ch) throws Exception {
                ch.pipeline().addLast(new UdpHandler(room));
            }
        }).bind(7008).sync();

        System.out.println("UDP Server is starting ……");

        future.channel().closeFuture().await();

        group.shutdownGracefully();
    }

    public static void main(String[] args) throws Exception {
        UdpServer server = new UdpServer();

        server.start();
    }
}
