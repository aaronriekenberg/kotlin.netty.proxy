package org.aaron.netty.proxy

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

class KotlinProxy(
        private val localPort: Int,
        private val remoteHostAndPort: HostAndPort) {

    companion object {
        private val LOG = LoggerFactory.getLogger(KotlinProxy::class.java)
    }

    fun run() {
        LOG.info("proxying *:{} to {}:{}", localPort, remoteHostAndPort.host, remoteHostAndPort.port)

        val bossGroup = createEventLoopGroup(1)
        val workerGroup = createEventLoopGroup()

        LOG.info("bossGroup {} executorCount {}", bossGroup.javaClass.simpleName, bossGroup.executorCount())
        LOG.info("workerGroup {} executorCount {}", workerGroup.javaClass.simpleName, workerGroup.executorCount())

        try {
            val b = ServerBootstrap()
            val channel = b.group(bossGroup, workerGroup)
                    .channel(serverSocketChannelClass().java)
                    .childHandler(ProxyInitializer(remoteHostAndPort))
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
    if (args.size != 3) {
        println("Usage: KotlinProxy <local port> <remote host> <remote port>")
        exitProcess(1)
    }

    KotlinProxy(
            localPort = args[0].toInt(),
            remoteHostAndPort = HostAndPort(
                    host = args[1],
                    port = args[2].toInt())
    ).run()
}