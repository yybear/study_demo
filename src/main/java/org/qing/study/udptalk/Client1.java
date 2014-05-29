package org.qing.study.udptalk;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

/**
 * Created by ganqin on 14-5-27.
 */
class ClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private String name;

    private UdpPeer udpPeer;

    ClientHandler(String name, UdpPeer udpPeer) {
        this.name = name;
        this.udpPeer = udpPeer;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        // send login request
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(1);
        buf.writeBytes(name.getBytes());
        ctx.writeAndFlush(buf);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        byte cmd = msg.readByte();
        if(cmd == 1) {
            System.out.println("login " + new String(msg.readBytes(msg.readableBytes()).array()));

            ByteBuf buf = Unpooled.buffer();
            buf.writeByte(2);
            buf.writeBytes("user2".getBytes());
            ctx.writeAndFlush(buf);
        } else if(cmd == 2){
            String roomId = new String(msg.readBytes(36).array());
            System.out.println("roomid is " + roomId);
            String username = new String(msg.readBytes(5).array());
            System.out.println("谈话对方是: " + username);

            // 消息都走udp server转
            udpPeer.send(1, roomId, "127.0.0.1", 7008);
        }
    }
}

public class Client1 {
    private final String host;
    private final int port;

    private UdpPeer udpPeer;
    private String name;

    public Client1(String host, int port, String name) {
        this.host = host;
        this.port = port;
        this.name = name;

        try {
            udpPeer = new UdpPeer(10001);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(host, port))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                            ch.pipeline().addLast(new ClientHandler(name, udpPeer));
                        }
                    });

            ChannelFuture f = b.connect().sync();

            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws Exception {

        new Client1("localhost", 7000, "user1").start();
    }
}
