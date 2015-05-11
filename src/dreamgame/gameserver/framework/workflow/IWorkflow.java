package dreamgame.gameserver.framework.workflow;

import dreamgame.gameserver.framework.bytebuffer.IByteBuffer;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.BusinessProperties;
import dreamgame.gameserver.framework.session.ISession;

public abstract interface IWorkflow
{
  public abstract void start()
    throws ServerException;

  public abstract IByteBuffer process(ISession paramISession, IByteBuffer paramIByteBuffer)
    throws ServerException;

  public abstract WorkflowConfig getWorkflowConfig();

  public abstract ISession sessionCreated(Object paramObject)
    throws ServerException;

  public abstract BusinessProperties createBusinessProperties();

  public abstract void serverStarted();

  public abstract void serverStoppted();

}