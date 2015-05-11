package dreamgame.gameserver.framework.protocol;

import dreamgame.gameserver.framework.common.ServerException;

public abstract interface IMessageProtocol
{
  public abstract boolean decode(Object paramObject, IRequestMessage paramIRequestMessage)
    throws ServerException;

  public abstract Object encode(IResponseMessage paramIResponseMessage)
    throws ServerException;
}