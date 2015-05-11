package test.performant;

import org.json.JSONObject;
import org.slf4j.Logger;


import dreamgame.gameserver.framework.common.LoggerContext;

public class TestPerforment {

    public static void main(String[] args) {

	for (int i = 0; i < 1000; i++) {
	    String str = "test_" + i;
	    Worker worker = new Worker(str, str);
	    worker.start();
	}
    }
}

class Worker extends Thread {

    private boolean isStop = false;
    private String username;
    private String password;
    private Timer timer;
    private Logger log = LoggerContext.getLoggerFactory().getLogger(Worker.class);
    ;
	public SocketServices mSocket = null;

    public Worker(String name, String p) {
	this.username = name;
	this.password = p;
	timer = new Timer(10);


	this.connect();
	this.log.debug(username + " : Init");
    }

    private void connect() {
	this.log.debug(username + " : Connect");
	mSocket = new SocketServices("127.0.0.1", 147);
	// Do connect
    }

    private void login() {
	this.log.debug(username + " : Login");
	try {
	    if (!mSocket.isConnected()) {
		mSocket.connect();
	    }
	    JSONObject login = Login(username, password);
	    mSocket.addRequestMessage(login);
	} catch (Throwable t) {
	    t.printStackTrace();
	}

    }

    private JSONObject Login(String username, String password) {
	JSONObject login = new JSONObject();
	try {
	    login.put("mid", MessagesID.LOGIN);
	    login.put("username", username);
	    login.put("password", password);
	    return login;
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    private void getWaitingRoom() {
	this.log.debug(username + " : Get waiting room");
	try {
	    JSONObject get = getWaitingRoom(15, 10, 0, 0);
	    mSocket.addRequestMessage(get);
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    public JSONObject getWaitingRoom(int level, int length, int offset, int minLevel) {
	try {
	    JSONObject getWRoom = new JSONObject();
	    getWRoom.put("mid", MessagesID.GET_WAITING_LIST);
	    getWRoom.put("level", level);
	    getWRoom.put("offset", offset);
	    getWRoom.put("length", length);
	    getWRoom.put("minLevel", minLevel);
	    return getWRoom;
	} catch (Throwable t) {
	    t.printStackTrace();
	    return null;
	}
    }

    private void cancel() {
	this.log.debug(username + " : Cancel");
	try {
	    JSONObject cancel = CancelMatch(0, 0); // Không quan trọng - chỉ cần xem server có bắt dc hay không
	    mSocket.addRequestMessage(cancel);
	} catch (Exception e) {
	    e.printStackTrace();
	}

	setStop(true); // Stop thread
    }

    public JSONObject CancelMatch(long uid, long matchID) {
	try {
	    JSONObject returnMatch = new JSONObject();
	    returnMatch.put("mid", MessagesID.MATCH_CANCEL);
	    returnMatch.put("uid", uid);
	    returnMatch.put("match_id", matchID);
	    return returnMatch;
	} catch (Throwable t) {
	    t.printStackTrace();
	    return null;
	}
    }

    private void setStop(boolean stop) {
	this.isStop = stop;
    }

    @Override
    public void run() {
	this.log.debug(username + " : Start");
	this.timer.start();
	this.connect();
	this.login();
	while (!isStop) {
	    try {
		wait();
	    } catch (Exception e) {
	    }
	}
    }

    class Timer extends Thread {

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
	private int time = 0; // refer to function which is activated

	public Timer(int timeout) {//Second
	    // Assign to member variable
	    m_length = timeout * 1000;

	    // Set time elapsed
	    m_elapsed = 0;
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
	public void run() {
	    // Keep looping
	    for (;;) {
		// Put the timer to sleep
		try {
		    Thread.sleep(m_rate);
		} catch (InterruptedException ioe) {
		    continue;
		}

		// Use 'synchronized' to prevent conflicts
		synchronized (this) {
		    // Increment time remaining
		    m_elapsed += m_rate;

		    // Check to see if the time has been exceeded
		    if (m_elapsed > m_length) {
			// Trigger a timeout
			timeout();
		    }
		}

	    }
	}

	public void timeout() {
	    time++;
	    if (time == 1) {
		Worker.this.getWaitingRoom();
	    } else if (time == 2) {
		Worker.this.cancel();

	    } else {
		// Do nothing
	    }

	}
    }
}