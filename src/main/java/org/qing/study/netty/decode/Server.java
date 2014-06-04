package org.qing.study.netty.decode;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-3 下午6:49
 */
class FixedLengthFrameDecoder extends ByteToMessageDecoder {

    private final int frameLength;

    public FixedLengthFrameDecoder(int frameLength) {
        if (frameLength <= 0) {
            throw new IllegalArgumentException(
                    "frameLength must be a positive integer: " + frameLength);
        }
        this.frameLength = frameLength;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() >= frameLength) {
            System.out.println("decode " + in.readableBytes());
            ByteBuf buf = in.readBytes(frameLength);
            out.add(buf);
        }
    }
}

class TestHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private Integer i = 0;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        System.out.println("channelRead0  " + i++);
    }
}

public class Server {
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
                            ch.pipeline()
                                    .addLast(new FixedLengthFrameDecoder(3))
                                    .addLast(new TestHandler());
                        }
                    });
            ChannelFuture f = b.bind().sync();
            f.channel().closeFuture().sync();

            System.out.println("CodecServer is starting ……");
        } finally {
            group.shutdownGracefully().sync();
        }
    }
}
