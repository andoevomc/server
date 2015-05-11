package dreamgame.gameserver.framework.workflow;

import java.net.InetAddress;
import java.sql.DriverManager;
import java.util.Calendar;

import org.slf4j.Logger;

import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.gameserver.framework.bytebuffer.IByteBuffer;
//import com.migame.gameserver.framework.common.ILoggerFactory;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
//import com.migame.gameserver.framework.db.DatabaseManager;
//import com.migame.gameserver.framework.db.DatabaseModel;
//import com.migame.gameserver.framework.db.IConnection;
//import com.migame.gameserver.framework.db.IConnectionManagerFactory;
import dreamgame.gameserver.framework.protocol.BusinessProperties;
import dreamgame.gameserver.framework.protocol.IBusiness;
import dreamgame.gameserver.framework.protocol.IBusinessPropertiesFactory;
import dreamgame.gameserver.framework.protocol.IPackageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestPackage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.protocol.SimpleRequestPackage;
import dreamgame.gameserver.framework.protocol.SimpleResponsePackage;
import dreamgame.gameserver.framework.protocol.messages.ExpiredSessionResponse;
import dreamgame.gameserver.framework.room.ZoneManager;
import dreamgame.gameserver.framework.scheduler.Schedulers;
import dreamgame.gameserver.framework.servers.IServer;
import dreamgame.gameserver.framework.servers.nettysocket.NettySocketServer;
import dreamgame.gameserver.framework.session.ISession;
import dreamgame.gameserver.framework.session.ISessionFactory;
import dreamgame.gameserver.framework.session.SessionManager;

public class SimpleWorkflow implements IWorkflow {

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(SimpleWorkflow.class);
    private ISessionFactory mSessionFactory;
    private IBusinessPropertiesFactory mBusinessPropertiesFactory;
    private ZoneManager mZoneMgr;
    private SessionManager mSessionMgr;
    private MessageFactory mMsgFactory;
    private Schedulers mScheduler;
    private WorkflowConfig mWorkflowConfig;

    public SimpleWorkflow() throws ServerException {
        try {
            this.mWorkflowConfig = new WorkflowConfig();
            this.mWorkflowConfig.getClass();
            this.mLog.info("[WF] 1. Load workflow's config from source " + "conf/workflow-config.xml");

            // if (true) return;

            this.mZoneMgr = new ZoneManager();
            this.mLog.info("[WF] 2. Load zones...");

            this.mMsgFactory = new MessageFactory();

            this.mLog.info("[WF] 3. Initial game's messages1241241");
            this.mMsgFactory.initModeling();

            String businessPropertiesFactoryName = this.mWorkflowConfig.getBusinessPropertiesFactory();
            this.mBusinessPropertiesFactory = ((IBusinessPropertiesFactory) Class
                    .forName(businessPropertiesFactoryName).newInstance());
            this.mLog.info("[WF] 4. Generate Business's Properties from 23523523"
                    + this.mBusinessPropertiesFactory.getClass().getName());

            boolean enableScheduler = this.mWorkflowConfig.enableScheduler();
            if (enableScheduler) {
                this.mScheduler = new Schedulers(this.mWorkflowConfig);
                this.mScheduler.start();
                this.mLog.info("[WF] 7. Enabled Schedulering");
            } else {
                this.mLog.info("[WF] 7. Disabled Schedulering");
            }

        } catch (Throwable t) {
            throw new ServerException(t);
        }
    }

