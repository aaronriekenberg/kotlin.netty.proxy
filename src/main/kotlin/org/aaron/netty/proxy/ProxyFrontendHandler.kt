package org.aaron.netty.proxy

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelOption
import org.slf4j.LoggerFactory


class ProxyFrontendHandler(
        private val remoteHost: String,
        private val remotePort: Int) : ChannelInboundHandlerAdapter() {

    companion object {
        private val LOG = LoggerFactory.getLogger(ProxyFrontendHandler::class.java)
    }

    // As we use inboundChannel.eventLoop() when building the Bootstrap this does not need to be volatile as
    // the outboundChannel will use the same EventLoop (and therefore Thread) as the inboundChannel.
    private var outboundChannel: Channel? = null

    override fun channelActive(ctx: ChannelHandlerContext) {
        LOG.info("channelActive {}", ctx.channel())

        val inboundChannel = ctx.channel()

        // Start the connection attempt.
        val b = Bootstrap()
        b.group(inboundChannel.eventLoop())
                .channel(inboundChannel.javaClass)
                .handler(ProxyBackendHandler(inboundChannel))
                .option(ChannelOption.AUTO_READ, false)

        val f = b.connect(remoteHost, remotePort)
        outboundChannel = f.channel()

        f.addListener({ future ->
            if (future.isSuccess) {
                // connection complete start to read first data
                inboundChannel.read()
            } else {
                // Close the connection if the connection attempt has failed.
                inboundChannel.close()
            }
        })
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        outboundChannel?.let {
            writeChunkAndTriggerRead(ctx.channel(), it, msg)
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        LOG.info("channelInactive {}", ctx.channel())

        closeOnFlush(outboundChannel)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        LOG.warn("exceptionCaught {}", ctx.channel(), cause)

        closeOnFlush(ctx.channel())
    }

}