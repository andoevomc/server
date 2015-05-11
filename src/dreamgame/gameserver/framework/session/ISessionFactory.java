package dreamgame.gameserver.framework.session;

public abstract interface ISessionFactory
{
  public abstract ISession createSession();
}