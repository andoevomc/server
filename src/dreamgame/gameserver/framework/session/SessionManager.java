package dreamgame.gameserver.framework.session;

//import com.migame.gameserver.framework.common.ILoggerFactory;
import dreamgame.bacay.data.BacayTable;
import dreamgame.config.GameRuleConfig;
import dreamgame.data.CPEntity;
import dreamgame.data.MessagesID;
import dreamgame.data.ResponseCode;
import dreamgame.data.UserEntity;
import dreamgame.data.ZoneID;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.LoginResponse;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.room.Room;
import dreamgame.gameserver.framework.room.Zone;
import dreamgame.gameserver.framework.room.ZoneManager;
import dreamgame.gameserver.framework.room.fake.ZoneConfigManager;
import dreamgame.gameserver.framework.room.fake.ZoneConfig_FakeRoom;
import dreamgame.protocol.messages.CancelResponse;
import dreamgame.protocol.messages.KickOutResponse;
import dreamgame.protocol.messages.OutResponse;
import dreamgame.protocol.messages.RegisterResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;

public class SessionManager {

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(SessionManager.class);
    private final ConcurrentHashMap<String, ISession> mSessions;
    private final ConcurrentHashMap<Long, String> mUIDSessions;
    private final Vector<ISessionListener> mSessionListeners;
    private IdGenerator mIdGenerator;
    private int mSessionTimeout = -1;
    public boolean shutDown = false;

    private MessageFactory msgFactory;
    public MessageFactory getMsgFactory() {
	return msgFactory;
    }
    public void setMsgFactory(MessageFactory msgFactory) {
	this.msgFactory = msgFactory;
    }
    
    private ZoneManager zoneMgr;
    public void setZoneMgr(ZoneManager zoneMgr) {
	this.zoneMgr = zoneMgr;
    }
    
    
    
    //TODO: (statistic) config this for statistic
    private static int maxNoOfGames = 20;
    private static int noOfRooms = 4;
    private static int maxNoOfPlayersPerTable = 10;
    private static int[] gameToStatistic = new int[]{ZoneID.GLOBAL, ZoneID.TIENLEN, ZoneID.PHOM, ZoneID.XITO, ZoneID.POKER, ZoneID.BACAY, ZoneID.TIENLEN_MB, ZoneID.COTUONG, ZoneID.BAUCUA, ZoneID.MAUBINH};
    
    
    // time between statistic:
    private static int statisticTime = 5;		    // 5 minutes for normal period
    private static int statisticTimeLowPeriod = 10;	    // 10 minutes at low period
    private static int statisticTimeHugeDataTables = 30;    // 30 minutes for logging big statistic like: countMinbetByGame, countCapacityByGame
    
    // time between realtime statistic
    private static int statisticRealtimeTime = 15;	    // 10s (realtime statistic period)
    
    // time between 2 checkTimeout Task
    private static int checkTimeoutPeriod = 20;		    // 20s
    
    // period of scanning for not sent admin message
    private static int messageAdminPeriod = 5;		    // 5s   for each scanning message task
    private static int disconnectUserPeriod = 5;		    // 5s   for each disconnect user task
    
    // period for create a new batch of fake tables
    private static int fakeTableRefreshPeriod = 60;	// 60s
    private static int runTableRefreshTimes = 0;
//    private static boolean runTableRefresh = true;
    
    // needed variable
    private static int statisticTotalRuns = 0;
    private static ScheduledExecutorService fScheduler;
    private ScheduledFuture<?> timeoutScheduledFuture
	, statisticScheduledFuture
	, statisticRealtimeScheduledFuture
	, messageAdminFuture
	, disconnectUserFuture
	, fakeTableRefreshFuture
	;
    private static final int NUM_THREADS = 10;
    
    private void startAllTasks() {
	// scheduler
	fScheduler = Executors.newScheduledThreadPool(NUM_THREADS);
	
	// session will time out after a period of time
	if (this.mSessionTimeout > 0) {
            //CloseTimeoutSessionScheduler sScheduler = new CloseTimeoutSessionScheduler(this, null);
            System.out.println("Start thread CloseTimeoutSessionScheduler!");

            CloseTimeoutSessionTask timeoutTask = new CloseTimeoutSessionTask();
	    // Thread timeoutThread = new Thread(timeoutTask);
	    // timeoutThread.start();
	    timeoutScheduledFuture = fScheduler.scheduleWithFixedDelay(
		timeoutTask, 0, checkTimeoutPeriod, TimeUnit.SECONDS
	    );
	    
        }
	
	// scheduler for doing statistic for analysis
	System.out.println("Start schedule StatisticSession task!");
	StatisticSessionTask statisticTask = new StatisticSessionTask();
	statisticScheduledFuture = fScheduler.scheduleWithFixedDelay(
		statisticTask, statisticTime, statisticTime, TimeUnit.MINUTES
	);
	
	// scheduler for doing realtime statistic
	System.out.println("Start schedule StatisticRealtimeSessionTask realtime!");
	StatisticRealtimeSessionTask statisticRealtimeTask = new StatisticRealtimeSessionTask();
	statisticRealtimeScheduledFuture = fScheduler.scheduleWithFixedDelay(
		statisticRealtimeTask, statisticRealtimeTime, statisticRealtimeTime, TimeUnit.SECONDS
	);
	
	// scheduler for doing scan message Admin and send to user
	System.out.println("Start schedule MessageAdminTask !");
	MessageAdminTask messageAdminTask = new MessageAdminTask();
	messageAdminFuture = fScheduler.scheduleWithFixedDelay(
		messageAdminTask, messageAdminPeriod, messageAdminPeriod, TimeUnit.SECONDS
	);

	// scheduler for disconnect user in list
	System.out.println("Start schedule DisconnectUserTask !");
	DisconnectUserTask disconnectUserTask = new DisconnectUserTask();
	disconnectUserFuture = fScheduler.scheduleWithFixedDelay(
		disconnectUserTask, disconnectUserPeriod, disconnectUserPeriod, TimeUnit.SECONDS
	);
	
	// scheduler for refresh fake tables
	System.out.println("Start schedule FakeTablesRefreshTask !");
	FakeTablesRefreshTask fakeTableTask = new FakeTablesRefreshTask();
	fakeTableRefreshFuture = fScheduler.scheduleWithFixedDelay(
		fakeTableTask, 5, fakeTableRefreshPeriod, TimeUnit.SECONDS
	);
    }
    
