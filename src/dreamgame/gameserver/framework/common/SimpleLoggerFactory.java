package dreamgame.gameserver.framework.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleLoggerFactory extends AbstractLoggerFactory
{  @SuppressWarnings("unchecked")
  public Logger getLogger(Class aClass)
  {
    return LoggerFactory.getLogger(aClass);
  }
}