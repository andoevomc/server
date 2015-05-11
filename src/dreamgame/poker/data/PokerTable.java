package dreamgame.poker.data;

import dreamgame.tienlen.data.TienLenPlayer;
import dreamgame.tienlen.data.TienLenException;
import dreamgame.config.DebugConfig;
import dreamgame.config.GameRuleConfig;
import dreamgame.data.MessagesID;
import java.util.ArrayList;
import java.util.logging.Level;
import org.slf4j.Logger;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.data.SimpleTable;
import dreamgame.data.Timer;
import dreamgame.data.ZoneID;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.EndMatchResponse;
import dreamgame.protocol.messages.TurnRequest;
import dreamgame.protocol.messages.TurnResponse;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IBusiness;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.room.Room;
import dreamgame.gameserver.framework.room.Zone;
import dreamgame.gameserver.framework.session.ISession;
import java.util.Vector;
import org.json.JSONArray;
import org.json.JSONObject;

public class PokerTable extends SimpleTable {

    //các quân bài chung giữa bàn
    private byte[] centerCard;
//    private byte[] testCard;
//    private int numCenterCard;
//    private long potMoney = 0;//Tiền gà trong bàn
    public final static int CARDS_DOI = 53, CARDS_THU = 54, CARDS_SAMCO = 55, CARDS_SANH = 56,
	CARDS_THUNG = 57, CARDS_CULU = 58, CARDS_TUQUY = 59, CARDS_THUNGPHASANH = 60;
    public static final Logger mLog =
	LoggerContext.getLoggerFactory().getLogger(PokerTable.class);
    public ArrayList<PokerPlayer> playings = new ArrayList<PokerPlayer>();
    private ArrayList<PokerPlayer> waitings = new ArrayList<PokerPlayer>();
    @SuppressWarnings("unused")
    public PokerPlayer owner;
    public PokerPlayer currPlayer;
    public PokerPlayer winner;
    public Timer timerAuto = new Timer(ZoneID.POKER, 10000);
    public ArrayList<long[]> blackList = new ArrayList<long[]>();
    public long[] pot;
    public boolean pokerTable = true;

//    public void updateBlackList(long uid) {
//        boolean addNew = true;
//        for (int i = 0; i < blackList.size(); i++) {
//            long[] blk = blackList.get(i);
//            if (blk[0] == uid) {
//                blk[1]++;
//                blackList.set(i, blk);
//                addNew = false;
//                break;
//            }
//        }
//        if (addNew) {
//            long[] blk = new long[2];
//            blk[0] = uid;
//            blk[1] = 1;
//            blackList.add(blk);
//        }
//    }
    public boolean roomIsFull() {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - roomIsFull");
	}
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}

	return ((getPlayings().size() + getWaitings().size()) >= getMaximumPlayer());
    }

//    public void resetBlackList() {
//        blackList = new ArrayList<long[]>();
//    }
//    public boolean isblk(long uid) {
//        for (long[] blk : blackList) {
//            if (blk[0] == uid && blk[1] >= 2) {
//                return true;
//            }
//        }
//        return false;
//    }
    public PokerPlayer getCurrPlayer() {
	return currPlayer;
    }

    public PokerPlayer getOwner() {
	return owner;
    }

    public ArrayList<PokerPlayer> getPlayings() {
	return playings;
    }

    public int getUserIndex(long id) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - getUserIndex: id = " + id);
	}
	
	for (int i = 0; i < this.playings.size(); i++) {
	    if (playings.get(i).id == id) {
		if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		return i;
	    }
	}
	
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return -1;
    }

    public long getCurrentTurnID() {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - getCurrentTurnID");
	}
	
//        return playings.get(currentTurn).id;
	if (currPlayer != null) {
	    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	    return currPlayer.id;
	} else {
	    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	    return -1;
	}
    }

    public ArrayList<PokerPlayer> copPlayerList() {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - copPlayerList");
	}
	
	ArrayList<PokerPlayer> list = new ArrayList<PokerPlayer>();
	for (PokerPlayer p : playings) {
	    list.add(p);
	}
	
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return list;
    }

    public void setOrder(long[] order) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - setOrder");
	}
	
	for (int i = 0; i < order.length; i++) {
	    order[i] = 0;
	}
	int i = 0;
	for (PokerPlayer p : playings) {
	    if (i < order.length) {
		order[i] = p.id;
	    }

	    i++;
	}
	
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
    }

    public boolean isAllReady() {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - isAllReady");
	}
	
	for (int i = 0; i < playings.size(); i++) {
	    PokerPlayer player = playings.get(i);
	    if ((!player.isReady) && (player.id != this.owner.id)) {
		if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		return false;
	    }
	}
	
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return true;
    }

    public boolean containPlayer(long id) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - containPlayer: id = " + id);
	}
	
	// in playing
	for (int i = 0; i < playings.size(); i++) {
	    if (playings.get(i).id == id) {
		if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		return true;
	    }
	}
	
	// in waiting list
	for (int i = 0; i < waitings.size(); i++) {
	    if (waitings.get(i).id == id) {
		if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		return true;
	    }
	}
	
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return false;
    }

    public ArrayList<PokerPlayer> getWaitings() {
	return waitings;
    }
    // Create table: owner, money, matchID, numberPlayer

    public PokerTable(PokerPlayer ow, long money, long match, int numPlayer) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - PokerTable constructor: owner'name = " + ow.username + "; money = " + money + "; match = " + match + "; numPlayer = " + numPlayer);
	}
	
	logdir = "poker_log";
	initLogFile();
	startTime = System.currentTimeMillis();
	this.owner = ow;
	this.firstCashBet = money;
	this.matchID = match;
	this.maximumPlayer = numPlayer;
	this.playings.add(ow);
	timerAuto.setRuning(false);
	timerAuto.start();
	
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
    }

    // Player join
    public void join(PokerPlayer player) throws TienLenException, PokerException {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - join: player'name = " + player.username);
	}
	
	synchronized (playings) {
	    for (PokerPlayer p : playings) {
		if (p.id == player.id) {
		    //user da ton tai!
		    throw new PokerException("User da ton tai.");
		}
	    }
	}
//        if (virtualRoom) {
//            player.cash = getMoney(player.id);
//        }

	if (this.isPlaying) {
	    if (this.playings.size() + this.waitings.size() < maximumPlayer) {
		player.isObserve = true;
		this.waitings.add(player);
	    } else {
		if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		throw new PokerException("Phong da thua nguoi roi ban");
	    }
	} else if (this.playings.size() < maximumPlayer) {
	    this.playings.add(player);
	} else {
	    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	    throw new PokerException("Phong da thua nguoi roi ban");
	}
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
    }

    //Player removed
    public void removePlayer(long id) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - removePlayer: id = " + id);
	}
	
	try {
	    for (PokerPlayer p : playings) {
		if (p.id == id) {
		    remove(p);
		    return;
		}
	    }
	    for (PokerPlayer p : waitings) {
		if (p.id == id) {
		    remove(p);
		    return;
		}
	    }
	} catch (Exception e) {
	    if (DatabaseDriver.log_code) {
		out_code.println("Error. Not found player : " + id);
	    }
	}
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
    }

    public void remove(PokerPlayer player) throws TienLenException {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - remove: player's name = " + player.username);
	}
	
	try {
	    synchronized (this.playings) {
		if (DatabaseDriver.log_code) {
		    out_code.println("Remove player : " + player.id);
		}
		for (PokerPlayer p : this.playings) {
		    if (p.id == player.id) {
			playings.remove(player);
			if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
			return;
		    }
		}
		for (PokerPlayer p : this.waitings) {
		    if (p.id == player.id) {
			waitings.remove(p);
			if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
			return;
		    }
		}
	    }
	} catch (Exception e) {
	    mLog.error(e.getMessage() + " : " + player.id);
	}
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
    }

    public int compareCards(byte[] cards1, byte[] cards2) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - compareCards: cards1, cards2");
	}
	
	int len = cards1.length;
	sortCards(cards1);
	sortCards(cards2);
	for (int i = len - 1; i >= 0; i--) {
	    if (getValue(cards1[i]) > getValue(cards2[i])) {
		if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		return 1;
	    } 
	    else if (getValue(cards1[i]) < getValue(cards2[i])) {
		if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		return -1;
	    }
	}
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return 0;
    }

    public int compareCards(byte[] cards1, byte[] cards2, byte[] subCards1, byte[] subCards2) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - compareCards: cards1, cards2, subCards1, subCards2");
	}
	
	int v1 = checkCardsType(cards1);
	int v2 = checkCardsType(cards2);
