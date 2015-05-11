package dreamgame.data;

import dreamgame.cotuong.data.CoTuongPlayer;
import dreamgame.cotuong.data.CoTuongTable;
import java.util.ArrayList;

//import dreamgame.oantuti.data.OTTPlayer;
//import dreamgame.oantuti.data.OantutiTable;

import org.slf4j.Logger;

import phom.data.PhomPlayer;
import phom.data.PhomTable;

import dreamgame.protocol.messages.EndMatchResponse;
import dreamgame.protocol.messages.TurnRequest;
import dreamgame.bacay.data.BacayPlayer;
import dreamgame.baucua.data.BauCuaPlayer;
import dreamgame.baucua.data.BauCuaTable;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.poker.data.PokerPlayer;
import dreamgame.poker.data.PokerTable;
import dreamgame.protocol.messages.TimeOutResponse;
import dreamgame.protocol.messages.TurnResponse;
import dreamgame.tienlen.data.TienLenPlayer;
import dreamgame.tienlen.data.TienLenTable;
import dreamgame.xito.data.XiToTable;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IBusiness;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.room.Room;
import dreamgame.gameserver.framework.room.Zone;
import dreamgame.gameserver.framework.session.ISession;

public class Timer extends Thread {

    /**
     * Rate at which timer is checked
     */
    protected int m_rate = 100;
    /**
     * Length of timeout
     */
    private int m_length;
    /**
     * Time elapsed
     */
    private int m_elapsed;
    /**
     * Creates a timer of a specified length
     *
     * @param	length	Length of time before timeout occurs
     */
    private Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(Timer.class);
    private BacayPlayer bacayPlayer;
    private SimplePlayer simplePlayer;
    @SuppressWarnings("unused")
//    private OTTPlayer ottPlayer;
    private boolean isRuning = false;
    private int zoneID;
    private boolean destroy = false;
//Thomc
    private TienLenPlayer tienlenPlayer;
    public TienLenTable tienlenTable;
//Thomc 
    private CoTuongPlayer cotuongPlayer;
    public CoTuongTable coTuongTable;
    //poker
    private PokerPlayer pokerPlayer;
    public PokerTable pokerTable;
    //bầu cua
//    private boolean betTimeOut;
//    private BauCuaPlayer bauCuaPlayer;
    public BauCuaTable baucuaTable;
    //xì tố
    private boolean isShowTime = false;
    public XiToTable xitoTable;
    //maubinh

    public void setBauCuaTable(BauCuaTable t) {
//        baucuaTable = t;
//        betTimeOut = true;
//    }
//    public void setTimeBauCua(BauCuaTable t) {
        baucuaTable = t;
//        betTimeOut = true;
    }

    public void destroy() {
        destroy = true;
    }

    public void setRuning(boolean isRuning) {
        this.isRuning = isRuning;
    }
    private ISession ownerSession;

    public Timer(int zone, int timeout) {
        // Assign to member variable
        m_length = timeout;
        zoneID = zone;
        // Set time elapsed
        m_elapsed = 0;
    }

    public void setTimer(int l) {
        m_length = l;
    }

    public void setOwnerSession(ISession ownerSession) {
        this.ownerSession = ownerSession;
    }

    public void setCurrentPlayer(SimplePlayer currentPlayer) {
        this.simplePlayer = currentPlayer;
    }

    public SimplePlayer getSimplePlayer() {
        return simplePlayer;
    }

    /**
     * Resets the timer back to zero
     */
    public synchronized void reset() {
        m_elapsed = 0;
    }

    /**
     * Performs timer specific code
     */
    public long getCurrentTime() {
        return m_elapsed;
    }

    public void run() {
        System.out.println("Came here! Timer run !");
        // Keep looping
        for (;;) {
            // Put the timer to sleep
            try {
                Thread.sleep(m_rate);
            } catch (InterruptedException ioe) {
                continue;
            }

            if (destroy) {
                System.out.println("Timer get out!");
                return;
            }

            // Use 'synchronized' to prevent conflicts
            synchronized (this) {
                // Increment time remaining
                m_elapsed += m_rate;
                if (this.zoneID == ZoneID.COTUONG) {
                    if (this.isRuning) {
                        cotuongPlayer.remainTime -= m_rate;
                    }
                }
                if (this.zoneID == ZoneID.MAUBINH) {
                }
                // Check to see if the time has been exceeded                
                //System.out.println(m_elapsed+ " : "+m_length+"  ; "+isRuning);

                if (m_elapsed > m_length) {
                    // Trigger a timeout
                    if (this.isRuning) {
                        switch (this.zoneID) {
                            case ZoneID.BACAY:
                                bacayTimeout();
                                break;
                            case ZoneID.OTT:
//                                oanTutiTimeOut();
                                break;
                            case ZoneID.PHOM: {
                                phomTimeOut();
                                break;

                            }
                            case ZoneID.GAME_CHAN: {
                                chanTimeOut();
                                break;

                            }
                            case ZoneID.TIENLEN: {
                                tienLenTimeOut();
                                break;
                            }
                            case ZoneID.COTUONG: {
                                cotuongTimeout();
                                break;

                            }
                            case ZoneID.POKER: {
                                pokerTimeOut();
                                break;

                            }
                            case ZoneID.XITO: {
                                xitoTimeOut();
                                break;

                            }
                            case ZoneID.BAUCUA: {
                                bauCuaTimeOut();
                                break;
                            }
                            case ZoneID.MAUBINH: {
                                break;
                            }
                            //TODO: add more here
                            default:
                                break;
                        }

                    } else {
                        reset();
                    }
                }
            }

        }
    }
    /**
     * Phom
     */
    public PhomTable phomTable;

