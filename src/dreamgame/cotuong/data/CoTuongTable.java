package dreamgame.cotuong.data;

import dreamgame.bacay.data.BacayPlayer;
import dreamgame.data.SimpleTable;
import dreamgame.data.Timer;
import dreamgame.data.ZoneID;
import dreamgame.databaseDriven.DatabaseDriver;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Dinhpv
 */
public class CoTuongTable extends SimpleTable {

    chessBoard board;
    final static int CARRIAGE = 0, HORSE = 1, PAO = 2, ELEPHANT = 3, GUARD = 4, KING = 5, SOLDIER = 6;
    private piece movingFrom;
    private boolean gameOver = false;
    private boolean saveHorse, saveCarriage, savePao;
    public int lastMove;
    public boolean mIsEnd;
    public Timer timer = new Timer(ZoneID.TIENLEN, 120000);
    public ArrayList<CoTuongPlayer> observers = new ArrayList<CoTuongPlayer>();
    public CoTuongPlayer owner;
    public CoTuongPlayer player;
    public CoTuongPlayer currPlayer;
    private Vector blkPieces, redPieces;
    public boolean isFull = false;
    public boolean isFullPlayer = false;
    public int available = 0;//chấp
    public boolean isPlayerReady = false;
    public ArrayList<CoTuongPlayer> player_list = new ArrayList<CoTuongPlayer>();
    public CoTuongPlayer prePlayer;
    public int turnTime = 120000;
    public int totalTime = 0;

    public CoTuongTable(CoTuongPlayer ow, long cash, int avail, int totalTime_) {
        this.available = avail;
        totalTime = totalTime_;
        owner = ow;
        player_list.add(ow);
        this.firstCashBet = cash;
//        timer.setRuning(false);
//        timer.start();
        logdir = "cotuong_log";
        resetBoard();
    }

    public ArrayList<CoTuongPlayer> getPlayers() {
        synchronized (this.observers) {
            return observers;
        }
    }

    public ArrayList<CoTuongPlayer> getPlayersList() {
        synchronized (this.player_list) {
            return player_list;
        }
    }

    public boolean canPlayerContinue() {

        long expectMoney = this.firstCashBet;
        long actualMoney = this.player.cash;
        if (actualMoney < expectMoney) {
            return false;
        }
        try {
            if (DatabaseDriver.getUserMoney(this.player.id) < 0) {
                return false;
            }
        } catch (Exception ex) {
        }
        return true;
    }

    public boolean canOwnerContinue() {

        long expectMoney = this.firstCashBet;
        long actualMoney = this.owner.cash;
        if (actualMoney < expectMoney) {
            return false;
        }
        try {
            if (DatabaseDriver.getUserMoney(this.owner.id) < 0) {
                return false;
            }
        } catch (Exception ex) {
        }
        return true;
    }

    public void setPlayer(CoTuongPlayer p) {
        p.remainTime = totalTime;
        player = p;
        isFull = true;
        isFullPlayer = true;
        isPlaying = true;
    }

    public void setCurrPlayer(boolean p) {
        if (p) {
            currPlayer = player;
        } else {
            currPlayer = owner;
        }
    }

    public void startTime() {
        System.out.println(currPlayer.getUsername() + " remain:" + currPlayer.remainTime);
        timer.setTimer(turnTime);
        if (currPlayer.remainTime < turnTime) {
            timer.setTimer(currPlayer.remainTime);
        }
        timer.setCoTuongPlayer(currPlayer);
        this.timer.setCoTuongTable(this);
        timer.setRuning(true);
        timer.reset();
        try {
            this.timer.start();
        } catch (Exception e) {
            this.timer.reset();
        }
    }

    public void endTime() {
        timer.setRuning(false);
    }

    public long getCurrPlayID() {
        return currPlayer.id;
    }