//        System.out.println("v1: " + v1 + ",   v2: " + v2);
	if (v1 == v2) {
	    if (v1 == 0) {
		System.out.println("2 bộ bài mậu thầu -> xét quân bài riêng lớn nhất");
		int maxSub1 = maxCard(subCards1);
		int maxSub2 = maxCard(subCards2);
		if (maxSub1 == maxSub2) {
		    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		    return 0;
		} else if (maxSub1 > maxSub2) {
		    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		    return 1;
		}
		if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		return -1;
	    } 
	    else {
		byte[] b1 = getCards(cards1, v1);
		byte[] b2 = getCards(cards2, v2);
		int compareV = compareCards(b1, b2);
		System.out.println("2 bộ bài cùng là:" + compareV);
		if (compareV == 0) {
		    if (b1.length == 5) {
			if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
			return 0;
		    }
		    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		    return compareCards(subCards1, subCards2);
		} 
		else {
		    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		    return compareV;
		}
	    }
	} else if (v1 > v2) {
	    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	    return 1;
	}
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return -1;
    }

    public void numStart() {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - numStart");
	}
	
	System.out.println("chay qua numstart!");
	currentTurn = getUserIndex(owner.id);
	if (owner.isOutGame || owner.notEnoughMoney() || owner.isFold) {
//            currentTurn = getUserIndex(findNext(owner.id).id);
	    PokerPlayer nextPlayer = findNext(minBet);
	    if (nextPlayer == null) {
		sendEndMatch(-1);
		if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		return;
	    } else {
		currentTurn = getUserIndex(nextPlayer.id);
	    }
	}

//        for (int i = 1; i < playings.size(); i++) {
//            if (isBigger(coppyHand(playings.get(i).myHand), coppyHand(playings.get(currentTurn).myHand))) {
//                currentTurn = i;
//            }
//        }
	this.currPlayer = this.playings.get(currentTurn);
	lastTurnID = currPlayer.id;
	startTime();
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
    }

    public byte[] coppyHand(byte[] aHand, boolean isEndMatch) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - coppyHand: aHand, isEndMatch = " + isEndMatch);
	}
	
	int numCenterCard = 0;
	if (isEndMatch) {
	    numCenterCard = 5;
	} else if (numRound == 1) {
	    numCenterCard = 3;
	} else if (numRound == 2) {
	    numCenterCard = 4;
	} else if (numRound == 3) {
	    numCenterCard = 5;
	}
	int len = aHand.length;
	byte[] hand = new byte[len + numCenterCard];
	int index = 0;
	for (int i = 0; i < aHand.length; i++) {
	    hand[i] = aHand[i];
	    index++;
	}
	for (int i = 0; i < numCenterCard; i++) {
	    hand[index + i] = centerCard[i];
	}
//        for (int i = 0; i < hand.length; i++) {
////            System.out.println(cardToString(hand[i]) + "; ");
////            hand[index + i] = centerCard[i];
//        }
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return hand;
    }
//    public void resetActive() {
////        for (PokerPlayer p : this.playings) {
////            p.isAcive = true;
////        }
//    }
    
//Thomc
    public int currentTurn = 0; //người hiện tại tố,
    public long lastTurnID = 0; // người vừa tố
//    public boolean isNewRound = true;
    public long currentBet = 0;//Tiền cược hiện tài
    public long mCurrentBet = 0;
    public long minBet = 0, maxBet = 0;
    public final static int BET_CHECK_TYPE = 0;//Theo
    public final static int BET_CALL_TYPE = 1;//Theo
    public final static int BET_RAISE_TYPE = 2;//Tố
    public final static int BET_ALL_IN_TYPE = 3;//tố hết
    public final static int TURN_NEXT = 4,
	TURN_INVALID = 5, TURN_NEW_ROUND = 6, TURN_END_MATCH = 7;
    public final static int numMaxBet = 10;
    public int numRound = 0;
    public String betTypeDes = "";
//    public void newRound() {
//        isNewRound = true;
//        resetActive();
//    }

    public String betTypeDes(int type) {
	if (type == BET_CALL_TYPE) {
	    return "Theo";
	} else if (type == BET_RAISE_TYPE) {
	    return "Thêm";
	} else if (type == BET_ALL_IN_TYPE) {
	    return "Tố hết";
	} else {
	    return "Bỏ qua";
	}
    }

    public void autoFold(long uid) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - autoFold: uid = " + uid);
	}
	
	System.out.println("ownerSession" + ownerSession);
//                System.out.println("tienlenPlayer.id" + tienlenPlayer.id);
	ISession session = ownerSession.getManager().findSession(uid);
	IResponsePackage responsePkg = session.getDirectMessages();//new SimpleResponsePackage();
	MessageFactory msgFactory = session.getMessageFactory();
	TurnRequest reqMatchTurn = (TurnRequest) msgFactory.getRequestMessage(MessagesID.MATCH_TURN);
	reqMatchTurn.isFold = true;
	reqMatchTurn.money = 0;
	reqMatchTurn.uid = uid;
//        reqMatchTurn.isTimeout = true;
	reqMatchTurn.mMatchId = getMatchID();
//                reqMatchTurn.isTimeoutTL = true;

	IBusiness business = null;
	// Check if timeout
	if (reqMatchTurn.uid != -1) {
	    try {
		business = msgFactory.getBusiness(MessagesID.MATCH_TURN);
		business.handleMessage(session, reqMatchTurn, responsePkg);
	    } catch (ServerException se) {
	    }
	}
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
    }

    public int onBet(long uid, long money, boolean isFold) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - onBet: uid = " + uid + "; money = " + money + "; isFold = " + isFold);
	}
	
	if (uid == getCurrentTurnID()) {
	    PokerPlayer p = findPlayer(uid);
	    int betType = checkBetType(money);
	    if (betType == -1) {
		if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		return 999;
	    }
	    if (isFold) {
		betTypeDes = "Úp bỏ";
	    } else {
		betTypeDes = betTypeDes(betType);
	    }
	    System.out.println("bet type: " + betTypeDes);
	    if (betType == BET_ALL_IN_TYPE) {
		System.out.println(p.username + " allinround: " + numRound);
		p.allInRound = numRound;
		p.allInMoney = money + p.currentBetMoney;

		//                lastTurnID = p.id;
//                p.cash = 0;
	    } else if (isFold) {
		p.isFold = true;
	    } else if (betType == BET_RAISE_TYPE) {
//                currentBet = money;
		lastTurnID = p.id;
	    }
	    if (!isFold) {
		long oldBet = currentBet;
		if (money + p.currentBetMoney > oldBet) {
		    currentBet = money + p.currentBetMoney;
		}
	    }
	    pot[numRound] += money;
	    System.out.println("  pot[" + numRound + "] :" + pot[numRound]);
	    p.cash -= money;
	    p.currentBetMoney += money;

	    p.potMoney[numRound] += money;
	    p.turned = true;

	    // logging
	    if (isFold) {
		if (DatabaseDriver.log_code) {
		    out.println(p.username + " : Úp bỏ");
		}
	    } else {
		if (DatabaseDriver.log_code) {
		    out.println(p.username + ": " + betTypeDes(betType) + " ," + money + " $" + " ---- Cash: " + p.cash);
		    out.println("Tiền gà của vòng: " + numRound + ": " + pot[numRound] + " $");
		}
	    }
	    if (DatabaseDriver.log_code) {
		out.flush();
	    }
//            out.flush();
//            }

//	    if (isFullAllIn()) {
//                return TURN_END_MATCH;
//            }
//	    
//            if (nextPlayer(getUserIndex(uid))) {
	    
	    if (isAllFold()) {
		if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		return TURN_END_MATCH;
	    }
	    
	    long nextPlayer = findNextPlayer(uid);
	    System.out.println("nextPlayer: " + nextPlayer);
	    // het vong to vua roi
	    if (nextPlayer == -1) {
		if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		return TURN_NEW_ROUND;
	    } 
	    // tiep tuc vong to nay, nguoi tiep theo
	    else {
		currentTurn = getUserIndex(nextPlayer);
		currPlayer = playings.get(currentTurn);
		startTime();	// auto fold if timeout
		if (currentBet == 0 & isFold && numRound == 0) {
		    resetBetLevel(true);
		} else {
		    resetBetLevel(false);
		}
//                if (isFold && lastTurnID == uid) {
//                    try {
//                        System.out.println("chay qua onbet!");
//                        lastTurnID = findNext(uid).id;
//                        if (checkEndMatch()) {
//                            return TURN_NEW_ROUND;
//                        }
//                    } catch (StackOverflowError eee) {
//                        return TURN_NEW_ROUND;
//                    }
//                }
		if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		return TURN_NEXT;
//            } else {
//                if (numRound >= 3) {
//                    return TURN_END_MATCH;
//                }
//                return TURN_NEW_ROUND;
	    }

	    //            if (nextPlayer(getUserIndex(uid))) {
	    //                if (currentBet == 0 & isFold && numRound == 0) {
	    //                    resetBetLevel(true);
	    //                } else {
	    //                    resetBetLevel(false);
	    //                }
	    //                if (isFold && lastTurnID == uid) {
	    //                    try {
	    //                        System.out.println("chay qua onbet!");
	    //                        lastTurnID = findNext(uid).id;
	    //                        if (checkEndMatch()) {
	    //                            return TURN_NEW_ROUND;
	    //                        }
	    //                    } catch (StackOverflowError eee) {
	    //                        return TURN_NEW_ROUND;
	    //                    }
	    //
	    //                }
	    //                return TURN_NEXT;
	    //            } else {
	    //
	    //                if (numRound >= 3) {
	    //                    return TURN_END_MATCH;
	    //                }
	    //                return TURN_NEW_ROUND;
	    //            }
	}
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return TURN_INVALID;
    }

    // tat ca moi nguoi da "to tat" chua
