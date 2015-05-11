package dreamgame.gameserver.framework.db;

import java.util.Date;

public abstract interface IResultSet
{
  public abstract boolean first()
    throws DBException;

  public abstract boolean last()
    throws DBException;

  public abstract int getRowPosition()
    throws DBException;

  public abstract boolean isAfterLast()
    throws DBException;

  public abstract boolean next()
    throws DBException;

  public abstract int getInt(String paramString)
    throws DBException;

  public abstract byte getByte(String paramString)
    throws DBException;

  public abstract long getLong(String paramString)
    throws DBException;

  public abstract double getDouble(String paramString)
    throws DBException;

  public abstract float getFloat(String paramString)
    throws DBException;

  public abstract boolean getBoolean(String paramString)
    throws DBException;

  public abstract String getString(String paramString)
    throws DBException;

  public abstract byte[] getBytes(String paramString)
    throws DBException;

  public abstract Date getDatetime(String paramString)
    throws DBException;

  public abstract Date getTimestamp(String paramString)
    throws DBException;

  public abstract void close()
    throws DBException;
}