package org.sakaiproject.calendaring;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Version;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.calendaring.api.ExternalCalendaringService;
import org.sakaiproject.calendaring.mocks.MockCalendarEventEdit;
import org.sakaiproject.calendaring.mocks.MockTimeService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserAlreadyDefinedException;
import org.sakaiproject.user.api.UserIdInvalidException;
import org.sakaiproject.user.api.UserPermissionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test class for the ExternalCalendaringService
 * @author Steve Swinsburg (Steve.swinsburg@gmail.com)
 *
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/test-components.xml"})
public class ExternalCalendaringServiceTest {

	private final String EVENT_NAME = "A new event";
	private final String LOCATION = "Building 1";
	private final String DESCRIPTION = "This is a sample event.";
	private final String CREATOR="steve";
	private final long START_TIME = 1336136400; // 4/May/2012 13:00 GMT
	private final long END_TIME = 1336140000; // 4/May/2012 14:00 GMT

	//for the test classes we can still use annotation based injection
	@Resource(name="org.sakaiproject.calendaring.api.ExternalCalendaringService")
	private ExternalCalendaringService service;
	
	@Autowired
	private ApplicationContext applicationContext;

	
	private List<User> users;
	
	
	@Before
	public void setupData() {
		users = generateUsers();
	}
	
	
	
	@Test
	public void testContext() {
		Assert.assertNotNull(applicationContext.getBean("org.sakaiproject.calendaring.logic.SakaiProxy"));
		Assert.assertNotNull(applicationContext.getBean("org.sakaiproject.calendaring.api.ExternalCalendaringService"));
	}
	
	/**
	 * Ensure the event generation works and returns a usable object. Internal test method, but useful.
	 */
	@Test
	public void testGeneratingEvent() {
		
		//generate new event
		CalendarEvent event = generateEvent();
		
		Assert.assertNotNull(event);
		
		//check attributes of the event are set correctly
		Assert.assertEquals(LOCATION, event.getLocation());
		Assert.assertEquals(DESCRIPTION, event.getDescription());
		Assert.assertEquals(EVENT_NAME, event.getDisplayName());
		Assert.assertEquals(CREATOR, event.getCreator());
		
	}

	
	@Test
	public void testGeneratingVEvent() {
		
		//generate new event
		CalendarEvent event = generateEvent();
		
		//create vevent
		net.fortuna.ical4j.model.component.VEvent vevent = service.createEvent(event);
		
		System.out.println("testGeneratingVEvent");
		System.out.println("####################");
		System.out.println(vevent);
		
		Assert.assertNotNull(vevent);
		
		//TODO check the attributes of the vevent
		//Assert.assertEquals(EVENT_NAME, vevent.getProperty("SUMMARY"));
		//Assert.assertEquals(LOCATION, vevent.getProperty("LOCATION"));
		
	}
	
	@Test
	public void testGeneratingVEventWithOverridenUuid() {
		
		//generate new event
		CalendarEventEdit event = generateEvent();
		
		//set the field that overrides the uuid
		event.setField("vevent_uuid", "XXX");
		
		//create vevent
		net.fortuna.ical4j.model.component.VEvent vevent = service.createEvent(event);
		
		System.out.println("testGeneratingVEventWithOverridenUuid");
		System.out.println("#####################################");
		System.out.println(vevent);
		
		Assert.assertNotNull(vevent);
		
		//should be equal to show it has been overriden
		Assert.assertEquals("XXX", vevent.getUid().getValue());
		
		
		//TODO check the attributes of the vevent
		//Assert.assertEquals(EVENT_NAME, vevent.getProperty("SUMMARY"));
		//Assert.assertEquals(LOCATION, vevent.getProperty("LOCATION"));
		
	}
	
	@Test
	public void testGeneratingVEventWithAttendees() {
		
		//generate new event
		CalendarEvent event = generateEvent();
		
		//create vevent
		net.fortuna.ical4j.model.component.VEvent vevent = service.createEvent(event, users);
		
		System.out.println("testGeneratingVEventWithAttendees");
		System.out.println("#################################");
		System.out.println(vevent);
		
		Assert.assertNotNull(vevent);
		
		//TODO check the attributes of the vevent
		//Assert.assertEquals(EVENT_NAME, vevent.getProperty("SUMMARY"));
		//Assert.assertEquals(LOCATION, vevent.getProperty("LOCATION"));
		
		//TODO check attendees
		
	}
	
	@Test
	public void testUpdatingVEventWithAttendees() {
		
		//generate new event
		CalendarEvent event = generateEvent();
		
		//create vevent
		net.fortuna.ical4j.model.component.VEvent vevent = service.createEvent(event);
		
		System.out.println("testUpdatingVEventWithAttendees");
		System.out.println("#################################");
		System.out.println("Before:");
		System.out.println(vevent);
		
		net.fortuna.ical4j.model.component.VEvent veventUpdated = service.addAttendeesToEvent(vevent, users);
		System.out.println("After:");
		System.out.println(vevent);
		
		Assert.assertNotNull(vevent);
		
		//TODO check the attributes of the vevent
		//Assert.assertEquals(EVENT_NAME, vevent.getProperty("SUMMARY"));
		//Assert.assertEquals(LOCATION, vevent.getProperty("LOCATION"));
		
		//TODO check attendees
		
	}
	
