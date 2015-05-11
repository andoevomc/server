package dreamgame.gameserver.framework.db;

public abstract class AbstractConnectionManager
  implements IConnectionManager
{
  public IConnection openConnection()
    throws DBException
  {
    IConnectionMaker connectionMaker = getConnectionMaker();
    IConnection result = connectionMaker.makeConnection();

    return result;
  }

  public IConnection openTransactionConnection() throws DBException
  {
    AbstractConnection conn = (AbstractConnection)openConnection();
    if (conn.getAutoCommit())
    {
      conn.setTransaction();
    }
    return conn;
  }

  public void closeConnection(IConnection aConn)
  {
    if (aConn != null)
    {
      ((AbstractConnection)aConn).close();
    }
  }

  public void closeConnection(IConnection aConn, boolean aIsCommit)
  {
    if (aConn != null)
    {
      ((AbstractConnection)aConn).close(aIsCommit);
    }
  }
}