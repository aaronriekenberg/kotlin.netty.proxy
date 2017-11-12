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

    override fun channelRead(ctx: ChannelHandlerContext, chunk: Any) {
        inboundChannel.writeChunkAndTriggerRead(
                readChannel = ctx.channel(),
                chunk = chunk)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        LOG.info("channelInactive {}", ctx.channel())

        inboundChannel.closeOnFlush()
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        LOG.warn("exceptionCaught {}", ctx.channel(), cause)

        ctx.channel().closeOnFlush()
    }

}