package caceresenzo.apps.boxplay.fragments.other.about;

import android.support.design.widget.TabLayout;
import android.view.View;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.BoxPlayActivity;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.fragments.BaseTabLayoutFragment;
import caceresenzo.apps.boxplay.fragments.utils.ViewFragment;
import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutFragment extends BaseTabLayoutFragment {
	
	public static final int PAGE_ABOUT = 0;
	public static final int PAGE_HOSTING = 1;
	public static final int PAGE_CHANGELOG = 2;
	public static final int PAGE_LIBRARIES = 3;
	
	@Override
	protected void initialize() {
		addFragment(new ViewFragment(createAboutView()), R.string.boxplay_other_about_about);
		addFragment(new PageAboutHostingFragment(), R.string.boxplay_other_about_hosting);
		addFragment(new PageAboutChangeLogFragment(), R.string.boxplay_other_about_changelog);
		addFragment(new PageAboutLibrariesFragment(), R.string.boxplay_other_about_libraries);
		
		setTabMode(TabLayout.MODE_SCROLLABLE);
	}
	
	@Override
	protected int getMenuItemIdByPageId(int pageId) {
		return R.id.drawer_boxplay_other_about;
	}
	
	private View createAboutView() {
		String teamFormat = getString(R.string.boxplay_other_about_group_team_format);
		
		String separator = getString(R.string.boxplay_other_about_group_team_format_separator, " ");
		String typeApplication = getString(R.string.boxplay_other_about_group_team_format_type_application);
		String typeApi = getString(R.string.boxplay_other_about_group_team_format_type_api);
		String typeUi = getString(R.string.boxplay_other_about_group_team_format_type_ui);
		String typeIcons = getString(R.string.boxplay_other_about_group_team_format_type_icons);
		String typeHosting = getString(R.string.boxplay_other_about_group_team_format_type_hosting);
		String typeEclipseFix = getString(R.string.boxplay_other_about_group_team_format_type_eclipse);
		String typeBecause = getString(R.string.boxplay_other_about_group_team_format_type_because);
		
		return new AboutPage(getActivity()) //
				.isRTL(false) //
				.setImage(R.drawable.boxplay_easter_egg) //
				.setBackgroundColor(R.color.colorBackground) //
				.setDescription(getString(R.string.boxplay_other_about_description)) //
				
				/*
				 * App information
				 */
				.addGroup(getString(R.string.boxplay_other_about_group_app_information)) //
				.addItem( //
						new Element(getString(R.string.boxplay_other_about_group_app_information_version, BoxPlayApplication.getVersion().get() + (BoxPlayActivity.BUILD_DEBUG ? " DEBUG-BUILD" : "")), null) //
								.setBackgroundColor(R.color.colorBackground)) //
				
				/*
				 * Team / Programmer
				 */
				.addGroup(getString(R.string.boxplay_other_about_group_team_programmer)) //
				.addItem( //
						new Element(String.format(teamFormat, "Enzo CACERES, *Caceresenzo", typeApplication), null) //
								.setBackgroundColor(R.color.colorBackground)) //
				.addItem(new Element(String.format(teamFormat, "Fanbien SENUT--SCHAPPACHER, *TheWhoosher", typeApi), null) //
						.setBackgroundColor(R.color.colorBackground)) //
				
				/*
				 * Team / Designer
				 */
				.addGroup(getString(R.string.boxplay_other_about_group_team_designer)) //
				.addItem( //
						new Element(String.format(teamFormat, "Enzo CACERES, *Caceresenzo", typeUi), null) //
								.setBackgroundColor(R.color.colorBackground)) //
				.addItem( //
						new Element(String.format(teamFormat, "Quentin BOTTA, *valgrebon", typeIcons + separator + typeUi), null) //
								.setBackgroundColor(R.color.colorBackground)) //
				
				/*
				 * Team / Supporter
				 */
				.addGroup(getString(R.string.boxplay_other_about_group_team_supporter)) //
				.addItem( //
						new Element(String.format(teamFormat, "Enzo CACERES, *Caceresenzo", typeHosting), null) //
								.setBackgroundColor(R.color.colorBackground)) //
				.addItem( //
						new Element(String.format(teamFormat, "Dorian HARDY, *thegostisdead", typeHosting), null) //
								.setBackgroundColor(R.color.colorBackground)) //
				.addItem( //
						new Element(String.format(teamFormat, "Jérémie BLERAUD", typeBecause), null) //
								.setBackgroundColor(R.color.colorBackground)) //
				
				/*
				 * Team / helper
				 */
				.addGroup(getString(R.string.boxplay_other_about_group_team_helper)) //
				.addItem( //
						new Element(String.format(teamFormat, "*dandar3", typeEclipseFix), null) //
								.setBackgroundColor(R.color.colorBackground)) //
				
				/*
				 * Finish
				 */
				.create() //
		; //
	}
	
	public AboutFragment withAbout() {
		return (AboutFragment) withPage(PAGE_ABOUT);
	}
	
	public AboutFragment withHosting() {
		return (AboutFragment) withPage(PAGE_HOSTING);
	}
	
	public AboutFragment withChangeLog() {
		return (AboutFragment) withPage(PAGE_CHANGELOG);
	}
	
	public AboutFragment withLibraries() {
		return (AboutFragment) withPage(PAGE_LIBRARIES);
	}
	
	public static AboutFragment getAboutFragment() {
		return (AboutFragment) INSTANCE;
	}
	
}