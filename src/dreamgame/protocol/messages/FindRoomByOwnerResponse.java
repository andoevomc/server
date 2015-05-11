package dreamgame.protocol.messages;


import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import dreamgame.gameserver.framework.room.RoomEntity;

public class FindRoomByOwnerResponse extends AbstractResponseMessage
{

    public String mErrorMsg;
    public RoomEntity mRoom;

    public void setSuccess(int aCode, RoomEntity aRoom)
    {
        mCode = aCode;
        mRoom = aRoom;
    }

    public void setFailure(int aCode, String aErrorMsg)
    {
        mCode = aCode;
        mErrorMsg = aErrorMsg;
    }

    public IResponseMessage createNew()
    {
        return new FindRoomByOwnerResponse();
    }
}
