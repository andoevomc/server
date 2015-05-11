package dreamgame.gameserver.framework.protocol;

import java.util.Hashtable;

public abstract class AbstractPackageProtocol
  implements IPackageProtocol
{
  private final Hashtable<Integer, IMessageProtocol> mMessages;
  @SuppressWarnings("unchecked")
  public AbstractPackageProtocol()
  {
    this.mMessages = new Hashtable();
  }

  public IMessageProtocol getMessageProtocol(int aMsgId) {
    IMessageProtocol msgProtocol = (IMessageProtocol)this.mMessages.get(Integer.valueOf(aMsgId));
    return msgProtocol;
  }

  void addMessageProtocol(int aMsgId, IMessageProtocol msgData)
  {
    if (msgData != null)
    {
      this.mMessages.put(Integer.valueOf(aMsgId), msgData);
    }
  }
}