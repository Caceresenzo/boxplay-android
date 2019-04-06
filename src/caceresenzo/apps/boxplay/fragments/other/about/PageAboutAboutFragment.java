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
						.setIcon(R.drawable.icon_info_white_24dp)) //
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
						.setIcon(R.drawable.icon_mail_outline_white_24dp)) //
				.build()); //
		
		addCard(new AboutCard.Builder(context) //
				.setTitle(R.string.boxplay_other_about_about_description) //
				.addItem(new NormalAboutItem.Builder(context) //
						.setTitle(R.string.boxplay_other_about_about_description_text) //
						.build()) //
				.build()); //
		
		String separator = getString(R.string.boxplay_other_about_about_team_format_separator, " ");
		String typeApplication = getString(R.string.boxplay_other_about_about_team_format_type_application);
		String typeApi = getString(R.string.boxplay_other_about_about_team_format_type_api);
		String typeUi = getString(R.string.boxplay_other_about_about_team_format_type_ui);
		String typeIcons = getString(R.string.boxplay_other_about_about_team_format_type_icons);
		String typeHosting = getString(R.string.boxplay_other_about_about_team_format_type_hosting);
		String typeCreativeAssistant = getString(R.string.boxplay_other_about_about_team_format_type_creative_assistant);
		
		addCard(new AboutCard.Builder(context) //
				.setTitle(R.string.boxplay_other_about_about_team) //
				.addItem(AboutItemBuilder.generatePersonaLinkItem(context, "https://github.com/Caceresenzo") //
						.setTitle("@Caceresenzo") //
						.setSubtitle(new StringBuilder() //
								.append(typeApplication) //
								.append(separator) //
								.append(typeUi) //
								.append(separator) //
								.append(typeHosting) //
								.append(separator) //
								.append(typeApi) //
								.toString()) //
						.setIcon(R.drawable.profile_caceresenzo) //
						.build()) //
				.addItem(new PersonAboutItem.Builder(context) //
						.setTitle("@valgrebon") //
						.setSubtitle(new StringBuilder() //
								.append(typeIcons) //
								.append(separator) //
								.append(typeUi) //
								.toString()) //
						.setIcon(R.drawable.profile_valgrebon) //
						.build()) //
				.addItem(new PersonAboutItem.Builder(context) //
						.setTitle("@Paradox") //
						.setSubtitle(new StringBuilder() //
								.append(typeCreativeAssistant) //
								.toString()) //
						.setIcon(R.drawable.profile_paradox) //
						.build()) //
				.build()); //
	}
	
}