package org.sakaiproject.calendaring.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.parameter.Rsvp;
import net.fortuna.ical4j.model.property.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendaring.logic.SakaiProxy;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.user.api.User;




/**
 * Implementation of {@link ExternalCalendaringService}
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@CommonsLog
public class ExternalCalendaringServiceImpl implements ExternalCalendaringService {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExtEvent createEvent(CalendarEvent event) {
		return createEvent(event, null);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExtEvent createEvent(CalendarEvent event, List<User> attendees) {
		
		if(!isIcsEnabled()) {
			log.debug("ExternalCalendaringService is disabled. Enable via calendar.ics.generation.enabled=true in sakai.properties");
			return null;
		}
		
		//timezone. All dates are in GMT so we need to explicitly set that
		TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
		TimeZone timezone = registry.getTimeZone("GMT");
		VTimeZone tz = timezone.getVTimeZone();

		//start and end date
		DateTime start = new DateTime(getStartDate(event.getRange()).getTime());
		DateTime end = new DateTime(getEndDate(event.getRange()).getTime());
		
		//create event incl title/summary
		VEvent vevent = new VEvent(start, end, event.getDisplayName());
			
		//add timezone
		vevent.getProperties().add(tz.getTimeZoneId());
		
		//add uid to event
		//could come from the vevent_uuid field in the calendar event, otherwise from event ID.
		String uuid = null;
		if(StringUtils.isNotBlank(event.getField("vevent_uuid"))) {
			uuid = event.getField("vevent_uuid");
		} else {
			uuid = event.getId();
		}		
		vevent.getProperties().add(new Uid(uuid));
		
		//add sequence to event
		//will come from the vevent_sequnece field in the calendar event, otherwise skip it
		String sequence = null;
		if(StringUtils.isNotBlank(event.getField("vevent_sequence"))) {
			sequence = event.getField("vevent_sequence");
			vevent.getProperties().add(new Sequence(sequence));
		}
			
		//add description to event
		vevent.getProperties().add(new Description(event.getDescription()));
		
		//add location to event
		vevent.getProperties().add(new Location(event.getLocation()));
		
		//add organiser to event
		if(StringUtils.isNotBlank(event.getCreator())) {

			String creatorEmail = sakaiProxy.getUserEmail(event.getCreator());

			if (creatorEmail != null && !creatorEmail.isEmpty()) {
				URI mailURI = createMailURI(creatorEmail);
				Cn commonName = new Cn(sakaiProxy.getUserDisplayName(event.getCreator()));

				Organizer organizer = new Organizer(mailURI);
				organizer.getParameters().add(commonName);
				vevent.getProperties().add(organizer);
			}
		}
		
		//add attendees to event with 'required participant' role
		vevent = toVEvent(addAttendeesToEvent(new ExtEventImpl(vevent), attendees));
		
		//add URL to event, if present
		String url = null;
		if(StringUtils.isNotBlank(event.getField("vevent_url"))) {
			url = event.getField("vevent_url");
			Url u = new Url();
			try {
				u.setValue(url);
				vevent.getProperties().add(u);
			} catch (URISyntaxException e) {
				//it doesnt matter, ignore it
			}
		}
		
		if(log.isDebugEnabled()){
			log.debug("VEvent:" + vevent);
		}
		
		return new ExtEventImpl(vevent);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExtEvent addAttendeesToEvent(ExtEvent extEvent, List<User> attendees) {
		return addAttendeesToEventWithRole(extEvent, attendees, Role.REQ_PARTICIPANT);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExtEvent addChairAttendeesToEvent(ExtEvent extEvent, List<User> attendees) {
		return addAttendeesToEventWithRole(extEvent, attendees, Role.CHAIR);
	}

	/**
	 * Adds attendees to an existing event with a given role
	 * Common logic for addAttendeesToEvent and addChairAttendeestoEvent
	 *
	 * @param extEvent  the ExtEvent to add the attendess too
	 * @param attendees list of Users that have been invited to the event
	 * @param role      the role with which to add each user
	 * @return          the ExtEvent for the given event or null if there was an error
	 */
	protected ExtEvent addAttendeesToEventWithRole(ExtEvent extEvent, List<User> attendees, Role role) {
		VEvent vevent = toVEvent(extEvent);
		if(!isIcsEnabled()) {
			log.debug("ExternalCalendaringService is disabled. Enable via calendar.ics.generation.enabled=true in sakai.properties");
			return null;
		}
		
		//add attendees to event with 'required participant' role
		if(attendees != null){
			for(User u: attendees) {
				Attendee a = new Attendee(createMailURI(u.getEmail()));
				a.getParameters().add(role);
				a.getParameters().add(new Cn(u.getDisplayName()));
				a.getParameters().add(PartStat.ACCEPTED);
				a.getParameters().add(Rsvp.FALSE);
			
				vevent.getProperties().add(a);
			}
		}
		
		if(log.isDebugEnabled()){
			log.debug("VEvent with attendees:" + vevent);
		}
		
		return extEvent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExtEvent cancelEvent(ExtEvent extEvent) {

		VEvent vevent = toVEvent(extEvent);
		if(!isIcsEnabled()) {
			log.debug("ExternalCalendaringService is disabled. Enable via calendar.ics.generation.enabled=true in sakai.properties");
			return null;
		}
		// You can only have one status so make sure we remove any previous ones.
		vevent.getProperties().removeAll(vevent.getProperties(Property.STATUS));
		vevent.getProperties().add(Status.VEVENT_CANCELLED);

		// Must define a sequence for cancellations. If one was not defined when the event was created use 1
		if (vevent.getProperties().getProperty(Property.SEQUENCE) == null) {
			vevent.getProperties().add(new Sequence("1"));
		}

		if(log.isDebugEnabled()){
			log.debug("VEvent cancelled:" + vevent);
		}
		
		return extEvent;
		
	}
	
	
	/**
	 * {@inheritDoc}
	 */@Override
	public ExtCalendar createCalendar(List<ExtEvent> events) {
		return createCalendar(events, null);
	}
	
	/**
	 * {@inheritDoc}
	 */@Override
	public ExtCalendar createCalendar(List<ExtEvent> events, String method) {
		
		if(!isIcsEnabled()) {
			log.debug("ExternalCalendaringService is disabled. Enable via calendar.ics.generation.enabled=true in sakai.properties");
			return null;
		}
		
		//setup calendar
		Calendar calendar = setupCalendar(method);
		
		//null check
		if(CollectionUtils.isEmpty(events)) {
			log.error("List of VEvents was null or empty, no calendar will be created.");
			return null;
		}
		
		//add vevents to calendar
		for (ExtEvent extEvent: events) {
			calendar.getComponents().add(toVEvent(extEvent));
		}
		
		//validate
		try {
			calendar.validate(true);
		} catch (ValidationException e) {
			e.printStackTrace();
			return null;
		}
		
		if(log.isDebugEnabled()){
			log.debug("Calendar:" + calendar);
		}
		
		return new ExtCalendarImpl(calendar);
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toFile(ExtCalendar extCalendar) {

		if(!isIcsEnabled()) {
			log.debug("ExternalCalendaringService is disabled. Enable via calendar.ics.generation.enabled=true in sakai.properties");
			return null;
		}
		
		//null check
		if(extCalendar == null) {
			log.error("Calendar is null, cannot generate ICS file.");
			return null;
		}
		Calendar calendar = toCalendar(extCalendar);
		String path = generateFilePath(UUID.randomUUID().toString());
		
		//test file
		File file = new File(path);
		try {
			if(!file.createNewFile()) {
				log.error("Couldn't write file to: " + path);
				return null;	
			}
		} catch (IOException e) {
			log.error("An error occurred trying to write file to: " + path + " : " + e.getClass() + " : " + e.getMessage());
			return null;
		}
		
		//if cleanup enabled, mark for deletion when the JVM exits.
		if(sakaiProxy.isCleanupEnabled()) {
			file.deleteOnExit();
		}
		
		FileOutputStream fout;
		try {
			fout = new FileOutputStream(file);
		
			CalendarOutputter outputter = new CalendarOutputter();
			outputter.output(calendar, fout);
		
			fout.flush();
			fout.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
		return path;
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isIcsEnabled() {
		return sakaiProxy.isIcsEnabled();
	}
	
	/**
	 * Helper method to setup the standard parts of the calendar
	 * @return
	 */
	private Calendar setupCalendar(String method) {
		
		String serverName = sakaiProxy.getServerName();
		
		//setup calendar
		Calendar calendar = new Calendar();
		calendar.getProperties().add(new ProdId("-//"+serverName+"//Sakai External Calendaring Service//EN"));
		calendar.getProperties().add(Version.VERSION_2_0);
		calendar.getProperties().add(CalScale.GREGORIAN);
		if (method != null) {
			calendar.getProperties().add(new Method(method));
		}
		return calendar;
	}
	
	
	
	
	/**
	 * Helper to extract the startDate of a TimeRange into a java.util.Calendar object. 
	 * @param range 
	 * @return
	 */
	private java.util.Calendar getStartDate(TimeRange range) {
		java.util.Calendar c = new GregorianCalendar();
		c.setTimeInMillis(range.firstTime().getTime());
		return c;
	}
	
	/**
	 * Helper to extract the endDate of a TimeRange into a java.util.Calendar object. 
	 * @param range 
	 * @return
	 */
	private java.util.Calendar getEndDate(TimeRange range) {
		java.util.Calendar c = new GregorianCalendar();
		c.setTimeInMillis(range.lastTime().getTime());
		return c;
	}

	private VEvent toVEvent(ExtEvent extEvent) {
		if (extEvent instanceof ExtEventImpl) {
			return ((ExtEventImpl)extEvent).getvEvent();
		} else {
			throw new IllegalArgumentException("We only accept ExtEvents generated by this class");
		}
	}

	private Calendar toCalendar(ExtCalendar extCalendar) {
		if (extCalendar instanceof ExtCalendarImpl) {
			return ((ExtCalendarImpl)extCalendar).getCalendar();
		} else {
			throw new IllegalArgumentException("We only accept ExtCalendars generated by this class");
		}
	}
	/**
	 * Helper to create the name of the ICS file we are to write
	 * @param filename
	 * @return
	 */
	private String generateFilePath(String filename) {
		StringBuilder sb = new StringBuilder();
		
		String base = sakaiProxy.getCalendarFilePath();
		sb.append(base);
		
		//add slash if reqd
		if(!StringUtils.endsWith(base, File.separator)) {
			sb.append(File.separator);
		}
		
		sb.append(filename);
		sb.append(".ics");
		return sb.toString();
	}

	private URI createMailURI(String email) {
		if (email == null || email.isEmpty()) {
			return URI.create("noemail");
		} else {
			return URI.create("mailto:" + email);
		}
	}

	/**
	 * init
	 */
	public void init() {
		log.info("init");
	}
	
	@Setter
	private SakaiProxy sakaiProxy;
	
}
