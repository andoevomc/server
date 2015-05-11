package dreamgame.gameserver.framework.protocol;

import dreamgame.gameserver.framework.session.ISession;
import java.util.Vector;

public class SimpleResponsePackage
  implements IResponsePackage
{
  protected final Vector<IResponseMessage> mResponseMsgs;
  private PackageHeader mHeader;
  @SuppressWarnings("unchecked")
  public SimpleResponsePackage()
  {
    this.mResponseMsgs = new Vector();
    this.mHeader = new PackageHeader();
  }

  public synchronized void addMessage(IResponseMessage aResponseMsg)
  {
    synchronized (this.mResponseMsgs)
    {
      this.mResponseMsgs.add(aResponseMsg);
    }
  }

  public synchronized void addPackage(IResponsePackage aResPkg)
  {
    if ((aResPkg != null) && (aResPkg instanceof SimpleResponsePackage))
    {
      synchronized (this.mResponseMsgs)
      {
        SimpleResponsePackage abstractResPkg = (SimpleResponsePackage)aResPkg;
        synchronized (abstractResPkg.mResponseMsgs)
        {
          this.mResponseMsgs.addAll(abstractResPkg.mResponseMsgs);
          abstractResPkg.mResponseMsgs.clear();
        }
      }
    }
  }

  public PackageHeader getResponseHeader()
  {
    if (this.mHeader == null)
    {
      this.mHeader = new PackageHeader();
    }
    return this.mHeader;
  }

  public void prepareEncode(ISession aSession)
  {
    IResponsePackage directMessages = aSession.getDirectMessages();
    synchronized (directMessages)
    {
      addPackage(directMessages);

      aSession.setIsHandling(Boolean.FALSE);
    }
  }
  @SuppressWarnings("unchecked")
  public Vector<IResponseMessage> optAllMessages()
  {
    synchronized (this.mResponseMsgs)
    {
      Vector result = new Vector();
      result.addAll(this.mResponseMsgs);
      this.mResponseMsgs.clear();
      return result;
    }
  }
}