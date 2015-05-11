package dreamgame.protocol.messages;

import dreamgame.cotuong.data.CoTuongPlayer;
import java.util.ArrayList;
import java.util.Hashtable;

//import dreamgame.oantuti.data.OTTPlayer;
import phom.data.PhomPlayer;

import dreamgame.bacay.data.BacayPlayer;
import dreamgame.bacay.data.Poker;
import dreamgame.baucua.data.BauCuaPlayer;
import dreamgame.poker.data.PokerPlayer;
import dreamgame.tienlen.data.TienLenPlayer;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class EndMatchResponse extends AbstractResponseMessage {

    public String mErrorMsg;
    public long roomOwnerID;
    public int zoneID;
    //Bacay
    public ArrayList<BacayPlayer> bacayPlayers;
    public long ownerMoney;
    public BacayPlayer owner;
    public Hashtable<Long, Poker[]> pokers;// Bacay
    //Phom
    public ArrayList<PhomPlayer> phomPlayers;
    public PhomPlayer phomWinner;
    public int uType;//0: Khong u, 1: u bt, 2: u khan
    //OTT
//    public ArrayList<OTTPlayer> ottPlayers;
    public boolean isFinalFight;
    //Co Tuong
    public long idWin;
    public long playerMoney;
    public long newOwner = 0;
    public boolean isPeace = false;
    //Thomc: Tienlen
    public ArrayList<Object[]> tienlenResult;
    public ArrayList<TienLenPlayer> tienLenPlayers;
    public long perfectType = 0;// Hết ván vì tới trắng!
    public String lastCards; //quân bài(s)  cuối cùng được đánh ra
    public ArrayList<long[]> fightInfo;// gửi về khi xảy ra chặt heo/hàng
    public long uid;// khi còn 2 người chơi mà uid thoát ra sẽ endgame nên phải gửi uid về
    public long uidTurn = 0;//id của ngưởi đánh (các) cây cuối
//poker
    public long money = 0;//tiền tố
    public ArrayList<PokerPlayer> pokerPlayers;
    public String remainPoker = "";
//bầu cua
    public ArrayList<BauCuaPlayer> bauCuaPlayers;
    public int[] result = new int[3];
    //Mau binh    
    public boolean isChi1 = false, isChi2 = false, isChi3 = false, isMauBinh = false, isFinal = false;

    public void setSuccessBauCua(int code, long mId, int[] result) {
        mCode = code;
        matchId = mId;
        this.result = result;
    }

    public void setBauCuaPlayer(ArrayList<BauCuaPlayer> players) {
        bauCuaPlayers = players;
    }

    public void setZoneID(int z) {
        zoneID = z;
    }
//Thomc

    public void setLastCards(String cards) {
        this.lastCards = cards;
    }
//chặt heo/hàng phải gửi thông tin $ về room

    public void setFightInfo(ArrayList<long[]> fightInfo_) {
        this.fightInfo = fightInfo_;
    }

    public void setTLPlayer(ArrayList<TienLenPlayer> players) {
        tienLenPlayers = players;
    }

    public void setPokerPlayer(ArrayList<PokerPlayer> players) {
        pokerPlayers = players;
    }

    public void setSuccessTienLen(int code, ArrayList<Object[]> result, long idwin_, long mId) {
        mCode = code;
        matchId = mId;

        this.tienlenResult = result;
        this.idWin = idwin_;
    }

    public void setSuccessPoker(int code, long mId) {
        mCode = code;
        matchId = mId;
    }

    public void setSuccess(int code, ArrayList<PhomPlayer> phomPlayers,
            PhomPlayer win) {
        mCode = code;
        this.phomPlayers = phomPlayers;
        this.phomWinner = win;
    }

//    public void setSuccess(int aCode, long aMatchId, ArrayList<OTTPlayer> ottP,
//            boolean isFinalF) {
//        mCode = aCode;
//        ottPlayers = ottP;
//        isFinalFight = isFinalF;
//    }

    public void setSuccess(int aCode, long aMatchId, ArrayList<BacayPlayer> p,
            long roomOwnerMoney, BacayPlayer owner) {
        mCode = aCode;
        matchId = aMatchId;
        bacayPlayers = p;
        ownerMoney = roomOwnerMoney;
        this.owner = owner;
    }

    public void setNewRoomOwner(long id) {
        this.roomOwnerID = id;
    }
//Co tuong

    public void setMoneyEndMatch(long ownerMoney_, long playerMoney_) {
        ownerMoney = ownerMoney_;
        playerMoney = playerMoney_;
    }

    public void setSuccess(int aCode, long idwin_, long aMatchId) {
        matchId = aMatchId;
        mCode = aCode;
        idWin = idwin_;

    }

    public void setBacayPokers(Hashtable<Long, Poker[]> ps) {
        this.pokers = ps;
    }

    public void setFailure(int aCode, String aErrorMsg) {
        mCode = aCode;
        mErrorMsg = aErrorMsg;
    }

    public IResponseMessage createNew() {
        return new EndMatchResponse();
    }
}
