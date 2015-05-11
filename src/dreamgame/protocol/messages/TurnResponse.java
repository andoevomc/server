/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.protocol.messages;

//import dreamgame.caro.data.TableCell;
import dreamgame.cotuong.data.moveFromTo;
import dreamgame.data.ZoneID;
import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import dreamgame.poker.data.PokerPlayer;
import dreamgame.tienlen.data.TienLenPlayer;
import java.util.ArrayList;

/**
 *
 * @author binh_lethanh
 */
public class TurnResponse extends AbstractResponseMessage {

    public boolean u = false;
    public String phom = "";
    public String mErrorMsg;
    public int mRow;
    public int mCol;
    public long money;
    public int mType;
    public boolean mIsEnd;
    public long nextID;
    public long preID;
    public int timeReq;
    public boolean isPlaying;
//    public TableCell mStartCell;
//    public TableCell mEndCell;
    public long sttTo;//(-1,0,n) (n*minbet)
    public int zoneID;
    public int phomCard;
    public boolean isDuoi, isChiu;
    public long deck;
    // Tien len mien nam
//    Thomc
//    public int[] tienlenCards ;
    public String tienlenCards;
    public boolean isNewRound = false;
    public boolean isGiveup;
    public long currID;
    public ArrayList<long[]> fightInfo = new ArrayList();// gửi về khi xảy ra chặt heo/hàng
    public ArrayList<TienLenPlayer> toiPlayers = new ArrayList<TienLenPlayer>();
    //co tuong
    public moveFromTo mv;
    public int remainTime = 0;
///poker, xì tố
    public boolean isVisible = false;
    public long minBet, maxBet;
    public String poker;
    public long potMoney = 0;
    public ArrayList<PokerPlayer> pokerPlayers;
//    public ArrayList<XiToPlayer> xitoPlayers;
    public String betTypeDes = "";
    public boolean canEat = false;
    //xito
    public int visibleCard;
    public boolean isShow = false;
//bau cua
    public int piece, num, totalPiece;
    public long cash;

    public void setMinMaxBet(long min, long max) {
        minBet = min;
        maxBet = max;
    }

    public void setSuccessPoker(int aCode, int zone) {
        mCode = aCode;
        zoneID = zone;
    }

    public void setToiPlayer(ArrayList<TienLenPlayer> players) {
        toiPlayers = players;
    }

    public void setSuccess(int aCode, long mn, long id, int time, int zone) {
        zoneID = zone;
        mCode = aCode;
        money = mn;
        //mType = aType;
        mIsEnd = false;
        nextID = id;
        timeReq = time;
    }

    public void setPreID(long id) {
        this.preID = id;
    }

    public void setcurrID(long id) {
        this.currID = id;
    }

    public void setIsGiveup(boolean istrue) {
        this.isGiveup = istrue;
    }

//    public void setResult(TableCell aStartCell, TableCell aEndCell) {
//        mIsEnd = true;
//        mStartCell = aStartCell;
//        mEndCell = aEndCell;
//    }

    public void setSuccess(int aCode, int aRow, int aCol, int aType, int zone) {
        mCode = aCode;
        mRow = aRow;
        mCol = aCol;
        mType = aType;
        mIsEnd = false;
        zoneID = zone;
    }
    //Phom

    public void setSuccess(int aCode, int p, long nID, int zone) {
        mCode = aCode;
        phomCard = p;
        nextID = nID;
        zoneID = zone;
    }
    //Co tuong

    public void setSuccess(int aCode, moveFromTo mm, long nID, int zone, int remainTime_) {
        mCode = aCode;
        zoneID = zone;
        mv = mm;
        remainTime = remainTime_;
        nextID = nID;
    }
    //tien len

    public void setSuccessTienLen(int aCode, String cards, long nID, boolean isNewRound_, int zone) {
        mCode = aCode;
        zoneID = zone;
        tienlenCards = cards;
        nextID = nID;
        isNewRound = isNewRound_;
    }

    //bau cua
    public void setSuccessBauCua(int aCode, long uid, int piece, int num, int totalPiece, long cash) {
        mCode = aCode;
        this.currID = uid;
        this.piece = piece;
        this.totalPiece = totalPiece;
        this.cash = cash;
        this.num = num;
        zoneID = ZoneID.BAUCUA;
    }

//chặt heo/hàng phải gửi thông tin $ về room
    public void setFightInfo(ArrayList<long[]> fightInfo_) {
        this.fightInfo = fightInfo_;
    }

    public void setSTTTo(long stt) {
        sttTo = stt;
    }

    public void setFailure(int aCode, String aErrorMsg) {
        mCode = aCode;
        mErrorMsg = aErrorMsg;
    }

    public IResponseMessage createNew() {
        return new TurnResponse();
    }

    public void setSuccessMaubinh(int aCode, long matchid) {
        mCode = aCode;
        this.matchId = matchid;
        zoneID = ZoneID.MAUBINH;
    }

}
