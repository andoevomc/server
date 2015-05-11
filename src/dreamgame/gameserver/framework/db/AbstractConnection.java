package dreamgame.gameserver.framework.db;

//import com.migame.gameserver.framework.common.ILoggerFactory;
import dreamgame.gameserver.framework.common.LoggerContext;
import java.util.Iterator;
import java.util.Vector;
import org.slf4j.Logger;

public abstract class AbstractConnection
  implements IConnection
{
  private final Logger log = LoggerContext.getLoggerFactory().getLogger(AbstractConnection.class);
  private Vector<IStatement> mPS = null;

  @SuppressWarnings("unchecked")
public AbstractConnection()
  {
    this.mPS = new Vector();
  }

  protected abstract IStatement createStatement(String paramString)
    throws DBException;

  public IStatement prepareStatement(String aQuery)
    throws DBException
  {
    IStatement ps = createStatement(aQuery);
    this.mPS.add(ps);
    return ps;
  }

  public boolean isTransaction() throws DBException
  {
    return getAutoCommit();
  }

  public void endConnection(boolean aIsCommit)
    throws DBException
  {
    freeResources();

    boolean autoCommit = getAutoCommit();

    if (!(autoCommit))
    {
      if (aIsCommit == true)
      {
        commit();
      }
      else
        rollback();
    }
  }

  public void close(boolean aIsCommit)
  {
    boolean autoCommit;
    try
    {
      autoCommit = getAutoCommit();

      if (!(autoCommit))
      {
        endConnection(aIsCommit);

        removeTransaction();
      }
      else {
        freeResources();
      }

      closeOnly();
    }
    catch (Throwable t) {
      this.log.error("[CLOSE DB] Close db connection error.", t);
    }
  }

  public void close()
  {
    try
    {
      freeResources();

      closeOnly();
    }
    catch (Throwable t) {
      this.log.error("[CLOSE DB] Close db connection error.", t);
    }
  }

  protected abstract void closeOnly()
    throws DBException;
  @SuppressWarnings("unchecked")
  protected void freeResources()
    throws DBException
  {
    for (Iterator i$ = this.mPS.iterator(); i$.hasNext(); ) { IStatement ps = (IStatement)i$.next();

      ps.close();
    }
  }

  protected abstract void setTransaction()
    throws DBException;

  protected abstract void removeTransaction()
    throws DBException;

  protected abstract boolean getAutoCommit()
    throws DBException;

  protected abstract void commit()
    throws DBException;

  protected abstract void rollback()
    throws DBException;

  protected abstract boolean isClosed()
    throws DBException;

  protected abstract boolean isValid(int paramInt)
    throws DBException;
}