	@Test
	public void testCancellingVEvent() {
		
		//generate new event
		CalendarEvent event = generateEvent();
		
		//create vevent
		net.fortuna.ical4j.model.component.VEvent vevent = service.createEvent(event);
		
		//set it to cancelled
		VEvent cancelled = service.cancelEvent(vevent);
		
		System.out.println("testCancellingVEvent");
		System.out.println("####################");
		System.out.println(cancelled);
		
		Assert.assertNotNull(cancelled);
		
		//TODO check the attributes of the vevent
		//Assert.assertEquals(EVENT_NAME, vevent.getProperty("SUMMARY"));
		//Assert.assertEquals(LOCATION, vevent.getProperty("LOCATION"));
		
	}
	
	/**
	 * Ensure we can get a ical4j Calendar from the generated event.
	 */
	@Test
	public void testGeneratingCalendar() {
		
		//generate new event
		CalendarEvent event = generateEvent();
		
		//create vevent
		net.fortuna.ical4j.model.component.VEvent vevent = service.createEvent(event);
		
		//create calendar from vevent
		net.fortuna.ical4j.model.Calendar calendar = service.createCalendar(Collections.singletonList(vevent));
		
		System.out.println("testGeneratingCalendar");
		System.out.println("######################");
		System.out.println(calendar);
		
		Assert.assertNotNull(calendar);
		
		//check attributes of the ical4j calendar are what we expect and match those in the event
		Assert.assertEquals(Version.VERSION_2_0, calendar.getVersion());
		Assert.assertEquals(CalScale.GREGORIAN, calendar.getCalendarScale());
		
	}
	
	/**
	 * Ensure we can get a ical4j Calendar from the list of vevents.
	 */
	@Test
	public void testGeneratingCalendarWithMultipleVEvents() {
				
		//create list of vevents
		List<net.fortuna.ical4j.model.component.VEvent> vevents = new ArrayList<net.fortuna.ical4j.model.component.VEvent>();
	
		for(int i=0;i<10;i++) {
			vevents.add(service.createEvent(generateEvent()));
		}
		
		//create calendar from vevent
		net.fortuna.ical4j.model.Calendar calendar = service.createCalendar(vevents);
		
		System.out.println("testGeneratingCalendarWithMultipleVEvents");
		System.out.println("#########################################");
		System.out.println(calendar);
		
		Assert.assertNotNull(calendar);
		
		//check attributes of the ical4j calendar are what we expect and match those in the event
		Assert.assertEquals(Version.VERSION_2_0, calendar.getVersion());
		Assert.assertEquals(CalScale.GREGORIAN, calendar.getCalendarScale());
		
		//TODO check count of vevents
		
	}
	
	@Test
	public void testGeneratingCalendarWithNullList() {
		
		//create calendar with null
		net.fortuna.ical4j.model.Calendar calendar = service.createCalendar(null);
		
		System.out.println("testGeneratingCalendarWithNullList");
		System.out.println("##################################");
		
		//should be null
		Assert.assertNull(calendar);
				
	}
	
	@Test
	public void testGeneratingCalendarWithEmptyList() {
		
		//create calendar with null
		net.fortuna.ical4j.model.Calendar calendar = service.createCalendar(Collections.EMPTY_LIST);
		
		System.out.println("testGeneratingCalendarWithEmptyList");
		System.out.println("###################################");
		
		//should be null
		Assert.assertNull(calendar);
				
	}
	
	
	@Test
	public void testCreatingFile() {
		
		//generate new event
		CalendarEvent event = generateEvent();
		
		//create vevent
		net.fortuna.ical4j.model.component.VEvent vevent = service.createEvent(event);
				
		//create calendar from vevent
		net.fortuna.ical4j.model.Calendar calendar = service.createCalendar(Collections.singletonList(vevent));
				
		String path = service.toFile(calendar);
		
		System.out.println("testCreatingFile");
		System.out.println("################");
		System.out.println(path);
		
		Assert.assertNotNull(path);
		
		//now see if the file actually exists
		File f = new File(path);
		Assert.assertTrue(f.exists());
		
		
	}
	
	/**
	 * Helper to generate an event. NOT A TEST METHOD
	 * @return
	 */
	private CalendarEventEdit generateEvent() {
		
		MockCalendarEventEdit edit = new MockCalendarEventEdit();
		
		edit.setDisplayName(EVENT_NAME);
		edit.setLocation(LOCATION);
		edit.setDescription(DESCRIPTION);
		edit.setId(UUID.randomUUID().toString());
		edit.setCreator(CREATOR);
		
		TimeService timeService = new MockTimeService();
		Time start = timeService.newTime(START_TIME);
		Time end = timeService.newTime(END_TIME);
		TimeRange timeRange = timeService.newTimeRange(start, end, true, false);
		
		edit.setRange(timeRange);
		
		//TODO set recurrencerule, then add to test above

		
		return edit;
	}
	
	/**
	 * Helper to generate a list of users. NOT A TEST METHOD
	 * @return
	 * @throws UserPermissionException 
	 * @throws UserAlreadyDefinedException 
	 * @throws UserIdInvalidException 
	 */
	private List<User> generateUsers(){
		List<User> users = new ArrayList<User>();
		
		for(int i=0;i<5;i++) {
			
			org.sakaiproject.mock.domain.User u = new org.sakaiproject.mock.domain.User(null, "user"+i, "user"+i, "user"+i, "user"+i+"@email.com", "User", String.valueOf(i),
					null, null, null, null, null,null,null,null,null,null);
			
			users.add(u);
		}
		
		return users;
	}
	
}
