package dreamgame.protocol.messages;

import dreamgame.data.SimplePlayer;
import java.util.ArrayList;

import dreamgame.bacay.data.BacayPlayer;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import dreamgame.cotuong.data.CoTuongPlayer;
import dreamgame.poker.data.PokerPlayer;
import dreamgame.tienlen.data.TienLenPlayer;
import phom.data.PhomPlayer;

public class RestartResponse extends AbstractResponseMessage {

    public String mErrorMsg;
    public boolean isEmptyRoom;
    public SimplePlayer roomOwner;
    public ArrayList<BacayPlayer> players;
    public ArrayList<PhomPlayer> phomPlayers;
    public ArrayList<CoTuongPlayer> cotuongPlayers;
    public long matchID;
    public String roomName;
    public int zoneID;
    public int available = 0; //cờ tướng: chấp
    public int totalTime = 0;
    public long begin_id; //cờ tướng: ID người thua được đi trước
    //Thomc
    public ArrayList<TienLenPlayer> tienLenPlayer;
    //Thomc
    public ArrayList<PokerPlayer> pokerPlayer;

    public void setZoneID(int z) {
        zoneID = z;
    }

    public void setSuccess(int aCode, SimplePlayer owner, ArrayList<BacayPlayer> pls,
            long match, String name) {
        mCode = aCode;
        roomOwner = owner;
        players = pls;
        matchID = match;
        roomName = name;
    }

    public void setCoTuongSuccess(int aCode, SimplePlayer owner, ArrayList<CoTuongPlayer> pls,
            long match, String name) {
        mCode = aCode;
        roomOwner = owner;
        cotuongPlayers = pls;
        matchID = match;
        roomName = name;
    }
    //Thomc

    public void setTienLenSuccess(int aCode, SimplePlayer owner, ArrayList<TienLenPlayer> pls,
            long match, String name) {
        mCode = aCode;
        roomOwner = owner;
        tienLenPlayer = pls;
        matchID = match;
        roomName = name;
    }

    //Thomc
    public void setPokerSuccess(int aCode, SimplePlayer owner, ArrayList<PokerPlayer> pls,
            long match, String name) {
        mCode = aCode;
        roomOwner = owner;
        pokerPlayer = pls;
        matchID = match;
        roomName = name;
    }

    public void setPhomSuccess(int aCode, SimplePlayer owner, ArrayList<PhomPlayer> pls,
            long match, String name) {
        mCode = aCode;
        roomOwner = owner;
        phomPlayers = pls;
        matchID = match;
        roomName = name;
    }

    public void setFailure(int aCode, String aErrorMsg, boolean isEmpty) {
        mCode = aCode;
        mErrorMsg = aErrorMsg;
    }

    public IResponseMessage createNew() {
        return new RestartResponse();
    }
}
