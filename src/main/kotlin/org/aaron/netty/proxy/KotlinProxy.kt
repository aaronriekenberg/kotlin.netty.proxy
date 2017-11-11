package org.aaron.netty.proxy

import com.google.common.util.concurrent.Uninterruptibles
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class KotlinProxy(
        private val localPort: Int,
        private val remoteHost: String,
        private val remotePort: Int) {

    companion object {
        private val LOG = LoggerFactory.getLogger(KotlinProxy::class.java)
    }

    fun run() {
        LOG.info("proxying *:{} to {}:{}", localPort, remoteHost, remotePort)

        val bossGroup = createEventLoopGroup(1)
        val workerGroup = createEventLoopGroup()

        LOG.info("bossGroup = {} workerGroup = {}", bossGroup, workerGroup)

        try {
            val b = ServerBootstrap()
            val channel = b.group(bossGroup, workerGroup)
                    .channel(serverSocketChannelClass().java)
                    .childHandler(ProxyInitializer(remoteHost, remotePort))
                    .childOption(ChannelOption.AUTO_READ, false)
                    .bind(localPort).sync().channel()
            LOG.info("listening channel {}", channel)
            channel.closeFuture().sync()
        } finally {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
        }
    }
}

fun main(args: Array<String>) {
    KotlinProxy(
            localPort = 8222,
            remoteHost = "192.168.0.100",
            remotePort = 22
    ).run()

    Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS)
}