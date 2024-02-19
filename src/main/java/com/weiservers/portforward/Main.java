package com.weiservers.portforward;

import com.weiservers.portforward.mapping.ConfigMapping;
import com.weiservers.portforward.thread.TcpForwardThread;
import com.weiservers.portforward.thread.UdpForwardThread;
import com.weiservers.portforward.utils.ConfigUtils;
import com.weiservers.portforward.utils.ThreadPool;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {


    public static void main(String[] args) {
        log.info("Loading ...");

        ConfigUtils.readToml();
        ConfigUtils.readAdvanced();
        EventLoopGroup TCPacceptEventLoopGroup = new NioEventLoopGroup(ConfigMapping.ioAcceptThreadNumber);
        EventLoopGroup TCPworkEventLoopGroup = new NioEventLoopGroup(ConfigMapping.ioWorkThreadNumber);
        EventLoopGroup UDPacceptEventLoopGroup = new NioEventLoopGroup(ConfigMapping.ioAcceptThreadNumber);
        EventLoopGroup UDPworkEventLoopGroup = new NioEventLoopGroup(ConfigMapping.ioWorkThreadNumber);

        ThreadPool.LoadThreadPool();
        ConfigUtils.readForwards().forEach(forwardMapping -> {
            if (forwardMapping.getType().equalsIgnoreCase("tcp")) {
                ThreadPool.execute(new TcpForwardThread(TCPacceptEventLoopGroup, TCPworkEventLoopGroup, forwardMapping));
            }
            if (forwardMapping.getType().equalsIgnoreCase("udp")) {
                ThreadPool.execute(new UdpForwardThread(UDPacceptEventLoopGroup, UDPworkEventLoopGroup, forwardMapping));
            }
        });


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            TCPacceptEventLoopGroup.shutdownGracefully();
            TCPworkEventLoopGroup.shutdownGracefully();
            UDPacceptEventLoopGroup.shutdownGracefully();
            UDPworkEventLoopGroup.shutdownGracefully();
            ThreadPool.shutdown();
        }));


    }
}