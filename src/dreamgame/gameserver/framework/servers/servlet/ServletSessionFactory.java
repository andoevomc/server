package dreamgame.gameserver.framework.servers.servlet;

import dreamgame.gameserver.framework.session.ISession;
import dreamgame.gameserver.framework.session.ISessionFactory;

public class ServletSessionFactory
  implements ISessionFactory
{
  public ISession createSession()
  {
    return new ServletSession();
  }
}