    public void setPhomTable(PhomTable phomTable) {
        this.phomTable = phomTable;
    }
    public PhomPlayer phomPlayer;

    public void setPhomPlayer(PhomPlayer phomPlayer) {
        if (phomPlayer == null) {
            System.out.println("OMG, it's null");
        }
        this.phomPlayer = phomPlayer;
    }
    //tiến lên

    public void setTienLenTable(TienLenTable tienlenTable_) {
        this.tienlenTable = tienlenTable_;
    }

    public void setTienLenPlayer(TienLenPlayer tienLenPlayer_) {
        this.tienlenPlayer = tienLenPlayer_;
    }

    public CoTuongTable getCoTuongTable() {
        return coTuongTable;
    }

    public void setCoTuongTable(CoTuongTable coTuongTable_) {
        this.coTuongTable = coTuongTable_;
    }

    public void setCoTuongPlayer(CoTuongPlayer coTuongPlayer_) {
        this.cotuongPlayer = coTuongPlayer_;
    }

    public void setPoker(PokerPlayer p, PokerTable t) {
        pokerPlayer = p;
        pokerTable = t;
    }


    public void chanTimeOut() {
        try {
            setRuning(false);

        } catch (Exception e) {
            e.printStackTrace();
            // TODO: handle exception
        }
    }

