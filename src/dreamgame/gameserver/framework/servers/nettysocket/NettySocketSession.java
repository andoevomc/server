package dreamgame.gameserver.framework.servers.nettysocket;

import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.session.AbstractSession;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

public class NettySocketSession extends AbstractSession
{
    //change by tuanha remove Synchronize. Fix Deadlocks
    
  protected  boolean writeResponse(byte[] aEncodedData)
    throws ServerException
  {
    boolean isDirect;
    try
    {
      isDirect = isDirect();

      ChannelBuffer responseBuffer = ChannelBuffers.copiedBuffer(aEncodedData);

      Channel outChannel = (Channel)getProtocolOutput();
      if ((outChannel != null) && (outChannel.isOpen()))
      {
        synchronized (outChannel)
        {

            //System.out.println("Vao day:"+new String(aEncodedData));
          ChannelFuture future = outChannel.write(responseBuffer);

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
    return true;
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