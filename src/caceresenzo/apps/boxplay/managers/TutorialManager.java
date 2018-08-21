package caceresenzo.apps.boxplay.managers;

import com.getkeepsafe.taptargetview.TapTargetSequence;

import caceresenzo.apps.boxplay.managers.XManagers.AbstractManager;
import caceresenzo.libs.json.JsonObject;
import caceresenzo.libs.json.parser.JsonException;
import caceresenzo.libs.json.parser.JsonParser;

public class TutorialManager extends AbstractManager {
	
	public static final String PREF_KEY_TUTORIAL_MAP = "tutorial_class";
	public static final int PROGRESS_NOTHING = -1;
	public static final int PROGRESS_EVERYTHING = 1000;
	
	private JsonObject finishedTutorials;
	
	@Override
	protected void initialize() {
		try {
			finishedTutorials = (JsonObject) new JsonParser().parse(getManagers().getPreferences().getString(PREF_KEY_TUTORIAL_MAP, "{}"));
		} catch (JsonException exception) {
			finishedTutorials = new JsonObject();
		}
	}
	
	public void executeActivityTutorial(final Tutorialable tutorialable) {
		if (tutorialable == null) {
			return;
		}
		
		if (finishedTutorials.containsKey(tutorialable.getClass().getSimpleName()) || finishedTutorials.getInteger(tutorialable.getClass().getSimpleName(), PROGRESS_NOTHING) == PROGRESS_EVERYTHING) {
			return;
		}
		
		try {
			if (tutorialable.getTapTargetSequence() != null) {
				tutorialable.getTapTargetSequence().start();
			}
		} catch (Exception exception) {
			;
		}
	}
	
	public void saveTutorialFinished(Tutorialable tutorialable) {
		if (tutorialable == null) {
			return;
		}
		
		finishedTutorials.put(tutorialable.getClass().getSimpleName(), PROGRESS_EVERYTHING);
		getManagers().getPreferences().edit().putString(PREF_KEY_TUTORIAL_MAP, finishedTutorials.toJsonString()).commit();
	}
	
	public void resetTutorials() {
		getManagers().getPreferences().edit().putString(PREF_KEY_TUTORIAL_MAP, "{}").commit();
		initialize();
	}
	
	public static interface Tutorialable {
		TapTargetSequence getTapTargetSequence();
	}
	
}