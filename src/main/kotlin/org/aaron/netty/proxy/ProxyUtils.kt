package org.aaron.netty.proxy

import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener

fun closeOnFlush(channel: Channel?) {
    channel?.let {
        if (it.isActive) {
            it.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE)
        }
    }
}