//    public boolean isFullAllIn() {
//        int sum = 0;
//        for (PokerPlayer p : playings) {
//            if ((!p.isOutGame) && (!p.isAllIn()) && (!p.isFold)) {
//                sum++;
//            }
//        }
//        return sum == 0;
//    }
    public void resetBetLevel(boolean isNewRound) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - resetBetLevel: isNewRound = " + isNewRound);
	}
	
//        if (getCurrPlayer().cash > numMaxBet * firstCashBet) {
//            maxBet = numMaxBet * firstCashBet;
//        } else {
	maxBet = getCurrPlayer().cash;
//        }
	if (isNewRound) {
	    if (numRound != 0) {
		minBet = 0;
	    } else {
		minBet = firstCashBet;
	    }
	} else {
	    minBet = mCurrentBet - currPlayer.potMoney[numRound];
//            minBet = currentBet - getCurrPlayer().currentBetMoney;
	}
	if (minBet > maxBet) {
	    minBet = maxBet;


	}
	System.out.println("minbet:" + minBet + "; maxBet: " + maxBet);

	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
    }

    public void startBet() {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - startBet");
	}
	
	for (PokerPlayer p : playings) {
	    p.cash -= firstCashBet;
	    p.potMoney[0] = firstCashBet;
	    pot[0] += firstCashBet;
//            potMoney += firstCashBet;
	}
	
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
    }

    public long getPotMoney() {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - getPotMoney");
	}
	
	long sum = 0;
	for (int i = 0; i
	    < pot.length; i++) {
	    sum += pot[i];
	}
	
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return sum;
    }

    public int checkBetType(long money) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - checkBetType: money = " + money);
	}
	
	if (money > maxBet) {
	    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	    return -1;
	}
	if (money == maxBet) {
	    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	    return BET_ALL_IN_TYPE;
	} 
	else if (money == minBet) {
	    if (money == 0) {
		if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		return BET_CHECK_TYPE;
	    }
	    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	    return BET_CALL_TYPE;
	} 
	else if (money > minBet) {
	    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	    return BET_RAISE_TYPE;
	}
	
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return 0;
    }

    // find next player
    public long findNextPlayer(long uid) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - findNextPlayer: uid = " + uid);
	}
	
	long currMaxBet = Long.MIN_VALUE;
	for (PokerPlayer p : playings) {
	    if (p.potMoney[numRound] > currMaxBet) {
		currMaxBet = p.potMoney[numRound];
	    }
	}
	mCurrentBet = currMaxBet;
	boolean b = true;
	for (PokerPlayer p : playings) {
	    if (!p.allIn() && !p.isFold && !p.isOutGame) {
		if (p.potMoney[numRound] != currMaxBet || !p.turned) {
		    b = false;
		    break;
		}
	    }
	}
	if (b) {
	    System.out.println("case 1");
	    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	    return -1;
	}
	int mIndex = getUserIndex(uid);
	int nextIndex = mIndex;
	for (int i = 1; i < 10; i++) {
	    nextIndex--;
	    if (nextIndex < 0) {
		nextIndex = playings.size() - 1;;

	    }
	    if (nextIndex == mIndex) {
		System.out.println("case 2");
		if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		return -1;
	    }
	    PokerPlayer nextPlayer = playings.get(nextIndex);
	    if (!nextPlayer.isAllIn() && !nextPlayer.isFold && !nextPlayer.isOutGame) {
		if (nextPlayer.potMoney[numRound] < currMaxBet || !nextPlayer.turned) {
//                    startTime();
		    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		    return nextPlayer.id;

		}
	    }
	}
	System.out.println("case 3");
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return -1;
    }

    public void subNextPlayer(int preIndex) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - subNextPlayer: preIndex = " + preIndex);
	}
	
	currentTurn = preIndex;
//        currentTurn++;
//
//        currentTurn =
//                currentTurn % playings.size();
	currentTurn--;
	if (currentTurn < 0) {
	    currentTurn = playings.size() - 1;
	}
	currPlayer = playings.get(currentTurn);
	
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
    }

    public boolean nextPlayer(int preIndex) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - nextPlayer" + "; preIndex = " + preIndex);
	}
	
//        System.out.println("lastturn:  " + findPlayer(lastTurnID).username);
//        System.out.println("getUserIndex(lastTurnID)" + getUserIndex(lastTurnID));
//        currentTurn = preIndex;
////        currentTurn++;
////
////        currentTurn =
////                currentTurn % playings.size();
//        currentTurn--;
//        if (currentTurn < 0) {
//            currentTurn = playings.size() - 1;
//        }
//        currPlayer = playings.get(currentTurn);

//        System.out.println("currentTurn: " + currentTurn);


	System.out.println("current bet: " + currentBet);
	for (int i = 0; i < 10; i++) {
	    if (i == 9) {
		if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		return false;
	    }
	    subNextPlayer(preIndex);
	    if ((currPlayer.isAllIn() || currPlayer.isFold || currPlayer.isOutGame) && currPlayer.id != lastTurnID) {
//            System.out.println("isOutGame: " + playings.get(currentTurn).isOutGame);
//                return nextPlayer(currentTurn);
		subNextPlayer(currentTurn);
		System.out.println("find next! ");
	    } else {
//            System.out.println("chia vong moi!!!");
		startTime();
		if (currPlayer.id == lastTurnID && currPlayer.currentBetMoney == currentBet) {
		    System.out.println("case 1 ");
		    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		    return false;
		} else {
		    if (currPlayer.currentBetMoney == currentBet && currentBet != 0) {
			System.out.println("case 2 ");
			if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
			return false;
		    }
		    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		    return true;
		}
	    }
	}
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return false;
//        startTime();
//        System.out.println("number user:  " + playings.size());
    }

    public int subFindNext(long uid) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - subFindNext: uid = " + uid);
	}
	
	int point = getUserIndex(uid);
//        point++;
//        point = point % playings.size();
	point--;
	if (point < 0) {
	    point = playings.size() - 1;
	}
	
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return point;
    }

    public PokerPlayer findNext(long uid) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - findNext" + "; uid = " + uid);
	}
	
//        int point = getUserIndex(uid);
////        point++;
////        point = point % playings.size();
//        point--;
//        if (point < 0) {
//            point = playings.size() - 1;
//        }

