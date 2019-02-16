package caceresenzo.apps.boxplay.application;

public class Constants {
	
	
	public interface ACTION {
		
	}
	
	public interface NOTIFICATION_CHANNEL {
		public static final String MAIN = "boxplay_main";
		public static final String SEARCH_AND_GO_UPDATE = "search_and_go_update";
	}
	
	public interface NOTIFICATION_ID {
		public static int BOXPLAY_FOREGROUND_SERVICE = 101;
	}
	
	public interface BROADCAST_ID {
		public static int BOXPLAY_ALARM_SERVICE = 1;
	}
	
	public interface REQUEST_ID {
		public static final int REQUEST_ID_UPDATE = 20;
		public static final int REQUEST_ID_VLC_VIDEO = 40;
		public static final int REQUEST_ID_VLC_VIDEO_URL = 41;
		public static final int REQUEST_ID_VLC_AUDIO = 42;
		public static final int REQUEST_ID_PERMISSION = 100;
	}
	
	public interface PROVIDER {
		public static final String FILEPROVIDER_AUTHORITY = "caceresenzo.apps.boxplay.provider";
	}
	
	public interface MANAGER {
		public static final long BACKGROUND_SERVICE_DEFAULT_FREQUENCY = 3600000L;
	}
	
}