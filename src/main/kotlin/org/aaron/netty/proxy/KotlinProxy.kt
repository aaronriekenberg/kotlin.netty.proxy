package org.aaron.netty.proxy

import com.google.common.util.concurrent.Uninterruptibles
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class KotlinProxy {

    companion object {
        private val LOCAL_PORT = 8443

        private val REMOTE_HOST = "www.google.com"

        private val REMOTE_PORT = 443

        private val LOG = LoggerFactory.getLogger(KotlinProxy::class.java)
    }

    fun run() {
        LOG.info("proxying *:{} to {}:{}", LOCAL_PORT, REMOTE_HOST, REMOTE_PORT)

        val bossGroup = NioEventLoopGroup(1)
        val workerGroup = NioEventLoopGroup()

        try {
            val b = ServerBootstrap()
            val channel = b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel::class.java)
                    .handler(LoggingHandler(LogLevel.DEBUG))
                    .childHandler(ProxyInitializer(REMOTE_HOST, REMOTE_PORT))
                    .childOption(ChannelOption.AUTO_READ, false)
                    .bind(LOCAL_PORT).sync().channel()
            LOG.info("listening channel {}", channel)
            channel.closeFuture().sync()
        } finally {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
        }
    }
}

fun main(args: Array<String>) {
    KotlinProxy().run()

    Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS)
}