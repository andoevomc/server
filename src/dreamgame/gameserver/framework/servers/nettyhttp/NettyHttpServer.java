package dreamgame.gameserver.framework.servers.nettyhttp;

//import com.migame.gameserver.framework.common.ILoggerFactory;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.servers.AbstractServer;
import dreamgame.gameserver.framework.session.ISessionFactory;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;

public class NettyHttpServer extends AbstractServer
{
  @SuppressWarnings("unused")
private final Logger mLog;
  private ISessionFactory mSessionFactory;

  public NettyHttpServer()
  {
    this.mLog = LoggerContext.getLoggerFactory().getLogger(NettyHttpServer.class);
  }

  protected void startServer()
  {
    ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));

    bootstrap.setPipelineFactory(new NettyHttpPipelineFactory(this.mWorkflow));

    bootstrap.setOption("child.tcpNoDelay", Boolean.valueOf(this.mTcpNoDelay));
    bootstrap.setOption("child.receiveBufferSize", Integer.valueOf(this.mReceiveBufferSize));
    bootstrap.setOption("connectTimeoutMillis", Integer.valueOf(this.mConnectTimeout));

    bootstrap.setOption("reuseAddress", Boolean.valueOf(this.mReuseAddress));
    Channel serverChannel = bootstrap.bind(new InetSocketAddress(this.mPort));

    ChannelFuture closeFuture = serverChannel.getCloseFuture();
    closeFuture.addListener(new CloseFutureListener(this));
  }

  public ISessionFactory getSessionFactory()
  {
    if (this.mSessionFactory == null)
    {
      this.mSessionFactory = new NettyHttpSessionFactory();
    }

    return this.mSessionFactory;
  }

  class CloseFutureListener
  implements ChannelFutureListener
  {
    private AbstractServer mServer;

    public CloseFutureListener(AbstractServer aServer)
    {
      this.mServer = aServer;
    }

    public  void operationComplete() throws Exception
    {
      try
      {
        this.mServer.stop();
      }
      catch (Throwable t) {

        //NettyHttpServer.access$000(this.this$0).error("[SERVER STOP]", t);
      }
    }

    public  void operationComplete(ChannelFuture channel) throws Exception
    {
      try
      {
        this.mServer.stop();
      }
      catch (Throwable t) {

        //NettyHttpServer.access$000(this.this$0).error("[SERVER STOP]", t);
      }
    }
  }
}