    public void startDB() throws Exception {
        DatabaseDriver.db_username = mWorkflowConfig.getDBAccount();
        DatabaseDriver.url = mWorkflowConfig.getDBUrl();

        DatabaseDriver.db_password = mWorkflowConfig.getDBPassword();
        DatabaseDriver.log_all_code = mWorkflowConfig.logAllCode();

        try {
            DatabaseDriver.choiDemLa = mWorkflowConfig.getBoolean("workflow.server.choiDemLa");
            System.out.println("choiDemLa : " + DatabaseDriver.choiDemLa);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Connect to db : " + DatabaseDriver.url);
        DatabaseDriver.conn = DriverManager.getConnection(DatabaseDriver.url, DatabaseDriver.db_username,
                DatabaseDriver.db_password);

        try {
            DatabaseDriver.update_download = mWorkflowConfig.getUpdateDownload();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (DatabaseDriver.update_download) {
            DatabaseDriver.url_download = mWorkflowConfig.getDownloadUrl();
            DatabaseDriver.conn_download = DriverManager.getConnection(DatabaseDriver.url_download,
                    DatabaseDriver.db_username, DatabaseDriver.db_password);
            System.out.println("Connected to download Database!");
            System.out.println("Connected to url : " + DatabaseDriver.url_download);
        }

        DatabaseDriver.chargeRegister = mWorkflowConfig.chargeRegister();
        DatabaseDriver.doubleMD5 = mWorkflowConfig.doubleMD5();

        mLog.info("Init DB successful!");
        mLog.info("DB account : " + DatabaseDriver.db_username);
        mLog.info("DB url : " + DatabaseDriver.url);

        DatabaseDriver.clearResetServer();
        mLog.info("DB clear data OK! ");

        DatabaseDriver.getPreConfig();
        mLog.info("DB check config OK! ");

        // http://mystore.com.vn/game3cay_test/web/epaymobile.php?u=68019&p=1&n=123456789&s=21432534346346
        // DatabaseDriver.requestCharge(68019, "123456789", "21432534346346",
        // 1);
    }

    public void start() throws ServerException {
        String serverName;
        try {

            serverName = this.mWorkflowConfig.getServerName();
            IServer server = (IServer) Class.forName(serverName).newInstance();

            server.setWorkflow(this);
            this.mSessionFactory = server.getSessionFactory();

            int serverPort = this.mWorkflowConfig.getServerPort();
            server.setServerPort(serverPort);

            int connectTimeout = this.mWorkflowConfig.getServerConnectTimeout();
            server.setConnectTimeout(connectTimeout);

            int receiveBufferSize = this.mWorkflowConfig.getServerReceiveBufferSize();
            server.setReceiveBufferSize(receiveBufferSize);

            boolean reuseAddress = this.mWorkflowConfig.getReuseAddress();
            server.setReuseAddress(reuseAddress);

            boolean tcpNoDelay = this.mWorkflowConfig.getTcpNoDelay();
            server.setTcpNoDelay(tcpNoDelay);

            startDB();
            server.start();
            DatabaseDriver.server = (NettySocketServer) server;

            int sessionTimeout = this.mWorkflowConfig.getSessionTimeout();
            this.mSessionMgr = new SessionManager(sessionTimeout);
            this.mSessionMgr.setMsgFactory(mMsgFactory);
            this.mSessionMgr.setZoneMgr(mZoneMgr);
            this.mSessionMgr.addSessionListener(this.mZoneMgr);
            this.mLog.info("[WF] 6. Create session manager with sessiontimeout = " + sessionTimeout);

            this.mLog.info("[WF] 7. end. Server started with name = " + serverName);
            try {
                this.mLog.info("IP = " + InetAddress.getLocalHost());
            } catch (Exception e) {
            }
            this.mLog.info("Port = " + serverPort);
            this.mLog.info("ConnectTimeout (ms) = " + connectTimeout);
            this.mLog.info("ReceiveBufferSize (bytes) = " + receiveBufferSize);
            this.mLog.info("ReuseAddress = " + reuseAddress);
            this.mLog.info("TcpNoDelay = " + tcpNoDelay);

            DatabaseDriver.sm = this.mSessionMgr;
            // DatabaseDriver.stopServer=true;
            // DatabaseDriver.server.stopServer();

        } catch (Throwable t) {
            System.out.println("Fail to start Server!");
            t.printStackTrace();
            throw new ServerException(t);
        }
    }

    public WorkflowConfig getWorkflowConfig() {
        return this.mWorkflowConfig;
    }

    public IByteBuffer process(ISession aSession, IByteBuffer aRequest) throws ServerException {
        synchronized (aSession) {
            System.out.println("process : " + aRequest.toString());

            byte[] bb = aRequest.array();
            for (int i = 0; i < bb.length && i < 10; i++)
                System.out.print(bb[i] + " : ");
            System.out.println("-------------------  bb.size : " + bb.length);

            if (SimpleRequestPackage.canDecode(aSession, aRequest)) {
                aSession.setIsHandling(Boolean.TRUE);

                aSession.setLastAccessTime(Calendar.getInstance().getTime());

                System.out.println("Can decode !");

                String pkgFormat = aRequest.getString().toLowerCase();

                aSession.setPackageFormat(pkgFormat);

                IRequestPackage requestPkg = decode(aSession, aRequest);

                filterIn(aSession, requestPkg);

                IResponsePackage responsePkg = new SimpleResponsePackage();

                handleRequest(aSession, requestPkg, responsePkg);

                filterOut(aSession, responsePkg);

                responsePkg.prepareEncode(aSession);

                IByteBuffer result = encode(aSession, responsePkg);

                return result;
            }

            return null;
        }
    }

    protected IRequestPackage decode(ISession aSession, IByteBuffer aRequest) throws ServerException {
        String pkgFormat = aSession.getPackageFormat();

        IPackageProtocol pkgProtocol = this.mMsgFactory.getPackageProtocol(pkgFormat);
        IRequestPackage requestPkg = pkgProtocol.decode(aSession, aRequest);

        // PackageHeader pkgHeader = requestPkg.getRequestHeader();
        //
        // String sessionId = pkgHeader.getSessionID();
        // if ((sessionId != null) && (!(sessionId.trim().equals("")))) {
        // this.mSessionMgr.addSession(pkgHeader.getSessionID(), aSession);
        // }

        return requestPkg;
    }

    protected void filterIn(ISession aSession, IRequestPackage aRequestPkg) {
        if (aSession.isClosed()) {
            return;
        }
    }

    @SuppressWarnings("unused")
    protected void handleRequest(ISession aSession, IRequestPackage aRequestPkg, IResponsePackage aResponsePkg) {
        if (aSession.isClosed()) {
            return;
        }
        try {
            while (true) {
                if (!(aRequestPkg.hasNext())) {
                    break;
                }

                IRequestMessage reqMsg = aRequestPkg.next();

                if ((reqMsg != null) && (!(reqMsg.isNeedLoggedIn()) || aSession.isLoggedIn())) {
                    long timeStart = 0L;
                    long timeEnd = 0L;
                    timeStart = System.currentTimeMillis();

                    int msgId = reqMsg.getID();

                    IBusiness business = this.mMsgFactory.getBusiness(msgId);
                    try {
                        int result = business.handleMessage(aSession, reqMsg, aResponsePkg);
                        timeEnd = System.currentTimeMillis();
                        if (timeEnd - timeStart > 200L) {
                            this.mLog.warn("LONG TIME REQUEST " + msgId + ": " + (timeEnd - timeStart));
                        }
                    } catch (ServerException se) {
                        this.mLog.error("[WF] process message " + msgId + " error.", se);
                    } finally {
                    }
                } else if (reqMsg != null) {
                    String sessionId = aSession.getID();
                    this.mSessionMgr.removeSession(sessionId);
                    this.mLog.debug("Fake message " + reqMsg.getID() + ", sessionid = " + sessionId);
                    ExpiredSessionResponse expiredSession = (ExpiredSessionResponse) this.mMsgFactory
                            .getResponseMessage(9999);
                    expiredSession.mErrorMsg = "Your connection was expired. Please try to login again to use.";
                    aResponsePkg.addMessage(expiredSession);
                    break;
                }
            }
        } catch (Throwable t) {
            label448: this.mLog.error("Unexpected error on handlePackage() method!", t);
        } finally {
            // aSession.setCurrentDBConnection(null);
        }
    }

    protected void filterOut(ISession aSession, IResponsePackage aResponsePkg) {
        if (aSession.isClosed()) {
            return;
        }
    }

    protected IByteBuffer encode(ISession aSession, IResponsePackage aResponsePkg) throws ServerException {
        if (aSession.isClosed()) {
            return null;
        }

        String pkgFormat = aSession.getPackageFormat();

        IPackageProtocol pkgProtocol = this.mMsgFactory.getPackageProtocol(pkgFormat);

        IByteBuffer result = pkgProtocol.encode(aSession, aResponsePkg);
        return result;
    }

    public ISession sessionCreated(Object aAttachmentObj) throws ServerException {
        ISession session = this.mSessionFactory.createSession();

        session.setCreatedTime(Calendar.getInstance().getTime());

        this.mSessionMgr.sessionCreated(session);

        session.sessionCreated(aAttachmentObj);

        BusinessProperties businessProps = createBusinessProperties();
        session.setBusinessProperties(businessProps);

        session.setZoneManager(this.mZoneMgr);

        session.setMessageFactory(this.mMsgFactory);
        return session;
    }

    public BusinessProperties createBusinessProperties() {
        return this.mBusinessPropertiesFactory.createBusinessProperties();
    }

    public void serverStarted() {
        this.mLog.debug("[WF] Server Started");
    }

    public void serverStoppted() {
        /*
         * if (this.mScheduler != null) { this.mScheduler.stop(); }
         * 
         * if (this.mWorkflowConfig.enableDB()) { String dbModelName =
         * this.mWorkflowConfig.getDBModelName();
         * DatabaseManager.destroy(dbModelName); }
         */
        this.mLog.debug("[WF] Server Stoppted");
    }

    public ZoneManager getZoneManager() {
        return this.mZoneMgr;
    }

    public static void main(String[] args) throws ServerException {
        SimpleWorkflow wf = new SimpleWorkflow();
        wf.start();
    }
}