    @SuppressWarnings("unchecked")
    public SessionManager(int aSessionTimeout) {
        this.mSessions = new ConcurrentHashMap();
        this.mUIDSessions = new ConcurrentHashMap();

	// scheduler for close timeout session
        this.mSessionTimeout = aSessionTimeout;
        System.out.println("mSessionTimeout : " + mSessionTimeout);
        
        this.mIdGenerator = new IdGenerator();
        this.mSessionListeners = new Vector();
	
	startAllTasks();
    }

    public void sessionCreated(ISession aSession) {
        ((AbstractSession) aSession).setManager(this);

        String nextId = this.mIdGenerator.generateId();
        ((AbstractSession) aSession).setID(nextId);

        Long nextUID = Long.valueOf(this.mIdGenerator.generateUID());
        aSession.setUID(nextUID);

        aSession.setTimeout(Integer.valueOf(this.mSessionTimeout));
    }

    public void addSession(String aId, ISession aSession) {
        if (aId.equals(aSession.getID())) {
            synchronized (this.mSessions) {
                this.mSessions.put(aId, aSession);

                this.mUIDSessions.put(aSession.getUID(), aId);
            }
        } else {
            ((AbstractSession) aSession).setID(aId);
        }
    }

    void addUIDSession(Long aUid, ISession aSession) {
        if (aUid.longValue() > 0L) {
            Long uid = aSession.getUID().longValue();
            synchronized (this.mSessions) {
                this.mUIDSessions.remove(Long.valueOf(uid));

                this.mUIDSessions.put(aUid, aSession.getID());
            }
        } else {
            synchronized (this.mSessions) {
                this.mUIDSessions.put(aUid, aSession.getID());
            }
        }
    }

    public ISession findSession(String aId) {
        synchronized (this.mSessions) {
            if (this.mSessions.containsKey(aId)) {
                return ((ISession) this.mSessions.get(aId));
            }

            return null;
        }
    }

