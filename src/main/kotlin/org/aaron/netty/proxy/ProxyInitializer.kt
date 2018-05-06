package org.aaron.netty.proxy

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import mu.KLogging

class ProxyInitializer(
        private val remoteHostAndPort: HostAndPort) : ChannelInitializer<SocketChannel>() {

    companion object : KLogging()

    override fun initChannel(ch: SocketChannel) {
        logger.info { "initChannel $ch" }

        ch.pipeline().addLast(ProxyFrontendHandler(remoteHostAndPort))
    }

}