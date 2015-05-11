package dreamgame.gameserver.framework.protocol;

import dreamgame.gameserver.framework.session.ISession;
import java.util.Vector;

public abstract interface IResponsePackage
{
  public abstract PackageHeader getResponseHeader();

  public abstract void addMessage(IResponseMessage paramIResponseMessage);

  public abstract void addPackage(IResponsePackage paramIResponsePackage);

  public abstract void prepareEncode(ISession paramISession);

  public abstract Vector<IResponseMessage> optAllMessages();
}