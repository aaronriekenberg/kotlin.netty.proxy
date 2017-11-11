package org.aaron.netty.proxy

import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.EventLoopGroup
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.kqueue.KQueue
import io.netty.channel.kqueue.KQueueEventLoopGroup
import io.netty.channel.kqueue.KQueueServerSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.ServerSocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import kotlin.reflect.KClass

fun createEventLoopGroup(threads: Int = 0): EventLoopGroup {
    return when {
        Epoll.isAvailable() -> EpollEventLoopGroup(threads)
        KQueue.isAvailable() -> KQueueEventLoopGroup(threads)
        else -> NioEventLoopGroup(threads)
    }
}

fun serverSocketChannelClass(): KClass<out ServerSocketChannel> {
    return when {
        Epoll.isAvailable() -> EpollServerSocketChannel::class
        KQueue.isAvailable() -> KQueueServerSocketChannel::class
        else -> NioServerSocketChannel::class
    }
}

fun closeOnFlush(channel: Channel?) {
    channel?.let {
        if (it.isActive) {
            it.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE)
        }
    }
}