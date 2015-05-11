package dreamgame.gameserver.framework.db;

public abstract interface IConnectionManagerFactory
{
  public abstract IConnectionManager createConnectionManager()
    throws DBException;

  public abstract DatabaseModel createDatabaseMode()
    throws DBException;
}