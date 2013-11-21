package org.sakaiproject.calendaring.api;

import net.fortuna.ical4j.model.component.VEvent;

/**
 * @author Matthew Buckett
 */
public class ExtEventImpl implements ExtEvent {

	private VEvent vEvent;

	public ExtEventImpl(VEvent vEvent) {
		this.vEvent = vEvent;
	}

	public VEvent getvEvent() {
		return vEvent;
	}

	public void setvEvent(VEvent vEvent) {
		this.vEvent = vEvent;
	}

	@Override
	public String toString() {
		return vEvent.toString();
	}
}
