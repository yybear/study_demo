package org.qing.study.udptalk;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by ganqin on 14-5-27.
 */
class CallHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        byte cmd = msg.readByte();
        if(cmd == 1) { // login server
            final String name = new String(msg.readBytes(msg.readableBytes()).array());
            System.out.println(name + " login server");

            ByteBuf buf = Unpooled.buffer();
            buf.writeByte(1);
            buf.writeBytes("ok".getBytes());
            ChannelFuture future = ctx.writeAndFlush(buf);
            future.addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    User user = new User(name, future.channel());
                    TcpServer.users.put(name, user);
                    future.channel().attr(TcpServer.UNAME).set(name);

                }
            });
        } else if(cmd == 2) { // need talk
            String username = new String(msg.readBytes(msg.readableBytes()).array());

            User user = TcpServer.users.get(username);

            String roomId = UUID.randomUUID().toString();

            ByteBuf buf = Unpooled.buffer();
            buf.writeByte(2);
            buf.writeBytes(roomId.getBytes());
            buf.writeBytes(username.getBytes());
            ctx.writeAndFlush(buf);

            String me = ctx.channel().attr(TcpServer.UNAME).get();
            buf = Unpooled.buffer();
            buf.writeByte(2);
            buf.writeBytes(roomId.getBytes());
            buf.writeBytes(me.getBytes());
            user.getChannel().writeAndFlush(buf);
        }
    }
}

public class TcpServer {
    public static final AttributeKey<String> UNAME = AttributeKey.valueOf("username");

    public static final Map<String, User> users = new HashMap<String, User>();

    public static void main(String[] args) throws Exception {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(new NioEventLoopGroup(), new NioEventLoopGroup())
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(7000))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                            ch.pipeline().addLast(new CallHandler());
                        }
                    });
            ChannelFuture f = b.bind().sync();
            f.channel().closeFuture().sync();

            System.out.println("tcp Server is starting ……");
        } finally {
            group.shutdownGracefully().sync();
        }
    }
}
