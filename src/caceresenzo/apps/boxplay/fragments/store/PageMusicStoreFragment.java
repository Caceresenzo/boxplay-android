package caceresenzo.apps.boxplay.fragments.store;

import static caceresenzo.apps.boxplay.fragments.store.PageMusicStoreFragment.MusicStoreSubCategory.ALBUMS;
import static caceresenzo.apps.boxplay.fragments.store.PageMusicStoreFragment.MusicStoreSubCategory.RANDOM;
import static caceresenzo.apps.boxplay.fragments.store.PageMusicStoreFragment.MusicStoreSubCategory.RECOMMENDED;
import static caceresenzo.apps.boxplay.fragments.store.PageMusicStoreFragment.MusicStoreSubCategory.YOURLIST;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.BoxPlayActivity;
import caceresenzo.apps.boxplay.activities.MusicActivity;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.libs.boxplay.models.element.implementations.MusicElement;
import caceresenzo.libs.boxplay.models.store.music.MusicAlbum;
import caceresenzo.libs.boxplay.models.store.music.MusicFile;
import caceresenzo.libs.boxplay.models.store.music.MusicGroup;
import caceresenzo.libs.boxplay.models.store.music.enums.MusicGenre;
import caceresenzo.libs.random.Randomizer;

public class PageMusicStoreFragment extends StorePageFragment {
	
	private MusicStorePopulator musicStorePopulator = new MusicStorePopulator();
	
	@Override
	protected void initializeViews(View view) {
		;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		onUserRefresh();
	}
	
	@Override
	public void onUserRefresh() {
		BoxPlayApplication.getManagers().getDataManager().fetchData(true);
	}
	
	@Override
	public void callDataUpdater(boolean newContent) {
		rowListItems.clear();
		
		try {
			musicStorePopulator.populate();
		} catch (Exception exception) {
			;
		}
		
		if (newContent) {
			BoxPlayApplication.getBoxPlayApplication().snackbar(R.string.boxplay_store_data_downloading_new_content, Snackbar.LENGTH_LONG);
		}
		
		finishUpdate(newContent);
	}
	
	@Override
	public StoreSearchHandler<MusicElement> createSearchHandler() {
		return new StoreSearchHandler<MusicElement>() {
			@Override
			public boolean onQueryTextChange(String newText) {
				if (recyclerView != null) {
					List<MusicElement> filteredModelList = filter(newText);
					
					recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
					recyclerView.setAdapter(new MusicListRowViewAdapter(filteredModelList));
				}
				return true;
			}
			
			@Override
			public List<MusicElement> filter(String query) {
				query = query.toLowerCase();
				boolean groupStringContains = false, albumStringContains = false;
				
				final List<MusicElement> filteredGroupList = new ArrayList<>();
				
				for (MusicGroup group : BoxPlayApplication.getManagers().getMusicManager().getGroups()) {
					if (group.getDisplay().toLowerCase().contains(query)) {
						groupStringContains = true;
					}
					
					for (MusicAlbum album : group.getAlbums()) {
						if (album.getTitle().toLowerCase().contains(query)) {
							albumStringContains = groupStringContains = true;
						}
						
						for (MusicFile music : album.getMusics()) {
							if (music.getTitle().toLowerCase().contains(query)) {
								albumStringContains = groupStringContains = true;
								
								if (query.length() >= 1) {
									filteredGroupList.add(music);
								}
							}
						}
						
						if (albumStringContains) {
							filteredGroupList.add(album);
						}
					}
					
					if (groupStringContains) {
						filteredGroupList.add(group);
					}
				}
				return filteredGroupList;
			}
		};
	}
	
	class MusicStorePopulator extends StorePopulator {
		private Map<Object, List<MusicElement>> population;
		
