package com.ettrema.scheduled;

import java.util.Date;
import com.ettrema.utils.CurrentDateService;
import com.ettrema.utils.LogUtils;
import com.ettrema.web.Component;
import com.ettrema.web.Folder;
import com.ettrema.web.Page;
import com.ettrema.web.RenderContext;
import com.ettrema.web.component.EvaluatableComponent;
import com.ettrema.web.component.InitUtils;
import com.ettrema.web.eval.Evaluatable;
import org.jdom.Element;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import static com.ettrema.context.RequestContext._;

/**
 * 
 *
 * @author bradm
 */
public class ScheduleTask extends Page {

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ScheduleTask.class);
	private static final long serialVersionUID = 1L;
	private String taskName = "task";
	/**
	 * If specified, the task will not execute if the hour of day is less then this value
	 */
	private Integer notBeforeHour;
	/**
	 * If specified, the task will not execute if the hour of day is greater then this value
	 */
	private Integer noLaterThenHour;
	/**
	 * when the last run occured
	 */
	private DateTime lastRun;
	/**
	 * The minimum allowable interval, in minutes, between runs
	 */
	private int intervalMinutes;

	public ScheduleTask(Folder parentFolder, String name) {
		super(parentFolder, name);
	}

	@Override
	public void populateXml(Element e2) {
		super.populateXml(e2);
		String s = "none";
		if(lastRun != null ) {
			s = lastRun.toString();
		}
		InitUtils.set(e2, "lastRun", s);
	}
	
	

	public boolean isTimeToRun() {
		Date dtNow = _(CurrentDateService.class).getNow();
		DateTime now = new DateTime(dtNow.getTime());
		if (notBeforeHour != null) {
			if (now.getHourOfDay() < notBeforeHour) {
				log.trace("isTimeToRun: Not time to run, before early hour");
				return false;
			}
		}
		if (noLaterThenHour != null) {
			if (now.getHourOfDay() > noLaterThenHour) {
				log.trace("isTimeToRun: Not time to run, is after late hour");
				return false;
			}
		}
		if (lastRun != null) {
			Duration dur = new Duration(lastRun, now);
			long actualMins = dur.getStandardSeconds()/60;
			boolean isAfterInterval = actualMins > intervalMinutes;
			LogUtils.trace(log, "isTimeToRun: check is after interval: lastRun", lastRun, "now", now, "intervalMinutes", intervalMinutes, "actual duration", actualMins, isAfterInterval);
			return isAfterInterval;
		}
		log.trace("isTimeToRun: no lastrun and is inside allowed hours, so yes");
		return true;
	}

	public void execute() {
		Component c = this.getComponent(taskName);
		if (c instanceof EvaluatableComponent) {
			LogUtils.trace(log, "execute: running",getName(), taskName, " in ", this.getHref());
			Evaluatable ev = ((EvaluatableComponent) c).getEvaluatable();
			RenderContext rc = new RenderContext(getTemplate(), this, null, false);
			ev.evaluate(rc, this);
			this.lastRun = new DateTime(_(CurrentDateService.class).getNow().getTime());
			this.save();
		} else {
			log.warn("execute: No evaluatable component called: " + taskName);
		}
	}

	/**
	 * @return the taskName
	 */
	public String getTaskName() {
		return taskName;
	}

	/**
	 * @param taskName the taskName to set
	 */
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	/**
	 * @return the notBeforeHour
	 */
	public Integer getNotBeforeHour() {
		return notBeforeHour;
	}

	/**
	 * @param notBeforeHour the notBeforeHour to set
	 */
	public void setNotBeforeHour(Integer notBeforeHour) {
		this.notBeforeHour = notBeforeHour;
	}

	/**
	 * @return the noLaterThenHour
	 */
	public Integer getNoLaterThenHour() {
		return noLaterThenHour;
	}

	/**
	 * @param noLaterThenHour the noLaterThenHour to set
	 */
	public void setNoLaterThenHour(Integer noLaterThenHour) {
		this.noLaterThenHour = noLaterThenHour;
	}

	/**
	 * @return the lastRun
	 */
	public DateTime getLastRun() {
		return lastRun;
	}

	/**
	 * @param lastRun the lastRun to set
	 */
	public void setLastRun(DateTime lastRun) {
		this.lastRun = lastRun;
	}

	/**
	 * @return the intervalMinutes
	 */
	public int getIntervalMinutes() {
		return intervalMinutes;
	}

	/**
	 * @param intervalMinutes the intervalMinutes to set
	 */
	public void setIntervalMinutes(int intervalMinutes) {
		this.intervalMinutes = intervalMinutes;
	}

	public void populateFieldsInXml(Element d) {
		InitUtils.set(d, "taskName", taskName);
		InitUtils.set(d, "notBeforeHour", notBeforeHour);
		InitUtils.set(d, "noLaterThenHour", noLaterThenHour);
		InitUtils.set(d, "intervalMinutes", intervalMinutes);
	}

	public void loadFieldsFromXml(Element d) {
		taskName = InitUtils.getValue(d, "taskName");
		notBeforeHour = InitUtils.getInteger(d, "notBeforeHour");
		noLaterThenHour = InitUtils.getInteger(d, "noLaterThenHour");
		intervalMinutes = InitUtils.getInt(d, "intervalMinutes");

	}
}