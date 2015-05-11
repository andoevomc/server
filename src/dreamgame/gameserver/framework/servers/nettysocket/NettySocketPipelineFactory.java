package dreamgame.gameserver.framework.servers.nettysocket;

import dreamgame.gameserver.framework.workflow.IWorkflow;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
/*import org.jboss.netty.handler.timeout.ReadTimeoutHandler;
import org.jboss.netty.handler.timeout.WriteTimeoutHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
 */

public class NettySocketPipelineFactory
        implements ChannelPipelineFactory {

    private IWorkflow mWorkflow;

    public NettySocketPipelineFactory(IWorkflow aWorkflow) {
        this.mWorkflow = aWorkflow;
    }

    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline;
        try {

            pipeline = Channels.pipeline();
            pipeline.addLast("decoder", new SimpleChannelUpstreamHandler());
            pipeline.addLast("handler", new NettySocketHandler(this.mWorkflow));
            /*
            // Binhlt - begin
            Timer timer = new HashedWheelTimer();
            pipeline = Channels.pipeline(new ReadTimeoutHandler(timer, 1800),
            new WriteTimeoutHandler(timer, 1800), // timer must be shared.
            new NettySocketHandler(this.mWorkflow));
            pipeline.addLast("decoder", new SimpleChannelUpstreamHandler());
            // Binhlt - end
             */
            return pipeline;
        } catch (Throwable t) {
            throw new Exception(t);
        }
    }
}
