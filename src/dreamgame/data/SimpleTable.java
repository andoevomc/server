package dreamgame.data;

import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.gameserver.framework.session.ISession;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import org.json.JSONArray;

public class SimpleTable {

    public int maximumPlayer;
    public boolean isPlaying = false;
    public int level;
    public long matchID;
    public long matchNum = 0;
    public long matchIDAuto;
    public ISession ownerSession;
    public long firstCashBet;
    public SimplePlayer owner;
    public String name;

    public SimpleTable() {}
    
    public SimplePlayer findPlayer(long uid) throws SimpleException {
        return null;
    }
    public long startTime = 0;
    public FileWriter outFile_code;// = new FileWriter(args[0]);
    public PrintWriter out_code = null;// = new PrintWriter(outFile);
    public FileWriter outFile;// = new FileWriter(args[0]);
    public PrintWriter out;// = new PrintWriter(outFile);

    // get amoung of money that player need to have in order to join the table
    public long getJoinMoney() { return 0; };
    public String getJoinMoneyErrorMessage() { return "Bạn không đủ tiền để tham gia"; };
    
    public long getMoney(long uid) {
        
        try {
            long cash = 0;
            cash = DatabaseDriver.getUserMoney(uid);
            return cash;
        } catch (Exception e) {
        }
        return 0;
    }
    public long getMatchID() {
        return matchID;
    }

    public void removePlayer(long id) {
    }

    public boolean roomIsFull() {
        return false;
    }

    public void setMatchID(long matchID) {
        this.matchID = matchID;
    }

    public boolean getIsPlaying() {
        return this.isPlaying;
    }

    public void setIsPlaying(boolean b) {
        this.isPlaying = b;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getMaximumPlayer() {
        return maximumPlayer;
    }

    public long getMinBet() {
        return firstCashBet;
    }

    public long getMatchIDAuto() {
        return matchIDAuto;
    }

    public void logOut(String s) {
        if (!DatabaseDriver.log_code) {
            return;
        }
        if (out != null) {
            out.println(s);
        }
    }

    public void logOutPrint(String s) {
        if (!DatabaseDriver.log_code) {
            return;
        }
        if (out != null) {
            out.print(s);
        }
    }

    public void logOut() {
        logOut("");
    }

    public void logCode(String s) {
        if (!DatabaseDriver.log_code) {
            return;
        }
        if (out_code != null) {
            out_code.println(s);
        }
    }

    public void logCode() {
        logCode("");
    }

    public void destroy() {
        try {
            if (out_code != null) {
                logCode("Room destroy!");
                logOut("Room destroy!");

                out.close();
                outFile.close();
                out_code.close();
                outFile_code.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMatchIDAuto(long matchIDAuto) {
        this.matchIDAuto = matchIDAuto;
    }

    public JSONArray getPlayerName() {
        JSONArray ja = new JSONArray();
        return ja;
    }

    public void setOwnerSession(ISession ownerSession) {
        this.ownerSession = ownerSession;
    }

    public ISession getOwnerSession() {
        return ownerSession;
    }
    public String logdir = "none_log";

    public void initLogFile() {
//        if (!DatabaseDriver.log_code) {
//            return;
//        }

        try {
            File dir1 = new File(".");
            System.out.println("Current dir : " + dir1.getCanonicalPath());

            boolean success = (new File("logs/" + logdir)).mkdirs();
            System.out.println("Create dir success : " + success);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String str = "logs/" + logdir + "/match_" + matchID + ".txt";
        System.out.println("matchID : " + matchID + "  ;  " + str);
        if (matchID == 0) {
            return;
        }
        try {


            File f = new File(str);
            boolean append = false;
            if (f.exists()) {
                append = true;
            }

            outFile = new FileWriter("logs/" + logdir + "/match_" + matchID + ".txt", append);
            out = new PrintWriter(outFile);

            outFile_code = new FileWriter("logs/" + logdir + "/match_" + matchID + "_code.txt", append);
            out_code = new PrintWriter(outFile_code);
            System.out.println("initlog complete!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //Bị đuổi quá 2 lần không được vào phòng.
    public ArrayList<long[]> blackList = new ArrayList<long[]>();

    public void updateBlackList(long uid) {
        boolean addNew = true;
        for (int i = 0; i < blackList.size(); i++) {
            long[] blk = blackList.get(i);
            if (blk[0] == uid) {
                blk[1]++;
                blackList.set(i, blk);
                addNew = false;
                break;
            }
        }
        if (addNew) {
            long[] blk = new long[2];
            blk[0] = uid;
            blk[1] = 1;
            blackList.add(blk);
        }
    }

    public void resetBlackList() {
        blackList = new ArrayList<long[]>();
    }

    public boolean isblk(long uid) {
        for (long[] blk : blackList) {
            if (blk[0] == uid && blk[1] >= 2) {
                return true;
            }
        }
        return false;
    }
}
