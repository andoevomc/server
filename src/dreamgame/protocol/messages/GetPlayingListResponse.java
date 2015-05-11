package dreamgame.protocol.messages;

import java.util.Vector;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import dreamgame.gameserver.framework.room.RoomEntity;

public class GetPlayingListResponse extends AbstractResponseMessage
{

    public String mErrorMsg;
    public int mNumPlayingRoom;
    public Vector<RoomEntity> mPlayingRooms;

    public void setSuccess(int aCode, int aNumPlayingRoom, Vector<RoomEntity> aPlayingRooms)
    {
        mCode = aCode;
        mNumPlayingRoom = aNumPlayingRoom;
        mPlayingRooms = aPlayingRooms;
    }

    public void setFailure(int aCode, String aErrorMsg)
    {
        mCode = aCode;
        mErrorMsg = aErrorMsg;
    }

    public IResponseMessage createNew()
    {
        return new GetPlayingListResponse();
    }

}
