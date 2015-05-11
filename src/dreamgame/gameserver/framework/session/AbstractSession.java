package dreamgame.gameserver.framework.session;

import dreamgame.data.CPEntity;
import dreamgame.data.MessagesID;
//import com.migame.protocol.messages.KeepConnectionResponse;
import dreamgame.data.ResponseCode;
import dreamgame.data.SimpleTable;
import dreamgame.data.UserEntity;
import dreamgame.data.ZoneID;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.CancelRequest;
import dreamgame.protocol.messages.GetAvatarListResponse;
import dreamgame.protocol.messages.KeepConnectionResponse;
import dreamgame.protocol.messages.LoginResponse;
import dreamgame.gameserver.framework.bytebuffer.IByteBuffer;
//import com.migame.gameserver.framework.common.ILoggerFactory;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
//import com.migame.gameserver.framework.db.IConnection;
import dreamgame.gameserver.framework.protocol.BusinessProperties;
import dreamgame.gameserver.framework.protocol.IBusiness;
import dreamgame.gameserver.framework.protocol.IPackageProtocol;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.protocol.SimpleResponsePackage;
import dreamgame.gameserver.framework.room.Room;
import dreamgame.gameserver.framework.room.Zone;
import dreamgame.gameserver.framework.room.ZoneManager;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.slf4j.Logger;

