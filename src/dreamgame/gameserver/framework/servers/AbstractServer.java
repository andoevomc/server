package dreamgame.gameserver.framework.servers;

import dreamgame.gameserver.framework.workflow.IWorkflow;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractServer
  implements IServer
{
  protected IWorkflow mWorkflow;
  protected int mPort;
  protected int mConnectTimeout;
  protected int mReceiveBufferSize;
  protected boolean mReuseAddress;
  protected boolean mTcpNoDelay;

  public void setWorkflow(IWorkflow aWorkflow)
  {
    this.mWorkflow = aWorkflow;
  }

  public void setServerPort(int aPort)
  {
    this.mPort = aPort;
  }

  public void setConnectTimeout(int aConnectTimeout)
  {
    this.mConnectTimeout = aConnectTimeout;
  }

  public void setReceiveBufferSize(int aReceiveBufferSize)
  {
    this.mReceiveBufferSize = aReceiveBufferSize;
  }

  public void setReuseAddress(boolean aReuseAddress)
  {
    this.mReuseAddress = aReuseAddress;
  }

  public void setTcpNoDelay(boolean aTcpNoDelay)
  {
    this.mTcpNoDelay = aTcpNoDelay;
  }

  public void start()
  {      
    int tryNo = 0;
    boolean serverIsUp = false;
    // max 100 times of trying
    while (tryNo < 100 && !serverIsUp) {
	try {
	    tryNo ++;
	    System.out.println("#### StartServer try number " + tryNo);
	    
	    startServer();
	    
	    serverIsUp = true;
	    System.out.println("#### Server is up after " + tryNo + " trys");
	} catch (Exception e) {
	    e.printStackTrace();
	    try {
		System.out.println("### Sleep for 20 seconds and retry again...");
		Thread.sleep(20000);
	    } catch (InterruptedException ex) {
		ex.printStackTrace();
	    }
	}
    }
    
    // after 1000 times of trying server still is not up -> stop running
    if ( ! serverIsUp) {
	// th
	int i = Integer.parseInt("Server could not start after " + tryNo + " trys :((");
    }
    
    this.mWorkflow.serverStarted();
  }

  protected abstract void startServer();

  public void stop()
  {
    this.mWorkflow.serverStoppted();
  }
}