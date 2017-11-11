package org.aaron.netty.proxy

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import org.slf4j.LoggerFactory

class ProxyBackendHandler(
        private val inboundChannel: Channel) : ChannelInboundHandlerAdapter() {

    companion object {
        private val LOG = LoggerFactory.getLogger(ProxyBackendHandler::class.java)
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        LOG.info("channelActive {}", ctx.channel());

        ctx.channel().read()
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        writeChunkAndTriggerRead(ctx.channel(), inboundChannel, msg)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        LOG.info("channelInactive {}", ctx.channel())

        closeOnFlush(inboundChannel)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        LOG.warn("exceptionCaught {}", ctx.channel(), cause)

        closeOnFlush(ctx.channel())
    }

}