    public void startMatch() {
        //set default player is current player
        if (currPlayer == null) {
            currPlayer = player;
        }
        this.timer = new Timer(ZoneID.COTUONG, turnTime);
        this.timer.setCoTuongTable(this);
        this.timer.setOwnerSession(ownerSession);
        this.timer.setCoTuongPlayer(currPlayer);
        this.timer.setRuning(true);
//        try {
//            DatabaseDriver.updateUserGameStatus(player.id, 1);
//            DatabaseDriver.updateUserGameStatus(owner.id, 1);
//        } catch (Exception ex) {
//            Logger.getLogger(CoTuongTable.class.getName()).log(Level.SEVERE, null, ex);
//        }

        try {
            this.timer.start();
        } catch (Exception e) {
            this.timer.reset();
        }
    }

    public void removePlayer() {
        player = null;
        isFull = false;
        isPlaying = false;
    }

    public void addPlayer(CoTuongPlayer p) {
        this.observers.add(p);
    }

    public void removePlayer(CoTuongPlayer p) {
        this.observers.remove(p);
    }
//Tho remove all PlayerCoTuong when start match

    public void removeAllPlayer() {
        this.observers.clear();
    }

    public boolean isEnd() {

        return mIsEnd;
    }

    public long checkGameStatus() {
        if (board.isRedLosing()) {
            System.out.println("Owner is win!");
//            Tho
//            return owner.id;
            return player.id;
        }
        if (board.isBlkLosing()) {
            System.out.println("Player is win!");
            return owner.id;
//            Tho
//            return player.id;
        }
        if (board.isPeace()) {
            System.out.println("Match is peace!");
            return 0;
        }
        return -1;
    }

    public void resetBoard() {
        if (player != null) {
            player.remainTime = totalTime;
        }
        owner.remainTime = totalTime;
        gameOver = false;
        blkPieces = new Vector();
        redPieces = new Vector();
        initAllPieces();
        removeAvail(available);
        board = new chessBoard(blkPieces, redPieces);
        isPlayerReady = false;
    }
    //remove những quân chấp

    public void removeAvail(int peace) {
        switch (peace) {
            //chấp xe
            case 1:
                redPieces.removeElementAt(0);
                break;
            //  chấp pháo
            case 2:
                redPieces.removeElementAt(4);
                break;
            //  chấp mã
            case 3:
                redPieces.removeElementAt(2);
                break;
            default:
                return;

        }
    }

    public void destroy() {
        try {
//            logCode("Room destroy!");
//            out.println("Room destroy!");
//
//            out.close();
//            outFile.close();
//
//            out_code.close();
//            outFile.close();

            timer.destroy();

        } catch (Exception e) {
            e.printStackTrace();
        }
        super.destroy();
        System.out.println("Destroy : " + this.name);


        //timer.destroy();
    }

    public void initAllPieces() {
        blkPieces.addElement(new piece(CARRIAGE, 0, 9, true, true));
        blkPieces.addElement(new piece(CARRIAGE, 8, 9, true, true));
        blkPieces.addElement(new piece(HORSE, 1, 9, true, true));
        blkPieces.addElement(new piece(HORSE, 7, 9, true, true));
        blkPieces.addElement(new piece(PAO, 1, 7, true, true));
        blkPieces.addElement(new piece(PAO, 7, 7, true, true));
        blkPieces.addElement(new piece(ELEPHANT, 2, 9, true, false));
        blkPieces.addElement(new piece(ELEPHANT, 6, 9, true, false));
        blkPieces.addElement(new piece(GUARD, 3, 9, true, false));
        blkPieces.addElement(new piece(GUARD, 5, 9, true, false));
        blkPieces.addElement(new piece(KING, 4, 9, true, false));
        blkPieces.addElement(new piece(SOLDIER, 0, 6, true, false));
        blkPieces.addElement(new piece(SOLDIER, 2, 6, true, false));
        blkPieces.addElement(new piece(SOLDIER, 4, 6, true, false));
        blkPieces.addElement(new piece(SOLDIER, 6, 6, true, false));
        blkPieces.addElement(new piece(SOLDIER, 8, 6, true, false));

        redPieces.addElement(new piece(CARRIAGE, 0, 0, false, true));
        redPieces.addElement(new piece(CARRIAGE, 8, 0, false, true));
        redPieces.addElement(new piece(HORSE, 1, 0, false, true));
        redPieces.addElement(new piece(HORSE, 7, 0, false, true));
        redPieces.addElement(new piece(PAO, 1, 2, false, true));
        redPieces.addElement(new piece(PAO, 7, 2, false, true));
        redPieces.addElement(new piece(ELEPHANT, 2, 0, false, false));
        redPieces.addElement(new piece(ELEPHANT, 6, 0, false, false));
        redPieces.addElement(new piece(GUARD, 3, 0, false, false));
        redPieces.addElement(new piece(GUARD, 5, 0, false, false));
        redPieces.addElement(new piece(KING, 4, 0, false, false));
        redPieces.addElement(new piece(SOLDIER, 0, 3, false, false));
        redPieces.addElement(new piece(SOLDIER, 2, 3, false, false));
        redPieces.addElement(new piece(SOLDIER, 4, 3, false, false));
        redPieces.addElement(new piece(SOLDIER, 6, 3, false, false));
        redPieces.addElement(new piece(SOLDIER, 8, 3, false, false));
    }

