package caceresenzo.apps.boxplay.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.helper.implementations.ApplicationHelper;
import caceresenzo.apps.boxplay.helper.implementations.CacheHelper;
import caceresenzo.apps.boxplay.helper.implementations.ImageHelper;
import caceresenzo.apps.boxplay.helper.implementations.LocaleHelper;
import caceresenzo.apps.boxplay.helper.implementations.MenuHelper;
import caceresenzo.apps.boxplay.helper.implementations.ViewHelper;

public class HelperManager {
	
	/* Manager */
	private BoxPlayApplication boxPlayApplication;
	
	/* Implementations */
	private ApplicationHelper applicationHelper;
	private CacheHelper cacheHelper;
	private ImageHelper imageHelper;
	private LocaleHelper localeHelper;
	private MenuHelper menuHelper;
	private ViewHelper viewHelper;
	
	/* Variables */
	private List<AbstractHelper> helpers;
	
	/* Constructor */
	public HelperManager(BoxPlayApplication boxPlayApplication) {
		this.boxPlayApplication = Objects.requireNonNull(boxPlayApplication);
		
		this.helpers = new ArrayList<>();
	}
	
	/**
	 * Initialize all helpers.
	 */
	public void initialize() {
		helpers.add(applicationHelper = new ApplicationHelper(boxPlayApplication));
		helpers.add(cacheHelper = new CacheHelper(boxPlayApplication));
		helpers.add(imageHelper = new ImageHelper(boxPlayApplication));
		helpers.add(localeHelper = new LocaleHelper(boxPlayApplication));
		helpers.add(menuHelper = new MenuHelper(boxPlayApplication));
		helpers.add(viewHelper = new ViewHelper(boxPlayApplication));
		
		for (AbstractHelper helper : helpers) {
			helper.initialize(this);
		}
	}
	
	public ApplicationHelper getApplicationHelper() {
		return applicationHelper;
	}
	
	public CacheHelper getCacheHelper() {
		return cacheHelper;
	}
	
	public ImageHelper getImageHelper() {
		return imageHelper;
	}
	
	public LocaleHelper getLocaleHelper() {
		return localeHelper;
	}
	
	public MenuHelper getMenuHelper() {
		return menuHelper;
	}
	
	public ViewHelper getViewHelper() {
		return viewHelper;
	}
	
}