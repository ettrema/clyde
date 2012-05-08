package com.ettrema.process;

import com.bradmcevoy.process.ProcessContext;
import com.bradmcevoy.process.TimerService;
import com.ettrema.vfs.VfsCommon;
import com.ettrema.web.BaseResource;
import com.ettrema.common.Service;
import com.ettrema.context.Context;
import com.ettrema.context.Executable2;
import com.ettrema.context.RootContextLocator;
import com.ettrema.grid.Processable;
import com.ettrema.logging.LogUtils;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Implements creating and removing persistent timer objects. Once persisted,
 * these will be detected and processed by the ScheduledTimerRunner
 *
 *
 * @author brad
 */
public class ClydeTimerService extends VfsCommon implements TimerService, Service {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ClydeTimerService.class);
    /**
     * Statically defined scheduled tasks
     */
    private List<Processable> scheduled;
    private final long schedulePeriodMs;
    private final RootContextLocator rootContextLocator;
    private final CheckAllProcessesTask checkAllProcessesTask;
    /**
     * For processing scheduled tasks, configured at startup
     */
    private ScheduledThreadPoolExecutor scheduler;
    private ArrayBlockingQueue<Processable> tasks = new ArrayBlockingQueue<>(1000);
    private boolean running;
    private boolean checkProcesses;

    public ClydeTimerService(RootContextLocator rootContextLocator) {
        this(rootContextLocator, null, 10000);
    }

    public ClydeTimerService(RootContextLocator rootContextLocator, List<Processable> scheduled, long schedulePeriodMs) {
        log.debug("Hello, from the timer service. Polling interval: " + schedulePeriodMs + "ms");
        this.rootContextLocator = rootContextLocator;
        if (scheduled != null) {
            this.scheduled = scheduled;
        } else {
            this.scheduled = new ArrayList<>();
        }
        checkAllProcessesTask = new CheckAllProcessesTask();
        this.schedulePeriodMs = schedulePeriodMs;
        tasks = new ArrayBlockingQueue<>(1000, true);
    }

    @Override
    public void registerTimer(ProcessContext context) {
        log.debug("registerTimer: " + context.getCurrentState().getProcess().getName());
        TokenValue token = (TokenValue) context.token;
        BaseResource page = (BaseResource) token.getContainer();
        TimerProcessor.create(page, context.token.getProcessName());
    }

    @Override
    public void unRegisterTimer(ProcessContext context) {
        log.debug("unRegisterTimer");
        TokenValue token = (TokenValue) context.token;
        BaseResource page = (BaseResource) token.getContainer();
        page.removeChildNode("timer_" + token.getProcessName());
    }

    @Override
    public void start() {
        running = true;
        log.debug("..starting scheduler");
        scheduler = new ScheduledThreadPoolExecutor(1);

        if (scheduled != null && scheduled.size() > 0) {
            for (Processable p : scheduled) {
                log.info("Scheduling static task: " + p.getClass());
                schedule(p, schedulePeriodMs);
            }
        }
        if (checkProcesses) {
            scheduler.scheduleWithFixedDelay(new Runnable() {

                @Override
                public void run() {
                    rootContextLocator.getRootContext().execute(new Executable2() {

                        @Override
                        public void execute(Context context) {
                            checkAllProcessesTask.doProcess(context);
                        }
                    });
                }
            }, 1000, schedulePeriodMs, TimeUnit.MILLISECONDS);
        }

        log.debug("starting task processor:");
        scheduler.scheduleWithFixedDelay(new TaskProcessorConsumer(), 2, 1, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        running = false;
        tasks.clear();
        scheduler.shutdown();
    }

    private void schedule(final Processable p, long period) {
        log.info("scheduling timer task: " + p.getClass() + " at interval: " + period + "ms");
        scheduler.scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {
                try {
                    runProcessable(p);
                } catch (Throwable e) {
                    log.error("Exception executing task: " + p.getClass(), e);
                }
            }
        }, 1000, period, TimeUnit.MILLISECONDS);
    }

    void runProcessable(final Processable p) {
        LogUtils.trace(log, "runProcessable", p.getClass());
        rootContextLocator.getRootContext().execute(new Executable2() {

            @Override
            public void execute(Context context) {
                p.doProcess(context);
            }
        });
    }

    public List<Processable> getScheduled() {
        return scheduled;
    }

    public void setScheduled(List<Processable> scheduled) {
        this.scheduled = scheduled;
    }

    public boolean isCheckProcesses() {
        return checkProcesses;
    }

    public void setCheckProcesses(boolean checkProcesses) {
        this.checkProcesses = checkProcesses;
    }

    /**
     * Look for all TimerProcessor instances and queue them for processing
     *
     */
    private class CheckAllProcessesTask extends VfsCommon {

        public void doProcess(Context context) {
            List<NameNode> timers = this.vfs().find(TimerProcessor.class, null);
            log.trace("check for timers found: " + timers.size());
            NameNode parentNode;
            DataNode parentData;
            for (NameNode nn : timers) {
                TimerProcessor tp = (TimerProcessor) nn.getData();
                if (tp != null) {
                    parentNode = tp.getNameNode().getParent();
                    parentData = parentNode.getData();
                    if (parentData == null) {
                        log.warn("Parent data node is null: " + nn.getId());
                    } else if (parentData instanceof BaseResource) {
                        BaseResource res = (BaseResource) parentData;
                        log.debug("enqueue check process task: " + res.getHref());
                        if (running == false) {
                            throw new RuntimeException("Service is stopped");
                        }
                        tasks.offer(new CheckProcessTask(res.getNameNodeId()));
                    } else {
                        log.warn("parent data node is not a BaseResource. Is a: " + parentData.getClass());
                    }
                } else {
                    log.warn("timer processor has a null data node: " + tp.getId());
                }
            }
        }

        public void run() {
        }
    }

    /**
     * Given the name node id for a BaseResource, locate it and find its process
     * instance and scan it
     */
    private static class CheckProcessTask extends VfsCommon implements Processable, Serializable {

        private static final long serialVersionUID = 1L;
        private final UUID timerProcessorId;

        public CheckProcessTask(UUID timerProcessorId) {
            this.timerProcessorId = timerProcessorId;
        }

        @Override
        public void doProcess(Context context) {
            log.debug("doProcess: " + timerProcessorId);
            try {
                NameNode nn = this.vfs().get(timerProcessorId);
                if (nn == null) {
                    log.warn("process name node not found: " + timerProcessorId);
                    return;
                }
                DataNode dn = nn.getData();
                if (dn == null) {
                    log.warn("process data node not found: " + timerProcessorId);
                    return;
                }
                if (!(dn instanceof BaseResource)) {
                    log.error("process data node is not a BaseResource, is a: " + dn.getClass());
                    return;
                }
                BaseResource parentRes = (BaseResource) dn;

                boolean didTransition = ProcessDef.scan(parentRes);

                if (didTransition) {
                    log.debug("timer did transition. saving: " + parentRes.getHref());
                    parentRes.save();
                    commit();
                } else {
//                log.debug("timer did not transition");
                    rollback();
                }
            } catch (Throwable e) {
                rollback();
                log.error("Exception processing timer node: namenodeid: " + this.timerProcessorId, e);
            }
        }

        @Override
        public void pleaseImplementSerializable() {
        }
    }

    public class TaskProcessorConsumer implements Runnable {

        @Override
        public void run() {
            //log.trace("take task. queue size: " + tasks.size());
            Processable p = tasks.poll();
            while (p != null) {
                log.trace("got task: " + p.getClass());
                try {
                    runProcessable(p);
                } catch (Throwable e) {
                    log.error("Failed to process task", e);
                }
                log.trace("finished task");
                p = tasks.poll();
            }
        }
    }
}
