package eu.h2020.symbiote.tm.services;

import eu.h2020.symbiote.tm.cron.TrustReputationUpdateTasks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

@Service
public class QoSService {

    private static final Logger logger = LoggerFactory.getLogger(QoSService.class);

    private static final String taskName = "getQualityOfServiceMetrics";

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Autowired
    private TrustReputationUpdateTasks tasks;

    public QoSService() {
    }

    public void updateIntervalPlatformReputationUpdate(String interval){
        logger.info(interval);
        logger.info("Cron tasks currently running: "+taskScheduler.getScheduledThreadPoolExecutor().getQueue().size());
        Runnable task = taskScheduler.getScheduledThreadPoolExecutor()
                .getQueue()
                .stream()
                .filter(x->x.toString().contains(taskName))
                .findFirst()
                .orElseThrow(()-> new RuntimeException("Task not found!"));

        try{
            logger.info("Cancelling task: '" + taskName + "'");
            taskScheduler.getScheduledThreadPoolExecutor().getQueue().remove(task);
            CronTrigger cron = new CronTrigger(interval);
            Runnable newTask = tasks::getQualityOfServiceMetrics;
            logger.info("Active tasks: "+taskScheduler.getScheduledThreadPoolExecutor().getQueue().size());
            taskScheduler.schedule(newTask, cron);
            logger.info("Active tasks after rescheduling: "+taskScheduler.getScheduledThreadPoolExecutor().getQueue().size());
        }
        catch(Exception e){
            logger.error(e.getMessage());
        }
    }
}