//        int point = subFindNext(uid);
//        PokerPlayer p = playings.get(point);
//        for (int i = 0; i < 10; i++) {
//            if (!p.isOutGame && !p.isFold && !p.isAllIn()) {
//                return playings.get(point);
//            } else {
//                return findNext(playings.get(point).id);
//            }
//        }
//        return p;

	int mIndex = getUserIndex(uid);
	int nextIndex = mIndex;
	for (int i = 1; i < 10; i++) {
	    nextIndex--;
	    if (nextIndex < 0) {
		nextIndex = playings.size() - 1;

	    }
	    if (nextIndex == mIndex) {
		System.out.println("case 2");
		if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		return null;
	    }
	    PokerPlayer nextPlayer = playings.get(nextIndex);
	    if (!nextPlayer.isAllIn() && !nextPlayer.isFold && !nextPlayer.isOutGame) {
		if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		return nextPlayer;

	    }
	}
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return null;
    }

    public int getType(int b) {
	return (b - 1) / 13;
    }

    public int getValue(int b) {
	int value = (b - 1) % 13;
	switch (value) {
	    case 0:
		return 12;
	    default:
		return value - 1;
	}
    }

    public byte[] sortCards(byte[] cards) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - sortCards: cards");
	}
	
	if (cards == null) {
	    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	    return null;
	}
	for (int i = 0; i
	    < cards.length - 1; i++) {
	    for (int j = i + 1; j
		< cards.length; j++) {
		if (getValue(cards[i]) >= getValue(cards[j])) {
		    if (getValue(cards[i]) == getValue(cards[j])) {
			if (getType(cards[i]) > getType(cards[j])) {
			    byte tempCard = cards[j];
			    cards[j] = cards[i];
			    cards[i] = tempCard;
			}
		    } else {
			byte tempCard = cards[j];
			cards[j] = cards[i];
			cards[i] = tempCard;
		    }
		}
	    }
	}
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return cards;
    }

    public byte[] sortCards2(byte[] cards) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - sortCards2: cards");
	}
	
	if (cards == null) {
	    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	    return null;
	}
	byte[] sorted = new byte[cards.length];
	for (int i = 0; i
	    < cards.length; i++) {
	    sorted[i] = cards[i];
	}
	for (int i = 0; i
	    < sorted.length - 1; i++) {
	    for (int j = i + 1; j
		< sorted.length; j++) {
		if (getValue(sorted[i]) > getValue(sorted[j])) {
		    byte tempCard = sorted[j];
		    sorted[j] = sorted[i];
		    sorted[i] = tempCard;
		}
	    }
	}
	Vector temp = new Vector();
	for (int i = 0; i
	    < sorted.length - 1; i++) {
	    for (int j = i + 1; j
		< sorted.length; j++) {
		if (sorted[j] != -1 && getValue(sorted[i]) == getValue(sorted[j])) {
		    temp.add(sorted[j]);
		    sorted[j] = -1;
		}
	    }
	}
	Vector temp2 = new Vector();
	for (int i = 0; i < sorted.length; i++) {
	    if (sorted[i] != -1) {
		temp2.add(sorted[i]);
	    }
	}
	for (int i = 0; i < temp.size(); i++) {
	    if ((byte) Integer.parseInt(temp2.elementAt(i).toString()) != -1) {
		temp2.add(temp.elementAt(i));
	    }
	}

	sorted = new byte[temp2.size()];
	for (int i = 0; i < temp2.size(); i++) {
	    sorted[i] = (byte) Integer.parseInt(temp2.elementAt(i).toString());
	}
	for (int i = 0; i < sorted.length; i++) {
	    System.out.print(sorted[i] + ": ");
	}
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return sorted;
    }

    public int checkCardsType(byte[] cards) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - checkCardsType: cards");
	}
	
	int check1 = checkCard1(cards);
//        for (int i = 0; i
//                < cards.length; i++) {
//            System.out.print(cards[i] + "; ");
//        }
	int check2 = checkCard2(cards);
//        System.out.println("check1: " + check1);
//        System.out.println("check2: " + check2);
//        System.out.println("Math.max(check1, check2): " + Math.max(check1, check2));
	if (check1 == check2) {
	    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	    return 0;
	} else {
	    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	    return Math.max(check1, check2);
	}
    }

    public int maxCard(byte[] cards) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - maxCard: cards");
	}
	
	int max = getValue(cards[0]);
	for (int i = 1; i
	    < cards.length; i++) {
	    int cardV = getValue(cards[i]);
	    if (max < cardV) {
		max = cardV;
	    }
	}
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return max;
    }

    public int checkCard1(byte[] cards) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - checkCard1: cards");
	}
	
//        boolean b = true;
	int type = 0;
	if (isThung(cards)) {
	    mLog.debug("thung thung");
	    type = CARDS_THUNG;
	}
//	else {
//            b = false;
//        }

	if (isSanh(cards)) {
	    mLog.debug("sanh sanh");
	    type = CARDS_SANH;
	}
//	else {
//            b = false;
//        }

	if (type == CARDS_SANH) {
	    mLog.debug("thung pha sanh");
	    byte[] sanhCards = getCards(cards, CARDS_SANH);
	    if (isThung(sanhCards)) {
		if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		return CARDS_THUNGPHASANH;
	    }
	}

//        if (b) {
//            return CARDS_THUNGPHASANH;
//        }
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return type;
    }

    public int checkCard2(byte[] cards) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - checkCard2: cards");
	}
	
