package org.qing.study.udptalk;

import io.netty.channel.Channel;

/**
 * Created by ganqin on 14-5-27.
 */
public class User {
    private String name;
    private Channel channel;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public User(String name, Channel channel) {
        this.name = name;
        this.channel = channel;
    }
}
