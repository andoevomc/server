package dreamgame.gameserver.framework.protocol;

import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.session.ISession;

public abstract interface IBusiness
{
  public static final int PROCESS_UNSUPPORTED = 0;
  public static final int PROCESS_OK = -1;
  public static final int PROCESS_FAILURE = -2;
  public static final int PROCESS_IGNORE = -3;
  public static final int PROCESS_ABORT = -4;

  public abstract int handleMessage(ISession paramISession, IRequestMessage paramIRequestMessage, IResponsePackage paramIResponsePackage)
    throws ServerException;
}