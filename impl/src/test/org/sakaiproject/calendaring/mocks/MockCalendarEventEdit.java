package org.sakaiproject.calendaring.mocks;

import java.util.Collection;
import java.util.List;
import java.util.Stack;

import lombok.Data;

import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.calendar.api.RecurrenceRule;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Data
public class MockCalendarEventEdit implements CalendarEventEdit {

	private String location;
	private String description;
	private TimeRange range;
	private RecurrenceRule exclusionRule;
	private RecurrenceRule recurrenceRule;
	private String creator;
	private String displayName;
	private String url;
	private String id;
	private String type;
	private String siteName;
	private String calendarReference;
	private String descriptionFormatted;
	private String modifiedBy;
	private String reference;
	protected ResourcePropertiesEdit m_properties;
	
	
	/* mocked out methods. Note that some don't do anything so you need to check that. */
	
	public MockCalendarEventEdit() {
		m_properties = new BaseResourcePropertiesEdit();
	}
	
	
	@Override
	public EventAccess getAccess() {
		return null;
	}

	@Override
	public String getField(String name) {
		return m_properties.getPropertyFormatted(name);
	}

	@Override
	public Collection getGroupObjects() {
		return null;
	}

	@Override
	public String getGroupRangeForDisplay(Calendar arg0) {
		return null;
	}

	@Override
	public Collection getGroups() {
		return null;
	}

	

	@Override
	public boolean isUserOwner() {
		return false;
	}

	
	@Override
	public ResourceProperties getProperties() {
		return null;
	}

	
	@Override
	public String getReference(String arg0) {
		return null;
	}

	
	@Override
	public String getUrl(String arg0) {
		return null;
	}

	@Override
	public Element toXml(Document arg0, Stack arg1) {
		return null;
	}

	@Override
	public int compareTo(Object o) {
		return 0;
	}

	@Override
	public List<Reference> getAttachments() {
		return null;
	}

	@Override
	public ResourcePropertiesEdit getPropertiesEdit() {
		return m_properties;
	}

	@Override
	public boolean isActiveEdit() {
		return false;
	}

	@Override
	public void addAttachment(Reference arg0) {
	}

	@Override
	public void clearAttachments() {
	}

	@Override
	public void removeAttachment(Reference arg0) {
	}

	@Override
	public void replaceAttachments(List arg0) {
	}

	@Override
	public void clearGroupAccess() throws PermissionException {
	}

	@Override
	public void setCreator() {		
	}

	@Override
	public void setField(String name, String value) {	
		if (value == null){
			m_properties.removeProperty(name);
		}
		else {
			m_properties.addProperty(name, value);
		}
	}

	@Override
	public void setGroupAccess(Collection arg0, boolean arg1) throws PermissionException {
	}

	@Override
	public void setModifiedBy() {		
	}

	/* special methods */
	public void setId(String uuid) {
		this.id=uuid;
	}
	
	public void setCreator(String creatorUuid) {
		this.creator=creatorUuid;
	}

	@Override
	public String getReference() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUrl() {
		// TODO Auto-generated method stub
		return null;
	}


}