		public void populate() {
			population = new HashMap<Object, List<MusicElement>>();
			List<MusicElement> yourWatchingList = new ArrayList<MusicElement>();
			
			List<String> userGenres = new ArrayList<String>();
			
			Set<String> userGenresStringSet = BoxPlayApplication.getManagers().getPreferences().getStringSet(getString(R.string.boxplay_other_settings_store_music_pref_my_genre_key), null);
			if (userGenresStringSet != null) {
				userGenres = new ArrayList<String>(userGenresStringSet);
			}
			
			if (userGenres.isEmpty()) {
				rowListItems.add(new ErrorRowItem(getString(R.string.boxplay_store_music_fragment_error_no_genre_selected_title), getString(R.string.boxplay_store_music_fragment_error_no_genre_selected_content), getString(R.string.boxplay_store_music_fragment_error_no_genre_selected_button_settings), getString(R.string.boxplay_store_music_fragment_error_no_genre_selected_button_selector), new OnClickListener() {
					@Override
					public void onClick(View view) {
						BoxPlayActivity.getBoxPlayActivity().forceFragmentPath(R.id.drawer_boxplay_other_settings);
					}
				}, new OnClickListener() {
					@Override
					public void onClick(View view) {
						showMusicGenrePreferenceDialog();
					}
				}));
			}
			
			// PREPARE LIST
			for (MusicStoreSubCategory category : MusicStoreSubCategory.values()) {
				if (category.equals(YOURLIST)) {
					continue;
				}
				population.put(category, new ArrayList<MusicElement>());
			}
			
			for (MusicGenre genre : MusicGenre.values()) {
				if (userGenres.contains(genre.toString())) {
					population.put(genre, new ArrayList<MusicElement>());
				}
			}
			// END
			
			for (MusicGroup group : BoxPlayApplication.getManagers().getMusicManager().getGroups()) {
				if (group == null) {
					continue;
				}
				
				if (!group.getAlbums().isEmpty()) {
					for (MusicAlbum album : group.getAlbums()) {
						for (MusicGenre genre : MusicGenre.values()) {
							if (album.getGenres().contains(genre)) {
								populate(genre, group);
								populate(genre, album);
							}
						}
						
						// for (MusicFile music : album.getMusics()) {
						// TODO: isInList()
						// }
					}
					
					for (MusicAlbum album : group.getAlbums()) {
						populate(ALBUMS, album);
					}
				}
				
				// if (musicGroup.isWatching()) {
				// yourWatchingList.add(musicGroup);
				// }
				if (group.isRecommended()) {
					populate(RECOMMENDED, group);
				}
				if (Randomizer.nextRangeInt(0, 10) == 5) {
					populate(RANDOM, group);
				}
			}
			
			if (!yourWatchingList.isEmpty()) {
				rowListItems.add(new TitleRowItem(YOURLIST));
				if (yourWatchingList.size() == 1) {
					rowListItems.add(new MusicElementRowItem(yourWatchingList.get(0)));
				} else {
					rowListItems.add(new MusicListRowItem(yourWatchingList));
				}
			}
			
			List<Object> keys = new ArrayList<Object>(population.keySet());
			Collections.shuffle(keys);
			for (Object category : keys) {
				if (!population.get(category).isEmpty()) {
					// rowListItems.add(new TitleRowItem(category));
					
					String headingTitle = BoxPlayApplication.getViewHelper().enumToStringCacheTranslation(category);
					
					if (category.equals(RANDOM) || population.get(category).size() < 2) {
						rowListItems.add(new MusicElementRowItem(population.get(category).get(0)).configurate(new RowListItemConfig().title(headingTitle)));
					} else {
						rowListItems.add(new MusicListRowItem(population.get(category)).configurate(new RowListItemConfig().title(headingTitle)));
					}
				}
			}
			
			if (!userGenres.isEmpty()) {
				rowListItems.add(new ErrorRowItem(null, getString(R.string.boxplay_store_music_fragment_error_genre_selector_content), getString(R.string.boxplay_store_music_fragment_error_genre_selector_button_change), new OnClickListener() {
					@Override
					public void onClick(View view) {
						showMusicGenrePreferenceDialog();
					}
				}));
			}
		}
		
		private void populate(Object category, MusicElement element) {
			if (population.containsKey(category) && !population.get(category).contains(element)) {
				population.get(category).add(element);
			}
		}
		
