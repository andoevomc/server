package dreamgame.gameserver.framework.workflow;

import dreamgame.gameserver.framework.common.ConfigProperties;
import dreamgame.gameserver.framework.common.ServerException;

public class WorkflowConfig {

    private final ConfigProperties mConfig;
    final String DEFAULT_WORKFLOW_CONFIG = "conf/workflow-config.xml";
    @SuppressWarnings("unused")
	private final String WF_APPNAME = "workflow.appname";
    @SuppressWarnings("unused")
    private final String WF_DB_ENABLE = "workflow.db.enable";
    @SuppressWarnings("unused")
    private final String WF_DB_FACTORY_NAME = "workflow.db.factory-name";
    @SuppressWarnings("unused")
    private final String WF_DB_MODELNAME = "workflow.db.modelname";
    @SuppressWarnings("unused")
    private final String WF_BUSINESS_PROPERTIES_FACTORY = "workflow.business.properties.factory";
    @SuppressWarnings("unused")
    private final String WF_SERVER_NAME = "workflow.server.name";
    @SuppressWarnings("unused")
    private final String WF_SERVER_PORT = "workflow.server.port";
    @SuppressWarnings("unused")
    private final String WF_SERVER_CONNECTTIMEOUT = "workflow.server.connecttimeout";
    @SuppressWarnings("unused")
    private final String WF_SERVER_SESSIONTIMEOUT = "workflow.server.sessiontimeout";
    @SuppressWarnings("unused")
    private final String WF_SERVER_RECEIVEBUFFERSIZE = "workflow.server.receivebuffersize";
    @SuppressWarnings("unused")
    private final String WF_SERVER_REUSEADDRESS = "workflow.server.reuseaddress";
    @SuppressWarnings("unused")
    private final String WF_SERVER_TCPNODELAY = "workflow.server.tcpnodelay";
    @SuppressWarnings("unused")
    private final String WF_SCHEDULER_ENABLE = "workflow.scheduler.enable";

    WorkflowConfig()
            throws ServerException {
        this.mConfig = new ConfigProperties();
        System.out.println(System.getProperty("user.dir"));
        this.mConfig.load(DEFAULT_WORKFLOW_CONFIG);
    }

    public String appName() {
        return this.mConfig.getString("workflow.appname");
    }

    public boolean enableDB() {
        return this.mConfig.getBoolean("workflow.db.enable");
    }

    public String getDBFactoryName() {
        return this.mConfig.getString("workflow.db.factory-name");
    }

    public String getDBModelName() {
        return this.mConfig.getString("workflow.db.modelname");
    }

    public String getBusinessPropertiesFactory() {
        return this.mConfig.getString("workflow.business.properties.factory");
    }

    public String getServerName() {
        return this.mConfig.getString("workflow.server.name");
    }

    public int getServerPort() {
        return this.mConfig.getInt("workflow.server.port");
    }

    public int getServerConnectTimeout() {
        return this.mConfig.getInt("workflow.server.connecttimeout");
    }

    public int getSessionTimeout() {
        return this.mConfig.getInt("workflow.server.sessiontimeout");
    }

    public int getServerReceiveBufferSize() {
        return this.mConfig.getInt("workflow.server.receivebuffersize");
    }

    public String getDBAccount() {
        return this.mConfig.getString("workflow.db.account");
    }
    public String getDBPassword() {
        return this.mConfig.getString("workflow.db.pass");
    }
    public String getDBUrl() {
        return this.mConfig.getString("workflow.db.url");
    }

    public boolean logAllCode() {
        return this.mConfig.getBoolean("workflow.db.log_all_code");
    }
    
    public boolean getBoolean(String value) {
        return this.mConfig.getBoolean(value);
    }

    public boolean getUpdateDownload() {
        return this.mConfig.getBoolean("workflow.db.update_download");
    }

    public String getDownloadUrl() {
        return this.mConfig.getString("workflow.db.url_download");
    }


    public boolean getReuseAddress() {
        return this.mConfig.getBoolean("workflow.server.reuseaddress");
    }

    public boolean chargeRegister() {
        return this.mConfig.getBoolean("workflow.server.charge_register");
    }
    public boolean doubleMD5() {
        return this.mConfig.getBoolean("workflow.server.double_md5");
    }

    public boolean getTcpNoDelay() {
        return this.mConfig.getBoolean("workflow.server.tcpnodelay");
    }

    public boolean enableScheduler() {
        return this.mConfig.getBoolean("workflow.scheduler.enable");
    }
}
