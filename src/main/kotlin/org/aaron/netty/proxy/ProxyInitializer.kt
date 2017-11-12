package org.aaron.netty.proxy

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import org.slf4j.LoggerFactory

class ProxyInitializer(
        private val remoteHostAndPort: HostAndPort) : ChannelInitializer<SocketChannel>() {

    companion object {
        private val LOG = LoggerFactory.getLogger(ProxyInitializer::class.java)
    }

    override fun initChannel(ch: SocketChannel) {
        LOG.info("initChannel {}", ch)

        ch.pipeline().addLast(ProxyFrontendHandler(remoteHostAndPort))
    }

}