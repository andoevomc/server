package dreamgame.gameserver.framework.servers.nettyhttp;

import dreamgame.gameserver.framework.session.ISession;
import dreamgame.gameserver.framework.session.ISessionFactory;

public class NettyHttpSessionFactory
  implements ISessionFactory
{
  public ISession createSession()
  {
    return new NettyHttpSession();
  }
}