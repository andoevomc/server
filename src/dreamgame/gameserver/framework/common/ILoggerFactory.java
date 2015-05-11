package dreamgame.gameserver.framework.common;

import org.slf4j.Logger;

public abstract interface ILoggerFactory
{
	  @SuppressWarnings("unchecked")
  public abstract Logger getLogger(Class paramClass);
}