package com.weiservers.portforward.thread;

import com.weiservers.portforward.handlers.Handlers;
import com.weiservers.portforward.handlers.TcpForwardHandler;
import com.weiservers.portforward.mapping.ConfigMapping;
import com.weiservers.portforward.mapping.ForwardMapping;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TcpForwardThread extends Thread {

    private final ForwardMapping forwardMapping;
    EventLoopGroup acceptEventLoopGroup;
    EventLoopGroup workEventLoopGroup;

    public TcpForwardThread(EventLoopGroup acceptEventLoopGroup, EventLoopGroup workEventLoopGroup, ForwardMapping forwardMapping) {
        this.acceptEventLoopGroup = acceptEventLoopGroup;
        this.workEventLoopGroup = workEventLoopGroup;
        this.forwardMapping = forwardMapping;
    }

    @Override
    public void run() {
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(acceptEventLoopGroup, workEventLoopGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, false)
                    .childOption(ChannelOption.AUTO_READ, false)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.SO_BACKLOG, ConfigMapping.ioMaxBacklog);
            if (ConfigMapping.openLoggingHandler) {
                bootstrap.handler(Handlers.LOGGING_HANDLER);
            }
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) {
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    if (ConfigMapping.openLoggingHandler) {
                        pipeline.addFirst(Handlers.LOGGING_HANDLER);
                    }
                    pipeline.addLast(new TcpForwardHandler(forwardMapping));
                }
            });
            //如果本地地址为localhost 则绑定所有地址
            ChannelFuture channelFuture;
            if (forwardMapping.getLocalAddress().equalsIgnoreCase("localhost")) {
                channelFuture = bootstrap.bind(forwardMapping.getLocalPort());
            } else channelFuture = bootstrap.bind(forwardMapping.getLocalAddress(), forwardMapping.getLocalPort());
            channelFuture.addListener((ChannelFutureListener) channelFuture0 -> {
                if (!channelFuture0.isSuccess()) {
                    return;
                }
                log.info(String.valueOf(forwardMapping));

            });
        } catch (Exception e) {
            log.error("failed {}", forwardMapping, e);
        }
    }
}