    public void phomTimeOut() {
        System.out.println("Came here! phomTimeOut ! " + phomPlayer.isAutoPlay);
        try {
            setRuning(false);

            if (phomTable.isPlaying) {
                if (phomPlayer.currentOwner != null) {

                    try {
                        phomTable.logCode();
                        phomTable.logCode("[Received][" + phomPlayer.currentSession.userInfo() + "][TIMEOUT] [ti_" + System.currentTimeMillis() + "]");
                    } catch (Exception e) {
                    }

                    phomPlayer.autoPlay(phomTable, this);
                } else {
                    System.out.println("OMG currentOwner is null!");
                }
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            // TODO: handle exception
        }
    }

    public void tienLenTimeOut() {
        try {
            setRuning(false);
            if (tienlenTable.isPlaying) {
                System.out.println("ownerSession" + ownerSession);
                System.out.println("tienlenPlayer.id" + tienlenPlayer.id);
                ISession session = ownerSession.getManager().findSession(tienlenPlayer.id);
                IResponsePackage responsePkg = session.getDirectMessages();//new SimpleResponsePackage();
                MessageFactory msgFactory = session.getMessageFactory();
                TurnRequest reqMatchTurn = (TurnRequest) msgFactory.getRequestMessage(MessagesID.MATCH_TURN);
                reqMatchTurn.isGiveup = true;
                reqMatchTurn.uid = tienlenPlayer.id;
                reqMatchTurn.mMatchId = tienlenTable.getMatchID();
                reqMatchTurn.isTimeoutTL = true;

                tienlenTable.logCode();
                tienlenTable.logCode("[Received]][" + tienlenPlayer.currentSession.userInfo() + "][TIMEOUT] [ti_" + System.currentTimeMillis() + "]");

                if (tienlenTable.isNewRound) {
                    reqMatchTurn.isGiveup = false;
                    reqMatchTurn.tienlenCards = tienlenPlayer.myHand[0] + "";
//                    tienlenTable.lastTurnID = tienlenTable.getPlayings().get(tienlenTable.findNext(tienlenTable.getUserIndex(tienlenPlayer.id))).id;
                }
                IBusiness business = null;
                // Check if timeout
                if (reqMatchTurn.uid != -1) {
                    try {
                        business = msgFactory.getBusiness(MessagesID.MATCH_TURN);
                        business.handleMessage(session, reqMatchTurn, responsePkg);
                    } catch (ServerException se) {
                    }
                }
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            // TODO: handle exception
        }
    }

    public void xitoTimeOut() {
        System.out.println("chay qua xi to timeout!");
        try {
            setRuning(false);
            if (isShowTime) {
                xitoTable.showAll();
                return;
            } else {
                System.out.println("chay qua xi to turn!");
                if (xitoTable.isPlaying) {
                    xitoTable.autoFold(pokerPlayer.id);
                    return;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            // TODO: handle exception
        }
    }

    public void pokerTimeOut() {
        System.out.println("timeout poker!");
        try {
            setRuning(false);
            if (pokerTable.isPlaying) {
//                System.out.println("ownerSession" + ownerSession);
//                System.out.println("tienlenPlayer.id" + tienlenPlayer.id);
                ISession session = ownerSession.getManager().findSession(pokerPlayer.id);
                IResponsePackage responsePkg = session.getDirectMessages();//new SimpleResponsePackage();
                MessageFactory msgFactory = session.getMessageFactory();
                TurnRequest reqMatchTurn = (TurnRequest) msgFactory.getRequestMessage(MessagesID.MATCH_TURN);
                reqMatchTurn.isFold = true;
                reqMatchTurn.money = 0;
                reqMatchTurn.uid = pokerPlayer.id;
                reqMatchTurn.isGiveup = true;
                reqMatchTurn.mMatchId = pokerTable.getMatchID();
//                reqMatchTurn.isTimeoutTL = true;

                pokerTable.logCode();
                pokerTable.logCode("[Received]][" + pokerPlayer.currentSession.userInfo() + "][TIMEOUT] [ti_" + System.currentTimeMillis() + "]");

                IBusiness business = null;
//                 Check if timeout
                if (reqMatchTurn.uid != -1) {
                    try {
                        business = msgFactory.getBusiness(MessagesID.MATCH_TURN);
                        business.handleMessage(session, reqMatchTurn, responsePkg);
                    } catch (ServerException se) {
                    }
                }
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            // TODO: handle exception
        }
    }

    public void bauCuaTimeOut() {
//        System.out.println("bau cua timeout!");
        try {
            setRuning(false);
//            if (betTimeOut) {
//                baucuaTable.stopBettingTime();
//                return;
//            } else {
            baucuaTable.endMatch();
//            }

        } catch (Exception eee) {
        }
    }
    /**
     * Oan Tu Ti
     */

    /**
     * Bacay
     */
    // Override this to provide custom functionality
    public void bacayTimeout() {
        reset();
        bacayPlayer = (BacayPlayer) simplePlayer;
        this.mLog.debug("[Bacay Timeout]: Process of: " + bacayPlayer.username);
        ISession session = ownerSession.getManager().findSession(bacayPlayer.id);
        IResponsePackage responsePkg = session.getDirectMessages();//new SimpleResponsePackage();
        MessageFactory msgFactory = session.getMessageFactory();
        IBusiness business = null;
        TurnRequest rqTurn = (TurnRequest) msgFactory.getRequestMessage(MessagesID.MATCH_TURN);
        rqTurn.mMatchId = bacayPlayer.currentMatchID;
        rqTurn.money = -1;
        rqTurn.isTimeout = true;
        rqTurn.uid = -1;
        business = msgFactory.getBusiness(MessagesID.MATCH_TURN);
        if (!bacayPlayer.isGetData) {
            this.mLog.debug(bacayPlayer.username + " Time Out!");
            rqTurn.uid = bacayPlayer.id;
        }
        // Check if timeout
        if (rqTurn.uid != -1) {
            try {
                business.handleMessage(session, rqTurn, responsePkg);
            } catch (ServerException se) {
            }
        }
    }

    public void setXiTo(PokerPlayer p, XiToTable t) {
        pokerPlayer = p;
        xitoTable = t;
        isShowTime = false;
    }

    public void setShowTime(XiToTable t) {
        xitoTable = t;
        isShowTime = true;
    }
    //co tuong

    public void cotuongTimeout() {
        if (!coTuongTable.isEnd()) {
            setRuning(false);
            reset();
            this.mLog.debug("[OTT]: Timeout Process");
            Zone zone = this.ownerSession.findZone(zoneID);
            System.out.println(coTuongTable + "coTuongTable isn't null!!!");
            System.out.println(coTuongTable.getMatchID() + "coTuongTable.getMatchID()isn't null!!!");
            Room room = zone.findRoom(this.coTuongTable.getMatchID());
            MessageFactory msgFactory = this.ownerSession.getMessageFactory();
            EndMatchResponse endMatchRes = (EndMatchResponse) msgFactory.getResponseMessage(MessagesID.MATCH_END);
            endMatchRes.setZoneID(zoneID);
            long idWin = this.ownerSession.getUID();
            if (this.ownerSession.getUID() == cotuongPlayer.id) {
                idWin = this.coTuongTable.player.id;
                coTuongTable.updateCash(false);
            } else {
                coTuongTable.updateCash(true);
            }
            try {
                endMatchRes.setMoneyEndMatch(DatabaseDriver.getUserMoney(coTuongTable.owner.id), DatabaseDriver.getUserMoney(coTuongTable.player.id));
            } catch (Exception ex) {
            }
            endMatchRes.setSuccess(ResponseCode.SUCCESS, idWin, this.coTuongTable.getMatchID());
            System.out.println(room + "isn't null!!!");

            room.broadcastMessage(endMatchRes, this.ownerSession, true);

        }
    }

}