		public void showMusicGenrePreferenceDialog() {
			final String[] availableGenreChoices = getResources().getStringArray(R.array.boxplay_other_settings_store_music_pref_my_genre_values);
			final int length = availableGenreChoices.length;
			final boolean[] checkedItems = new boolean[availableGenreChoices.length];
			final String[] genreChoices = new String[availableGenreChoices.length];
			List<String> userGenres = new ArrayList<String>();
			
			Set<String> userGenresStringSet = BoxPlayApplication.getManagers().getPreferences().getStringSet(getString(R.string.boxplay_other_settings_store_music_pref_my_genre_key), null);
			if (userGenresStringSet != null) {
				userGenres = new ArrayList<String>(userGenresStringSet);
			}
			
			for (int i = 0; i < length; i++) {
				MusicGenre targetGenre = MusicGenre.fromString(availableGenreChoices[i]);
				
				if (targetGenre.isNotUnknown()) {
					genreChoices[i] = BoxPlayApplication.getViewHelper().enumToStringCacheTranslation(targetGenre);
					checkedItems[i] = (userGenres.size() != 0 && userGenres.contains(availableGenreChoices[i]));
				}
				
				if (genreChoices[i] == null) {
					genreChoices[i] = "<empty>";
				}
			}
			
			AlertDialog dialog = new AlertDialog.Builder(getContext()) //
					.setTitle(getString(R.string.boxplay_other_settings_store_music_pref_my_genre_title)) //
					.setMultiChoiceItems(genreChoices, checkedItems, new OnMultiChoiceClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which, boolean isChecked) {
							checkedItems[which] = isChecked;
						}
					}) //
					.setPositiveButton(getString(R.string.boxplay_other_settings_store_music_pref_my_genre_dialog_ok), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							List<String> newGenres = new ArrayList<String>();
							for (int i = 0; i < length; i++) {
								if (checkedItems[i]) {
									newGenres.add(availableGenreChoices[i]);
								}
							}
							Set<String> set = new HashSet<String>(newGenres);
							BoxPlayApplication.getManagers().getPreferences().edit().putStringSet(getString(R.string.boxplay_other_settings_store_music_pref_my_genre_key), set).commit();
							
							swipeRefreshLayout.setRefreshing(true);
							callDataUpdater(false);
						}
					}).create(); //
			
			dialog.show()
			//
			;
		}
	}
	
	public static enum MusicStoreSubCategory {
		YOURLIST, RECOMMENDED, ALBUMS, RANDOM, RELEASE;
	}
	
	/*
	 * Element
	 */
	protected static class MusicElementRowViewAdapter extends RecyclerView.Adapter<MusicElementRowViewHolder> {
		private MusicElement element;
		
		public MusicElementRowViewAdapter(MusicElement element) {
			this.element = element;
		}
		
		@Override
		public MusicElementRowViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_store_page_music_element_cardview, viewGroup, false);
			return new MusicElementRowViewHolder(view);
		}
		
		@Override
		public void onBindViewHolder(MusicElementRowViewHolder viewHolder, int position) {
			viewHolder.bind(element);
		}
		
		@Override
		public int getItemCount() {
			return 1;
		}
	}
	
	protected static class MusicElementRowViewHolder extends RecyclerView.ViewHolder {
		private View view;
		private TextView titleTextView, subtitleTextView;
		private ImageView thumbnailImageView;
		
		public MusicElementRowViewHolder(View itemView) {
			super(itemView);
			
			view = itemView;
			titleTextView = (TextView) itemView.findViewById(R.id.item_store_page_music_element_layout_textview_title);
			subtitleTextView = (TextView) itemView.findViewById(R.id.item_store_page_music_element_layout_textview_subtitle);
			thumbnailImageView = (ImageView) itemView.findViewById(R.id.item_store_page_music_element_layout_imageview_thumbnail);
		}
		
		public void bind(MusicElement item) {
			if (item instanceof MusicGroup) {
				final MusicGroup group = (MusicGroup) item;
				
				titleTextView.setText(group.getDisplay());
				
				if (group.getAlbums() != null) {
					int totalAlbum = 0, totalSong = 0;
					
					totalAlbum += group.getAlbums().size();
					for (MusicAlbum album : group.getAlbums()) {
						if (album.getMusics() != null) {
							totalSong += album.getMusics().size();
						}
					}
					String sForAlbum = totalAlbum > 1 ? "s" : "", sForSong = totalSong > 1 ? "s" : "";
					subtitleTextView.setText(BoxPlayApplication.getBoxPlayApplication().getString(R.string.boxplay_store_music_card_info, totalAlbum, sForAlbum, totalSong, sForSong));
					titleTextView.setVisibility(View.VISIBLE);
				} else {
					titleTextView.setVisibility(View.GONE);
				}
				
				if (group.getImageUrl() != null) {
					BoxPlayApplication.getViewHelper().downloadToImageView(thumbnailImageView, group.getImageUrl());
				}
				
				view.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						MusicActivity.start(group);
					}
				});
				
				view.setOnLongClickListener(new OnLongClickListener() {
					@Override
					public boolean onLongClick(View view) {
						return true;
					}
				});
			}
		}
	}
	
	protected static class MusicElementRowItem extends RowListItem {
		private MusicElement element;
		
		public MusicElementRowItem(MusicElement element) {
			this.element = element;
		}
		
		public MusicElement getMusicFile() {
			return element;
		}
		
		@Override
		public int getType() {
			return TYPE_MUSIC_ELEMENT;
		}
	}
	
	/*
	 * List
	 */
	public static class MusicListRowViewAdapter extends RecyclerView.Adapter<MusicListRowViewHolder> {
		private List<MusicElement> elements;
		
		public MusicListRowViewAdapter(List<MusicElement> elements) {
			this.elements = elements;
		}
		
		@Override
		public MusicListRowViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_store_page_music_list_cardview, viewGroup, false);
			return new MusicListRowViewHolder(view);
		}
		
		@Override
		public void onBindViewHolder(MusicListRowViewHolder viewHolder, int position) {
			MusicElement item = elements.get(position);
			viewHolder.bind(item);
		}
		
		@Override
		public int getItemCount() {
			return elements.size();
		}
	}
	
	public static class MusicListRowViewHolder extends RecyclerView.ViewHolder {
		private View view;
		private TextView titleTextView;
		private ImageView thumbnailImageView;
		
		public MusicListRowViewHolder(View itemView) {
			super(itemView);
			view = itemView;
			titleTextView = (TextView) itemView.findViewById(R.id.item_store_page_music_list_layout_textview_title);
			thumbnailImageView = (ImageView) itemView.findViewById(R.id.item_store_page_music_list_layout_imageview_thumbnail);
		}
		
		public void bind(final MusicElement item) {
			String imageUrl = null;
			titleTextView.setText(item.getClass() != null ? String.valueOf(item.getClass()) : "null");
			
			if (item instanceof MusicGroup) {
				final MusicGroup group = (MusicGroup) item;
				
				titleTextView.setText(group.getDisplay());
				
				if (group.getImageUrl() != null) {
					imageUrl = group.getImageUrl();
				}
			} else if (item instanceof MusicAlbum) {
				final MusicAlbum album = (MusicAlbum) item;
				
				titleTextView.setText(album.getTitle());
				
				if (album.getImageUrl() != null) {
					imageUrl = album.getImageUrl();
				}
			} else if (item instanceof MusicFile) {
				final MusicFile music = (MusicFile) item;
				
				titleTextView.setText(music.getTitle());
				
				if (music.getParentAlbum() != null && music.getParentAlbum().getImageUrl() != null) {
					imageUrl = music.getParentAlbum().getImageUrl();
				}
			}
			
			if (imageUrl != null) {
				view.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						MusicActivity.start(item);
					}
				});
			}
			
			BoxPlayApplication.getViewHelper().downloadToImageView(thumbnailImageView, imageUrl);
		}
	}
	
	protected static class MusicListRowItem extends RowListItem {
		private List<MusicElement> elements;
		
		public MusicListRowItem(List<MusicElement> elements) {
			this.elements = elements;
		}
		
		public List<MusicElement> getMusicElements() {
			return elements;
		}
		
		@Override
		public int getType() {
			return TYPE_MUSIC_LIST;
		}
	}
	
	public static PageMusicStoreFragment getMusicFragment() {
		return (PageMusicStoreFragment) INSTANCE;
	}
	
}