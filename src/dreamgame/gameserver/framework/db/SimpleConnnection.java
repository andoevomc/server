package dreamgame.gameserver.framework.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SimpleConnnection extends AbstractConnection {

    private Connection mConn = null;

    public SimpleConnnection(Connection aConn) {
        this.mConn = aConn;
    }

    protected AbstractStatement createStatement(String aQuery)
            throws DBException {
        PreparedStatement ps;
        try {
            ps = this.mConn.prepareStatement(aQuery);
            return new SimpleStatement(ps);
        } catch (SQLException ex) {
            throw new DBException(ex);
        }
    }

    protected void setTransaction()
            throws DBException {
        try {
            this.mConn.setAutoCommit(false);
        } catch (SQLException ex) {
            throw new DBException(ex);
        }
    }

    protected void removeTransaction()
            throws DBException {
        try {
            this.mConn.setAutoCommit(true);
        } catch (SQLException ex) {
            throw new DBException(ex);
        }
    }

    protected boolean getAutoCommit()
            throws DBException {
        try {
            return this.mConn.getAutoCommit();
        } catch (SQLException ex) {
            throw new DBException(ex);
        }
    }

    protected void commit()
            throws DBException {
        try {
            this.mConn.commit();
        } catch (SQLException ex) {
            throw new DBException(ex);
        }
    }

    protected void rollback()
            throws DBException {
        try {
            this.mConn.rollback();
        } catch (SQLException ex) {
            throw new DBException(ex);
        }
    }

    protected boolean isClosed()
            throws DBException {
        try {
            return this.mConn.isClosed();
        } catch (SQLException ex) {
            throw new DBException(ex);
        }
    }

    protected boolean isValid(int aTimeout)
            throws DBException {
        try {
            return this.mConn.isValid(aTimeout);
        } catch (SQLException ex) {
            throw new DBException(ex);
        }
    }

    protected void closeOnly() throws DBException {
        try {
            if ((this.mConn != null) && (!(this.mConn.isClosed()))) {
                this.mConn.close();
            }
        } catch (Throwable t) {
            throw new DBException(t);
        }
    }
}
