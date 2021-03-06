package dreamgame.business;

import dreamgame.config.DebugConfig;
import dreamgame.gameserver.framework.protocol.BusinessProperties;
import dreamgame.gameserver.framework.protocol.IBusinessPropertiesFactory;

public class BacayBusinessPropertiesFactory implements IBusinessPropertiesFactory{
	@Override
	public BusinessProperties createBusinessProperties() {
		int previousMethodCallLevel;
		if (DebugConfig.FOR_DEBUG) {
		    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
		    DebugConfig.CALL_LEVEL ++;
		    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "BacayBusinessPropertiesFactory - createBusinessProperties");
		}
		if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		return new BacayBusinessProperties();
	}
}
