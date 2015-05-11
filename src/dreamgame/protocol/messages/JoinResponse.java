/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.protocol.messages;

import dreamgame.data.SimplePlayer;
import dreamgame.bacay.data.BacayPlayer;
import dreamgame.baucua.data.BauCuaPlayer;
//import bacay.data.BacayPlayer;

//import dreamgame.caro.data.TableCell;

//import dreamgame.chan.data.ChanPlayer;
import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import dreamgame.cotuong.data.CoTuongPlayer;
import dreamgame.cotuong.data.piece;
import java.util.ArrayList;
import java.util.Vector;

//import dreamgame.oantuti.data.OTTPlayer;
import dreamgame.poker.data.PokerPlayer;
import phom.data.PhomPlayer;
import dreamgame.tienlen.data.TienLenPlayer;

/**
 *
 * @author binh_lethanh
 */
public class JoinResponse extends AbstractResponseMessage {

    public String mErrorMsg;
    public ArrayList<PhomPlayer> mWaitingPlayerPhom;
    public ArrayList<PhomPlayer> mPlayerPhom;
    public ArrayList<BacayPlayer> mPlayerBacay;
//    public ArrayList<OTTPlayer> mPlayerOTT;
    public ArrayList<CoTuongPlayer> mPlayerCoTuong;
    public ArrayList<CoTuongPlayer> mPlayerList;
    public ArrayList<TienLenPlayer> mTienLenPlayer;
    public ArrayList<TienLenPlayer> mWaitingPlayerTienlen;
    //poker
    public ArrayList<PokerPlayer> mPokerPlayer;
    public ArrayList<PokerPlayer> mWaitingPokerPlayer;
//    public ArrayList<ChanPlayer> mWaitingPlayerChan;
//    public ArrayList<ChanPlayer> mPlayerChan;
    public SimplePlayer roomOwner;
    //public OTTPlayer roomOwnerOTT;
    public String roomName;
    public long mMatchId;
    public long minBet;
    public int zoneID;
    public int capacity;
    public boolean isAn;
    public boolean isTaiGui;
    public boolean isPlaying;
    public boolean isResume;
    public boolean isObserve;
    public long turn;
    public int deck;
    public String cards;
    public boolean isInvite = false;
    //Caro
    public boolean mIsStarting;
    public boolean mIsPlayer;
    public boolean mIsYourTurn;
    public int mType;
//    public Vector<TableCell> mValues;
//Tho: for obverser match
    public piece[][] board;
    public boolean isJoinAfterPlaying = false;
    public int available = 0; //cờ tướng: quân cờ chấp
    public int totalTime = 0;
    public long currentID = 0;
    public ArrayList<BauCuaPlayer> mBauCuaPlayer;
    public ArrayList<BauCuaPlayer> mWaitingBauCuaPlayer;
//bau cua: 
    public int time = 0;
    public long uid = 0;
    //mau binh
    public boolean chi1 = false;
    public boolean chi2 = false;
    public boolean chi3 = false;
    public boolean fi = false;
    public boolean maubinh = false;
    public int remainTime = 0;
    //Mau binh    
    

    public void setCurrentBauCuaPlayers(ArrayList<BauCuaPlayer> aValues, ArrayList<BauCuaPlayer> bValues,
            SimplePlayer owner) {
        roomOwner = owner;
        mBauCuaPlayer = aValues;
        mWaitingBauCuaPlayer = bValues;
    }

    public void setChessBoard(piece[][] board_) {
        this.board = board_;

    }

    public void setIsJoinAfterPlaying(boolean isTrue) {
        this.isJoinAfterPlaying = isTrue;
    }
//Tho :end

    public void setFailure(int aCode, String aErrorMsg) {
        mCode = aCode;
        mErrorMsg = aErrorMsg;
    }

    public void setOwner(SimplePlayer owner) {
        this.roomOwner = owner;
    }

    public void setCapacity(int c) {
        capacity = c;
    }

    public void setPhomInfo(boolean isAn, boolean isTaiGui, boolean isPlaying, boolean isResume, long turn, String cards, int deck) {
        this.isAn = isAn;
        this.isTaiGui = isTaiGui;
        this.isPlaying = isPlaying;
        this.isResume = isResume;
        this.turn = turn;
        this.cards = cards;
        this.deck = deck;
    }

    public void setSuccess(int aCode, String rName, long bet, int zone) {
        zoneID = zone;
        mCode = aCode;
        roomName = rName;
        minBet = bet;
    }

    public void setSuccessMauBinh(boolean maubinh, boolean chi1, boolean chi2, boolean chi3, boolean fi) {
        this.chi1 = chi1;
        this.chi2 = chi2;
        this.chi3 = chi3;
        this.fi = fi;
        this.maubinh = maubinh;
    }


    public void setRoomID(long aRoomId) {
        mMatchId = aRoomId;
    }

    public void setSuccess(int aCode, boolean aIsPlayer, boolean aIsStarting) {
        mCode = aCode;
        mIsPlayer = aIsPlayer;
        mIsStarting = aIsStarting;
    }

    public void setValue(boolean aIsYourTurn, int aType) {
        mIsYourTurn = aIsYourTurn;
        mType = aType;
    }

//    public void setCurrentTable(Vector<TableCell> aValues) {
//        mValues = aValues;
//    }

    public void setCurrentPlayersBacay(ArrayList<BacayPlayer> aValues,
            SimplePlayer owner) {
        roomOwner = owner;
        mPlayerBacay = aValues;
    }

    public void setCurrentTienLenPlayers(ArrayList<TienLenPlayer> aValues, ArrayList<TienLenPlayer> bValues,
            SimplePlayer owner) {
        roomOwner = owner;
        mTienLenPlayer = aValues;
        mWaitingPlayerTienlen = bValues;
    }

    public void setCurrentPlayersPoker(ArrayList<PokerPlayer> aValues, ArrayList<PokerPlayer> bValues,
            SimplePlayer owner) {
        roomOwner = owner;
        mPokerPlayer = aValues;
        mWaitingPokerPlayer = bValues;
    }

    public void setCurrentPlayersCoTuong(ArrayList<CoTuongPlayer> aValues,
            SimplePlayer owner) {
        roomOwner = owner;
        mPlayerCoTuong = aValues;
    }

    public void setPlayerList(ArrayList<CoTuongPlayer> aValues) {
        mPlayerList = aValues;
    }

    public void setCurrentPlayersPhom(ArrayList<PhomPlayer> aValues, ArrayList<PhomPlayer> bValues,
            SimplePlayer owner) {
        roomOwner = owner;
        mPlayerPhom = aValues;
        mWaitingPlayerPhom = bValues;
    }

//    public void setCurrentPlayersOTT(ArrayList<OTTPlayer> aValues,
//            SimplePlayer owner) {
//        roomOwner = owner;
//        mPlayerOTT = aValues;
//    }

    public IResponseMessage createNew() {
        return new JoinResponse();
    }
}
