package dreamgame.gameserver.framework.common;

import org.slf4j.Logger;

public abstract class AbstractLoggerFactory
  implements ILoggerFactory
{
  public AbstractLoggerFactory()
  {
    LoggerContext.mLoggerFactory = this;
  }

  @SuppressWarnings("unchecked")
public Logger getLogger(Class aClass)
  {
    return null;
  }
}