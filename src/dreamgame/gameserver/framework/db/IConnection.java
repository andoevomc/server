package dreamgame.gameserver.framework.db;

public abstract interface IConnection
{
  public abstract IStatement prepareStatement(String paramString)
    throws DBException;

  public abstract boolean isTransaction()
    throws DBException;
}