package dreamgame.gameserver.framework.protocol.bin;

import dreamgame.gameserver.framework.bytebuffer.ByteBufferFactory;
import dreamgame.gameserver.framework.bytebuffer.IByteBuffer;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.AbstractPackageProtocol;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestPackage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.protocol.PackageHeader;
import dreamgame.gameserver.framework.protocol.SimpleRequestPackage;
import dreamgame.gameserver.framework.session.ISession;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class PackageProtocolBIN extends AbstractPackageProtocol
{
  public IRequestPackage decode(ISession aSession, IByteBuffer aEncodedObj)
    throws ServerException
  {
    IRequestPackage pkgRequest;
    try
    {
      pkgRequest = new SimpleRequestPackage();

      PackageHeader header = pkgRequest.getRequestHeader();

      String sessionId = aEncodedObj.getString();

      header.setSessionID(sessionId);

      while (aEncodedObj.hasRemaining())
      {
        int msgId = aEncodedObj.getInt();

        IMessageProtocol msgProtocol = getMessageProtocol(msgId);

        MessageFactory msgFactory = aSession.getMessageFactory();
        IRequestMessage requestMsg = msgFactory.getRequestMessage(msgId);

        boolean decodedResult = msgProtocol.decode(aEncodedObj, requestMsg);

        if (decodedResult)
        {
          pkgRequest.addMessage(requestMsg);
        }
      }

      return pkgRequest;
    }
    catch (Throwable t) {
      throw new ServerException(t);
    }
  }
  @SuppressWarnings("unchecked")
  public IByteBuffer encode(ISession aSession, IResponsePackage aResPkg)
    throws ServerException
  {
    String sessionId;
    try
    {
      sessionId = aSession.getID();

      Vector pkgData = aResPkg.optAllMessages();

      int size = pkgData.size();
      List msgList = new ArrayList();
      int msgSize = 0;
      for (int i = 0; i < size; ++i)
      {
        IResponseMessage resMsg = (IResponseMessage)pkgData.get(i);

        IMessageProtocol msgProtocol = getMessageProtocol(resMsg.getID());

        IByteBuffer msgBuffer = (IByteBuffer)msgProtocol.encode(resMsg);
        msgBuffer.flip();

        msgList.add(msgBuffer);
        msgSize += msgBuffer.remaining();
      }

      String pkgFormat = aSession.getPackageFormat();

      int dataSize = sessionId.getBytes("utf-8").length + 2 + pkgFormat.getBytes("utf-8").length + 2 + msgSize;
      IByteBuffer pkgBuffer = ByteBufferFactory.allocate(dataSize + 4);

      pkgBuffer.putInt(dataSize);
      pkgBuffer.putString(pkgFormat);

      pkgBuffer.putString(sessionId);
      for (int i = 0; i < msgList.size(); ++i)
      {
        pkgBuffer.put((IByteBuffer)msgList.get(i));
      }
      pkgBuffer.flip();

      return pkgBuffer;
    }
    catch (Throwable t) {
      throw new ServerException(t);
    }
  }
}