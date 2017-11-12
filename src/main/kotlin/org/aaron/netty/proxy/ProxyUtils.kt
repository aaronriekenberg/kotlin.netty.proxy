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
import io.netty.util.ReferenceCountUtil
import kotlin.reflect.KClass

data class HostAndPort(val host: String, val port: Int)

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

fun Channel.closeOnFlush() {
    if (isActive) {
        writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE)
    }
}

fun Channel?.writeChunkAndTriggerRead(chunk: Any, readChannel: Channel) {
    var consumedChunk = false

    try {
        val writeChannel = this
        if (writeChannel != null && writeChannel.isActive) {
            writeChannel.writeAndFlush(chunk).addListener({ future ->
                if (future.isSuccess) {
                    readChannel.read()
                } else {
                    writeChannel.close()
                }
            })
            consumedChunk = true
        }
    } finally {
        if (!consumedChunk) {
            ReferenceCountUtil.release(chunk)
        }
    }
}