public abstract class AbstractSession
        implements ISession {

    // trungnm -> handle big request ( > 1024 bytes)
    
//    private byte[] incompleteData = null;
//    private int completeDataLength = 0;
//    public byte[] getIncompleteData() { return incompleteData; }
//    public void setIncompleteData(byte[] incompleteData) { this.incompleteData = incompleteData; }        
//    public int getCompleteDataLength() { return completeDataLength; }
//    public void setCompleteDataLength(int completeDataLength) { this.completeDataLength = completeDataLength; }
    
    private IByteBuffer[] incompleteData;
    private int incompleteDataMaxChunks = 64;
    private int incompleteDataPointer = 0;
    private int incompleteDataLength;
    private int completeDataLength;
    private byte[] test;

    public void resetIncompleteData() {
        completeDataLength = 0;
        incompleteData = new IByteBuffer[incompleteDataMaxChunks];
        incompleteDataPointer = 0;
        incompleteDataLength = 0;
    }
    
    public IByteBuffer[] getIncompleteData() {
        return incompleteData;
    }

    public void addIncompleteDataChunk(IByteBuffer chunk) {
        if (incompleteData == null) {
            incompleteData = new IByteBuffer[incompleteDataMaxChunks];
            incompleteDataPointer = 0;
            incompleteDataLength = 0;
        }
        incompleteData[incompleteDataPointer] = chunk;
        incompleteDataPointer++;
        incompleteDataLength += chunk.limit();
    }
    
    public int getIncompleteDataLength() {
//        int total = 0;
//        for (IByteBuffer chunk : incompleteData) {
//            if (chunk != null)
//                total += chunk.limit();
//        }
//        return total;
        return incompleteDataLength;
    }

    // merge incomple data array into byte[]
    public byte[] getCompleteData() {
        byte[] result = new byte[incompleteDataLength];
        int pointer = 0;
        for (IByteBuffer chunk : incompleteData) {
            if (chunk != null) {
                while (chunk.hasRemaining()) {
                    result[pointer] = chunk.get();
                    pointer ++;
                }
            }
        }
        return result;
    }
    
    public int getCompleteDataLength() {
        return completeDataLength;
    }

    public void setCompleteDataLength(int completeDataLength) {
        this.completeDataLength = completeDataLength;
    }
    // end trungnm fix receive big data request which spans over several requests
    
    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(AbstractSession.class);
    @SuppressWarnings("unused")
    private final String SESSION_ID = "session.id";
    @SuppressWarnings("unused")
    private final String SESSION_USER_ID = "session.user.id";
    @SuppressWarnings("unused")
    private final String SESSION_USER_NAME = "session.user.name";
    @SuppressWarnings("unused")
    private final String SESSION_BUSINESS_PROPS = "session.business.props";
    @SuppressWarnings("unused")
    private final String SESSION_IS_HANDLING = "session.is.handling";
    @SuppressWarnings("unused")
    private final String SESSION_ATTACHTMENT_OBJECT = "session.attachment.object";
    @SuppressWarnings("unused")
    private final String SESSION_COOKIES = "session.cookies";
    @SuppressWarnings("unused")
    private final String SESSION_MESSAGE_FACTORY = "session.message.factory";
    @SuppressWarnings("unused")
    private final String SESSION_PKG_FORMAT = "session.package.format";
    @SuppressWarnings("unused")
    private final String SESSION_CREATED_TIME = "session.created.time";
    @SuppressWarnings("unused")
    private final String SESSION_LASTACCESS_TIME = "session.lastaccess.time";
    @SuppressWarnings("unused")
    private final String SESSION_TIMEOUT = "session.timeout";
    @SuppressWarnings("unused")
    private final String SESSION_LOGGED_IN = "session.logged.in";
    @SuppressWarnings("unused")
    private final String SESSION_CURRENT_DB_CONNECTION = "session.current.db.connection";
    @SuppressWarnings("unused")
    private final String SESSION_IS_COMMIT = "session.is.commit";
    private ConcurrentHashMap<String, Object> mAttrs;
    private final IResponsePackage mResPkg;
    private boolean mIsClosed = true;
    private SessionManager mSessionMgr;
    private ZoneManager mZoneMgr;
    private final ConcurrentHashMap<Long, Room> mJoinedRooms;
    public boolean isMobile = false;
    public boolean isAndroid = false;
    public boolean isIphone = false;
    private String mobileVersion = "";
    private String screenSize = "";

    @SuppressWarnings("unchecked")
    public AbstractSession() {
        this.mAttrs = new ConcurrentHashMap();
        this.mResPkg = new SimpleResponsePackage();
        this.mJoinedRooms = new ConcurrentHashMap();
        this.mIsClosed = true;

    }

// trungnm
    private long currentMoneyMatch = 0;
    public long getCurrentMoneyMatch() {
	return currentMoneyMatch;
    }
    public void setCurrentMoneyMatch(long currentMoneyMatch) {
	this.currentMoneyMatch = currentMoneyMatch;
    }
    
    public void setCPEntity(CPEntity cpentity) {
	setAttribute("session.cpentity", cpentity);
    }
    
    public CPEntity getCPEntity() {
	CPEntity cpentity = (CPEntity) getAttribute("session.cpentity");
        return cpentity;
    }
    
    public void setCP(String cp) {
        setAttribute("session.cp", cp);
    }

    public String getCP() {
        String cp = (String) getAttribute("session.cp");
        return cp;
    }
    
    public void setLoginTime(Date time) {
        setAttribute("session.loginTime", time);
    }

    public Date getLoginTime() {
        Date time = (Date) getAttribute("session.loginTime");
        return time;
    }
    
    public void setDevice(String device) {
        setAttribute("session.device", device);
    }

    public String getDevice() {
        String device = (String) getAttribute("session.device");
        return device;
    }
    
    public String getDeviceType() {
        if (isAndroid) {
	    return "android";
	} else if (isIphone) {
	    return "ios";
	} else if (isMobile) {
	    return "j2me";
	}
        return "flash";
    }
// end trungnm 
    
//BINHLT
    private int currentZone = -1;

    public int getCurrentZone() {
        return currentZone;
    }

    public void setScreenSize(String screen) {
        this.screenSize = screen;
    }
    public String getScreenSize() {
	return this.screenSize;
    }

    public void setAndroid() {
        isAndroid = true;
    }

    public boolean isAndroid() {
        return isAndroid;
    }

    public void setIphone() {
        isIphone = true;
    }

    public boolean isIphone() {
        return isIphone;
    }

    public void setMobile(String ver) {
        mobileVersion = ver;
        isMobile = true;
    }
    long lastPing = 0;
    public int receive_gift = 0;
    public int remaint_gift = 0;
    public int max_gift = 0;
    public int cash_gift = 0;

    public int getRemainGift() {
        return remaint_gift;
    }

    public int getReceiveGift() {
        return receive_gift;
    }

    public int getCashGift() {
        return cash_gift;
    }

    public int getMaxGift() {
        return max_gift;
    }

    public void setGiftInfo(int receive_gift, int remaint_gift, int max_gift, int cash_gift) {

        this.receive_gift = receive_gift;
        this.remaint_gift = remaint_gift;
        this.max_gift = max_gift;
        this.cash_gift = cash_gift;
    }

    public void setGiftInfo(int receive_gift, int remaint_gift) {
        this.receive_gift = receive_gift;
        this.remaint_gift = remaint_gift;
    }
    String clientType = "";

    public void setClientType(String s) {
        clientType = s;
    }

    public String getClientType() {
        return clientType;
    }

    public long getLastMessage() {
        return lastMessage;
    }
    public FileWriter outFile;// = new FileWriter(args[0]);
    public PrintWriter out;// = new PrintWriter(outFile);

    public void writeMessage(String msg)
            throws ServerException {
        MessageFactory msgFactory = getMessageFactory();
        LoginResponse resLogin = (LoginResponse) msgFactory.getResponseMessage(MessagesID.LOGIN);
        resLogin.setFailure(ResponseCode.FAILURE, msg);
        write(resLogin);
    }

    public void logCode(String msg) {

        if (!DatabaseDriver.log_user) {
            return;
        }
        if (this.getUserName().length() == 0) {
            return;
        }

        try {
            if (out == null) {
                try {
                    File dir1 = new File(".");
                    System.out.println("Current dir : " + dir1.getCanonicalPath());
                    boolean success = (new File("logs/user_log")).mkdirs();
                    System.out.println("Create dir success : " + success);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Calendar c = Calendar.getInstance();

                String dd = "_" + c.get(Calendar.YEAR) + "_" + (c.get(Calendar.MONTH) + 1) + "_" + c.get(Calendar.DAY_OF_MONTH);
                String str = "logs/user_log/user_" + this.getUserName() + dd + ".txt";

                File f = new File(str);
                boolean append = false;
                if (f.exists()) {
                    append = true;
                }


                outFile = new FileWriter(str, append);
                out = new PrintWriter(outFile);
                //System.out.println("matchID : " + matchID + "  ;  " + str);
            }

            if (msg.contains("Received")) {
                out.println();
            }

            out.println(msg + " [ti_" + System.currentTimeMillis() + "]");
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean realExpired() {
        if (lastMessage > 0 && (System.currentTimeMillis() - lastMessage) > (Integer) getAttribute("session.timeout")) {
            return true;
        }
        return false;
    }
    public int channel;

    public void setChannel(int c) {
        channel = c;
    }

    public int getChannel() {
        return channel;
    }
    public String remoteIP = "";

    public void setIP(String ip) {
        remoteIP = ip;
    }

    public String getIP() {
        return remoteIP;
    }

    public boolean realDead() {
        try {

            /*
             * System.out.println("Real Dead !"); System.out.println("lastPing :
             * "+lastPing+" : "+(lastPing - lastMessage));
             * //System.out.println("getLastAccessTime().getTime() :
             * "+getLastAccessTime().getTime()); System.out.println("lastMessage
             * : "+lastMessage);
             */

            if (lastPing != 0 && (lastPing - lastMessage) > 15000) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    private long lastMessage = 0;

    public void receiveMessage() {
        lastMessage = System.currentTimeMillis();
    }

    public void ping(ISession owner) {


        //KeepConnectionRequest k;
        //KeepConnectionResponse;

        System.out.println("Name Owner : " + owner.getUserName() + "  ;  " + owner.getUID());
        System.out.println("This : " + getUserName() + "  ;  " + getUID());

        //MessageFactory msgFactory = owner.getMessageFactory();
        MessageFactory msgFactory = getMessageFactory();
        //KeepConnectionResponse k = new KeepConnectionResponse();
        KeepConnectionResponse k = (KeepConnectionResponse) msgFactory.getResponseMessage(MessagesID.KEEP_CONNECTION);

        //GetPokerResponse k = (GetPokerResponse) msgFactory.getResponseMessage(MessagesID.GET_POKER);
        try {
            if (k == null) {
                System.out.println("Errror :(");
            }

            this.write(k);
            if (!realDead()) {
                lastPing = System.currentTimeMillis();
            }

        } catch (ServerException ex) {
            java.util.logging.Logger.getLogger(AbstractSession.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public boolean getMobile() {
        return isMobile;
    }

    public String getMobileVer() {
        return mobileVersion;
    }
    public boolean active;

    public void setActive(boolean a) {
        active = a;
    }

    public boolean getActive() {
        return active;
    }

    public void setCurrentZone(int currentZone) {
        this.currentZone = currentZone;
    }
    //End

    protected void setAttribute(String aKey, Object aValue) {
        if (aValue == null) {
            this.mAttrs.remove(aKey);
        } else {
            this.mAttrs.put(aKey, aValue);
        }
    }

    protected Object getAttribute(String aKey) {
        Object value = this.mAttrs.get(aKey);
        return value;
    }

    void setID(String aId) {
        String sId = (String) getAttribute("session.id");
        if (sId != null) {
            getManager().removeSession(sId);

            AbstractSession existedSession = (AbstractSession) getManager().removeSession(aId);
            if ((existedSession != null) && (!(existedSession.isClosed()))) {
                this.mAttrs.clear();
                this.mAttrs.putAll(existedSession.mAttrs);

                existedSession.doClose();
            }
        }

        setAttribute("session.id", aId);

        getManager().addSession(aId, this);
    }

    public String getID() {
        String sessionId = (String) getAttribute("session.id");
        return sessionId;
    }

    public void setUID(Long aId) {
        getManager().addUIDSession(aId, this);

        setAttribute("session.user.id", aId);
    }

    public Long getUID() {
        Long uid = (Long) getAttribute("session.user.id");
        if (uid == null) {
            uid = Long.valueOf(0L);
        }
        return uid;
    }

    public void setUserName(String aUserName) {
        setAttribute("session.user.name", aUserName);
    }

    public String getUserName() {
        String userName = (String) getAttribute("session.user.name");
        return userName;
    }

    void setManager(SessionManager aSessionMgr) {
        this.mSessionMgr = aSessionMgr;
    }

    public SessionManager getManager() {
        return this.mSessionMgr;
    }

    public void setBusinessProperties(BusinessProperties aBusinessProps) {
        setAttribute("session.business.props", aBusinessProps);
    }

    public BusinessProperties getBusinessProperties() {
        BusinessProperties businessProps = (BusinessProperties) getAttribute("session.business.props");
        return businessProps;
    }

    public synchronized void close() {
        if (!(isClosed())) {
            long uid = this.getUID();
            DatabaseDriver.updateUserOnline(uid, false);

            String id = getID();
            getManager().sessionClosed(id);

            doClose();

            if (DatabaseDriver.log_user) {
                try {
                    if (out != null) {
                        outFile.close();
                        out.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            this.mLog.debug("[SESSION] Session Closed: " + id);

        }
    }

    private void doClose() {
        this.mIsClosed = true;

        BusinessProperties businessProps = getBusinessProperties();
        if (businessProps != null) {
            businessProps.freeResources();
        }

        this.mAttrs.clear();
    }

    public String userInfo() {
        String s = "";
        if (this.isMobile) {
            s = s + "Mobile:Ver-" + mobileVersion + ":" + screenSize + ":";
        } else {
            s = s + "Flash:";
        }

        s = s + getUserName() + "][" + getIP() + "][" + getUID() + "-" + getCurrentZone();
        return "[" + s + "]";
    }

    public synchronized void sessionClosed() {
        if (!(isClosed())) {
            if ((isDirect()) || (isExpired())) {
                close();
            } else {
                setAttribute("session.attachment.object", null);
            }
        }
    }

    public boolean isClosed() {
        return this.mIsClosed;
    }

    // attach channel to session
    public void sessionCreated(Object aAttachmentObj) {
        setAttribute("session.attachment.object", aAttachmentObj);

        this.mIsClosed = false;
    }

    // get channel to write output to client
    public Object getProtocolOutput() {
        return getAttribute("session.attachment.object");
    }

    public boolean write(Object aObj) throws ServerException {
        if (realDead()) {
            mLog.info(this.getUserName() + " : write to RealDead");
            return false;
        }

        synchronized (this.mResPkg) {
            if (aObj instanceof IResponseMessage) {
                this.mResPkg.addMessage((IResponseMessage) aObj);
            } else if (aObj instanceof IResponsePackage) {
                this.mResPkg.addPackage((IResponsePackage) aObj);
            } else if (aObj instanceof String) {
                String res = (String) aObj;
                return writeResponse(res.getBytes());
            }

            return write();
        }
    }

    @SuppressWarnings("unchecked")
    public boolean write() throws ServerException {

        synchronized (this.mResPkg) {
            if (isDirect()) {
                if (!(isHandling())) {
                    String pkgFormat = getPackageFormat();

                    MessageFactory msgFactory = getMessageFactory();
                    IPackageProtocol pkgProtocol = msgFactory.getPackageProtocol(pkgFormat);

                    IByteBuffer encodedRes = pkgProtocol.encode(this, this.mResPkg);
                    if (encodedRes != null) {
                        setLastAccessTime(Calendar.getInstance().getTime());
                        byte[] resData = encodedRes.array();
                        return writeResponse(resData);
                    }
                    return true;
                }
                return true;
            }

            Vector directMsgs = this.mResPkg.optAllMessages();
            directMsgs.clear();
            return true;
        }
    }

    public static final byte[] intToByteArray(int value) {
        return new byte[]{
                    (byte) (value >>> 24),
                    (byte) (value >>> 16),
                    (byte) (value >>> 8),
                    (byte) value};
    }

    public static final byte[] byteToByteArray(int value) {
        return new byte[]{
                    (byte) value};
    }

    public static final byte[] booleanToByteArray(boolean value) {
	byte b = (byte) (value ? 1 : 0);
	return new byte[] { b };
    }
    
    // string to utf-8 bytes array with first byte indicate the length 
    public static final byte[] stringToByteArray(String str) {
	try {
	    byte[] b = str.getBytes("UTF-8");
	    byte[] result = new byte[ b.length + 1];
	    result[0] = (byte) (b.length);
	    System.arraycopy(b, 0, result, 1, b.length);
	    return result;
	} catch (UnsupportedEncodingException ex) {
	    ex.printStackTrace();
	    return null;
	}
    }
    
    public static final int byteArrayToInt(byte[] b) {
        return (b[0] << 24)
                + ((b[1] & 0xFF) << 16)
                + ((b[2] & 0xFF) << 8)
                + (b[3] & 0xFF);
    }

    public static byte[] appendByte(byte[] source, byte[] append) {
        byte[] res = new byte[source.length + append.length];
        System.arraycopy(source, 0, res, 0, source.length);
        System.arraycopy(append, 0, res, source.length, append.length);

        return res;
    }

    public void writeImage(byte[] data) {
        try {

            writeResponse(data);


        } catch (ServerException ex) {
            ex.printStackTrace();
        }
    }

    protected abstract boolean writeResponse(byte[] paramArrayOfByte)
            throws ServerException;

    public IResponsePackage getDirectMessages() {
        return this.mResPkg;
    }

    public void setIsHandling(Boolean aIsHandling) {
        if (realDead()) {
            mLog.info(this.getUserName() + " : setIsHandling to RealDead");
        }

        synchronized (this.mResPkg) {
            setAttribute("session.is.handling", aIsHandling);
        }
    }

    public boolean isHandling() {
        synchronized (this.mResPkg) {
            Boolean result = (Boolean) getAttribute("session.is.handling");
            if (result == null) {
                result = Boolean.valueOf(false);
            }
            return result.booleanValue();
        }
    }

    public String getCookies() {
        String result = (String) getAttribute("session.cookies");
        return result;
    }

    public void setCookies(String aCookies) {
        setAttribute("session.cookies", aCookies);
    }

    public MessageFactory getMessageFactory() {
        MessageFactory msgFactory = (MessageFactory) getAttribute("session.message.factory");
        return msgFactory;
    }

    public void setMessageFactory(MessageFactory aMsgFactory) {
        setAttribute("session.message.factory", aMsgFactory);
    }

    public String getPackageFormat() {
        String pkgFormat = (String) getAttribute("session.package.format");
        return pkgFormat;
    }

    public void setPackageFormat(String aPkgFormat) {
        setAttribute("session.package.format", aPkgFormat);
    }

    public Date getCreatedTime() {
        Date createdTime = (Date) getAttribute("session.created.time");
        return createdTime;
    }

    public void setCreatedTime(Date aCreatedTime) {
        setAttribute("session.created.time", aCreatedTime);
    }

    public Date getLastAccessTime() {
        Date lastAccessTime = (Date) getAttribute("session.lastaccess.time");
        return lastAccessTime;
    }

    public void setLastAccessTime(Date aLastAccessTime) {
        setAttribute("session.lastaccess.time", aLastAccessTime);
    }

    public void setTimeout(Integer aMiliSeconds) {
        setAttribute("session.timeout", aMiliSeconds);
    }

    public boolean isExpired() {
        Date lastAccessTime = getLastAccessTime();
        if (lastAccessTime == null) {
            lastAccessTime = getCreatedTime();
            if (lastAccessTime == null) {
                return false;
            }
        }

        Integer timeout = (Integer) getAttribute("session.timeout");
        if (timeout == null) {
            timeout = Integer.valueOf(0);
        }

        long lastTimeout = lastAccessTime.getTime() + timeout.intValue();

        long now = Calendar.getInstance().getTimeInMillis();

        return (lastTimeout < now);
    }

    public void setLoggedIn(Boolean aIsLoggedIn) {
        setAttribute("session.logged.in", aIsLoggedIn);
    }

    public boolean isLoggedIn() {
        Boolean isLoggedIn = (Boolean) getAttribute("session.logged.in");
        if (isLoggedIn == null) {
            isLoggedIn = Boolean.FALSE;
        }
        return isLoggedIn.booleanValue();
    }

    /*
     * public void setCurrentDBConnection(IConnection aConn) {
     * setAttribute("session.current.db.connection", aConn); }
     *
     * public IConnection getCurrentDBConnection() { IConnection result =
     * (IConnection)getAttribute("session.current.db.connection"); return
     * result; }
     */
    public void setCommit(boolean aIsCommit) {
        setAttribute("session.is.commit", Boolean.valueOf(aIsCommit));
    }

    public boolean isCommit() {
        Boolean result = (Boolean) getAttribute("session.is.commit");
        if (result == null) {
            result = Boolean.FALSE;
        }
        return result.booleanValue();
    }

    public void joinedRoom(Room aRoom) {
        if ((aRoom != null) && (aRoom.getRoomId() > 0L)) {
            synchronized (this.mJoinedRooms) {
                this.mJoinedRooms.put(Long.valueOf(aRoom.getRoomId()), aRoom);
            }
        }
    }

    public Room findJoinedRoom(long aRoomId) {
        synchronized (this.mJoinedRooms) {
            return ((Room) this.mJoinedRooms.get(Long.valueOf(aRoomId)));
        }
    }

    public void leaveAllRoom(IResponsePackage aResPkg) {

        Vector<Room> joinedRoom = getJoinedRooms();
        MessageFactory msgFactory = getMessageFactory();
        if (joinedRoom.size() > 0) {
            for (Room r : joinedRoom) {
                //Room r = joinedRoom.firstElement();
                System.out.println("User is in room : " + r.getName());
                SimpleTable p = (SimpleTable) r.getAttactmentData();
                System.out.println("room : p.isPlaying : " + p.isPlaying);
                if (true) {
                    //remove player from room!
                    CancelRequest rqBoc = (CancelRequest) msgFactory.getRequestMessage(MessagesID.MATCH_CANCEL);
                    rqBoc.mMatchId = r.getRoomId();
                    rqBoc.uid = getUID();
                    IBusiness business = msgFactory.getBusiness(MessagesID.MATCH_CANCEL);
                    try {
                        business.handleMessage(this, rqBoc, aResPkg);
                    } catch (ServerException ex) {
                        java.util.logging.Logger.getLogger(AbstractSession.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

    }
    UserEntity realUser;

    public UserEntity getUserEntity() {
        return realUser;
    }

    public void setUserEntity(UserEntity u) {
        realUser = u;
    }

    public Room leftRoom(long aRoomId) {
        synchronized (this.mJoinedRooms) {
            return ((Room) this.mJoinedRooms.remove(Long.valueOf(aRoomId)));
        }
    }

    @SuppressWarnings("unchecked")
    public Vector<Room> getJoinedRooms() {
        Enumeration eRooms = null;
        synchronized (this.mJoinedRooms) {
            eRooms = this.mJoinedRooms.elements();
        }
        Vector joinedRooms = new Vector();
        while (eRooms.hasMoreElements()) {
            Room aRoom = (Room) eRooms.nextElement();
            joinedRooms.add(aRoom);
        }
        return joinedRooms;
    }

    @SuppressWarnings("unchecked")
    public boolean isJoinedFull(int aZoneId) {
        int joinedZone = 0;

        Enumeration eRooms = null;
        synchronized (this.mJoinedRooms) {
            eRooms = this.mJoinedRooms.elements();
        }
        while (eRooms.hasMoreElements()) {
            Room aRoom = (Room) eRooms.nextElement();
            if (aRoom.getZone().getZoneId() == aZoneId) {
                ++joinedZone;
            }
        }

        Zone zone = findZone(aZoneId);
        return ((zone.getJoinLimited() != -1) && (zone.getJoinLimited() <= joinedZone));
    }

    public void setZoneManager(ZoneManager aZoneMgr) {
        this.mZoneMgr = aZoneMgr;
    }

    public Zone findZone(int aZoneId) {
        return this.mZoneMgr.findZone(aZoneId);
    }
    //Thomc
    ArrayList<Long> friendSession;

    public void setFriendSession(ArrayList al) {
        friendSession = al;
    }

    public void sendFriendNotification(String msg) {
//        System.out.println("Send friend list : " + friendSession.size());
//        for (Long l : friendSession) {
//            ISession is = getManager().findSession(l);
//            if (is != null && !is.isClosed()) {
//                try {
//                    MessageFactory msgFactory = getMessageFactory();
//                    GetAvatarListResponse res = (GetAvatarListResponse) msgFactory.getResponseMessage(MessagesID.GET_AVATAR_LIST);
//                    res.setNotice(msg, true);
//                    res.mCode = 1;
//                    is.write(res);
//                } catch (ServerException e) {
//                }
//            }
//        }
    }

    public boolean isPlaying() {

        Vector<Room> joinedRoom = getJoinedRooms();
        MessageFactory msgFactory = getMessageFactory();
        if (joinedRoom.size() > 0) {
            for (Room j : joinedRoom) {
                if (j.isPlaying()) {
                    return true;
                }
            }
        }
        return false;

    }

    public void sendNotification(String msg) {
//        try {
//            MessageFactory msgFactory = getMessageFactory();
//            GetAvatarListResponse res = (GetAvatarListResponse) msgFactory.getResponseMessage(MessagesID.GET_AVATAR_LIST);
//            res.setNotice(msg, false);
//            res.mCode = 1;
////            res.msgNotification = msg;
//            write(res);
//        } catch (ServerException e) {
//        }
    }
}
