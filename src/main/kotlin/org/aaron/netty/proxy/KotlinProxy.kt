package org.aaron.netty.proxy

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.util.Version
import mu.KLogging
import kotlin.system.exitProcess

class KotlinProxy(
        private val localPort: Int,
        private val remoteHostAndPort: HostAndPort) {

    companion object : KLogging()

    fun run() {
        logger.info { "proxying *:$localPort to ${remoteHostAndPort.host}:${remoteHostAndPort.port}" }

        logger.info { "netty version ${Version.identify()}" }

        val bossGroup = createEventLoopGroup(1)
        val workerGroup = createEventLoopGroup()

        logger.info { "bossGroup=${bossGroup.javaClass.simpleName} executorCount=${bossGroup.executorCount()}" }
        logger.info { "workerGroup=${workerGroup.javaClass.simpleName} executorCount=${workerGroup.executorCount()}" }

        try {
            val b = ServerBootstrap()
            val channel = b.group(bossGroup, workerGroup)
                    .channel(serverSocketChannelClass().java)
                    .childHandler(ProxyInitializer(remoteHostAndPort))
                    .childOption(ChannelOption.AUTO_READ, false)
                    .bind(localPort).sync().channel()
            logger.info { "listening channel $channel" }
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