//        int type = 0;
	byte[] appearRate = new byte[13];// tần suất xuất hiện các quân bài


	int length = cards.length;


	for (int i = 0; i
	    < 13; i++) {
	    appearRate[i] = 0;


	}
	for (int j = 0; j
	    < length; j++) {
//            System.out.println("getValue(cards[j])" + getValue(cards[j]));
	    appearRate[getValue(cards[j])]++;


	}
	int app2 = 0, app3 = 0; //xuất hiện 2,3 lần


	for (int i = 0; i
	    < appearRate.length; i++) {
	    if (appearRate[i] == 4) {
		return CARDS_TUQUY;


	    }
	    if (appearRate[i] == 3) {
		app3++;


	    }
	    if (appearRate[i] == 2) {
		app2++;


	    }

	}
	
	if (app3 > 0 && app2 > 0) {
	    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	    return CARDS_CULU;
	}
	
	if (app3 > 0) {
	    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	    return CARDS_SAMCO;
	}
	
	if (app2 > 1) {
	    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	    return CARDS_THU;
	}
	
	if (app2 > 0) {
	    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	    return CARDS_DOI;
	}
	
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return 0;
    }

    public boolean isThung(byte[] cards) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - isThung: cards");
	}
	
	int len = cards.length;
	if (len >= 5) {
	    byte[] typeArr = new byte[4];// tần suất xuất hiện các quân bài
	    for (int i = 0; i < 4; i++) {
		typeArr[i] = 0;
	    }
	    
	    for (int j = 0; j < len; j++) {
		typeArr[getType(cards[j])]++;
	    }
	    
	    for (int i = 0; i < 4; i++) {
		if (typeArr[i] >= 5) {
		    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		    return true;
		}
	    }
	}
	
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return false;
    }

    public boolean isSanh(byte[] cards) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - isSanh");
	}
	
	int len = cards.length;
	if (len >= 5) {
	    if (len == 5) {
		if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		return isSanh5(cards);
	    } 
	    else {
		byte[] sorted = sortCards2(cards);
		for (int i = 0; i < sorted.length; i++) {
		    System.out.print(sorted[i] + "; ");
		}
		for (int i = len - 5; i >= 0; i--) {
		    if (isSanh5(getByte5(sorted, i))) {
			if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
			return true;
		    }
		}
	    }
	}
	
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return false;
    }

    public byte[] getByte5(byte[] cards, int start) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - getByte5: cards, start = " + start);
	}
	
	byte[] b = new byte[5];
	for (int i = 0; i < 5; i++) {
	    b[i] = cards[i + start];
	}
	
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return b;
    }

    public boolean isSanh5(byte[] cards) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - isSanh5: cards");
	}
	
	if (cards.length == 5) {
	    sortCards(cards);
	    for (int i = 0; i < cards.length - 1; i++) {
		if (getValue(cards[i]) + 1 != getValue(cards[i + 1])) {
		    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		    return false;
		}
	    }
	    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	    return true;
	}
	
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return false;
    }

    public byte[] getCards(byte[] cards, int type) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - getCards: cards, type = " + type);
	}
	
	int len = cards.length;
	if (type == CARDS_THUNG) {
	    sortCards(cards);
	    if (len == 5) {
		return cards;
	    }
	    byte[] result = new byte[5];
	    int mType = 0;
	    byte[] typeArr = new byte[4];
	    for (int i = 0; i < 4; i++) {
		typeArr[i] = 0;
	    }
	    
	    for (int j = 0; j < len; j++) {
		typeArr[getType(cards[j])]++;
	    }
	    
	    for (int i = 0; i < 4; i++) {
		if (typeArr[i] >= 5) {
		    mType = i;
		    break;
		}
	    }
	    int index = 0;

	    for (int j = len - 1; j >= 0; j--) {
		if (getType(cards[j]) == mType) {
		    if (index >= 5) {
			break;
		    } 
		    else {
			result[index] = cards[j];
			index++;
		    }
		}
	    }
	    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	    return result;
	} 
	else if (type == CARDS_SANH) {
	    if (len == 5) {
		if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		return cards;
	    }
	    byte[] sorted = sortCards2(cards);
	    byte[] result = new byte[5];
	    for (int i = len - 5; i >= 0; i--) {
		result = getByte5(sorted, i);
		if (isSanh5(result)) {
		    break;
		}
	    }
	    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	    return result;
	} 
	else if (type == CARDS_THUNGPHASANH) {
	    if (len == 5) {
		if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		return cards;
	    }
	    sortCards(cards);
	    byte[] result = new byte[5];
	    for (int i = len - 5; i >= 0; i--) {
		result = getByte5(cards, i);
		if (isSanh5(result)) {
		    if (isThung(result)) {
			break;
		    }
		}
	    }
	    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	    return result;
	} 
	else {
	    byte[] appearRate = new byte[13];// tần suất xuất hiện các quân bài
	    int length = cards.length;
	    for (int i = 0; i < 13; i++) {
		appearRate[i] = 0;
	    }
	    for (int j = 0; j < length; j++) {
		appearRate[getValue(cards[j])]++;
	    } //            int app2 = 0, app3 = 0; //xuất hiện 2,3 lần
	    byte[] result = null;
	    if (type == CARDS_TUQUY) {
		result = new byte[4];
		int cardV = 0;
		for (int i = appearRate.length - 1; i >= 0; i--) {
		    if (appearRate[i] == 4) {
			cardV = i;
//                        System.out.println("cardV: " + cardV);
			break;
		    }
		}
		int i = 0;
		for (int j = 0; j < length; j++) {
		    if (getValue(cards[j]) == cardV) {
			result[i] = cards[j];
			i++;
		    }
		}
	    } else if (type == CARDS_CULU) {
		if (len == 5) {
		    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		    return cards;
		}
		result = new byte[5];
		int cardV1 = 0, cardV2 = 0;
		for (int i = appearRate.length - 1; i >= 0; i--) {
		    if (appearRate[i] == 2 || appearRate[i] == 3) {
			if (cardV1 == 0) {
			    cardV1 = i;
			} else {
			    cardV2 = i;
			}
		    }
		}
		int i = 0;
		for (int j = 0; j < length; j++) {
		    if (getValue(cards[j]) == cardV1 || getValue(cards[j]) == cardV2) {
			result[i] = cards[j];
			i++;
		    }
		}
	    } else if (type == CARDS_SAMCO) {
		result = new byte[3];
		int cardV = 0;
		for (int i = appearRate.length - 1; i >= 0; i--) {
		    if (appearRate[i] == 3) {
			cardV = i;
			break;
		    }
		}
		int i = 0;
		for (int j = 0; j < length; j++) {
		    if (getValue(cards[j]) == cardV) {
			result[i] = cards[j];
			i++;
		    }
		}
	    } else if (type == CARDS_THU) {
		result = new byte[4];
		int cardV1 = 0, cardV2 = 0;
		for (int i = appearRate.length - 1; i >= 0; i--) {
		    if (appearRate[i] == 2) {
			if (cardV1 == 0) {
			    cardV1 = i;
			} else {
			    cardV2 = i;
			}
		    }
		}
		int i = 0;
		for (int j = 0; j < length; j++) {
		    if (getValue(cards[j]) == cardV1 || getValue(cards[j]) == cardV2) {
			result[i] = cards[j];
			i++;
		    }
		}
	    } else if (type == CARDS_DOI) {
		result = new byte[2];
		int cardV = 0;
		for (int i = appearRate.length - 1; i >= 0; i--) {
		    if (appearRate[i] == 2) {
			cardV = i;
			break;
		    }
		}
		int i = 0;
		for (int j = 0; j < length; j++) {
		    if (getValue(cards[j]) == cardV) {
			result[i] = cards[j];
			i++;
		    }
		}
	    }
//            for (int i = 0; i < result.length; i++) {
//                System.out.println("; " + result[i]);
//            }
//        System.out.println(result);
	    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	    return result;
	}
    }

    public int getValue2(int b) {
//	int previousMethodCallLevel;
//	if (DebugConfig.FOR_DEBUG) {
//	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
//	    DebugConfig.CALL_LEVEL ++;
//	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - getValue2" + "; b = " + b);
//	}
//	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	
	return (b - 1) % 13;
    }

    public String cardToString(int card) {
	String[] s = {"tep", "bich", "ro", "co"};


	if ((getValue2(card) + 1) == 11) {
	    return "J" + s[(card - 1) / 13];
	}
	
	if ((getValue2(card) + 1) == 12) {
	    return "Q" + s[(card - 1) / 13];
	}
	
	if ((getValue2(card) + 1) == 13) {
	    return "K" + s[(card - 1) / 13];
	}
	
	if ((getValue2(card) + 1) == 1) {
	    return "A" + s[(card - 1) / 13];
	}

	return "" + (getValue2(card) + 1) + "" + s[(card - 1) / 13];
    }

    public String cardToString(byte[] card) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - cardToString: cards");
	}
	
	String s = "";
	for (int i = 0; i
	    < card.length; i++) {
	    s = s + " " + cardToString(card[i]);
	}
	
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return s;
    }

    private void chia() {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - chia");
	}
	
	matchNum++;
	if (out == null) {
	    initLogFile();
	}
	if (DatabaseDriver.log_code) {
	    out.println();
	    out.println("*******************" + matchID + "-" + matchNum + " : (" + owner.username + ")***************************");
	    out.println("Minbet : " + firstCashBet);
	    out_code.println();
	    out_code.println("*******************" + matchID + "-" + matchNum + " : (" + owner.username + ")***************************");
	}
	ArrayList<Integer> currList = getRandomList();
	
	if ((this.playings.size() <= 6) && (this.playings.size() > 1)) {
	    for (int i = 0; i < playings.size(); i++) {
		PokerPlayer p = this.playings.get(i);
		byte[] cards = new byte[2];
		for (int j = 2 * i; j < 2 * (i + 1); j++) {
		    cards[j - (2 * i)] = currList.get(j).byteValue();
		}
		p.setMyCards(cards);
		String s = p.username + ":(Cash " + p.cash + "):";
		for (int k = 0; k < cards.length; k++) {
		    s = s + " " + cards[k];
		}

		s = s + "   ;(" + cardToString(cards) + ")";
		if (DatabaseDriver.log_code) {
		    out_code.println(s);
		}
	    } // end for
	    centerCard = new byte[5];
	    for (int i = 0; i < 5; i++) {
		centerCard[i] = currList.get(i + 47).byteValue();
		System.out.println("centerCard[" + i + "] = " + centerCard[i] + "; ten = " + cardToString(centerCard[i]));
	    } // end for
	    String s = "Quan bai chung: ";

	    

	    for (int k = 0; k < centerCard.length; k++) {
		s = s + " " + centerCard[k];
	    } // end for

	    s = s + "   ;(" + cardToString(centerCard) + ")";
	    if (DatabaseDriver.log_code) {
		out.println(s);
		out.flush();
	    }
//            if (DatabaseDriver.log_code) {
//                logOut(s);
//                out.flush();
//            }
	    //cheat
//            playings.get(0).setMyCards(new byte[]{7, 4});
//            playings.get(1).setMyCards(new byte[]{10, 17});
//            centerCard = new byte[]{18, 32, 50, 16, 30};

	} else {
	    mLog.debug("Sai ne!");
	}
	
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
    }

    public ArrayList<Integer> getRandomList() {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - getRandomList");
	}
	
	ArrayList<Integer> res = new ArrayList<Integer>();
	ArrayList<Integer> currList = new ArrayList<Integer>();


	for (int i = 0; i < 52; i++) {
	    currList.add(i, i + 1);
	}
	
	for (int i = 0; i < 52; i++) {
	    int index = getRandomNumber(currList, res);
	    currList.remove(index);
	}
	
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return res;
    }

    public int getRandomNumber(ArrayList<Integer> input, ArrayList<Integer> result) {
//	int previousMethodCallLevel;
//	if (DebugConfig.FOR_DEBUG) {
//	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
//	    DebugConfig.CALL_LEVEL ++;
//	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - getRandomNumber");
//	}
	
	int lengh = input.size() - 1;
	int index = (int) Math.round(Math.random() * lengh);
	result.add(input.get(index));
	
//	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return index;
    }
    // Start match

    public void startMatch() {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - startMatch");
	}
	
	long time = System.currentTimeMillis();
//        System.out.println(getValue(18));
	resetTable();
	if (this.playings.size() > 1) {
	    this.isPlaying = true;
	    for (PokerPlayer p : playings) {
		try {
		    DatabaseDriver.updateUserGameStatus(p.id, 1);
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
	    }
	    chia();
	    numStart();
	    startBet();
	    resetBetLevel(true);

	    if (DatabaseDriver.log_code && out != null) {
		out.println("Start Game DB Processing : " + (System.currentTimeMillis() - time) + "ms");
		out.flush();
	    }

	    this.timerAuto.setOwnerSession(ownerSession);
//            startTime();
//            try {
//                this.timerAuto.start();
//            } catch (Exception e) {
//                this.timerAuto.reset();
//            }
	} else {
	    mLog.debug("Chua co nguoi choi nao!");
	}

	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
    }

    public void resetBetMoney() {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - resetBetMoney");
	}
	
	for (PokerPlayer p : playings) {
	    p.currentBetMoney = 0;
	}
	
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
    }

    public void resetPlayerTurn() {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - resetPlayerTurn");
	}
	
	System.out.println("reset player turn!");
	for (PokerPlayer p : playings) {
	    p.turned = false;
	}
	
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
    }

    public void resetAllInPlayer() {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - resetAllInPlayer");
	}
	
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
//        long min = Long.MAX_VALUE;
//
//        for (PokerPlayer p : playings) {
//            if (!p.isFold && p.currentBetMoney < min) {
//                min = p.currentBetMoney;
//            }
//        }
//        for (PokerPlayer p : playings) {
//            if (p.allInRound == numRound) {
//                if (p.allInMoney > min) {
//                    p.allInRound = -1;
//                    p.cash = p.allInMoney - min;
//                    pot[numRound] -= p.cash;
//                    System.out.println(p.username + " reset allinround: " + p.cash);
//
//
//                }
////                else {
////                    p.cash -= min;
////                }
//
//            }
//        }
//
//        for (PokerPlayer p : playings) {
//            if (p.allInRound == numRound && p.allInMoney < min) {
//                min = p.allInMoney;
//
//
//            }
//        }
//        for (PokerPlayer p : playings) {
//            if (p.allInRound == numRound) {
//                if (p.allInMoney > min) {
//                    System.out.println(p.username + " reset allinround");
//                    p.allInRound = -1;
//                    p.cash = p.allInMoney - min;
//                    pot[numRound] -= p.cash;
//
//
//                }
////                else {
////                    p.cash -= min;
////                }
//
//            }
//        }
    }

    public void startNewRound() {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - startNewRound");
	}
	
	resetAllInPlayer();
	resetPlayerTurn();

