package org.aaron.netty.proxy

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import org.slf4j.LoggerFactory

class ProxyInitializer(
        private val remoteHost: String,
        private val remotePort: Int) : ChannelInitializer<SocketChannel>() {

    companion object {
        private val LOG = LoggerFactory.getLogger(ProxyInitializer::class.java)
    }

    override fun initChannel(ch: SocketChannel) {
        LOG.info("initChannel {}", ch)

        ch.pipeline().addLast(
                LoggingHandler(LogLevel.DEBUG),
                ProxyFrontendHandler(remoteHost, remotePort))
    }

}