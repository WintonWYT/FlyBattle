import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Created by wuyingtan on 2017/1/9.
 */
public class BattleServerTest {
    public void connect(int port, String host) throws InterruptedException {
        EventLoopGroup woker = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(woker).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    BattleHandlerTest.handleActive(ctx);
                                    //ctx.close();
                                }

                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    BattleHandlerTest.handleRead(ctx, msg);
                                }
                            });
                        }
                    });

            ChannelFuture f = b.connect(host, port).sync();
            f.channel().closeFuture().sync();
        } finally {
            woker.shutdownGracefully();
        }

    }

    public static void main(String[] args) {
        new Thread(() -> {

            BattleServerTest test = new BattleServerTest();
            try {
                test.connect(9090, "10.18.20.56");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }).start();


    }
}