//        numStart();
	if (!checkEndMatch() && numRound < 3) {
	    resetBetMoney();
	    numStart();
	    lastTurnID = getCurrentTurnID();
	    {
		new Thread() {
		    public void run() {
			try {
			    sleep(2000);
			} catch (InterruptedException ex) {
			    java.util.logging.Logger.getLogger(PokerTable.class.getName()).log(Level.SEVERE, null, ex);
			}
			sendPoker(false);
		    }
		}.start();
	    }
	} else {
	    sendEndMatch(-1);
	}
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
    }

//    public void cancelMatch(long uid) {
//        onBet(uid, 0, true);
//    }
    public void sendEndMatch(long idCancel) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - sendEndMatch: " + "; idCancel = " + idCancel);
	}
	
//        new Thread() {
//
//            public void run() {
//                sendPoker(true);
//
//
//                try {
//                    sleep(2000);
//
//
//
//                } catch (InterruptedException ex) {
//                    java.util.logging.Logger.getLogger(PokerTable.class.getName()).log(Level.SEVERE, null, ex);
//                }
//
//
//
//            }
//        }.start();
	endMatchProcess();
	isPlaying = false;
	Zone zone = this.ownerSession.findZone(ZoneID.POKER);
	Room room = zone.findRoom(this.getMatchID());
	MessageFactory msgFactory = this.ownerSession.getMessageFactory();
	EndMatchResponse endMatchRes = (EndMatchResponse) msgFactory.getResponseMessage(MessagesID.MATCH_END);
	endMatchRes.money = getPotMoney();
	endMatchRes.setPokerPlayer(copPlayerList());
	endMatchRes.setZoneID(ZoneID.POKER);
	endMatchRes.remainPoker = Utils.bytesToString(getRemainPoker());

	if (idCancel != 0) {
	    endMatchRes.uid = idCancel;
	}
	endMatchRes.setSuccessPoker(1, getMatchID());

	if (owner.notEnoughMoney()) {
	    PokerPlayer p1 = ownerQuit();
	    room.setOwnerName(p1.username);
	    owner = p1;
	    System.out.println(p1.id + " " + p1.username + " p1");
	    endMatchRes.newOwner = p1.id;
	}
	resetPlayer();
	room.broadcastMessage(endMatchRes, ownerSession, true);
	
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
    }

    public byte[] getCurrentPoker() {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - getCurrentPoker");
	}
	
	byte[] poker = null;
	if (numRound == 1) {
	    poker = new byte[3];
	    for (int i = 0; i < 3; i++) {
		poker[i] = centerCard[i];
	    }
	} else if (numRound == 2) {
	    poker = new byte[4];
	    for (int i = 0; i < 4; i++) {
		poker[i] = centerCard[i];
	    }

	} else if (numRound == 3) {
	    poker = new byte[5];
	    poker = new byte[5];
	    for (int i = 0; i < 5; i++) {
		poker[i] = centerCard[i];
	    }
	}
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return poker;
    }

    public byte[] getRemainPoker() {
//        if (numRound == 3) {
//            ;
//        }
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - getRemainPoker");
	}
	
	byte[] poker = new byte[0];
	if (numRound == 0) {
	    poker = new byte[5];
	    for (int i = 0; i < 5; i++) {
		poker[i] = centerCard[i];
	    }
	} else if (numRound == 1) {
	    poker = new byte[2];
	    poker[0] = centerCard[3];
	    poker[1] = centerCard[4];
	} else if (numRound == 2) {
	    poker = new byte[1];
	    poker[0] = centerCard[4];
	}
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return poker;
    }

    public void sendPoker(boolean sendAll) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - sendPoker: sendAll = " + sendAll);
	}
	
	Zone zone = this.ownerSession.findZone(ZoneID.POKER);
	Room room = zone.findRoom(this.getMatchID());
	MessageFactory msgFactory = this.ownerSession.getMessageFactory();
	TurnResponse res = (TurnResponse) msgFactory.getResponseMessage(MessagesID.MATCH_TURN);
	byte[] poker = new byte[1];
	if (!sendAll) {
	    numRound++;
	    resetBetLevel(true);
	    currentBet = minBet;
	    res.setMinMaxBet(minBet, maxBet);

	    if (numRound == 1) {
		poker = new byte[3];
		for (int i = 0; i < 3; i++) {
		    poker[i] = centerCard[i];
		}
	    } else if (numRound == 2) {
		poker[0] = centerCard[3];
	    } else if (numRound == 3) {
		poker[0] = centerCard[4];
	    }
	    res.isNewRound = true;
	    res.nextID = getCurrentTurnID();

	    res.potMoney = getPotMoney();
	    for (PokerPlayer p : playings) {
		if (!p.isOutGame && !p.isFold && !p.isAllIn()) {
		    int cardsType = checkCardsType(coppyHand((p.myHand), false));
		    if (cardsType > 0) {
			p.setCardsType(cardsType, getCards(coppyHand((p.myHand), false), cardsType));
		    }
		}
	    }
	    res.pokerPlayers = copPlayerList();
	} else {
//            if (numRound == 3) {
//                return;
//            }
//            if (numRound == 0) {
//                poker = new byte[5];
//
//
//                for (int i = 0; i
//                        < 5; i++) {
//                    poker[i] = centerCard[i];
//                }
//            } else if (numRound == 1) {
//                poker = new byte[2];
//                poker[0] = centerCard[3];
//                poker[1] = centerCard[4];
//            } else if (numRound == 2) {
//                poker[0] = centerCard[4];
//            }
//            res.isSendAll = true;
	}
	res.poker = Utils.bytesToString(poker);
	res.matchId = this.getMatchID();
	res.setSuccessPoker(1, ZoneID.POKER);
	room.broadcastMessage(res, this.ownerSession, true);
	if (DatabaseDriver.log_code) {
	    if (!sendAll) {
		out.println("-------------bat dau chia vong: " + numRound + "-----------");
	    } else {
		out.println("-------------chia het cac quan bai con lai (ket thuc van):-----------");
	    }
	    String s = "Chia : ";
	    for (int k = 0; k < poker.length; k++) {
		s = s + " " + poker[k];
	    }
	    s = s + " (" + cardToString(poker) + ")";
	    out.println(s);
//            out.flush();
	}
	
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
    }

    @Override
    public PokerPlayer findPlayer(long uid) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - findPlayer: uid = " + uid);
	}
	
	for (PokerPlayer p : this.playings) {
	    if (p.id == uid) {
		if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		return p;
	    }
	}
	for (PokerPlayer p : this.waitings) {
	    if (p.id == uid) {
		if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		return p;
	    }
	}
	
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return null;
    }

    public int numRealPlaying() {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - numRealPlaying");
	}
	
	int sum = 0;
	for (PokerPlayer p : playings) {
	    if ((!p.isOutGame)) {
		sum++;
	    }
	}
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return sum;
    }

    public boolean isAllFold() {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - isAllFold");
	}
	
	int sum = 0;
	for (PokerPlayer p : playings) {
	    if ((!p.isOutGame) && (!p.isFold)) {
		sum++;
	    }
	}
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return sum < 2;
    }

    public boolean checkEndMatch() {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - checkEndMatch");
	}
	
	int sum = 0;
	for (PokerPlayer p : playings) {
	    if ((!p.isOutGame) && (!p.isAllIn()) && (!p.isFold)) {
		sum++;
	    }
	}
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return sum < 2;
    }

    public void endMatchProcess() {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - endMatchProcess");
	}
	
	resetAllInPlayer();
	for (int i = 3; i >= 0; i--) {
	    if (pot[i] > 0) {
		ArrayList<PokerPlayer> arr = new ArrayList<PokerPlayer>();
		for (PokerPlayer p : playings) {
//                    System.out.println(p.username + ": allinround: " + p.allInRound);
		    if ((!p.allIn() || p.allInRound >= i) && !p.isOutGame && !p.isFold) {
			System.out.println("added: " + p.username);
			arr.add(p);
		    }
		}
		//người thắng từng vòng
		if (arr.size() > 0) {
		    ArrayList<PokerPlayer> winners = checkWinner(arr);
		    int numWinner = winners.size();
		    int numLost = 0;
		    long realWinMoney = 0;
		    long totalWinMoney = 0;
		    Vector<Long> winMoneys = new Vector<Long>();
		    for (PokerPlayer p : winners) {
			realWinMoney += p.potMoney[i];
			p.isWinner = true;
		    }
		    System.out.println("số người thắng vòng " + i + ": " + numWinner);
		    logOut("số người thắng vòng " + i + ": " + numWinner);
		    for (PokerPlayer p : playings) {
			if (!p.isWinner) {
			    System.out.println(p.username + " bỏ ra: " + p.potMoney[i] + " ở vòng " + i);
			    logOut(p.username + " bỏ ra: " + p.potMoney[i] + " ở vòng " + i);
			    long win = 0;
			    if (realWinMoney > p.potMoney[i]) {
				totalWinMoney += p.potMoney[i];
				win = p.potMoney[i];
				System.out.println(p.username + " thua  " + p.potMoney[i] + " ở vòng " + i);
				logOut(p.username + " thua  " + p.potMoney[i] + " ở vòng " + i);
			    } else {
				p.cash += p.potMoney[i] - realWinMoney;
				win = realWinMoney;
				totalWinMoney += realWinMoney;
				System.out.println(p.username + " thua  " + realWinMoney + " ở vòng " + i);
				logOut(p.username + " thua  " + realWinMoney + " ở vòng " + i);
			    }
			    if (win > 0) {
				numLost++;
				winMoneys.add(Long.valueOf(win));
			    }
			} else {
			    p.cash += p.potMoney[i];
			}
		    }
//                    System.out.println("số người thua vòng " + i + ": " + numLost);
		    System.out.println("Tổng tiền thắng ở vòng " + i + ": " + totalWinMoney);
		    logOut("Tổng tiền thắng ở vòng " + i + ": " + totalWinMoney);
		    if (numWinner >= 1 && totalWinMoney > 0) {
			for (long aWin : winMoneys) {
			    int currNumWinner = numWinner;
			    do {
				long winMoney = aWin / currNumWinner;
				boolean b = false;
				for (PokerPlayer p : winners) {
				    if (!p.addedMoney) {
					System.out.println(p.username + " bỏ ra: " + p.potMoney[i] + " ở vòng " + i);
					logOut(p.username + " bỏ ra: " + p.potMoney[i] + " ở vòng " + i);
					if (p.potMoney[i] < winMoney) {
					    p.money += p.potMoney[i];

					    System.out.println(p.username + " thắng  " + p.potMoney[i] * numLost + " ờ vòng " + i);
					    logOut(p.username + " thắng  " + p.potMoney[i] * numLost + " ờ vòng " + i);
					    currNumWinner--;
					    aWin -= p.potMoney[i];
					    p.addedMoney = true;
					    b = true;
					}
				    }
				}
				if (!b) {
				    for (PokerPlayer p : winners) {
					if (!p.addedMoney) {
					    p.money += winMoney;
					    System.out.println(p.username + " thắng  " + winMoney + " ờ vòng " + i);
					    logOut(p.username + " thắng  " + winMoney + " ờ vòng " + i);
					    currNumWinner--;
					    p.addedMoney = true;
					}
				    }
				}
			    } while (currNumWinner > 0);
			    for (PokerPlayer p : winners) {
				p.addedMoney = false;
			    }
			}
		    }
		    for (PokerPlayer p : winners) {
			p.addedMoney = false;
			p.isWinner = false;
//                        System.out.println("người thắng vòng " + i + ": " + p.username + " - tiền: " + p.money);
			byte[] biggestCard = coppyHand(p.myHand, true);
			int t = checkCardsType(biggestCard);
			if (t > 0) {
			    System.out.println(cardToString(getCards(biggestCard, t)));
			}
			if (DatabaseDriver.log_code) {
//                            out.println("nguoi thang vong" + i + ": " + p.username + ": +" + p.money + "US");
//                            out.flush();
			}
		    }
		}

	    }
	}
	for (PokerPlayer p : playings) {
	    if (!p.isOutGame && !p.isFold) {
		int cardsType = checkCardsType(coppyHand((p.myHand), true));
		if (cardsType > 0) {
		    p.setCardsType(cardsType, getCards(coppyHand((p.myHand), true), cardsType));
		}
	    }
	    if (p.money == 0) {
		System.out.println("người thua: " + p.username + " : " + (p.cash - p.firstCash));
		logOut("người thua: " + p.username + " : " + (p.cash - p.firstCash));
		p.money = p.cash - p.firstCash;
	    } else {
		p.cash = p.firstCash + p.money;
		logOut("người thắng: " + p.username + " : " + (p.cash - p.firstCash));
		System.out.println("người thắng: " + p.username + " : " + (p.cash - p.firstCash));

//                p.money = p.cash - p.firstCash;
	    }
	}
	updateCash();
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
    }

    public void endMatchProcessOld() {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - endMatchProcessOld");
	}
	
	resetAllInPlayer();
	for (int i = 3; i >= 0; i--) {
	    if (pot[i] > 0) {
		ArrayList<PokerPlayer> arr = new ArrayList<PokerPlayer>();


		for (PokerPlayer p : playings) {
		    System.out.println(p.username + ": allinround: " + p.allInRound);


		    if ((!p.isAllIn() || p.allInRound >= i) && !p.isOutGame && !p.isFold) {
			System.out.println("added: " + p.username);
			arr.add(p);

		    }
		}
		//người thắng từng vòng
		if (arr.size() > 0) {
		    ArrayList<PokerPlayer> winners = checkWinner(arr);
		    int num = winners.size();
		    for (PokerPlayer p : winners) {
			long winMoney = pot[i] / num;
			System.out.println("pot " + i + pot[i] + "----num " + num);

			System.out.println(p.username + ": " + p.isAllIn() + " " + p.allInMoney + " ---- " + p.allInRound);
			if (p.isAllIn() && p.allInRound == i) {
			    if (p.allInMoney < winMoney) {
				long realWinMoney = p.allInMoney;
				long returnMoney = 0;
				long totalLostMoney = 0;
				int numLost = 0;
				System.out.println("realWinMoney: " + realWinMoney);
				System.out.println("arr.size: " + arr.size());

				for (PokerPlayer player : arr) {
				    System.out.println("player.isAllIn(): " + player.isAllIn() + "---player.isAllIn()" + player.allInRound);

				    if (!player.isAllIn() || player.allInRound != i) {
					boolean isWinner = false;
					for (PokerPlayer winPlayer : winners) {
					    if (winPlayer.id == player.id) {
						isWinner = true;
					    }
					}
					if (!isWinner) {
					    totalLostMoney += realWinMoney;
					    numLost++;
					}
				    }

				}
//                                numLost+=1;
				if (numLost > 0) {
				    returnMoney = (winMoney - (realWinMoney + totalLostMoney) / num) / numLost;
				    System.out.println("returnMoney: " + returnMoney + "----numLost" + numLost);

				    for (PokerPlayer player : arr) {
					if (!player.isAllIn() || player.allInRound != i) {
					    boolean isWinner = false;
					    for (PokerPlayer winPlayer : winners) {
						if (winPlayer.id == player.id) {
						    isWinner = true;
						}
					    }
					    if (!isWinner) {
						player.money += returnMoney;
					    }
					}

				    }


				    winMoney = (realWinMoney * numLost + realWinMoney) / num;
				    System.out.println("winMoney: " + winMoney);
				}
			    }
			}
			p.money += winMoney;
			System.out.println("người thắng vòng " + i + ": " + p.username + " - tiền: " + winMoney);
//                        byte[] biggestCard = p.getMyHandForMe(numRound, true);
			byte[] biggestCard = coppyHand(p.myHand, true);
			int t = checkCardsType(biggestCard);
			if (t > 0) {
			    System.out.println(cardToString(getCards(biggestCard, t)));
			}
			if (DatabaseDriver.log_code) {
			    out_code.println("nguoi thang vong" + i + ": " + p.username + ": +" + winMoney + "US");
//                            out.flush();
			}
		    }
		}

//                if (i == 3 || winner == null) {
//                    winner = aWinner;
//               }
//                /*for debug*/
//                byte[] biggestCard = coppyHand(aWinner.myHand);
//                int t = checkCardsType(biggestCard);
//                if (t> 52) {
//                    System.out.println(cardToString(getCards(biggestCard, t)));
//                } else {
//                    System.out.println("maxcard:" + maxCard(biggestCard));
//                }
//                /*for debug*/
	    }
	}
	for (PokerPlayer p : playings) {
	    if (!p.isOutGame && !p.isFold) {
		int cardsType = checkCardsType(coppyHand((p.myHand), true));
		if (cardsType > 0) {
		    p.setCardsType(cardsType, getCards(coppyHand((p.myHand), true), cardsType));
		}
	    }
	    if (p.money == 0) {
		System.out.println("người thua: " + p.username + " tien: " + (p.cash - p.firstCash));
		p.money = p.cash - p.firstCash;
	    } else {
		System.out.println("người thang: " + p.username + " p.cash  " + p.cash + "  p.money  " + p.money + "  p.firstCash  " + p.firstCash);

		p.cash += p.money;
		p.money = p.cash - p.firstCash;
	    }
	}
	updateCash();
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
    }

    public ArrayList<PokerPlayer> checkWinner(ArrayList<PokerPlayer> arr) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - checkWinner: arrPlayers");
	}
	
	ArrayList<PokerPlayer> resultArr = new ArrayList<PokerPlayer>();
	if (arr.size() == 1) {
	    resultArr.add(findPlayer(arr.get(0).id));
	    return resultArr;
	}
	int winIndex = 0;
	for (int i = 1; i < arr.size(); i++) {
	    if (compareCards(coppyHand(arr.get(i).myHand, true), coppyHand(arr.get(winIndex).myHand, true), arr.get(i).myHand, arr.get(winIndex).myHand) == 1) {
		winIndex = i;
	    }
	}
	PokerPlayer p = arr.get(winIndex);
	resultArr.add(p);
	for (int i = 1; i < arr.size(); i++) {
	    if (compareCards(coppyHand(p.myHand, true), coppyHand(arr.get(i).myHand, true), p.myHand, arr.get(i).myHand) == 0) {
		PokerPlayer p1 = arr.get(i);
		if (p1.id != p.id) {
		    resultArr.add(p1);
		}
	    }
	}
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return resultArr;
    }

    public boolean isCenterCards(byte[] revCards) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - isCenterCards");
	}
	
	for (int i = 0; i < revCards.length; i++) {
	    boolean isContainsCard = false;
	    for (int j = 0; j
		< centerCard.length; j++) {
		if (revCards[i] == centerCard[j]) {
		    isContainsCard = true;
		    break;
		}
	    }
	    if (!isContainsCard) {
		if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		return false;
	    }
	}
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return true;
    }

    public PokerPlayer ownerQuit() {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - ownerQuit");
	}
