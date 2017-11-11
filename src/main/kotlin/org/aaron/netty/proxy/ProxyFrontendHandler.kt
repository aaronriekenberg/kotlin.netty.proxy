package org.aaron.netty.proxy

import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
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
        LOG.info("channelActive", ctx)

        val inboundChannel = ctx.channel()

        // Start the connection attempt.
        val b = Bootstrap()
        b.group(inboundChannel.eventLoop())
                .channel(ctx.channel().javaClass)
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
            if (it.isActive) {
                it.writeAndFlush(msg).addListener(ChannelFutureListener { future ->
                    if (future.isSuccess) {
                        // was able to flush out data, start to read the next chunk
                        ctx.channel().read()
                    } else {
                        future.channel().close()
                    }
                })
            }
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        LOG.info("channelInactive", ctx)

        outboundChannel?.let { closeOnFlush(it) }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        LOG.warn("exceptionCaught {}", ctx, cause)

        closeOnFlush(ctx.channel())
    }

}