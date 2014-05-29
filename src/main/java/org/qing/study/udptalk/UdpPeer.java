package org.qing.study.udptalk;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-5-28 上午10:05
 */
class Handler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        DatagramPacket packet = (DatagramPacket)msg;
        ByteBuf buf = packet.content();

        int cmd = buf.readByte();
        if(cmd == 1) {
            System.out.println("双方连接已经准备完成");
            ByteBuf buf1 = Unpooled.buffer();

            buf1.writeByte(2);
            buf1.writeBytes(buf.readBytes(buf.readableBytes()).array());
            buf1.writeBytes("test udp send".getBytes());
            ctx.writeAndFlush(new DatagramPacket(
                    buf1,
                    packet.sender()));
        } else if(cmd == 2) {
            // 接收消息
            System.out.println("获取对方消息");
            System.out.println(buf.toString());
            System.out.println(buf.readBytes(buf.readableBytes()).toString());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}

public class UdpPeer {
    private NioEventLoopGroup group = new NioEventLoopGroup();

    private ChannelFuture future;

    public UdpPeer(int inetPort) throws Exception {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioDatagramChannel.class).handler(new ChannelInitializer<DatagramChannel>() {
            @Override
            protected void initChannel(DatagramChannel ch) throws Exception {
                ch.pipeline().addLast(new Handler());
            }
        });

        future = bootstrap.bind(inetPort).sync();
    }

    public void send(int cmd, String msg, String host, int port) throws InterruptedException {
        System.out.println("send to " + host + ":" + port + ", msg is " + msg);
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(cmd);
        buf.writeBytes(msg.getBytes());
        future.channel().writeAndFlush(new DatagramPacket(
                buf,
                new InetSocketAddress(host, port))).sync();
    }

    public boolean await() throws Exception {
        return future.await(4000);
    }

    public void destory() {
        future.channel().close();
        group.shutdownGracefully();
    }

    public static void main(String[] args) throws Exception {

        UdpPeer udpPeer = new UdpPeer(7009);
        UdpPeer udpPeer2 = new UdpPeer(7008);

        udpPeer.send(1, "i am udpPeer", "127.0.0.1", 7008);
        udpPeer2.send(1, "i am udpPeer2", "127.0.0.1", 7009);
    }
}
