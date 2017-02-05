package com.flybattle.battle.server;

import com.flybattle.battle.core.BattleCenter;
import com.flybattle.battle.util.BattleConfig;
import com.flybattle.battle.util.BattleLogger;
import com.flybattle.battle.util.LogUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * Created by wuyingtan on 2017/1/4.
 */
public class BattleServerStart {
    private ServerBootstrap bootstrap;
    private EventLoopGroup boss;
    private EventLoopGroup worker;

    public BattleServerStart() {

    }

    private void init() {
        BattleCenter.getInstance();
        LogUtil.initLogger();
    }


    public void start() {
        init();
        bootstrap = new ServerBootstrap();
        boss = new NioEventLoopGroup();
        worker = new NioEventLoopGroup();
        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 10000)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast("FrameDecoder",new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4));
                        ch.pipeline().addLast(new BattleHandler());
                    }
                });
        try {
            ChannelFuture channelFuture = bootstrap.bind(BattleConfig.SERVER_IP, BattleConfig.SERVER_PORT).sync();
            BattleLogger.info("Battle Server is now running!");
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            BattleLogger.error("battle server failed to start!", e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
            BattleLogger.info("Battle Server shutdown!");
        }
    }

    public static void main(String[] args) {
        BattleServerStart serverStart = new BattleServerStart();
        serverStart.start();
    }

}
