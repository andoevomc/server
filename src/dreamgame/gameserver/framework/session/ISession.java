package dreamgame.gameserver.framework.session;

import dreamgame.data.CPEntity;
import dreamgame.data.UserEntity;
import dreamgame.gameserver.framework.bytebuffer.IByteBuffer;
import dreamgame.gameserver.framework.common.ServerException;
//import com.migame.gameserver.framework.db.IConnection;
import dreamgame.gameserver.framework.protocol.BusinessProperties;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.room.Room;
import dreamgame.gameserver.framework.room.Zone;
import dreamgame.gameserver.framework.room.ZoneManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

public abstract interface ISession {

    public static final long UNSPECIFIED_ID = 0L;
    
    // trungnm    
    public abstract long getCurrentMoneyMatch();
    public abstract void setCurrentMoneyMatch(long currentMoneyMatch);
    
    public abstract void setCP(String cp);
    public abstract String getCP();
    
    public abstract void setCPEntity(CPEntity cpentity);
    public abstract CPEntity getCPEntity();
    
    public abstract void setLoginTime(Date time);
    public abstract Date getLoginTime();
    
    public abstract void setDevice(String device);
    public abstract String getDevice();
    
    public abstract String getDeviceType();
    
    public IByteBuffer[] getIncompleteData();
    public void addIncompleteDataChunk(IByteBuffer chunk);
    public void resetIncompleteData();
    public int getIncompleteDataLength();
    public byte[] getCompleteData();

    // NOTE: byte[] not works in Netty, session cannot be created
//    public byte[] getIncompleteData();
//    public void setIncompleteData(byte[] incompleteData);
    
    public int getCompleteDataLength();
    public void setCompleteDataLength(int completeDataLength);
    // end trungnm
    
//BINHLT

    public abstract void setCurrentZone(int zoneID);

    public abstract int getCurrentZone();
    //END

    public abstract void setGiftInfo(int receive_gift, int remaint_gift, int max_gift, int cash_gift);

    public abstract void setGiftInfo(int receive_gift, int remaint_gift);

    public abstract int getRemainGift();

    public abstract int getReceiveGift();

    public abstract int getCashGift();

    public abstract int getMaxGift();

    public abstract void setIP(String ip);

    public abstract String getIP();

    public abstract void setClientType(String s);

    public abstract String getClientType();

    public abstract String getID();

    public abstract void setUID(Long paramLong);

    public abstract Long getUID();

    public abstract void setUserName(String paramString);

    public abstract String getUserName();

    public abstract UserEntity getUserEntity();

    public abstract void setUserEntity(UserEntity c);

    public abstract void close();

    public abstract SessionManager getManager();

    public abstract void setBusinessProperties(BusinessProperties paramBusinessProperties);

    public abstract BusinessProperties getBusinessProperties();

    public abstract void sessionClosed();

    public abstract boolean isClosed();

    public abstract void setScreenSize(String screen);

    public abstract String getScreenSize();
    
    public abstract void setMobile(String ver);

    public abstract void ping(ISession i);

    public abstract void receiveMessage();

    public abstract boolean getMobile();

    public abstract String getMobileVer();

    public abstract void setAndroid();

    public abstract boolean isAndroid();

    public abstract void setIphone();

    public abstract boolean isIphone();

    public abstract void logCode(String msg);

    public abstract void setChannel(int c);

    public abstract int getChannel();

    public abstract void setActive(boolean a);

    public abstract boolean getActive();

    public abstract void sessionCreated(Object paramObject);

    public abstract Object getProtocolOutput();

    public abstract void writeImage(byte[] data);

    public abstract boolean write(Object paramObject)
            throws ServerException;

    public abstract void writeMessage(String msg)
            throws ServerException;

    public abstract String userInfo();

    public abstract boolean isDirect();

    public abstract boolean write()
            throws ServerException;

    public abstract IResponsePackage getDirectMessages();

    public abstract void setIsHandling(Boolean paramBoolean);

    public abstract boolean isHandling();

    public abstract String getCookies();

    public abstract void setCookies(String paramString);

    public abstract MessageFactory getMessageFactory();

    public abstract boolean realDead();

    public abstract void setMessageFactory(MessageFactory paramMessageFactory);

    public abstract String getPackageFormat();

    public abstract void setPackageFormat(String paramString);

    public abstract Date getCreatedTime();

    public abstract void setCreatedTime(Date paramDate);

    public abstract Date getLastAccessTime();

    public abstract void setLastAccessTime(Date paramDate);

    public abstract void setTimeout(Integer paramInteger);

    public abstract boolean isExpired();

    public abstract boolean realExpired();

    public abstract long getLastMessage();

    public abstract void setLoggedIn(Boolean paramBoolean);

    public abstract boolean isLoggedIn();

    //public abstract void setCurrentDBConnection(IConnection paramIConnection);
    //public abstract IConnection getCurrentDBConnection();
    public abstract void setCommit(boolean paramBoolean);

    public abstract boolean isCommit();

    public abstract void joinedRoom(Room paramRoom);

    public abstract Room findJoinedRoom(long paramLong);

    public abstract boolean isJoinedFull(int paramInt);

    public abstract Room leftRoom(long paramLong);

    public abstract Vector<Room> getJoinedRooms();

    public abstract void leaveAllRoom(IResponsePackage aResPkg);

    public abstract void setZoneManager(ZoneManager paramZoneManager);

    public abstract Zone findZone(int paramInt);

    //Thomc
    public abstract void sendNotification(String msg);

    public abstract boolean isPlaying();

    public abstract void setFriendSession(ArrayList al);

    public abstract void sendFriendNotification(String msg);
}
