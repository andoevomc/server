package dreamgame.gameserver.framework.servers.nettysocket;

import dreamgame.gameserver.framework.session.ISession;
import dreamgame.gameserver.framework.session.ISessionFactory;

public class NettySocketSessionFactory
  implements ISessionFactory
{
  public ISession createSession()
  {
    return new NettySocketSession();
  }
}