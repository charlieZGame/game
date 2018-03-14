/*
package com.beimi.config.web;

import com.beimi.config.web.CustomSocketIOChannelInitializer;
import com.corundumstudio.socketio.Configuration;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@ChannelHandler.Sharable
public class CustomWrongUrlHandler extends ChannelInboundHandlerAdapter {

    private final Logger log = LoggerFactory.getLogger(getClass());

    Configuration configuration = null;

    */
/**
     * @param configuration
     *//*

    public CustomWrongUrlHandler(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof FullHttpRequest) {
            FullHttpRequest req = (FullHttpRequest) msg;
            Channel channel = ctx.channel();
            QueryStringDecoder queryDecoder = new QueryStringDecoder(req.getUri());

            // Don't log when port is pinged for monitoring. Must use context that starts with /ping.
            if (configuration.isAllowCustomRequests() && queryDecoder.path().startsWith("/ping")) {
                HttpResponse res = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
                channel.writeAndFlush(res).addListener(ChannelFutureListener.CLOSE);
                req.release();
                //log.info("Blocked wrong request! url: {}, ip: {}", queryDecoder.path(), channel.remoteAddress());
                return;
            }

            // This is the last channel handler in the pipe so if it is not ping then log warning.
            HttpResponse res = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
            ChannelFuture f = channel.writeAndFlush(res);
            f.addListener(ChannelFutureListener.CLOSE);
            req.release();
            log.warn("Blocked wrong socket.io-context request! url: {}, params: {}, ip: {}", channel.remoteAddress() + " " + queryDecoder.path(), queryDecoder.parameters());
        }
    }
}


*/
