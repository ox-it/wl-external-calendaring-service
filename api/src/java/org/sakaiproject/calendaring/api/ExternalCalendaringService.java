package org.sakaiproject.calendaring.api;

import java.util.List;

import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.user.api.User;

/**
 * A service to provide integrations with external calendars using the iCalendar standard.
 * <p>
 * Tools and services can leverage this to generate an ICS file for an event, as well as make updates.
 * 
 * Ref: http://www.ietf.org/rfc/rfc2445.txt
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public interface ExternalCalendaringService {

	/**
	 * Creates an ExtEvent for a Sakai CalendarEvent.
	 * This must then be turned into a Calendar before it can be turned into an ICS file.
	 * 
	 * <br>If the CalendarEvent has the field 'vevent_uuid', that will be used as the UUID of the ExtEvent preferentially.
	 * <br>If the CalendarEvent has the field 'vevent_sequence', that will be used as the sequence of the ExtEvent preferentially.
	 * <br>If the CalendarEvent has the field 'vevent_url', that will be added to the URL property of the ExtEvent.
	 * 
	 * @param event Sakai CalendarEvent
	 * @return the ExtEvent for the given event or null if there was an error
	 */
	public ExtEvent createEvent(CalendarEvent event);
	
	/**
	 * Creates an ExtEvent for a Sakai CalendarEvent with the given attendees.
	 * This must then be turned into a Calendar before it can be turned into an ICS file.
	 * 
	 * <br>If the CalendarEvent has the field 'vevent_uuid', that will be used as the UUID of the ExtEvent preferentially.
	 * <br>If the CalendarEvent has the field 'vevent_sequence', that will be used as the sequence of the ExtEvent preferentially.
	 * <br>If the CalendarEvent has the field 'vevent_url', that will be added to the URL property of the ExtEvent.
	 * 
	 * @param event Sakai CalendarEvent
	 * @param attendees list of Users that have been invited to the event
	 * @return the ExtEvent for the given event or null if there was an error
	 */
	public ExtEvent createEvent(CalendarEvent event, List<User> attendees);
	
	/**
	 * Adds a list of attendees to an existing ExtEvent.
	 * This must then be turned into a Calendar before it can be turned into an ICS file. 
	 * 
	 * @param extEvent  The ExtEvent to add the attendess too
	 * @param attendees list of Users that have been invited to the event
	 * @return
	 */
	public ExtEvent addAttendeesToEvent(ExtEvent extEvent, List<User> attendees);
	
	/**
	 * Set the status of an existing ExtEvent to cancelled.
	 * This must then be turned into a Calendar before it can be turned into an ICS file.
	 * 
	 * @param extEvent The ExtEvent to cancel
	 * @return the updated ExtEvent
	 */
	public ExtEvent cancelEvent(ExtEvent extEvent);
	
	/**
	 * Creates an iCal calendar from a list of ExtEvents.
	 * 
	 * @param events iCal ExtEvents
	 * @return the Calendar for the given events or null if there was an error
	 */
	public ExtCalendar createCalendar(List<ExtEvent> events);
	
	/**
	 * Write an iCal calendar out to a file in the filesystem and return the path.
	 * @param calendar iCal calendar object
	 * @return the path to the file
	 */
	public String toFile(ExtCalendar calendar);
	
	/**
	 * Is the ICS service enabled? Tools can use this public method for test in their own UIs.
	 * If this is disabled, nothing will be generated.
	 * @return
	 */
	public boolean isIcsEnabled();
	
	
}
