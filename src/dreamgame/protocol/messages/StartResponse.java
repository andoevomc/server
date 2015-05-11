/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dreamgame.protocol.messages;

import dreamgame.bacay.data.BacayPlayer;
import dreamgame.bacay.data.Poker;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import java.util.ArrayList;

/**
 *
 * @author binh_lethanh
 */
public class StartResponse extends AbstractResponseMessage {
    
    public long mMatchId;
    public String mErrorMsg;
    public ArrayList<BacayPlayer> mPlayer;
    public BacayPlayer roomOwner;
    public boolean mIsYourTurn;
    public int mType;
    public BacayPlayer firstPlayer;
    public long mRoomId;
    public Poker[] pokers;
    public int zoneID;
    public void setFailure(int aCode, String aErrorMsg)
    {
        mCode = aCode;
        mErrorMsg = aErrorMsg;
    }
    public void setSuccess(int aCode, int zone)
    {
        mCode = aCode;
        zoneID = zone;
    }
    public void setSuccess(int aCode, Poker[] p, int zone)
    {
        mCode = aCode;
        pokers = p;
        zoneID = zone;
    }

    public void setValue(boolean aIsYourTurn, int aType)
    {
        mIsYourTurn = aIsYourTurn;
        mType = aType;
    }

    public void setRoomID(long aRoomId)
    {
        mRoomId = aRoomId;
    }

    public void setCurrentPlayer(ArrayList<BacayPlayer> aValues,
            BacayPlayer owner)    {
        roomOwner= owner;
        mPlayer = aValues;
        firstPlayer = mPlayer.get(0);
    }

    public IResponseMessage createNew()
    {
        return new StartResponse();
    }
}
