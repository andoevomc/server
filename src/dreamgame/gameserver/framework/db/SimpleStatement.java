package dreamgame.gameserver.framework.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

public class SimpleStatement extends AbstractStatement
{
  private PreparedStatement mPS;

  public SimpleStatement(PreparedStatement aPS)
  {
    this.mPS = aPS;
  }

  protected IResultSet createQuery() throws DBException
  {
    ResultSet rs;
    try
    {
      rs = this.mPS.executeQuery();
      return new SimpleResultSet(rs);
    }
    catch (Exception ex) {
      throw new DBException(ex);
    }
  }

  protected void closeOnly()
    throws DBException
  {
    try
    {
      this.mPS.close();
    }
    catch (Exception ex) {
      throw new DBException(ex);
    }
  }

  public int executeUpdate() throws DBException
  {
    try
    {
      return this.mPS.executeUpdate();
    }
    catch (Exception ex) {
      throw new DBException(ex);
    }
  }

  public void setInt(int aParamIndex, int aValue) throws DBException
  {
    try
    {
      this.mPS.setInt(aParamIndex, aValue);
    }
    catch (Exception ex) {
      throw new DBException(ex);
    }
  }

  public void setByte(int aParamIndex, byte aValue) throws DBException
  {
    try
    {
      this.mPS.setByte(aParamIndex, aValue);
    }
    catch (Exception ex) {
      throw new DBException(ex);
    }
  }

  public void setBytes(int aParamIndex, byte[] aValue) throws DBException
  {
    try
    {
      this.mPS.setBytes(aParamIndex, aValue);
    }
    catch (Exception ex) {
      throw new DBException(ex);
    }
  }

  public void setString(int aParamIndex, String aValue) throws DBException
  {
    try
    {
      this.mPS.setString(aParamIndex, aValue);
    }
    catch (Exception ex) {
      throw new DBException(ex);
    }
  }

  public void setBoolean(int aParamIndex, boolean aValue) throws DBException
  {
    try
    {
      this.mPS.setBoolean(aParamIndex, aValue);
    }
    catch (Exception ex) {
      throw new DBException(ex);
    }
  }

  public void setLong(int aParamIndex, long aValue) throws DBException
  {
    try
    {
      this.mPS.setLong(aParamIndex, aValue);
    }
    catch (Exception ex) {
      throw new DBException(ex);
    }
  }

  public void setDouble(int aParamIndex, double aValue) throws DBException
  {
    try
    {
      this.mPS.setDouble(aParamIndex, aValue);
    }
    catch (Exception ex) {
      throw new DBException(ex);
    }
  }

  public void setFloat(int aParamIndex, float aValue) throws DBException
  {
    try
    {
      this.mPS.setFloat(aParamIndex, aValue);
    }
    catch (Exception ex) {
      throw new DBException(ex);
    }
  }

  public void setDatetime(int aParamIndex, Date aValue) throws DBException
  {
    long timeVal;
    try {
      timeVal = aValue.getTime();
      Timestamp ts = new Timestamp(timeVal);
      this.mPS.setTimestamp(aParamIndex, ts);
    }
    catch (Exception ex) {
      throw new DBException(ex);
    }
  }
}