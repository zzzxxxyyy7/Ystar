package ystar.im.core.server.starter;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import ystar.im.core.server.common.ChannelHandlerContextCache;
import ystar.im.core.server.common.ImMsgDecoder;
import ystar.im.core.server.common.ImMsgEncoder;
import ystar.im.core.server.handler.ImServerCoreHandler;

@Configuration
@RefreshScope
public class NettyImServerStarter implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyImServerStarter.class);

    //要监听的端口
    @Value("${ystar.im.port}")
    private int port;

    @Value("${ystar.im.server.ip}")
    private String serverIpAddress;

    /**
     * 用来获取环境变量，获取启动参数
     */
    @Resource
    private Environment environment;

    @Resource
    private ImServerCoreHandler imServerCoreHandler;

    //基于Netty去启动一个java进程，绑定监听的端口
    public void startApplication() throws InterruptedException {

        //处理accept事件
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();

        //处理read&write事件
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);

        //netty初始化相关的handler
        bootstrap.childHandler(new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                //打印日志，方便观察
                LOGGER.info("初始化连接渠道");
                //设计消息体ImMsg
                //添加编解码器
                channel.pipeline().addLast(new ImMsgEncoder());
                channel.pipeline().addLast(new ImMsgDecoder());
                //设置这个netty处理handler
                channel.pipeline().addLast(imServerCoreHandler);
            }
        });

        //基于JVM的钩子函数去实现优雅关闭
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }));

        /**
         * 服务地址和IP端口存入本地内存
         */
        ChannelHandlerContextCache.setServerIpAddress(serverIpAddress);

        ChannelFuture channelFuture = bootstrap.bind(port).sync();
        LOGGER.info("Netty服务启动成功，监听端口为{}", port);

        //这里会阻塞主线程，实现服务长期开启的效果
        channelFuture.channel().closeFuture().sync();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        new Thread(() -> {
            try {
                startApplication();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, "ystar-im-server").start();
    }
}