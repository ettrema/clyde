package com.bradmcevoy.process;

import com.bradmcevoy.process.State.TimeDependentInterval;
import org.jdom.Element;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.ReadableDuration;

/**
 *
 * @author HP
 */
public class IntervalRule implements Rule {

	private State.TimeDependentInterval intervalType;
	private int intervalVal;

	public IntervalRule(Element el) {
		String text = el.getText();
		parseDuration(text);
	}

	
	public void arm(ProcessContext context) {
	}

	public void disarm(ProcessContext context) {
	}

	public boolean eval(ProcessContext context) {
		DateTime entered = context.getToken().getTimeEntered();
		long millis = calcDuration();
		ReadableDuration duration = new Duration(millis);
		DateTime diff = entered.plus(duration);
		if (diff.isBeforeNow()) {
			// duration has passed
			return true;
		} else {
			return false;
		}
	}

	public void populateXml(Element elRule) {
		elRule.setText( formatDuration());
	}

	private long calcDuration() {
		switch (intervalType) {
			case MINUTE:
				return intervalVal * 60 * 1000;
			case HOUR:
				return intervalVal * 60 * 60 * 1000;
			case DAY:
				return intervalVal * 24 * 60 * 60 * 1000;
			case WEEK:
				return intervalVal * 7 * 24 * 60 * 60 * 1000;
			default:
				throw new RuntimeException("Unsupported interval type" + intervalType);
		}
	}

	private String formatDuration() {
		return intervalVal + " " + intervalType.name();
	}

	private void parseDuration(String s) {
		String[] arr = s.split(" ");
		try {
			intervalVal = Integer.parseInt(arr[0]);
			intervalType = TimeDependentInterval.valueOf(arr[1]);
		} catch (Throwable e) {
			throw new RuntimeException("Invalid duration. Please use the form X I, where X is an integer and I is an interval from MINUTE,HOUR,DAY,WEEK. Eg 3 WEEK");
		}
	}
}
