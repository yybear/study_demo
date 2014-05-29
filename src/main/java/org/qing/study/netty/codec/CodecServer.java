package org.qing.study.netty.codec;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.ReplayingDecoder;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-5-29 下午4:51
 */
/*class ByteToIntegerDecoder extends
        ByteToMessageDecoder {

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
            throws Exception {
        System.out.println("ByteToIntegerDecoder");
        if (in.readableBytes() >= 4) {
            out.add(in.readInt());
        }
    }
}*/

class ByteToIntegerDecoder extends
        ReplayingDecoder<Integer> {

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
            throws Exception {
        System.out.println("ByteToIntegerDecoder");
        out.add(in.readInt());
    }
}

class IntegerToStringDecoder extends
        MessageToMessageDecoder<Integer> {

    @Override
    public void decode(ChannelHandlerContext ctx, Integer msg, List<Object> out)
            throws Exception {
        System.out.println("IntegerToStringDecoder");
        out.add(String.valueOf(msg));
    }
}

class StringToByteEncoder extends
        MessageToByteEncoder<String> {

    @Override
    public void encode(ChannelHandlerContext ctx, String msg, ByteBuf out)
            throws Exception {
        System.out.println("StringToByteEncoder " + msg);
        out.writeBytes(msg.getBytes());
    }
}

class ByteToIntegerCodec extends ByteToMessageCodec<Integer> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Integer msg, ByteBuf out) throws Exception {
        System.out.println("encode " + msg);
        out.writeInt(msg);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if(in.readableBytes() >= 4) {
            out.add(in.readInt());
        }
    }
}

class CodecHandler extends SimpleChannelInboundHandler<Integer> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Integer msg) throws Exception {
        System.out.println(msg);
        ctx.writeAndFlush(msg);
    }
}
public class CodecServer {
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
                            ch.pipeline()/*.addLast(new ByteToIntegerDecoder()).addLast(new IntegerToStringDecoder())
                                    .addLast(new StringToByteEncoder())*/
                                    .addLast(new ByteToIntegerCodec())
                                    .addLast(new CodecHandler());
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
