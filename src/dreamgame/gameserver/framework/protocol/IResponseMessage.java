package dreamgame.gameserver.framework.protocol;

public abstract interface IResponseMessage
{
  public abstract int getID();

  public abstract IResponseMessage createNew();
}