package dreamgame.gameserver.framework.scheduler;

//import com.migame.gameserver.framework.common.ILoggerFactory;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.workflow.WorkflowConfig;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Schedulers
{
  private Logger mLog = LoggerContext.getLoggerFactory().getLogger(Schedulers.class);
  @SuppressWarnings("unused")
private final String DEFAULT_SCHEDULER_CONFIG = "conf/scheduler-config.xml";
  @SuppressWarnings("unused")
private static final String SCHEDULER_COMPONENT = "scheduler";
  private Scheduler mScheduler = null;
  private WorkflowConfig mWorkflowConfig;

  public Schedulers(WorkflowConfig aWorkflowConfig)
  {
    this.mWorkflowConfig = aWorkflowConfig;
  }

  public void start() throws ServerException
  {
    SchedulerFactory schedFact;
    try
    {
      schedFact = new StdSchedulerFactory();

      this.mScheduler = schedFact.getScheduler();

      if ((this.mScheduler.isShutdown()) || ((this.mScheduler.isInStandbyMode())))
      {
        this.mScheduler.start();
        this.mLog.info("[SCHEDULE] Scheduler started!");
      }
      else {
        this.mLog.info("[SCHEDULE] Scheduler already started!");
      }

      File fConfig = new File("conf/scheduler-config.xml");
      this.mLog.info("[SCHEDULER] From file = conf/scheduler-config.xml");

      if (!(fConfig.exists()))
      {
        throw new IOException("File " + fConfig.getName() + " is not exist.");
      }
      if (!(fConfig.canRead()))
      {
        throw new IOException("File " + fConfig.getName() + " must be readable.");
      }

      DocumentBuilderFactory docBuildFac = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docBuildFac.newDocumentBuilder();
      Document aDoc = docBuilder.parse(fConfig);

      Element root = aDoc.getDocumentElement();

      String schedulerPkg = root.getAttribute("package");

      NodeList nodeList = root.getElementsByTagName("job");
      int nodeCount = nodeList.getLength();

      String appName = this.mWorkflowConfig.appName();

      int iCount = 0;
      while (true) { if (iCount >= nodeCount)
          break;

        Element jobNode = (Element)nodeList.item(iCount);

        boolean enableJob = Boolean.parseBoolean(jobNode.getAttribute("enable"));
        if (enableJob)
        {
          String jobName = appName + "_" + jobNode.getAttribute("jobname");
          try
          {
            this.mScheduler.deleteJob(jobName, "DEFAULT");
          }
          catch (Throwable t) {
            this.mLog.error("", t);
          }

          int intervalTimer = Integer.parseInt(jobNode.getAttribute("interval"));
          if (intervalTimer > 0)
          {
            String jobClass = jobNode.getAttribute("name");

            String module = "";
            if (jobNode.hasAttribute("module"))
            {
              module = jobNode.getAttribute("module");
            }

            if ((module != null) && (!(module.trim().equals(""))))
            {
              jobClass = schedulerPkg + "." + "scheduler" + "." + module + "." + jobClass + "Job";
            }
            else {
              jobClass = schedulerPkg + "." + "scheduler" + "." + jobClass + "Job";
            }

            JobDetail newJob = new JobDetail(jobName, "DEFAULT", Class.forName(jobClass));

            Trigger jobTrigger = TriggerUtils.makeSecondlyTrigger(intervalTimer);

            int delayTime = 0;
            if (jobNode.hasAttribute("delay"))
            {
              delayTime = Integer.parseInt(jobNode.getAttribute("delay"));
            }
            jobTrigger.setStartTime(new Date(System.currentTimeMillis() + delayTime));

            jobTrigger.setName("Trigger_" + jobName);

            JobDataMap jobData = new JobDataMap();

            String dbModelName = this.mWorkflowConfig.getDBModelName();
            jobData.put("job.db.model.name", dbModelName);

            jobTrigger.setJobDataMap(jobData);

            this.mScheduler.scheduleJob(newJob, jobTrigger);
            this.mLog.info("[SCHEDULER] Job " + jobName + " started!");
          }
        }
        ++iCount;
      }

    }
    catch (Throwable t)
    {
      throw new ServerException(t);
    }
  }

  public void stop()
  {
    try
    {
      if (this.mScheduler != null)
      {
        this.mScheduler.shutdown();
      }
    }
    catch (Exception ex) {
      this.mLog.error("", ex);
    }
  }
}