    public void move(moveFromTo mv) {
        board.getPieceRef(mv.fromCol, mv.fromRow).col = mv.toCol;
        board.getPieceRef(mv.fromCol, mv.fromRow).row = mv.toRow;
        board.move(mv);
        board.incMovingCnt();
    }

    public CoTuongPlayer getOwner() {
        return owner;
    }

    public void setOwner(CoTuongPlayer owner) {
        this.owner = owner;
    }

    public ArrayList<CoTuongPlayer> getAllUserList() {
        ArrayList<CoTuongPlayer> temp = observers;
        temp.add(player);
        return temp;
    }
    //Tho

    public piece[][] getBoard() {
        return board.board;
    }

    public void updatePeaceCash() {
        String desc = "Choi game Co tuong matchID : " + matchID;
        long lostMoney = (long) (0.05 * firstCashBet);
        try {
//            DatabaseDriver.updateUserGameStatus(player.id, 0);
//            DatabaseDriver.updateUserGameStatus(owner.id, 0);
            
            owner.cash = DatabaseDriver.updateUserMoney(lostMoney, false, owner.id, desc);
            player.cash = DatabaseDriver.updateUserMoney(lostMoney, false, player.id, desc);

        } catch (Exception ex) {
            Logger.getLogger(CoTuongTable.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    //Thomc
    public void updateCash(boolean isOwnerWin) {
        try {
            String desc = "Choi game Co tuong matchID : " + matchID;
            long firstBet = firstCashBet;

            if (isOwnerWin) {
                long oldCash = DatabaseDriver.getUserMoney(player.id);
                if (firstBet > oldCash) {
                    firstBet = oldCash;
                }
            } else {
                long oldCash = DatabaseDriver.getUserMoney(owner.id);
                if (firstBet > oldCash) {
                    firstBet = oldCash;
                }
            }

            long winMoney = (long) (firstBet - (0.05 * firstBet));

//            DatabaseDriver.updateUserGameStatus(player.id, 0);
//            DatabaseDriver.updateUserGameStatus(owner.id, 0);

            if (isOwnerWin) {

                this.owner.cash = DatabaseDriver.updateUserMoney(winMoney, true, owner.id, desc);
                this.player.cash = DatabaseDriver.updateUserMoney(firstBet, false, player.id, desc);

            } else {

                this.owner.cash = DatabaseDriver.updateUserMoney(firstBet, false, owner.id, desc);
                this.player.cash = DatabaseDriver.updateUserMoney(winMoney, true, player.id, desc);

            }

        } catch (Exception ex) {
            ex.printStackTrace();
//            java.util.logging.Logger.getLogger(CoTuongTable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public JSONArray getPlayerName() {
        JSONArray ja = new JSONArray();

        try {

            for (CoTuongPlayer p : this.player_list) {
                JSONObject jo = new JSONObject();
                jo.put("name", p.username);
                jo.put("id", p.id);
                ja.put(jo);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ja;
    }

    @Override
    public long getJoinMoney() {
	return firstCashBet;
    }
    
    @Override
    public String getJoinMoneyErrorMessage() {
	return "Bạn không đủ tiền để tham gia bàn này!";
    }
}