//        System.out.println("owner quit!");
	for (int i = 0; i < playings.size(); i++) {
	    if (!playings.get(i).notEnoughMoney() && !playings.get(i).isOutGame) {
		ISession p = playings.get(i).currentSession;
		for (int j = 0; j
		    < playings.size(); j++) {
		    playings.get(j).currentOwner = p;
		}
		owner = playings.get(i);
		ownerSession = p;
//                resetBlackList();
		if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		return owner;
	    }
	}
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return null;
    }

    public void resetTable() {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - resetTable");
	}
	
	betTypeDes = "";
	currentBet = 0;
	mCurrentBet = 0;
	numRound = 0;
	pot = new long[4];
	centerCard = new byte[5];
	winner = null;
//        potMoney = 0;
	for (PokerPlayer player : this.playings) {
	    player.cash = getMoney(player.id);
	    player.reset(firstCashBet);
	}

	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
    }

    public void resetPlayer() {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - resetPlayer");
	}
	
//remove người chơi thoát giữa chừng để chuẩn bị ván mới
	ArrayList<PokerPlayer> needRemovePlayer = new ArrayList<PokerPlayer>();
	for (PokerPlayer p : playings) {
	    if (p.isOutGame || p.notEnoughMoney()) {
		needRemovePlayer.add(p);
	    }
	}
	if (needRemovePlayer.size() > 0) {
	    for (int i = 0; i
		< needRemovePlayer.size(); i++) {
		playings.remove(needRemovePlayer.get(i));
	    }
	}

	playings.addAll(waitings);
	waitings.clear();

	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
    }

    public void updateCashQuitPlayer(TienLenPlayer p) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - updateCashQuitPlayer");
	}
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
//        updateGift();
    }

    public void startTime() {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - startTime");
	}
	
	timerAuto.setTimer(20000);
	timerAuto.setPoker(currPlayer, this);
