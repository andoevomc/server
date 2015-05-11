package dreamgame.gameserver.framework.servers.servlet;

import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.session.AbstractSession;

public class ServletSession extends AbstractSession
{
  protected boolean writeResponse(byte[] aEncodedData)
    throws ServerException
  {
    synchronized (this)
    {
      if (isClosed())
      {
        throw new ServerException("Can't write into a closed session.");
      }

      return true;
    }
  }

  public void close()
  {
    super.close();
  }

  public boolean isDirect()
  {
    return false;
  }
}