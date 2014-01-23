package org.sakaiproject.calendaring;

import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Version;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.calendaring.api.*;
import org.sakaiproject.calendaring.mocks.MockCalendarEventEdit;
import org.sakaiproject.calendaring.mocks.MockTimeService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.user.api.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Test the changes to implement REQUEST and CANCEL methods
 *
 * @author Ben Holmes
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/test-components.xml"})
public class CalendarWithMethodTest {

    private final String EVENT_NAME = "A new event";
    private final String LOCATION = "Building 1";
    private final String DESCRIPTION = "This is a sample event.";
    private final String CREATOR = "steve";
    private final long START_TIME = 1336136400; // 4/May/2012 13:00 GMT
    private final long END_TIME = 1336140000; // 4/May/2012 14:00 GMT
    //for the test classes we can still use annotation based injection
    @Resource(name = "org.sakaiproject.calendaring.api.ExternalCalendaringService")
    private ExternalCalendaringService service;
    @Autowired
    private ApplicationContext applicationContext;
    private List<User> users;

    @Before
    public void setupData() {
        users = new ArrayList<User>();
        for (int i = 0; i < 5; i++) {
            org.sakaiproject.mock.domain.User u = new org.sakaiproject.mock.domain.User(null, "user" + i, "user" + i, "user" + i, "user" + i + "@email.com", "User", String.valueOf(i),
                    null, null, null, null, null, null, null, null, null, null);
            users.add(u);
        }
    }

    @Test
    public void testContext() {
        Assert.assertNotNull(applicationContext.getBean("org.sakaiproject.calendaring.logic.SakaiProxy"));
        Assert.assertNotNull(applicationContext.getBean("org.sakaiproject.calendaring.api.ExternalCalendaringService"));
    }

    /**
     * A calendar with REQUEST method is not valid if it has no attendees
     */
    @Test(expected=ValidationException.class)
    public void testRequestCalendarWithoutAttendees() {
        CalendarEvent event = generateEvent();
        ExtEvent extEvent = service.createEvent(event);
        ExtCalendar extCalendar = service.createCalendar(Collections.singletonList(extEvent), "REQUEST");
    }

    private CalendarEventEdit generateEvent(List<ical4j>) {

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

        return edit;
    }

}
