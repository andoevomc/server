package dreamgame.protocol.messages;

import java.util.Vector;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import dreamgame.gameserver.framework.room.RoomEntity;

public class GetWaitingListResponse extends AbstractResponseMessage
{

    public boolean compress=false;
    public String mErrorMsg;
    public int mNumPlayingRoom;
    public Vector<RoomEntity> mWaitingRooms;
    public int zoneID;
    public int totalRoom=0;
    public boolean isMobile=false;
    
    public void setSuccess(int aCode, int aNumPlayingRoom, 
    		Vector<RoomEntity> aWaitingRooms, int zone)
    {
        mCode = aCode;
        mNumPlayingRoom = aNumPlayingRoom;
        mWaitingRooms = aWaitingRooms;
        zoneID = zone;
    }

    public void setFailure(int aCode, String aErrorMsg)
    {
        mCode = aCode;
        mErrorMsg = aErrorMsg;
    }

    public IResponseMessage createNew()
    {
        return new GetWaitingListResponse();
    }

}
