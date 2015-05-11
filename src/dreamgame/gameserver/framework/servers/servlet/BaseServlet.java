package dreamgame.gameserver.framework.servers.servlet;

import dreamgame.gameserver.framework.bytebuffer.ByteBufferFactory;
import dreamgame.gameserver.framework.bytebuffer.IByteBuffer;
//import com.migame.gameserver.framework.common.ILoggerFactory;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.servers.IServer;
import dreamgame.gameserver.framework.servers.nettyhttp.NettyHttpSessionFactory;
import dreamgame.gameserver.framework.session.ISession;
import dreamgame.gameserver.framework.session.ISessionFactory;
import dreamgame.gameserver.framework.workflow.IWorkflow;
import dreamgame.gameserver.framework.workflow.SimpleWorkflow;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;

@SuppressWarnings("serial")
public class BaseServlet extends HttpServlet
  implements IServer
{
  private final Logger mLog;
  private IWorkflow mWorkflow;
  private ISessionFactory mSessionFactory;

  public BaseServlet()
  {
    this.mLog = LoggerContext.getLoggerFactory().getLogger(BaseServlet.class);
  }

  public void init()
    throws ServletException
  {
    try
    {
      this.mLog.info("[SERVER] Starting BaseServlet");
      this.mWorkflow = new SimpleWorkflow();
      start();
    }
    catch (ServerException ex) {
      throw new ServletException(ex);
    }
    super.init();
  }

  public void destroy()
  {
    stop();
    super.destroy();
  }

  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    response.setContentType("text/html;charset=UTF-8");
    PrintWriter out = response.getWriter();
    ISession session = null;
    try
    {
      session = this.mWorkflow.sessionCreated(request.getSession());

      ServletInputStream sIn = request.getInputStream();
      int pkgSize = sIn.available();
      byte[] requestData = new byte[pkgSize];
      int readBytes = 0;
      while (readBytes < pkgSize)
      {
        int offset = readBytes;
        int length = pkgSize - readBytes;
        readBytes += sIn.read(requestData, offset, length);
      }

      IByteBuffer requestBuffer = ByteBufferFactory.wrap(requestData);

      IByteBuffer responseBuffer = this.mWorkflow.process(session, requestBuffer);

      byte[] responseData = responseBuffer.array();
      ServletOutputStream sOut = response.getOutputStream();
      sOut.write(responseData);
      sOut.flush();
    }
    catch (Throwable t)
    {
    }
    finally
    {
      if ((session != null) && (!(session.isClosed())))
      {
        session.close();
      }

      out.close();
    }
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    processRequest(request, response);
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    processRequest(request, response);
  }

  public String getServletInfo()
  {
    return "Base Servlet From Punch's Server Framework";
  }

  public ISessionFactory getSessionFactory()
  {
    if (this.mSessionFactory == null)
    {
      this.mSessionFactory = new NettyHttpSessionFactory();
    }

    return this.mSessionFactory;
  }

  public void setConnectTimeout(int aConnectTimeout)
  {
  }

  public void setReceiveBufferSize(int aReceiveBufferSize)
  {
  }

  public void setReuseAddress(boolean aReuseAddress)
  {
  }

  public void setServerPort(int aPort)
  {
  }

  public void setTcpNoDelay(boolean aTcpNoDelay)
  {
  }

  public void setWorkflow(IWorkflow aWorkflow)
  {
  }

  public void start()
  {
    this.mWorkflow.serverStarted();
  }

  public void stop()
  {
    this.mWorkflow.serverStoppted();
  }
}