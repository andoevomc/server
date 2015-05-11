package dreamgame.gameserver.framework.db;

import java.util.Date;

public abstract interface IStatement
{
  public abstract IResultSet executeQuery()
    throws DBException;

  public abstract int executeUpdate()
    throws DBException;

  public abstract void setInt(int paramInt1, int paramInt2)
    throws DBException;

  public abstract void setByte(int paramInt, byte paramByte)
    throws DBException;

  public abstract void setBytes(int paramInt, byte[] paramArrayOfByte)
    throws DBException;

  public abstract void setString(int paramInt, String paramString)
    throws DBException;

  public abstract void setBoolean(int paramInt, boolean paramBoolean)
    throws DBException;

  public abstract void setLong(int paramInt, long paramLong)
    throws DBException;

  public abstract void setDouble(int paramInt, double paramDouble)
    throws DBException;

  public abstract void setFloat(int paramInt, float paramFloat)
    throws DBException;

  public abstract void setDatetime(int paramInt, Date paramDate)
    throws DBException;

  public abstract void close()
    throws DBException;
}