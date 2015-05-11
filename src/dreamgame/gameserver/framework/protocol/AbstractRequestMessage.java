package dreamgame.gameserver.framework.protocol;

public abstract class AbstractRequestMessage
  implements IRequestMessage
{
  private int mMsgId;
  private boolean mIsNeedLoggedIn;
  private int mDBFlag;
  private IRequestMessage mNext;
  private IRequestMessage mPrevious;

  public int getID()
  {
    return this.mMsgId;
  }

  void setID(int aMsgId)
  {
    this.mMsgId = aMsgId;
  }

  public boolean isNeedLoggedIn()
  {
    return this.mIsNeedLoggedIn;
  }

  void setNeedLoggedIn(boolean aIsNeedLoggedIn)
  {
    this.mIsNeedLoggedIn = aIsNeedLoggedIn;
  }

  public int getDBFlag()
  {
    return this.mDBFlag;
  }

  void setDBFlag(int aDBFlag)
  {
    this.mDBFlag = aDBFlag;
  }

  public IRequestMessage getNext()
  {
    return this.mNext;
  }

  public IRequestMessage getPrevious()
  {
    return this.mPrevious;
  }

  public void setNext(IRequestMessage aRequestMsg)
  {
    this.mNext = aRequestMsg;
  }

  public void setPrevious(IRequestMessage aRequestMsg)
  {
    this.mPrevious = aRequestMsg;
  }

  protected AbstractRequestMessage clone()
  {
    AbstractRequestMessage newMsg = (AbstractRequestMessage)createNew();
    newMsg.setID(this.mMsgId);
    newMsg.setNeedLoggedIn(this.mIsNeedLoggedIn);
    newMsg.setDBFlag(this.mDBFlag);
    return newMsg;
  }
}