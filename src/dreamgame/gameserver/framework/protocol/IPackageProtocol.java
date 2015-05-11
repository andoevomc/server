package dreamgame.gameserver.framework.protocol;

import dreamgame.gameserver.framework.bytebuffer.IByteBuffer;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.session.ISession;

public abstract interface IPackageProtocol
{
  public abstract IMessageProtocol getMessageProtocol(int paramInt);

  public abstract IRequestPackage decode(ISession paramISession, IByteBuffer paramIByteBuffer)
    throws ServerException;

  public abstract IByteBuffer encode(ISession paramISession, IResponsePackage paramIResponsePackage)
    throws ServerException;
}