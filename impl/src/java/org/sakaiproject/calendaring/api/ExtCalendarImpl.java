package org.sakaiproject.calendaring.api;

import net.fortuna.ical4j.model.Calendar;

/**
 * @author Matthew Buckett
 */
public class ExtCalendarImpl implements ExtCalendar {

	private Calendar calendar;

	public ExtCalendarImpl(Calendar calendar) {
		this.calendar = calendar;
	}

	public Calendar getCalendar() {
		return calendar;
	}

	public void setCalendar(Calendar calendar) {
		this.calendar = calendar;
	}

	@Override
	public String toString() {
		return calendar.toString();
	}
}
