package org.aaron.netty.proxy

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import mu.KLogging

class ProxyBackendHandler(
        private val inboundChannel: Channel) : ChannelInboundHandlerAdapter() {

    companion object : KLogging()

    override fun channelActive(ctx: ChannelHandlerContext) {
        logger.info { "channelActive ${ctx.channel()}" }

        ctx.channel().read()
    }

    override fun channelRead(ctx: ChannelHandlerContext, chunk: Any) {
        inboundChannel.writeChunkAndTriggerRead(
                chunk = chunk,
                readChannel = ctx.channel())
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        logger.info { "channelInactive ${ctx.channel()}" }

        inboundChannel.closeOnFlush()
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        logger.info { "exceptionCaught ${ctx.channel()}" }

        ctx.channel().closeOnFlush()
    }

}