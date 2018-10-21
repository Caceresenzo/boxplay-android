package caceresenzo.apps.boxplay.application;

public class Constants {
	
	
	public interface ACTION {
		
	}
	
	public interface NOTIFICATION_CHANNEL {
		public static final String ANDROID_CHANNEL_ID = "channel";
	}
	
	public interface NOTIFICATION_ID {
		public static int BOXPLAY_FOREGROUND_SERVICE = 101;
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