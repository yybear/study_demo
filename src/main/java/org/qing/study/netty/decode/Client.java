package org.qing.study.netty.decode;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-3 下午6:49
 */
public class Client {
    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress("localhost", 7000))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {

                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {

                                    ByteBuf buf = Unpooled.buffer();
                                    for (int i = 0; i < 12; i++) {
                                        buf.writeByte(i);
                                    }
                                    ByteBuf input = buf.duplicate();

                                    ctx.writeAndFlush(input.readBytes(3));
                                    Thread.sleep(1000);
                                    ctx.writeAndFlush(input.readBytes(9));
                                }

                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                                    //System.out.println(new String(msg.readBytes(msg.readableBytes()).array()));
                                    while (msg.isReadable())
                                        System.out.println(msg.readInt());
                                }
                            });
                        }
                    });

            ChannelFuture f = b.connect().sync();

            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }
}
