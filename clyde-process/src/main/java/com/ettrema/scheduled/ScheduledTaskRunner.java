package com.ettrema.scheduled;

import com.ettrema.utils.LogUtils;
import com.ettrema.common.Service;
import com.ettrema.context.Executable2;
import com.ettrema.context.RootContextLocator;
import com.ettrema.context.Context;
import com.ettrema.grid.Processable;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;
import static com.ettrema.context.RequestContext._;

/**
 *
 * @author bradm
 */
public class ScheduledTaskRunner implements Processable, Serializable, Service {

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ScheduledTaskRunner.class);
	private final BlockingQueue<UUID> scheduledTasksToRun = new ArrayBlockingQueue<UUID>(10000);
	private final RootContextLocator rootContextLocator;
	private boolean started;
	private Consumer consumer;
	private Thread consumerThread;

	@Autowired
	public ScheduledTaskRunner(RootContextLocator rootContextLocator) {
		this.rootContextLocator = rootContextLocator;
	}

	@Override
	public void doProcess(Context context) {
		log.trace("doProcess");
		if (!started) {
			log.trace("service isnt started, so starting it now");
			start();
		}

		if( !scheduledTasksToRun.isEmpty()) {
			log.trace("Have tasks in queue already, so exit");
			return ;
		}

		List<NameNode> tasks = _(VfsSession.class).find(ScheduleTask.class, null);
		for (NameNode task : tasks) {
			if (scheduledTasksToRun.contains(task.getId())) {
				log.trace("scheduled task is already in queue");
			} else {
				if (isTimeToRun(task)) {
					enqueueTask(task);
				}
			}
		}
	}

	private void enqueueTask(NameNode task) {
		log.trace("enqueueTask");
		scheduledTasksToRun.offer(task.getId());
	}

	private boolean isTimeToRun(NameNode task) {
		DataNode dn = task.getData();
		if (dn == null) {
			log.warn("data node is null for name node id: " + task.getId());
			return false;
		}
		if (dn instanceof ScheduleTask) {
			ScheduleTask st = (ScheduleTask) dn;
			return st.isTimeToRun();
		} else {
			log.warn("data node is not a ScheduleTask! Is a:" + dn.getClass() + " id:" + task.getId());
			return false;
		}
	}

	@Override
	public void pleaseImplementSerializable() {
	}

	@Override
	public void start() {
		started = true;
		consumer = new Consumer();
		consumerThread = new Thread(consumer, "ScheduledTaskRunner.queue.consumer");
		consumerThread.setDaemon(true);
		consumerThread.start();
	}

	@Override
	public void stop() {
		started = false;
	}

	class Consumer implements Runnable {

		@Override
		public void run() {
			try {
				while (true) {
					consume(scheduledTasksToRun.take());
				}
			} catch (InterruptedException ex) {
				log.warn("interrupted");
			}
		}

		void consume(final UUID nameNodeId) {
			rootContextLocator.getRootContext().execute(new Executable2() {

				@Override
				public void execute(Context context) {
					NameNode node = _(VfsSession.class).get(nameNodeId);
					if (node == null) {
						log.warn("Didnt find node");
						return;
					}
					DataNode dn = node.getData();
					if (dn instanceof ScheduleTask) {
						ScheduleTask st = (ScheduleTask) dn;
						LogUtils.trace(log, "Execute task", st.getName());
						st.execute();
						_(VfsSession.class).commit();
					} else {
						log.warn("data node is not a ScheduleTask! Is a:" + dn.getClass() + " id:" + node.getId());
					}
				}
			});
		}
	}
}
