package com.bradmcevoy.web.mail;

import com.ettrema.common.Service;
import com.ettrema.mail.StandardMessage; 
import com.ettrema.mail.send.MailSender;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintains a queue of emails to send, retrying a number of times if errors
 * occur.
 * 
 * Can optionally callback when an email is sent or has failed
 * 
 * Note that this class is intended to provide feedback about whether emails
 * were delivered or not, so it is not appropriate to use it with a MailSender
 * which buffers messages for sending.
 *
 * @author brad
 */
public class RetryingMailService implements Service{

	private final static Logger log = LoggerFactory.getLogger(RetryingMailService.class);
	private final MailSender mailSender;
	private Map<String,SendJob> mapOfJobs = new ConcurrentHashMap<String, SendJob>();
	private DelayQueue<DelayMessage> delayQueue = new DelayQueue<DelayMessage>();
	private boolean running;
	private Consumer consumer;
	private Thread thConsumer;
	private int maxRetries = 3;

	public RetryingMailService(MailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void start() {
		running = true;
		mailSender.start();
		consumer = new Consumer(delayQueue);
		thConsumer = new Thread(consumer);
		thConsumer.start();

	}

	public void stop() {
		running = false;
		delayQueue.clear();
		thConsumer.interrupt();
		mailSender.stop();
		consumer = null;
		thConsumer = null;
	}

	public int getMaxRetries() {
		return maxRetries;
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}


	/**
	 * Submit the list of emails to send and associate with an id for this job
	 * 
	 * @param sm
	 * @param callback
	 * @return 
	 */
	public void sendMails(String id, List<StandardMessage> msgs, EmailResultCallback callback) { 
		List<DelayMessage> list = new ArrayList<DelayMessage>();
		SendJob sendJob = new SendJob(id);
		for (StandardMessage sm  : msgs) { 
			DelayMessage dm = new DelayMessage(sendJob, sm, callback);
			list.add(dm);
		}
		sendJob.msgs = list;
		
		mapOfJobs.put(sendJob.id, sendJob);
		
		for(DelayMessage dm : sendJob.msgs) {
			delayQueue.add(dm);
		}
		log.info("Queue size is now: " + delayQueue.size());
	}
	
	public SendJob getJob(String id) {
		return mapOfJobs.get(id);
	}
	
	public void cancel(String id) {
		SendJob job = mapOfJobs.get(id);
		if( job != null ) {
			job.cancel();
			job.cancelled = true;
		}
	}

	private class Consumer implements Runnable {

		private final DelayQueue<DelayMessage> queue;

		private Consumer(DelayQueue<DelayMessage> q) {
			queue = q;
		}

		@Override
		public void run() {
			try {
				log.info("Starting queue processing consumer");
				while (running) {
					consume(queue.take());
					log.info("Remaining queue items: " + queue.size());
				}
			} catch (InterruptedException ex) {
				log.info("Exitting consumer thread");
			}
		}

		void consume(DelayMessage dm) {
			log.info("Attempt to send: " + dm);
			if( dm.completedOk || dm.failed ) {
				log.info("Email is marked as failed or complete");
				return ;
			}
			try {
				send(dm);
				dm.completedOk = true;
				dm.failed = false;
				dm.callback.onSuccess(dm.sm);
			} catch (Throwable e) {
				dm.onFailed(e);
				if (dm.attempts <= maxRetries) {
					log.info("Failed to send message: " + dm + " will retry in " + dm.getDelayMillis() / 1000 + "seconds");
					dm.failed = true;
					queue.add(dm);
				} else {
					log.error("Failed to send message: " + dm + " Exceeded retry attempts: " + dm.attempts + ", will not retry");
					dm.callback.onFailed(dm.sm, dm.lastException);
				}
			} finally {
				// If there is a sendJob, and all emails are sent, then call the finished callback
				if( dm.sendJob != null ) {
					if( dm.sendJob.checkComplete() ) {						
						dm.callback.finished(dm.sendJob.id, dm.sendJob.msgs);
					}
				}
			}
		}

		private void send(DelayMessage dm) {
			mailSender.sendMail(dm.sm);
		}
	}

	public class SendJob {

		private Collection<DelayMessage> msgs;
		private final String id;
		private boolean cancelled;

		public SendJob(String id) {
			this.id = id;
		}

		public Collection<DelayMessage> getMsgs() {
			return msgs;
		}

		
		public boolean isCancelled() {
			return cancelled;
		}

		
		private boolean checkComplete() {
			for( DelayMessage dm : msgs) {
				boolean isComplete = dm.completedOk || dm.failed;
				if( !isComplete ) {
					return false;
				}
			}
			return true;
		}
		
		private void cancel() {
			for( DelayMessage dm : msgs) {
				boolean isComplete = dm.completedOk || dm.failed;
				if( !isComplete ) {
					dm.failed = true;
				}
			}			
		}
	}

	public class DelayMessage implements Delayed {
		private final SendJob sendJob;
		private final StandardMessage sm; 
		private final EmailResultCallback callback;
		private int attempts;
		private boolean completedOk;
		private boolean failed;
		private Throwable lastException;

		public DelayMessage(SendJob sendJob, StandardMessage sm, EmailResultCallback callback) { 
			this.sendJob = sendJob;
			this.sm = sm;
			this.callback = callback;
		}

		public StandardMessage getSm() { 
			return sm;
		}

		@Override
		public long getDelay(TimeUnit unit) {
			return unit.convert(getDelayMillis(), TimeUnit.MILLISECONDS);
		}

		@Override
		public int compareTo(Delayed o) {
			if (o instanceof DelayMessage) {
				DelayMessage other = (DelayMessage) o;
				return this.getDelayMillis().compareTo(other.getDelayMillis());
			} else {
				throw new RuntimeException("Not supported comparison with type: " + o.getClass() + " - should be: " + this.getClass());
			}
		}

		private void onFailed(Throwable e) {
			attempts++;
			this.lastException = e;
		}

		public Long getDelayMillis() {
			if (attempts < 1) {
				return 0l; // no delay
			} else if (attempts < 2) {
				return 5 * 1000l; // 5 seconds
			} else if (attempts < 3) {
				return 30 * 1000l; // 30 seconds
			} else {
				return attempts * 1000 * 60 * 60l; // attempts x hours Eg 5 attempts means a delay of 5 hours
			}
		}

		public boolean isCompletedOk() {
			return completedOk;
		}

		/**
		 * True if we've given up
		 * 
		 * @return 
		 */
		public boolean isFatal() {
			return failed;
		}

		public int getAttempts() {
			return attempts;
		}

		public Throwable getLastException() {
			return lastException;
		}
		
		
	}

	public interface EmailResultCallback {

		void onSuccess(StandardMessage sm); 

		void onFailed(StandardMessage sm, Throwable lastException); 

		void finished(String id, Collection<DelayMessage> msgs);
	}
}


