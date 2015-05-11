package dreamgame.gameserver.framework.servers.nettysocket;

//import com.migame.gameserver.framework.common.ILoggerFactory;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.servers.AbstractServer;
import dreamgame.gameserver.framework.session.ISessionFactory;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.util.internal.ExecutorUtil;
import org.slf4j.Logger;

public class NettySocketServer extends AbstractServer {

    @SuppressWarnings("unused")
    private final Logger mLog;
    private ISessionFactory mSessionFactory;

    public NettySocketServer() {
	this.mLog = LoggerContext.getLoggerFactory().getLogger(NettySocketServer.class);
    }
    
    public static ChannelGroup allChannels = new DefaultChannelGroup("time-server");
    NioServerSocketChannelFactory factory;
    ServerBootstrap bootstrap;

    public void stopServer() {
	Channel tt;


	System.out.println("Stop the server now! " + allChannels.getName() + " : " + allChannels.size());
	//allChannels.
	ChannelGroupFuture future = allChannels.close();
	//ChannelGroupFuture future = allChannels.close().awaitUninterruptibly();
	//allChannels.unbind().awaitUninterruptibly();
	//allChannels.disconnect().awaitUninterruptibly();

	future.awaitUninterruptibly();
	try {
	    ExecutorUtil.terminate(workerExecutor);
	    ExecutorUtil.terminate(bossExecutor);

	    //factory.releaseExternalResources();


	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
    ExecutorService bossExecutor;
    ExecutorService workerExecutor;

    protected void startServer() {
	bossExecutor = Executors.newCachedThreadPool();
	workerExecutor = Executors.newCachedThreadPool();

	factory = new NioServerSocketChannelFactory(bossExecutor, workerExecutor);
	bootstrap = new ServerBootstrap(factory);

	bootstrap.setPipelineFactory(new NettySocketPipelineFactory(this.mWorkflow));

	bootstrap.setOption("child.tcpNoDelay", Boolean.valueOf(this.mTcpNoDelay));
	bootstrap.setOption("child.receiveBufferSize", Integer.valueOf(this.mReceiveBufferSize));
	bootstrap.setOption("connectTimeoutMillis", Integer.valueOf(this.mConnectTimeout));

	bootstrap.setOption("reuseAddress", Boolean.valueOf(this.mReuseAddress));

	Channel serverChannel = bootstrap.bind(new InetSocketAddress(this.mPort));

	//tuanha
	allChannels.add(serverChannel);

	ChannelFuture closeFuture = serverChannel.getCloseFuture();



	closeFuture.addListener(new CloseFutureListener(this, this));


    }

    public ISessionFactory getSessionFactory() {
	if (this.mSessionFactory == null) {
	    this.mSessionFactory = new NettySocketSessionFactory();
	}

	return this.mSessionFactory;
    }

    class CloseFutureListener
	implements ChannelFutureListener {

	private AbstractServer mServer;

	public CloseFutureListener(AbstractServer aServer, AbstractServer paramAbstractServer) {
	    this.mServer = aServer;
	}

	public void operationComplete() throws Exception {
	    try {
		this.mServer.stop();
	    } catch (Throwable t) {
		//NettySocketServer.access$000(this.this$0).error("[SERVER STOP]", t);
	    }
	}

	public void operationComplete(ChannelFuture chanel) {
	    try {
		this.mServer.stop();
	    } catch (Throwable t) {
		//NettySocketServer.access$000(this.this$0).error("[SERVER STOP]", t);
	    }
	}
    }
}