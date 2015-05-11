package dreamgame.gameserver.framework.db;

import java.util.Iterator;
import java.util.Vector;

public abstract class AbstractStatement
  implements IStatement
{
  private Vector<IResultSet> mRS = null;
  @SuppressWarnings("unchecked")
  public AbstractStatement()
  {
    this.mRS = new Vector();
  }

  protected abstract IResultSet createQuery() throws DBException;

  public IResultSet executeQuery() throws DBException
  {
    IResultSet rs = createQuery();
    this.mRS.add(rs);
    return rs;
  }

  protected abstract void closeOnly() throws DBException;

  public void close() throws DBException
  {
    freeResources();
    closeOnly();
  }
  @SuppressWarnings("unchecked")
  protected void freeResources() throws DBException
  {
    for (Iterator i$ = this.mRS.iterator(); i$.hasNext(); ) { IResultSet rs = (IResultSet)i$.next();

      rs.close();
    }
  }
}