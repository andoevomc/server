package dreamgame.gameserver.framework.protocol;

public abstract interface IRequestMessage
{
  public abstract int getID();

  public abstract boolean isNeedLoggedIn();

  public abstract int getDBFlag();

  public abstract IRequestMessage createNew();

  public abstract IRequestMessage getNext();

  public abstract IRequestMessage getPrevious();

  public abstract void setNext(IRequestMessage paramIRequestMessage);

  public abstract void setPrevious(IRequestMessage paramIRequestMessage);
}