package dreamgame.gameserver.framework.db;

public abstract interface IConnectionManager
{
  public abstract IConnectionMaker getConnectionMaker()
    throws DBException;

  public abstract IConnection openConnection()
    throws DBException;

  public abstract IConnection openTransactionConnection()
    throws DBException;

  public abstract void closeConnection(IConnection paramIConnection);

  public abstract void closeConnection(IConnection paramIConnection, boolean paramBoolean);

  public abstract void destroy();

  public abstract void init(String paramString)
    throws DBException;
}