//        timerAuto.setTienLenTable(this);
	timerAuto.setRuning(true);
	timerAuto.reset();
	
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
    }

    public void updateGift() {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - updateGift");
	}
	
	for (PokerPlayer p : playings) {
	    if (p.cash == 0) {
		ISession is = p.currentSession;
		if (is != null) {
		    if (is.getRemainGift() > 0) {
			if (DatabaseDriver.log_code) {
			    logOut("Gift to " + p.username + " -> " + is.getCashGift());
			}
			is.setGiftInfo(1, is.getRemainGift());
			DatabaseDriver.updateGiftInfo(p.id, 1, is.getRemainGift());
		    } // end if (is.getRemainGift() > 0)
		} // end if (is != null)
	    } // end if (p.cash == 0)
	} // end for 
    }

    public void updateCash() {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - updateCash");
	}
	
	startTime = System.currentTimeMillis();
	//TODO:
	for (int i = 0; i < playings.size(); i++) {
	    try {
		long plus = playings.get(i).money;

		String desc = "Choi game Poker matchID : " + matchID + "-" + matchNum + " . (cash : " + playings.get(i).cash + ")";
//        
//                int zoneID = ZoneID.POKER;
		if (!pokerTable) {
		    desc = "Choi game Xi To matchID : " + matchID + "-" + matchNum + " . (cash : " + playings.get(i).cash + ")";

//                    zoneID = ZoneID.XITO;
		}
		//                if (playings.get(i).id == winner.id) {
		if (plus > 0) {
		    playings.get(i).money = plus - plus * 10 / 100;
		    plus = playings.get(i).money;
		}
//                System.out.println("Tiền người thắng được cộng: " + plus);
//                playings.get(i).cash = DatabaseDriver.getUserMoney(winner.id) + plus;

//                }
		//                playings.get(i).cash = playings.get(i).cash + plus;

		logOut("End : " + playings.get(i).username + " : " + plus + " -> " + playings.get(i).cash);
		//người thoát giữa chừng đã bị trừ tiền lúc thoát rồi


//                if (!playings.get(i).isOutGame) {
//                if (virtualRoom) {
//                    DatabaseDriver.updateVirtualUserGameMoney(plus, true, playings.get(i).id, desc, zoneID, 10);
//                } else {
		DatabaseDriver.updateUserMoney(plus, true, playings.get(i).id, desc);
//                    DatabaseDriver.updateUserGameMoney(plus, true, playings.get(i).id, desc, zoneID);
//                }
		DatabaseDriver.updateUserGameStatus(playings.get(i).id, 0);
//                DatabaseDriver.updateUserEndGame(playings.get(i).id, playings.get(i).isWin);
		//                }

		//DatabaseDriver.updateExp(playings.get(i).id, 1, playings.get(i).currentSession);
//                playings.get(i).cash = getMoney(playings.get(i).id);
		playings.get(i).cash = DatabaseDriver.getUserMoney(playings.get(i).id);
	    } catch (Exception ex) {
		ex.printStackTrace();
//                java.util.logging.Logger.getLogger(PhomTable.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}
	try {
//            updateGift();
	} catch (Exception e) {
	    e.printStackTrace();
	}


	logOut("End Game DB Processing : " + (System.currentTimeMillis() - startTime) + "ms");
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
//        out.flush();
    }

    public void destroy() {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - destroy");
	}
	
	for (PokerPlayer p : playings) {
	    try {
		DatabaseDriver.updateUserGameStatus(p.id, 0);
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	}
	
	try {
	    timerAuto.destroy();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	super.destroy();
//        System.out.println("Destroy : " + this.name);
	//timer.destroy();
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
    }

    public JSONArray getPlayerName() {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PokerTable - getPlayerName");
	}
	
	JSONArray ja = new JSONArray();
	try {
	    for (PokerPlayer p : playings) {
		JSONObject jo = new JSONObject();
		jo.put("name", p.username);
		jo.put("id", p.id);
		ja.put(jo);
	    }
	    for (PokerPlayer p : waitings) {
		JSONObject jo = new JSONObject();
		jo.put("name", p.username);
		jo.put("id", p.id);
		ja.put(jo);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return ja;
    }

// them moi khong can quan tam    
    @Override
    public long getJoinMoney() {
	return GameRuleConfig.MONEY_TIMES_BET_TO_JOIN * firstCashBet;
    }

    @Override
    public String getJoinMoneyErrorMessage() {
	return "Bạn cần có ít nhất " + GameRuleConfig.MONEY_TIMES_BET_TO_JOIN + " lần tiền cược của bàn để tham gia!";
    }
}
