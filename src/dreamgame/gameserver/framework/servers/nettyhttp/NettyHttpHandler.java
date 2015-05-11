package dreamgame.gameserver.framework.servers.nettyhttp;

import dreamgame.gameserver.framework.bytebuffer.ByteBufferFactory;
import dreamgame.gameserver.framework.bytebuffer.IByteBuffer;
//import com.migame.gameserver.framework.common.ILoggerFactory;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.session.ISession;
import dreamgame.gameserver.framework.workflow.IWorkflow;
import java.util.Iterator;
import java.util.Set;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;

public class NettyHttpHandler extends SimpleChannelUpstreamHandler
{
  private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(NettyHttpHandler.class);
  private IWorkflow mWorkflow;

  public NettyHttpHandler(IWorkflow aWorkflow)
  {
    this.mWorkflow = aWorkflow;
  }
  @SuppressWarnings("unchecked")
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception
  {
    HttpRequest request;
    try
    {
      request = (HttpRequest)e.getMessage();

      if (!(request.isChunked()))
      {
        ISession session = (ISession)ctx.getAttachment();

        byte[] resData = null;

        ChannelBuffer content = request.getContent();
        if (content.readable())
        {
          byte[] data = content.array();

          IByteBuffer dataBuffer = ByteBufferFactory.wrap(data);
          IByteBuffer resultBuffer = this.mWorkflow.process(session, dataBuffer);
          if (resultBuffer != null)
          {
            resData = resultBuffer.array();
          }

        }

        if ((!(session.isClosed())) && (resData != null))
        {
          boolean isDirect = session.isDirect();

          HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
          response.setContent(ChannelBuffers.copiedBuffer(resData));
          response.setHeader("content-type", "text/plain; charset=UTF-8");

          if (isDirect)
          {
            response.setHeader("content-length", Integer.valueOf(response.getContent().readableBytes()));
          }

          String cookieString = request.getHeader("cookie");
          if (cookieString != null)
          {
            CookieDecoder cookieDecoder = new CookieDecoder();
            Set cookies = cookieDecoder.decode(cookieString);
            if (!(cookies.isEmpty()))
            {
              CookieEncoder cookieEncoder = new CookieEncoder(true);
              for (Iterator i$ = cookies.iterator(); i$.hasNext(); ) { Cookie cookie = (Cookie)i$.next();

                cookieEncoder.addCookie(cookie);
              }
              response.addHeader("set-cookie", cookieEncoder.encode());
            }

          }

          ChannelFuture future = e.getChannel().write(response);

          if (!(isDirect))
          {
            future.addListener(ChannelFutureListener.CLOSE);
          }
        }
      }
    }
    catch (ServerException se) {
      this.mLog.error("[Netty HTTP] Request Process Error", se);
    }
  }

  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
    throws Exception
  {
    this.mLog.error("[Netty HTTP] Unexpected Exception: ", e.getCause().toString());
    e.getChannel().close();
  }

  public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
    throws Exception
  {
    Channel currentChannel;
    try
    {
      currentChannel = e.getChannel();
      ISession session = this.mWorkflow.sessionCreated(currentChannel);

      ctx.setAttachment(session);

      super.channelConnected(ctx, e);
      this.mLog.debug("[Netty HTTP] Channel Connected: " + session.getID() + ", " + currentChannel.getId());
    }
    catch (ServerException se) {
      this.mLog.error("[Netty HTTP] Channel Connected Exception", se);
    }
  }

  public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e)
    throws Exception
  {
    ISession session = (ISession)ctx.getAttachment();
    if (session != null)
    {
      this.mLog.debug("Channel Disconnected: " + session.getID());
      session.sessionClosed();
    }
    super.channelDisconnected(ctx, e);
  }

  public void channelBound(ChannelHandlerContext ctx, ChannelStateEvent e)
    throws Exception
  {
    super.channelBound(ctx, e);
  }

  public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
    throws Exception
  {
    super.channelClosed(ctx, e);
  }

  public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
    throws Exception
  {
    super.channelOpen(ctx, e);
  }

  public void channelUnbound(ChannelHandlerContext ctx, ChannelStateEvent e)
    throws Exception
  {
    super.channelUnbound(ctx, e);
  }
}