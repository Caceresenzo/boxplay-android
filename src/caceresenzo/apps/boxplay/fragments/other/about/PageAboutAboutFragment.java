package caceresenzo.apps.boxplay.fragments.other.about;

import com.marcoscg.easyabout.EasyAboutFragment;
import com.marcoscg.easyabout.helpers.AboutItemBuilder;
import com.marcoscg.easyabout.items.AboutCard;
import com.marcoscg.easyabout.items.NormalAboutItem;
import com.marcoscg.easyabout.items.PersonAboutItem;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import caceresenzo.apps.boxplay.R;

public class PageAboutAboutFragment extends EasyAboutFragment {
	
	@Override
	protected void configureFragment(Context context, View rootView, Bundle savedInstanceState) {
		addCard(new AboutCard.Builder(context) //
				.addItem(AboutItemBuilder.generateAppTitleItem(context) //
						.setSubtitle(R.string.boxplay_other_about_about_application_author)) //
				.addItem(AboutItemBuilder.generateAppVersionItem(context, true) //
						.setIcon(R.drawable.icon_info_light)) //
				.build()); //
		
		addCard(new AboutCard.Builder(context) //
				.setTitle(R.string.boxplay_other_about_about_author) //
				.addItem(new PersonAboutItem.Builder(context) //
						.setTitle(R.string.boxplay_other_about_about_author_author_detail) //
						.setSubtitle(R.string.boxplay_other_about_about_author_author_location) //
						.setIcon(R.drawable.profile_caceresenzo) //
						.build()) //
				.addItem(AboutItemBuilder.generateLinkItem(context, getString(R.string.author_github)) //
						.setTitle(R.string.boxplay_other_about_about_author_github) //
						.setIcon(R.drawable.icon_social_github_black_24dp)) //
				.addItem(AboutItemBuilder.generateLinkItem(context, getString(R.string.application_website)) //
						.setTitle(R.string.boxplay_other_about_about_author_website) //
						.setIcon(R.drawable.icon_web_black_24dp)) //
				.addItem(AboutItemBuilder.generateEmailItem(context, getString(R.string.application_email)) //
						.setTitle(R.string.boxplay_other_about_about_author_email) //
						.setIcon(R.drawable.icon_mail_outline_dark)) //
				.build()); //
		
		addCard(new AboutCard.Builder(context) //
				.setTitle(R.string.boxplay_other_about_about_description) //
				.addItem(new NormalAboutItem.Builder(context) //
						.setTitle(R.string.boxplay_other_about_about_description_text) //
						.setIcon(R.drawable.icon_info_light) //
						.build()) //
				.build()); //
		
		String separator = getString(R.string.boxplay_other_about_about_team_format_separator, " ");
		String typeApplication = getString(R.string.boxplay_other_about_about_team_format_type_application);
		String typeApi = getString(R.string.boxplay_other_about_about_team_format_type_api);
		String typeUi = getString(R.string.boxplay_other_about_about_team_format_type_ui);
		String typeIcons = getString(R.string.boxplay_other_about_about_team_format_type_icons);
		String typeHosting = getString(R.string.boxplay_other_about_about_team_format_type_hosting);
		String typeEclipseFix = getString(R.string.boxplay_other_about_about_team_format_type_eclipse);
		String typeBecause = getString(R.string.boxplay_other_about_about_team_format_type_because);
		
		addCard(new AboutCard.Builder(context) //
				.setTitle(R.string.boxplay_other_about_about_team) //
				.addItem(AboutItemBuilder.generatePersonaLinkItem(context, "https://github.com/Caceresenzo") //
						.setTitle("Enzo CACERES, @Caceresenzo") //
						.setSubtitle(new StringBuilder() //
								.append(typeApplication) //
								.append(separator) //
								.append(typeUi) //
								.append(separator) //
								.append(typeHosting) //
								.toString()) //
						.setIcon(R.drawable.profile_caceresenzo) //
						.build()) //
				.addItem(AboutItemBuilder.generatePersonaLinkItem(context, "https://github.com/TheWhoosher") //
						.setTitle("Fanbien SENUT--SCHAPPACHER, @TheWhoosher") //
						.setSubtitle(new StringBuilder() //
								.append(typeApi) //
								.toString()) //
						.setIcon(R.drawable.profile_thewhoosher) //
						.build()) //
				.addItem(AboutItemBuilder.generatePersonaLinkItem(context, "https://github.com/thegostisdead") //
						.setTitle("Dorian HARDY, @thegostisdead") //
						.setSubtitle(new StringBuilder() //
								.append(typeHosting) //
								.toString()) //
						.setIcon(R.drawable.profile_thegostisdead) //
						.build()) //
				.addItem(new NormalAboutItem.Builder(context) //
						.setTitle("Quentin BOTTA, @valgrebon") //
						.setSubtitle(new StringBuilder() //
								.append(typeIcons) //
								.append(separator) //
								.append(typeUi) //
								.toString()) //
						.build()) //
				.addItem(new NormalAboutItem.Builder(context) //
						.setTitle("Jérémie BLERAUD") //
						.setSubtitle(new StringBuilder() //
								.append(typeBecause) //
								.toString()) //
						.build()) //
				.addItem(AboutItemBuilder.generatePersonaLinkItem(context, "https://github.com/dandar3") //
						.setTitle("@dandar3") //
						.setSubtitle(new StringBuilder() //
								.append(typeEclipseFix) //
								.toString()) //
						.setIcon(R.drawable.profile_dandar3) //
						.build()) //
				.build()); //
	}
	
}