    public void shutdown() {
        try {
            synchronized (this.mSessions) {
                for (ISession h : this.mSessions.values()) {
                    LoginResponse msg = (LoginResponse) h.getMessageFactory().getResponseMessage(MessagesID.LOGIN);
                    msg.disconnect = true;
                    msg.setFailure(ResponseCode.FAILURE, "Server hiện tại đang bảo trì bạn hãy đăng nhập lại trong vài phút nữa!.");
                    try {
                        h.write(msg);
                    } catch (ServerException ex) {
                        java.util.logging.Logger.getLogger(SessionManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            Thread.sleep(2000);
            /*
             * synchronized (this.mSessions) { for (ISession h :
             * this.mSessions.values()) { h.close(); } }
             */

            Thread.sleep(1000);
            //DatabaseDriver.server.stopServer();
            //DatabaseDriver.server.stop();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public long numMobileUser() {
        long num = 0;
        synchronized (this.mSessions) {
            for (ISession h : this.mSessions.values()) {
                if (h.getMobile()) {
                    num++;
                }
            }

        }
        return num;
    }

    public long numUser() {
        long num = 0;
        synchronized (this.mSessions) {
            num = mSessions.size();
        }
        return num;
    }

    public ISession findSession(Long aUid) {
        synchronized (this.mSessions) {
            String sessionId = (String) this.mUIDSessions.get(aUid);
            if (sessionId != null) {
//                return findSession(sessionId);
		if (this.mSessions.containsKey(sessionId)) {
		    return ((ISession) this.mSessions.get(sessionId));
		}
		return null;
            }
            return null;
        }
    }
    
    public Vector<UserEntity> findUsersOnline(Vector<UserEntity> users) {
	synchronized (this.mSessions) {
            Vector<UserEntity> res = new Vector<UserEntity>();
	    if (users == null) return res;
	    // find
	    ISession session;
            for (UserEntity user : users) {
                String sessionId = (String) this.mUIDSessions.get(user.mUid);
		if (sessionId != null && this.mSessions.containsKey(sessionId)) {
		    session = ((ISession) this.mSessions.get(sessionId));
		    if (session != null)
			res.add(session.getUserEntity());
		}
            }
	    return res;
        }
    }

    public ISession removeSession(String aId) {
        synchronized (this.mSessions) {
            ISession session = (ISession) this.mSessions.remove(aId);

            this.mUIDSessions.remove(session.getUID());
            return session;
        }
    }

    public ISession removeSession(ISession aSession) {
        synchronized (this.mSessions) {
            if (aSession != null) {
                this.mUIDSessions.remove(aSession.getUID());

                String id = aSession.getID();
                return ((ISession) this.mSessions.remove(id));
            }

            return null;
        }
    }

    public ISession sessionClosed(String aId) {
        synchronized (this.mSessions) {
            ISession session = (ISession) this.mSessions.remove(aId);

            if (session == null) {
                mLog.error("Fuck session omg! " + aId + "  ;  mUIDSessions : " + mUIDSessions.size());

                //Iterator itr = mUIDSessions.keys().;
//iterate through HashMap values iterator
                Enumeration t = mUIDSessions.keys();

                Object foundItem = null;
                while (t.hasMoreElements()) {
                    Object l = t.nextElement();
                    //System.out.println(l+" : "+mUIDSessions.get(l));
                    if (mUIDSessions.get(l).equalsIgnoreCase(aId)) {
                        foundItem = l;
                    }
                }
                if (foundItem != null) {
                    mUIDSessions.remove(foundItem, aId);
                }
                mLog.error("Fuck session omg After! " + aId + "  ;  mUIDSessions : " + mUIDSessions.size());
            } else {
                this.mUIDSessions.remove(session.getUID());
                notifySessionClosed(session);
            }

            return session;
        }
    }

    public void addSessionListener(ISessionListener aSessionListener) {
        synchronized (this.mSessionListeners) {
            this.mSessionListeners.add(aSessionListener);
        }
    }

    @SuppressWarnings("unchecked")
    private void notifySessionClosed(ISession aSession) {
        synchronized (this.mSessionListeners) {
            Iterator it = this.mSessionListeners.iterator();
            while (true) {
                if (!(it.hasNext())) {
                    break;
                }
                ISessionListener sessionListener = (ISessionListener) it.next();
                sessionListener.sessionClosed(aSession);
            }
        }
    }

    

    // trungnm
    public Vector<UserEntity> dumpFreeFriend(int aOffset, int aLength, long money, int currentZone) {
        Vector<UserEntity> userEntities = new Vector<UserEntity>();

        synchronized (this.mUIDSessions) {
            //int loopIdx = (aOffset >= 0) ? aOffset : 0;

            //int numRooms = this.mUIDSessions.size();
//System.out.println("zise = " + numRooms);
            Enumeration<Long> keys = this.mUIDSessions.keys();
            int results = 0;
            while (true) {
                if ((results >= aLength) || (!keys.hasMoreElements())) {
                    break;
                }
                long uid = keys.nextElement();
                //String userName = ((String)this.mUIDSessions.get(uid));
                //System.out.println("username = " + userName);
                //System.out.println("loop = " + uid);

                try {

                    ISession session = findSession(uid);
                    UserEntity user = session.getUserEntity();

                    Vector<Room> joinedRoom = session.getJoinedRooms();

                    boolean exist = false;
                    for (UserEntity u : userEntities) {
                        if (user.mUid == u.mUid) {
                            exist = true;
                        }
                    }

                    if (!exist) {
                        if ((joinedRoom.size() == 0) && (session.getCurrentZone() == currentZone)) {
			    if (user.money >= GameRuleConfig.getRequiredMoneyToJoin(session.getCurrentZone(), money)) {
				userEntities.add(user);
				System.out.println("Add user : " + user.mUid + "  ; " + userEntities.size());
				++results;
			    }
                        }
                    }
                } catch (Exception ex) {
                    //ex.printStackTrace();
                }
                //  ++loopIdx;
            }
        }

        return userEntities;
    }
    
    // Binhlt
    public Vector<UserEntity> dumpFreeFriend(int aOffset, int aLength, int aLevel, int currentZone) {
        Vector<UserEntity> userEntities = new Vector<UserEntity>();

        synchronized (this.mUIDSessions) {
            //int loopIdx = (aOffset >= 0) ? aOffset : 0;

            //int numRooms = this.mUIDSessions.size();
//System.out.println("zise = " + numRooms);
            Enumeration<Long> keys = this.mUIDSessions.keys();
            int results = 0;
            while (true) {
                if ((results >= aLength) || (!keys.hasMoreElements())) {
                    break;
                }
                long uid = keys.nextElement();
                //String userName = ((String)this.mUIDSessions.get(uid));
                //System.out.println("username = " + userName);
                //System.out.println("loop = " + uid);

                try {

                    ISession session = findSession(uid);
                    UserEntity user = session.getUserEntity();

                    Vector<Room> joinedRoom = session.getJoinedRooms();

                    boolean exist = false;
                    for (UserEntity u : userEntities) {
                        if (user.mUid == u.mUid) {
                            exist = true;
                        }
                    }

                    if (!exist) {
                        if ((joinedRoom.size() == 0) && (user.level >= aLevel) && (session.getCurrentZone() <= 0 || session.getCurrentZone() == currentZone)) {
                            userEntities.add(user);
                            System.out.println("Add user : " + user.mUid + "  ; " + userEntities.size());
                            ++results;
                        }
                    }


                } catch (Exception ex) {
                    //ex.printStackTrace();
                }
                //  ++loopIdx;
            }
        }

        return userEntities;
    }

    //Thomc
    public void sendAllNotification() {
        try {
            synchronized (this.mSessions) {
                for (ISession h : this.mSessions.values()) {
                    try {
                        h.sendNotification(DatabaseDriver.noticeText);
                    } catch (Exception ex) {
                        java.util.logging.Logger.getLogger(SessionManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            Thread.sleep(2000);
        } catch (Exception e) {
        }
    }
    
// Schedulers:
    
    @SuppressWarnings("unchecked")
    private class CloseTimeoutSessionTask
            implements Runnable {

        public void run() {
//            System.out.println("Run : CloseTimeoutSessionScheduler");

            Enumeration values;
	    Collection<ISession> sessions;
            try {
//                values = null;
//
//                synchronized (SessionManager.this.mSessions) {
//                    values = SessionManager.this.mSessions.elements();
//                }
//
//                System.out.println("values : " + (values == null));

//                while (true) {
                    if (shutDown || DatabaseDriver.stopServer) {
                        System.out.println("Shut down the server! Stop close thread session!");
                        DatabaseDriver.server.stopServer();

                        return;
                    }
		    synchronized (mSessions) {
			sessions = mSessions.values();
		    }

                    //System.out.println("values : "+(values==null)+" ; "+SessionManager.this.mSessions.values().size());
//                    if ((values != null) && (SessionManager.this.mSessions.values().size() > 0)) {

                        for (ISession session : sessions) {
                            if ((!(session.isClosed())) && (session.isExpired())) {
                                //SessionCloseThead sThread = new SessionCloseThead(session, session);
                                //sThread.start();
                                mLog.info("Close idle session : " + session.userInfo());
                                session.close();
                            }
			    
                            //close bacay time out
                            if (session.getCurrentZone() == ZoneID.BACAY) {
//                                mLog.debug("name = " + session.getUserName());
//                                mLog.debug("last time send ms = " + session.getLastMessage());
                                Zone zone = session.findZone(ZoneID.BACAY);
                                try {
                                    Room room = zone.findRoomPlayer(session.getUserName());
                                    if (room != null && System.currentTimeMillis() - session.getLastMessage() >= 1000 * 60 * 5) {
                                        BacayTable table = (BacayTable) room.getAttactmentData();
                                        if (table.getRoomOwner().id != session.getUID().longValue()) {
                                            MessageFactory msgFactory = session.getMessageFactory();
                                            OutResponse kickRes = (OutResponse) msgFactory.getResponseMessage(MessagesID.OUT);
                                            kickRes.setSuccess(1, session.getUID().longValue(), session.getUserName() + " bị chủ bàn đá ra ngoài", session.getUserName(), 1);
                                            kickRes.matchId = room.getRoomId();
//                                            session.write(kickRes);
                                            room.broadcastMessage(kickRes, session, true);
                                            room.left(session);
                                        }
//                                        session.close();
                                    }
                                } catch (Exception e) {
                                    continue;
                                }
                            }
//                        }
                    }
                    
//                }


            } catch (Throwable t) {
                //SessionManager.access$200(this.this$0).error("", t);
                SessionManager.this.mLog.error("", t);
            }
        }

        private class SessionCloseThead extends Thread {

            private final ISession mSession;

            public SessionCloseThead(ISession aSession, ISession paramISession) {
                this.mSession = aSession;
            }

            public void run() {
                synchronized (this.mSession) {
                    //SessionManager.access$200(this.this$1.this$0).debug("[SCHEDULER] Session " + this.mSession.getID() + " has been timeout, closed it!");
                    SessionManager.this.mLog.debug("[SCHEDULER] Session " + this.mSession.getID() + " has been timeout, closed it!");
                    this.mSession.close();
                }
            }
        }
    }
    
    // statistic
    private class StatisticSessionTask implements Runnable {

        public void run() {
	    System.out.println("[statistic] statisticTotalRuns = " + statisticTotalRuns);
	    statisticTotalRuns ++;	// keep track of no of times task runs
            try {
		    SimpleDateFormat sdf = new SimpleDateFormat("H");
		    int currentHour;
		    // 23h -> 10h: 10 phut 1 lan statistic
		    currentHour = Integer.parseInt(sdf.format(new Date()));
		    if (currentHour < 10 || currentHour > 22 ) {
			if ( ((statisticTotalRuns - 1)  * statisticTime) % statisticTimeLowPeriod != 0 ) {
			    return;
			}
//			timeBetweenStatistic = 10 * 60 * 1000;
//			timeBetweenStatistic = statisticTimeLow;
		    }
		System.out.println("Run : StatisticSession Task");
		SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		int total, totalIOS, totalAndroid, totalJ2ME, totalFlash;
		int totalFacebook, totalYahoo, totalGoogle, totalTheGioiBai;
		int[] countGame, capacityCountValues;
		int[][] countRoom, capacityByGameCountValues;
		HashMap map, cpCountValues, minBetCountValues, countIP;
		HashMap[] minBetByGameCountValues;
		Collection<ISession> sessions;
		
		    // sleep a period then do the logging
//		    Thread.sleep(timeBetweenStatistic);

		    System.out.println("[statistic] time = " + sd.format(new Date()));
		    try {
			// init
			map = new HashMap();

			total = totalIOS = totalAndroid = totalJ2ME = totalFlash = totalFacebook = totalYahoo = totalGoogle = totalTheGioiBai = 0;
			countGame = new int[maxNoOfGames];
			countRoom = new int[maxNoOfGames][noOfRooms + 1];

			// get all cp info:
			ArrayList<CPEntity> cps = DatabaseDriver.getAllCPInfo();
			cpCountValues = new HashMap();
			for (CPEntity cp : cps) {
			    cpCountValues.put(cp.cpID, 0L);
			}

			// minBet statistic init
			minBetCountValues = new HashMap();
			minBetByGameCountValues = new HashMap[maxNoOfGames];

			// get all minbet values
			HashSet<Long> bets = DatabaseDriver.getAllBetsValue();
			for (Long bet : bets) {
			    minBetCountValues.put(bet, 0L);
			}
			for (int i = 0; i < gameToStatistic.length; i++) {
			    minBetByGameCountValues[gameToStatistic[i]] = new HashMap();
			    for (Long bet : bets) {
				minBetByGameCountValues[gameToStatistic[i]].put(bet, 0L);
			    }
			}

			// capacity statistic init
			capacityCountValues = new int[maxNoOfPlayersPerTable + 1];
			capacityByGameCountValues = new int[maxNoOfGames][maxNoOfPlayersPerTable + 1];

			// IP statistic
			countIP = new HashMap();
			
			// get synchronized sessions
			synchronized (mSessions) {
			    sessions = mSessions.values();
			}

			long l;
			int in;
			Pattern pattern = Pattern.compile("\\d+\\.\\d+\\.\\d+\\.\\d+");
			Matcher matcher;
			
			// loop to get count
			for (ISession session : sessions) {
			    try {
				if ( session.isClosed() || ! session.isLoggedIn() ) {
				    continue;
				}
				total ++;
				countGame[session.getCurrentZone()] ++;
				countRoom[session.getCurrentZone()][session.getChannel()] ++;
				// device?
				if (session.isAndroid()) {
				    totalAndroid ++;
				} else if (session.isIphone()) {
				    totalIOS ++;
				} else if (session.getMobile()) {
				    totalJ2ME ++;
				} else {
				    totalFlash ++;
				}
				// cp?
				l = ((Long) cpCountValues.get(session.getCP())).longValue();
				l ++;
				cpCountValues.put(session.getCP(), l);
				// yahoo, facebook, google?
				String name = (session.getUserEntity().mUsername).toLowerCase();
				if (name.indexOf("@facebook") > -1) {
				    totalFacebook ++;
				}
				else if (name.indexOf("@yahoo") > -1) {
				    totalYahoo ++;
				}
				else if (name.indexOf("@gmail") > -1) {
				    totalGoogle ++;
				}
				else {
				    totalTheGioiBai ++;
				}

				//kieu phong
				Vector<Room> rooms = session.getJoinedRooms();
				for (Room room : rooms) {				
				    // minbet count
				    l = ((Long) minBetCountValues.get(room.minBet)).longValue();
				    l ++;
				    minBetCountValues.put(room.minBet, l);
				    // minbet count by games
				    l = ((Long) minBetByGameCountValues[session.getCurrentZone()].get(room.minBet)).longValue();
				    l ++;
				    minBetByGameCountValues[session.getCurrentZone()].put(room.minBet, l);
				    // capacity count
				    capacityCountValues[room.getAttactmentData().maximumPlayer] ++;
				    capacityByGameCountValues[session.getCurrentZone()][room.getAttactmentData().maximumPlayer] ++;
				}

				// countIP
				String IP = "";
				matcher = pattern.matcher(session.getIP());
				while (matcher.find()) {
				    IP = matcher.group();
				}
				if (countIP.containsKey(IP)) {
				    l = (Long) countIP.get(IP);
				    l++;
				    countIP.put(IP, l);
				}
				else {
				    l = 1L;
				    countIP.put(IP, l);
				}
			    } 
			    catch (Exception ex1) {
				ex1.printStackTrace();
				mLog.error("StatisticSessionTask - for each session", ex1);
				mLog.error("session: cp = " + session.getCP() + "; username = " + session.getUserName() + "; userID = " + session.getUID());
			    }
			}

			map.put("total", (long) total);
			map.put("totalIOS", (long) totalIOS);
			map.put("totalAndroid", (long) totalAndroid);
			map.put("totalJ2ME", (long) totalJ2ME);
			map.put("totalFlash", (long) totalFlash);
			map.put("totalFacebook", (long) totalFacebook);
			map.put("totalYahoo", (long) totalYahoo);
			map.put("totalGoogle", (long) totalGoogle);
			map.put("totalTheGioiBai", (long) totalTheGioiBai);
			map.put("countGame", countGame);
			map.put("countRoom", countRoom);
			map.put("countCP", cpCountValues);

			map.put("countMinBet", minBetCountValues);
			map.put("countCapacity", capacityCountValues);
			
			map.put("countIP", countIP);
			
			// doi 1 thoi gian moi log countMinBetByGame va countCapacityByGame 1 lan
			if ( ((statisticTotalRuns - 1)  * statisticTime) % statisticTimeHugeDataTables != 0 ) {
			    map.put("countMinBetByGame", minBetByGameCountValues);
			    map.put("countCapacityByGame", capacityByGameCountValues);
			} else {
			    map.put("countMinBetByGame", null);
			    map.put("countCapacityByGame", null);
			}

			// store it to db
			DatabaseDriver.logStatisticToDB(map, gameToStatistic, noOfRooms, maxNoOfPlayersPerTable);

			System.out.println("END statistic - time = " + sd.format(new Date()));
		    } 
		    catch (Exception e) {
			System.out.println("END statistic - exception");
			mLog.error("StatisticSessionTask", e);
		    }
//		} // end while(true)
            } catch (Throwable t) {
                //SessionManager.access$200(this.this$0).error("", t);
                SessionManager.this.mLog.error("", t);
            }
        }
    } // end StatisticSessionScheduler
    
    
    // statistic realtime
    private class StatisticRealtimeSessionTask implements Runnable {

        public void run() {
//            System.out.println("Run : StatisticRealtimeSessionTask realtime");

//            Enumeration values;
            try {
//                values = null;
//                synchronized (mSessions) {
//                    values = mSessions.elements();
//                }
//                System.out.println("values : " + (values == null));
		
		int total, totalIOS, totalAndroid, totalJ2ME, totalFlash;
		int totalFacebook, totalYahoo, totalGoogle, totalTheGioiBai;
		int[] countGame;
		HashMap map, cpCountValues;
		ArrayList<HashMap> userList;
		Collection<ISession> sessions;
		
		int timeBetweenStatistic = statisticRealtimeTime;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		// do it
//		while (true) {
		    // sleep a period then do the logging
//		    Thread.sleep(timeBetweenStatistic);
		    try {
			// init
			map = new HashMap();

			total = totalIOS = totalAndroid = totalJ2ME = totalFlash = totalFacebook = totalYahoo = totalGoogle = totalTheGioiBai = 0;
			countGame = new int[maxNoOfGames];

			// get all cp info:
			ArrayList<CPEntity> cps = DatabaseDriver.getAllCPInfo();
			cpCountValues = new HashMap();
			for (CPEntity cp : cps) {
			    cpCountValues.put(cp.cpID, 0L);
			}

			userList = new ArrayList<HashMap>();
			
			// get synchronized sessions
//			synchronized (mSessions) {
			    sessions = mSessions.values();
//			}

			long l;
			int in;
			
			Pattern pattern = Pattern.compile("\\d+\\.\\d+\\.\\d+\\.\\d+");
			Matcher matcher;
			
			// loop to get count
			for (ISession session : sessions) {
			    try {
				if ( session.isClosed() || ! session.isLoggedIn() ) {
				    continue;
				}
				total ++;
				countGame[session.getCurrentZone()] ++;
				// device?
				if (session.isAndroid()) {
				    totalAndroid ++;
				} else if (session.isIphone()) {
				    totalIOS ++;
				} else if (session.getMobile()) {
				    totalJ2ME ++;
				} else {
				    totalFlash ++;
				}

				// cp?
				l = ((Long) cpCountValues.get(session.getCP())).longValue();
				l ++;
				cpCountValues.put(session.getCP(), l);

				// yahoo, facebook, google?
				String name = (session.getUserEntity().mUsername).toLowerCase();
				if (name.indexOf("@facebook") > -1) {
				    totalFacebook ++;
				}
				else if (name.indexOf("@yahoo") > -1) {
				    totalYahoo ++;
				}
				else if (name.indexOf("@gmail") > -1) {
				    totalGoogle ++;
				}
				else {
				    totalTheGioiBai ++;
				}

				// userList
				HashMap user = new HashMap();
				user.put("username", session.getUserEntity().mUsername);
				user.put("userID", session.getUserEntity().mUid);
				user.put("cp", session.getCP());
				user.put("loginTime", sdf.format(session.getLoginTime()));
				String IP = "";
				matcher = pattern.matcher(session.getIP());
				while (matcher.find()) {
				    IP = matcher.group();
				}
				user.put("ip", IP);
				user.put("deviceType", session.getDeviceType());
				user.put("clientVersion", session.getMobileVer());
				user.put("device", session.getDevice());
				user.put("screenSize", session.getScreenSize());
				user.put("gameZoneID", (long) session.getCurrentZone());
				user.put("room", (long) session.getChannel());
				String roomName = "";
				try {				    
				    for (Room r : session.getJoinedRooms()) {
					if (r.getAttactmentData() != null) {
					    roomName = r.getAttactmentData().name;
					}
//					user.put("table", session.getJoinedRooms().get(0).getAttactmentData().name);
				    }
				    
				} catch (Exception e) {
//				    user.put("table", "");
				    e.printStackTrace();
				}
				roomName = roomName == null ? "" : roomName;
				user.put("table", roomName);
				userList.add(user);
			    }
			    catch (Exception ex1) {
				ex1.printStackTrace();
				mLog.error("StatisticRealtimeSessionTask - for each session", ex1);
				mLog.error("session: cp = " + session.getCP() + "; username = " + session.getUserName() + "; userID = " + session.getUID());
			    }
			}

			map.put("total", (long) total);
			map.put("totalIOS", (long) totalIOS);
			map.put("totalAndroid", (long) totalAndroid);
			map.put("totalJ2ME", (long) totalJ2ME);
			map.put("totalFlash", (long) totalFlash);
			map.put("totalFacebook", (long) totalFacebook);
			map.put("totalYahoo", (long) totalYahoo);
			map.put("totalGoogle", (long) totalGoogle);
			map.put("totalTheGioiBai", (long) totalTheGioiBai);
			map.put("countGame", countGame);
			map.put("countCP", cpCountValues);

			map.put("userList", userList);
			
			// store it to db
			DatabaseDriver.logStatisticRealtimeToDB(map, gameToStatistic, noOfRooms, maxNoOfPlayersPerTable);

//			System.out.println("END statistic - time = " + sd.format(new Date()));
		    } 
		    catch (Exception e) {
			System.out.println("END statistic realtime - exception");
			mLog.error("StatisticRealtimeSessionTask", e);
		    }
//		} // end while(true)
            } catch (Throwable t) {
                //SessionManager.access$200(this.this$0).error("", t);
                SessionManager.this.mLog.error("", t);
            }
        }
    } // end StatisticRealtimeSessionScheduler
    
    
    // scan for admin message and send them to users
    private class MessageAdminTask implements Runnable {

        public void run() {
            try {
		Collection<ISession> sessions;
		try {
		    ArrayList<HashMap> messageList = DatabaseDriver.getAllMessageAdmin();
		    
		    // no message -> exit
		    if (messageList.size() < 1)
			return;
		    
		    // get synchronized sessions
//		    synchronized (mSessions) {
			sessions = mSessions.values();
//		    }
		    
		    RegisterResponse resMsg = (RegisterResponse) msgFactory.getResponseMessage(MessagesID.REGISTER);
		    String[] temp;		    
		    long[] userIDReceiveList = null;
		    
		    // send every message to target receivers
		    for (HashMap message : messageList) {
			String messageStr = "[Thông báo] " + (String) message.get("message");
			resMsg.setFailure(ResponseCode.FAILURE, messageStr);
			String receiveList = (String) message.get("receiveList");
			userIDReceiveList = null;
			// blank -> exit
			if (receiveList.equals(""))
			    continue;
			// not all -> parse receive list
			if ( ! receiveList.equals("all")) {
			    temp = receiveList.split(",");
			    userIDReceiveList = new long[temp.length];
			    for (int i = 0; i < temp.length; i++) {
				try {
				    userIDReceiveList[i] = Long.parseLong(temp[i]);
				} catch (Exception e) {
				    System.out.println("messageAdminTask Exception - parse userIDReceiveList no " + i + ": " + temp[i]);
				}
			    }
			}
			// send to all
			if (userIDReceiveList == null) {
			    // loop to send message
			    for (ISession session : sessions) {
				try {
				    if ( ! session.isClosed() || ! session.isLoggedIn()) {
					session.write(resMsg);
				    }
				} catch (Exception ex1) {
				    ex1.printStackTrace();
				    mLog.error("messageAdminTask - for each session write message ", ex1);
				    mLog.error("session: cp = " + session.getCP() + "; username = " + session.getUserName() + "; userID = " + session.getUID());
				}
			    }
			}
			// send to specific users
			else {
			    // loop to send message
			    int sentMessage = 0;
			    for (ISession session : sessions) {
				if ( session.isClosed() || ! session.isLoggedIn()) {
				    continue;
				}
				boolean isFound = false;
				for (int i = 0; i < userIDReceiveList.length; i++) {
				    if (session.getUserEntity().mUid == userIDReceiveList[i]) {
					isFound = true;
					break;
				    }
				}
				if (isFound) {
				    session.write(resMsg);
				    sentMessage ++;
				    if (sentMessage == userIDReceiveList.length) {
					break;
				    }
				}
			    }
			}
			
			// update db
			DatabaseDriver.updateDBAfterMessageSent(message);
		    }
		}
		catch (Exception e) {
		    System.out.println("END messageAdmin - exception");
		    mLog.error("messageAdminTask", e);
		}
            } catch (Throwable t) {
                SessionManager.this.mLog.error("", t);
            }
        }
    } // end messageAdminTask
    
    // disconnect user list
    private class DisconnectUserTask implements Runnable {

        public void run() {
            try {
		Collection<ISession> sessions;
		try {
		    ArrayList<HashMap> disconnectList = DatabaseDriver.getDisconnectList();
		    
		    // no message -> exit
		    if (disconnectList.size() < 1)
			return;
		    
		    // get synchronized sessions
//		    synchronized (mSessions) {
			sessions = mSessions.values();
//		    }
		    
		    String[] temp;		    
		    long[] userIDReceiveList = null;
		    
		    // disconnect some users or all
		    for (HashMap dis : disconnectList) {
			String kickList = (String) dis.get("kickList");
			userIDReceiveList = null;
			// blank -> exit
			if (kickList.equals(""))
			    continue;
			// not all -> parse kick list
			if ( ! kickList.equals("all")) {
			    temp = kickList.split(",");
			    userIDReceiveList = new long[temp.length];
			    for (int i = 0; i < temp.length; i++) {
				try {
				    userIDReceiveList[i] = Long.parseLong(temp[i]);
				} catch (Exception e) {
				    System.out.println("DisconnectUserTask Exception - parse userIDReceiveList no " + i + ": " + temp[i]);
				}
			    }
			}
			// disconnect all
			if (userIDReceiveList == null) {
			    // loop to send message
			    for (ISession session : sessions) {
				try {
				    if ( ! session.isClosed() ) {
					session.close();
				    }
				} catch (Exception ex1) {
				    ex1.printStackTrace();
				    mLog.error("DisconnectUserTask - for each session close ", ex1);
				    mLog.error("session: cp = " + session.getCP() + "; username = " + session.getUserName() + "; userID = " + session.getUID());
				}
			    }
			}
			// disconnect to specific users
			else {
			    // loop to disconnect
			    int disconnectNo = 0;
			    for (ISession session : sessions) {
				if ( session.isClosed() ) {
				    continue;
				}
				boolean isFound = false;
				for (int i = 0; i < userIDReceiveList.length; i++) {
				    if (session.getUserEntity().mUid == userIDReceiveList[i]) {
					isFound = true;
					break;
				    }
				}
				if (isFound) {
				    session.close();
				    disconnectNo ++;
				    if (disconnectNo == userIDReceiveList.length) {
					break;
				    }
				}
			    }
			}
			
			// update db
			DatabaseDriver.updateDBAfterDisconnectUser(dis);
		    }
		}
		catch (Exception e) {
		    System.out.println("END disconnectUser - exception");
		    mLog.error("DisconnectUserTask", e);
		}
            } catch (Throwable t) {
                SessionManager.this.mLog.error("", t);
            }
        }
    } // end DisconnectUserTask
    
    // fake tables refresher
    private class FakeTablesRefreshTask implements Runnable {

	private int generateRandomNumber(int aStart, int aEnd) {
	    if ( aStart > aEnd ) {
		throw new IllegalArgumentException("Start cannot exceed End.");
	    }
	    return aStart + (int)(Math.random() * ((aEnd - aStart) + 1));
	}
	
        public void run() {
            try {
		
		// load config every 20 times of running
		if (runTableRefreshTimes % 20 == 0)
		    ZoneConfigManager.init();
		++ runTableRefreshTimes;	    // keep track of how many times the task has been runned
		    
		try {
		    // fake room configs
//		    ZoneConfigManager zcm = new ZoneConfigManager();
		    
		    ConcurrentHashMap<Integer, ArrayList<Long>> fakeRoomMapByZone;
		    ConcurrentHashMap<Long, String> existedOwnerNameMap;
		    
		    synchronized (ZoneConfigManager.fakeRoomMapByZone) {
			fakeRoomMapByZone = ZoneConfigManager.fakeRoomMapByZone;
			existedOwnerNameMap = ZoneConfigManager.fakeRoomExistedOwnerNameMap;
		    }
		    
//		    synchronized (ZoneConfigManager.fakeRoomExistedOwnerNameMap) {
//			existedOwnerNameMap = ZoneConfigManager.fakeRoomExistedOwnerNameMap;
//		    }
		    
		    ArrayList<Long> fakeRoomIdList, fakeRoomIdListCopy;
		    Integer zoneId;
		    
		    // xu ly cho tung zone
		    for (ZoneConfig_FakeRoom zc : ZoneConfigManager.zoneConfigs ) {
			try {
//			    System.out.println("zoneconfig: " + zc.zoneId + "; " + zc.zoneName + "; " + zc.minNoOfFakeRooms + "; " + zc.maxNoOfFakeRooms);
			    
			    zoneId = new Integer(zc.zoneId);
			    Zone zone = zoneMgr.findZone(zoneId);
			    
			    if (zone == null) continue;
			    
			    // maxOfFakeRooms < 1 -> no fake room
			    if (zc.maxNoOfFakeRooms < 1 || zc.minNoOfFakeRooms > zc.maxNoOfFakeRooms) {
				// neu da co fake room tu truoc -> remove
				if (fakeRoomMapByZone.containsKey(zoneId)) {
				    fakeRoomIdList = fakeRoomMapByZone.get(zoneId);
				    if (fakeRoomIdList != null && fakeRoomIdList.size() > 0) {
					for (long roomId : fakeRoomIdList) {
					    zone.deleteFakeRoom(roomId);
					    existedOwnerNameMap.remove(roomId);
					}
				    }
				    fakeRoomMapByZone.remove(zoneId);
				    synchronized (ZoneConfigManager.fakeRoomMapByZone) {
					ZoneConfigManager.fakeRoomMapByZone = fakeRoomMapByZone;
					ZoneConfigManager.fakeRoomExistedOwnerNameMap = existedOwnerNameMap;
				    }
				}
				// duyet zone tiep theo
				continue;
			    }
			    
			    // lan nay xem de bao nhieu room nao
			    int noOfFakeRoomTotal = generateRandomNumber(zc.minNoOfFakeRooms, zc.maxNoOfFakeRooms);
			    
			    // suitable number based on current time, eg 0-9 h thi chi 1/3 so ban
			    noOfFakeRoomTotal = ZoneConfigManager.noOfFakeUserOrTablesBasedOnCurrentTime(noOfFakeRoomTotal);
			    
			    int noOfFakeRoom = 0;
			    // da ton tai danh sach ban fake trong zone do tu truoc thi xu ly chung
			    if ( fakeRoomMapByZone.containsKey(zoneId) ) {
				fakeRoomIdList = fakeRoomMapByZone.get(zoneId);
				fakeRoomIdListCopy = new ArrayList<Long>(fakeRoomIdList);
				if (fakeRoomIdList != null && fakeRoomIdList.size() > 0) {
				    for (long roomId : fakeRoomIdListCopy) {
					// đã đủ số fake rooms -> xóa bỏ room đó nào
					if (noOfFakeRoom == noOfFakeRoomTotal) {
					    zone.deleteFakeRoom(roomId);
					    fakeRoomIdList.remove(roomId);
					    existedOwnerNameMap.remove(roomId);
					    continue;
					}
					// chưa đủ thì xử lý tung đồng xu, giữ lại hoặc không giữ lại
					else {
					    int chance = generateRandomNumber(2, 4);
					    boolean isKeep = (chance % 2) == 0;
					    // giu lai nao
					    if (isKeep) {
						// tung đồng xu xem để chế độ bàn là đang chơi hay đang chờ
						chance = generateRandomNumber(2, 4);
						boolean isPlaying = (chance % 2) == 0;
						zone.changeFakeRoomPlayingStatus(roomId, isPlaying);
						++ noOfFakeRoom;
					    }
					    // khong giu lai
					    else {
						zone.deleteFakeRoom(roomId);
						fakeRoomIdList.remove(roomId);
						existedOwnerNameMap.remove(roomId);
						continue;
					    }
					}
				    }
				}
			    }
			    else {
				fakeRoomIdList = new ArrayList<Long>();
			    }
			    
			    // chua du so room fake thi them so room fake tiep theo cho du thi thoi
			    long roomId, money;
			    int chance, numberOfPlayers, roomPosition, tempi;
			    String roomName, ownerName;
			    boolean isPlaying;
			    ArrayList<Integer> availableRoomPositions = zone.findAvailablePositionForFakeRoom();
			    while (noOfFakeRoom < noOfFakeRoomTotal && availableRoomPositions.size() > 0) {
//				roomId = new Long(System.currentTimeMillis() + generateRandomNumber(1, 1000));
				roomId = new Long(generateRandomNumber(162148700, 262148700));
				ownerName = ZoneConfigManager.generateAvailableOwnerName();
				roomName = "p_" + ownerName;
				
				if (zoneId.intValue() == ZoneID.COTUONG) {
				    numberOfPlayers = 2;
				}
				else 
				    numberOfPlayers = generateRandomNumber(2, 4);
				
				money = ZoneConfigManager.generateRandomMoneyForTable(zoneId.intValue());
				    chance = generateRandomNumber(1, 4);
				isPlaying = (chance % 2) == 0;
				
				tempi = availableRoomPositions.size() < 10 ? generateRandomNumber(0, availableRoomPositions.size()) : generateRandomNumber(0, 10);
				roomPosition = availableRoomPositions.get(tempi).intValue();
				availableRoomPositions.remove(tempi);
				
				Room r = zone.createFakeRoom(zoneId.intValue(), roomId, roomName, ownerName, numberOfPlayers, money, isPlaying, roomPosition);
				zone.addFakeRoom(r);
				fakeRoomIdList.add(roomId);
				existedOwnerNameMap.put(roomId, ownerName);
				
				++ noOfFakeRoom;
			    }
			    
			    fakeRoomMapByZone.put(zoneId, fakeRoomIdList);
			}
			catch (Exception ex) {
			    ex.printStackTrace();
			    mLog.error("FakeTablesRefreshTask - loop each ZoneConfig_FakeRoom: ", ex);
			}
			
		    } // end for each zone processing
		    
		    ZoneConfigManager.fakeRoomMapByZone = fakeRoomMapByZone;
		    ZoneConfigManager.fakeRoomExistedOwnerNameMap = existedOwnerNameMap;
		}
		catch (Exception e) {
		    System.out.println("END fakeTableTask - exception");
		    mLog.error("FakeTablesRefreshTask", e);
		}
            } catch (Throwable t) {
                SessionManager.this.mLog.error("", t);
            }
        }
    } // end FakeTablesRefreshTask
    
} // end class SessionManager
