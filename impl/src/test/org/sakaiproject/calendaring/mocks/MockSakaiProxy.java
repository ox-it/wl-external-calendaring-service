package org.sakaiproject.calendaring.mocks;

import org.sakaiproject.calendaring.logic.SakaiProxy;

/**
 * Mock of SakaiProxy so we can call the main service API
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class MockSakaiProxy implements SakaiProxy {

	public static final String NO_EMAIL_ID = "noEmailPlease";

	@Override
	public String getCurrentUserId() {
		return "abc123";
	}

	@Override
	public boolean isSuperUser() {
		return false;
	}

	@Override
	public String getServerName() {
		return "server_xyz";
	}

	@Override
	public String getCalendarFilePath() {
		return "/tmp";
	}

	@Override
	public String getUserEmail(String uuid) {
		return uuid.equals(NO_EMAIL_ID) ? "" : uuid + "@email.com";
	}

	@Override
	public String getUserDisplayName(String uuid) {
		return "User " + uuid;
	}

	@Override
	public boolean isIcsEnabled() {
		return true;
	}

	@Override
	public boolean isCleanupEnabled() {
		return true;
	}

}
