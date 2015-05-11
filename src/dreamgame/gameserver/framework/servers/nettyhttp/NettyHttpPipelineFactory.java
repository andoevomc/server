package dreamgame.gameserver.framework.servers.nettyhttp;

import dreamgame.gameserver.framework.workflow.IWorkflow;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

public class NettyHttpPipelineFactory
  implements ChannelPipelineFactory
{
  private IWorkflow mWorkflow;

  public NettyHttpPipelineFactory(IWorkflow aWorkflow)
  {
    this.mWorkflow = aWorkflow;
  }

  public ChannelPipeline getPipeline() throws Exception
  {
    ChannelPipeline pipeline;
    try
    {
      pipeline = Channels.pipeline();

      pipeline.addLast("decoder", new HttpRequestDecoder());

      pipeline.addLast("encoder", new HttpResponseEncoder());

      pipeline.addLast("handler", new NettyHttpHandler(this.mWorkflow));
      return pipeline;
    }
    catch (Throwable t) {
      throw new Exception(t);
    }
  }
}