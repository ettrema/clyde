package com.bradmcevoy.scheduled;

import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Page;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.component.InitUtils;
import com.bradmcevoy.web.eval.Evaluatable;
import org.jdom.Element;
import org.joda.time.DateTime;
import org.joda.time.Duration;

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

	public boolean isTimeToRun() {
		DateTime now = new DateTime();
		if (notBeforeHour != null) {
			if (now.getHourOfDay() < notBeforeHour) {
				return false;
			}
		}
		if (noLaterThenHour != null) {
			if (now.getHourOfDay() > noLaterThenHour) {
				return false;
			}
		}
		if (lastRun != null) {
			Duration dur = new Duration(lastRun, now);
			return dur.getStandardSeconds() > intervalMinutes * 60;
		}
		return true;
	}

	public void execute() {
		Component c = this.getComponent(taskName);
		if (c instanceof Evaluatable) {
			Evaluatable ev = (Evaluatable) c;
			RenderContext rc = new RenderContext(getTemplate(), this, null, false);
			ev.evaluate(rc, this);
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