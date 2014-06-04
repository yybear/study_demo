package org.qing.study.netty.chapter14;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.util.CharsetUtil;

import java.util.List;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-4 上午10:05
 */
public class MemcachedResponseDecoder2 extends ReplayingDecoder<MemcachedResponseDecoder2.State> {
    private int totalBodySize;
    private byte magic;
    private byte opCode;
    private short keyLength;
    private byte extraLength;
    private byte dataType;
    private short status;
    private int id;
    private long cas;

    enum State {
        Header,
        Body
    }

    public MemcachedResponseDecoder2() {
        super(State.Header);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state()) {
            case Header:
                magic = in.readByte();
                opCode = in.readByte();
                keyLength = in.readShort();
                extraLength = in.readByte();
                dataType = in.readByte();
                status = in.readShort();
                totalBodySize = in.readInt();
                id = in.readInt(); //referred to in the protocol spec as opaque
                cas = in.readLong();
                checkpoint(State.Body);
            case Body:
                int flags = 0, expires = 0;
                int actualBodySize = totalBodySize;
                if (extraLength > 0) {
                    flags = in.readInt();
                    actualBodySize -= 4;
                }
                if (extraLength > 4) {
                    expires = in.readInt();
                    actualBodySize -= 4;
                }
                String key = "";
                if (keyLength > 0) {
                    ByteBuf keyBytes = in.readBytes(keyLength);
                    key = keyBytes.toString(CharsetUtil.UTF_8);
                    actualBodySize -= keyLength;
                }
                ByteBuf body = in.readBytes(actualBodySize);
                String data = body.toString(CharsetUtil.UTF_8);
                out.add(new MemcachedResponse(
                        magic,
                        opCode,
                        dataType,
                        status,
                        id,
                        cas,
                        flags,
                        expires,
                        key,
                        data
                ));

                checkpoint(State.Header);
        }
    }
}
