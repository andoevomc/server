package dreamgame.gameserver.framework.db;

import java.sql.ResultSet;
import java.util.Date;

public class SimpleResultSet
  implements IResultSet
{
  private ResultSet mRS = null;

  public SimpleResultSet(ResultSet aRS)
  {
    this.mRS = aRS;
  }

  public boolean first() throws DBException
  {
    try
    {
      return this.mRS.first();
    }
    catch (Exception ex) {
      throw new DBException(ex);
    }
  }

  public boolean last() throws DBException
  {
    try
    {
      return this.mRS.last();
    }
    catch (Exception ex) {
      throw new DBException(ex);
    }
  }

  public int getRowPosition() throws DBException
  {
    try
    {
      return this.mRS.getRow();
    }
    catch (Exception ex) {
      throw new DBException(ex);
    }
  }

  public boolean isAfterLast() throws DBException
  {
    try
    {
      return this.mRS.isAfterLast();
    }
    catch (Exception ex) {
      throw new DBException(ex);
    }
  }

  public boolean next() throws DBException
  {
    try
    {
      return this.mRS.next();
    }
    catch (Exception ex) {
      throw new DBException(ex);
    }
  }

  public int getInt(String aColName) throws DBException
  {
    try
    {
      return this.mRS.getInt(aColName);
    }
    catch (Exception ex) {
      throw new DBException(ex);
    }
  }

  public byte getByte(String aColName) throws DBException
  {
    try
    {
      return this.mRS.getByte(aColName);
    }
    catch (Exception ex) {
      throw new DBException(ex);
    }
  }

  public long getLong(String aColName) throws DBException
  {
    try
    {
      return this.mRS.getLong(aColName);
    }
    catch (Exception ex) {
      throw new DBException(ex);
    }
  }

  public double getDouble(String aColName) throws DBException
  {
    try
    {
      return this.mRS.getDouble(aColName);
    }
    catch (Exception ex) {
      throw new DBException(ex);
    }
  }

  public float getFloat(String aColName) throws DBException
  {
    try
    {
      return this.mRS.getFloat(aColName);
    }
    catch (Exception ex) {
      throw new DBException(ex);
    }
  }

  public boolean getBoolean(String aColName) throws DBException
  {
    try
    {
      return this.mRS.getBoolean(aColName);
    }
    catch (Exception ex) {
      throw new DBException(ex);
    }
  }

  public String getString(String aColName) throws DBException
  {
    try
    {
      return this.mRS.getString(aColName);
    }
    catch (Exception ex) {
      throw new DBException(ex);
    }
  }

  public byte[] getBytes(String aColName) throws DBException
  {
    try
    {
      return this.mRS.getBytes(aColName);
    }
    catch (Exception ex) {
      throw new DBException(ex);
    }
  }

  public Date getDatetime(String aColName) throws DBException
  {
    try
    {
      return this.mRS.getDate(aColName);
    }
    catch (Exception ex) {
      throw new DBException(ex);
    }
  }

  public Date getTimestamp(String aColName) throws DBException
  {
    try
    {
      return this.mRS.getTimestamp(aColName);
    }
    catch (Exception ex) {
      throw new DBException(ex);
    }
  }

  public void close() throws DBException
  {
    try
    {
      this.mRS.close();
    }
    catch (Exception ex) {
      throw new DBException(ex);
    }
  }
}