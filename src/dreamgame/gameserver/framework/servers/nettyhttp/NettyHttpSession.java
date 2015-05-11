package dreamgame.gameserver.framework.servers.nettyhttp;

import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.session.AbstractSession;
import java.util.Iterator;
import java.util.Set;
//import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

public class NettyHttpSession extends AbstractSession
{
	  @SuppressWarnings("unchecked")
  protected synchronized boolean writeResponse(byte[] aEncodedData)
    throws ServerException
  {
    boolean isDirect;
    try
    {
      isDirect = isDirect();

      HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
      response.setContent(ChannelBuffers.copiedBuffer(aEncodedData));
      response.setHeader("content-type", "text/plain; charset=UTF-8");

      if (isDirect)
      {
        response.setHeader("content-length", Integer.valueOf(response.getContent().readableBytes()));
      }

      String cookieString = getCookies();
      if (cookieString != null)
      {
        CookieDecoder cookieDecoder = new CookieDecoder();

        Set<Cookie> cookies = cookieDecoder.decode(cookieString);
        if (!(cookies.isEmpty()))
        {
          CookieEncoder cookieEncoder = new CookieEncoder(true);
          for (Iterator it = cookies.iterator(); it.hasNext(); ) { Cookie cookie = (Cookie)it.next();

            cookieEncoder.addCookie(cookie);
          }
          response.addHeader("set-cookie", cookieEncoder.encode());
        }

      }

      Channel outChannel = (Channel)getProtocolOutput();
      if ((outChannel != null) && (outChannel.isOpen()))
      {
        synchronized (outChannel)
        {
          ChannelFuture future = outChannel.write(response);

          if (!(isDirect))
          {
            future.addListener(ChannelFutureListener.CLOSE);
          }
        }
      }

      return true;
    }
    catch (Throwable t) {
      throw new ServerException(t);
    }
  }

  public boolean isDirect()
  {
    return false;
  }

  public void close()
  {
    Channel outChannel = (Channel)getProtocolOutput();
    if ((outChannel != null) && (outChannel.isOpen()))
    {
      synchronized (outChannel)
      {
        outChannel.close();
      }
    }
    super.close();
  }
}