package caceresenzo.apps.boxplay.activities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import caceresenzo.android.libs.internet.AndroidDownloader;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.fragments.store.PageMusicStoreFragment;
import caceresenzo.apps.boxplay.helper.LocaleHelper;
import caceresenzo.apps.boxplay.providers.media.music.MusicController;
import caceresenzo.libs.boxplay.models.element.implementations.MusicElement;
import caceresenzo.libs.boxplay.models.store.music.MusicAlbum;
import caceresenzo.libs.boxplay.models.store.music.MusicFile;
import caceresenzo.libs.boxplay.models.store.music.MusicGroup;
import caceresenzo.libs.boxplay.models.store.music.enums.MusicGenre;
import caceresenzo.libs.boxplay.models.store.video.VideoGroup;

/**
 * TODO: NEED TO BE COMPLETLY REDONE
 * 
 * @author Enzo CACERES
 */
@SuppressWarnings("unused")
public class MusicActivity extends AppCompatActivity {
	
	public static final String BUNDLE_KEY_MUSIC_ELEMENT_ITEM = "music_element_item";
	
	private static final int INDEX_TITLE_1 = 0, INDEX_TITLE_2 = 1, INDEX_TITLE_3 = 2;
	private static final int INDEX_CONTENT_1 = 3, INDEX_CONTENT_2 = 4, INDEX_CONTENT_3 = 5;
	private static final int INDEX_HEAD_TITLE = 6, INDEX_NECK_TITLE = 7, INDEX_BODY_TITLE = 8, INDEX_FEET_TITLE = 9;
	private static final int INDEX_HEAD = 0, INDEX_NECK = 1, INDEX_BODY = 2, INDEX_FEET = 3;
	private static final int INDEX_NECK_LIST = 0, INDEX_BODY_LIST = 1, INDEX_FEET_LIST = 2;
	
	/**
	 * @Album
	 */
	private static final int INDEX_TEXT_GENRE = 7, INDEX_TEXT_SONG = 8, INDEX_TEXT_OTHER_ALBUM = 9;
	private static final int INDEX_LIST_GENRE = 0, INDEX_LIST_SONG = 1, INDEX_LIST_OTHER_ALBUMS = 2;
	
	private static MusicActivity INSTANCE;
	
	private MusicElement musicElement;
	
	private String STRING_MUSIC_DURATION_HOUR, STRING_MUSIC_DURATION_MINUTE, STRING_MUSIC_DURATION_SECONDE;
	
	private CoordinatorLayout coordinatorLayout;
	private Toolbar toolbar;
	private ActionBar actionBar;
	
	private CardView[] cardViewArray = new CardView[4];
	private TextView[] textViewArray = new TextView[10];
	private ImageView iconImageView;
	private RecyclerView[] recyclerViewArray = new RecyclerView[3];
	
	private GenreTagViewAdapter genreTagViewAdapter;
	private SongViewAdapter songViewAdapter;
	private AlbumViewAdapter albumViewAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_music);
		INSTANCE = this;
		
		musicElement = (MusicElement) getIntent().getSerializableExtra(BUNDLE_KEY_MUSIC_ELEMENT_ITEM);
		if (musicElement == null) {
			if (BoxPlayApplication.getBoxPlayApplication() != null) {
				BoxPlayApplication.getBoxPlayApplication().toast(getString(R.string.boxplay_error_activity_invalid_data)).show();
			}
			finish();
		}
		
		initializeStrings();
		
		initializeViews();
	}
	
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(LocaleHelper.onAttach(base));
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putSerializable(BUNDLE_KEY_MUSIC_ELEMENT_ITEM, (Serializable) musicElement);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private void initializeStrings() {
		STRING_MUSIC_DURATION_HOUR = getString(R.string.boxplay_store_music_activity_body_song_item_time_hour);
		STRING_MUSIC_DURATION_MINUTE = getString(R.string.boxplay_store_music_activity_body_song_item_time_minute);
		STRING_MUSIC_DURATION_SECONDE = getString(R.string.boxplay_store_music_activity_body_song_item_time_seconde);
	}
	
	@SuppressWarnings("unchecked")
	private void initializeViews() {
		coordinatorLayout = (CoordinatorLayout) findViewById(R.id.activity_music_coordinatorlayout_container);
		
		toolbar = (Toolbar) findViewById(R.id.activity_music_toolbar_bar);
		
		cardViewArray[INDEX_HEAD] = (CardView) findViewById(R.id.activity_music_cardview_container_head);
		cardViewArray[INDEX_NECK] = (CardView) findViewById(R.id.activity_music_cardview_container_neck);
		cardViewArray[INDEX_BODY] = (CardView) findViewById(R.id.activity_music_cardview_container_body);
		cardViewArray[INDEX_FEET] = (CardView) findViewById(R.id.activity_music_cardview_container_feet);
		
		textViewArray[INDEX_TITLE_1] = (TextView) findViewById(R.id.activity_music_textview_title_1);
		textViewArray[INDEX_TITLE_2] = (TextView) findViewById(R.id.activity_music_textview_title_2);
		textViewArray[INDEX_TITLE_3] = (TextView) findViewById(R.id.activity_music_textview_title_3);
		
		textViewArray[INDEX_CONTENT_1] = (TextView) findViewById(R.id.activity_music_textview_content_1);
		textViewArray[INDEX_CONTENT_2] = (TextView) findViewById(R.id.activity_music_textview_content_2);
		textViewArray[INDEX_CONTENT_3] = (TextView) findViewById(R.id.activity_music_textview_content_3);
		
		iconImageView = (ImageView) findViewById(R.id.activity_music_imageview_icon);
		
		textViewArray[INDEX_NECK_TITLE] = (TextView) findViewById(R.id.activity_music_textview_neck_title); // Genre
		recyclerViewArray[INDEX_NECK_LIST] = (RecyclerView) findViewById(R.id.activity_music_recyclerview_neck_list);
		
		textViewArray[INDEX_BODY_TITLE] = (TextView) findViewById(R.id.activity_music_textview_body_title); // Song
		recyclerViewArray[INDEX_BODY_LIST] = (RecyclerView) findViewById(R.id.activity_music_recyclerview_body_list);
		
		textViewArray[INDEX_FEET_TITLE] = (TextView) findViewById(R.id.activity_music_textview_feet_title); // Other Albums
		recyclerViewArray[INDEX_FEET_LIST] = (RecyclerView) findViewById(R.id.activity_music_recyclerview_feet_list);
		
		/*
		 * Code
		 */
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		
		actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		recyclerViewArray[INDEX_NECK_LIST].setHasFixedSize(true);
		recyclerViewArray[INDEX_NECK_LIST].setNestedScrollingEnabled(false);
		
		recyclerViewArray[INDEX_BODY_LIST].setHasFixedSize(true);
		recyclerViewArray[INDEX_BODY_LIST].setNestedScrollingEnabled(false);
		
		recyclerViewArray[INDEX_FEET_LIST].setHasFixedSize(true);
		recyclerViewArray[INDEX_FEET_LIST].setNestedScrollingEnabled(false);
		
		MusicGroup groupForOtherAlbums = null;
		MusicAlbum albumToRemoveForOtherAlbums = null;
		String imageUrlForIcon = null;
		
		if (musicElement instanceof MusicGroup) {
			MusicGroup group = groupForOtherAlbums = (MusicGroup) musicElement;
			
			actionBar.setTitle(getString(R.string.boxplay_store_music_activity_group_title, group.getDisplay(), BoxPlayApplication.getViewHelper().enumToStringCacheTranslation(group.getMusicAuthorType())));
			
			textViewArray[INDEX_TITLE_1].setText(getString(R.string.boxplay_store_music_activity_head_group_title_name));
			textViewArray[INDEX_CONTENT_1].setText(group.getDisplay());
			
			textViewArray[INDEX_TITLE_2].setText(getString(R.string.boxplay_store_music_activity_head_group_title_type));
			textViewArray[INDEX_CONTENT_2].setText(BoxPlayApplication.getViewHelper().enumToStringCacheTranslation(group.getMusicAuthorType()));
			
			textViewArray[INDEX_TITLE_3].setText(getString(R.string.boxplay_store_music_activity_head_group_title_downloadable));
			if (group.isDownloadable()) {
				textViewArray[INDEX_CONTENT_3].setText(R.string.boxplay_store_music_activity_head_group_content_downloadable_yes);
			} else {
				textViewArray[INDEX_CONTENT_3].setText(R.string.boxplay_store_music_activity_head_group_content_downloadable_no);
			}
			
			imageUrlForIcon = group.getImageHdUrl() != null ? group.getImageHdUrl() : group.getImageUrl();
			
			cardViewArray[INDEX_NECK].setVisibility(View.GONE);
			cardViewArray[INDEX_BODY].setVisibility(View.GONE);
		} else if (musicElement instanceof MusicAlbum) {
			MusicAlbum album = albumToRemoveForOtherAlbums = (MusicAlbum) musicElement;
			final MusicGroup group = groupForOtherAlbums = album.getParentGroup();
			
			String formattedAuthors = album.formatAuthor();
			
			actionBar.setTitle(getString(R.string.boxplay_store_music_activity_album_title, album.getTitle(), formattedAuthors));
			
			cardViewArray[INDEX_HEAD].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					if (group != null) {
						start(group);
					}
				}
			});
			
			textViewArray[INDEX_TITLE_1].setText(getString(R.string.boxplay_store_music_activity_head_album_title_album));
			textViewArray[INDEX_CONTENT_1].setText(album.getTitle());
			
			textViewArray[INDEX_TITLE_2].setText(getString(R.string.boxplay_store_music_activity_head_album_title_artist, album.getAuthors().size() > 1 ? "s" : ""));
			textViewArray[INDEX_CONTENT_2].setText(formattedAuthors);
			
			textViewArray[INDEX_TITLE_3].setText(getString(R.string.boxplay_store_music_activity_head_album_title_release));
			textViewArray[INDEX_CONTENT_3].setText(album.getReleaseDateString()); // TODO: Add date
			
			imageUrlForIcon = album.getImageHdUrl() != null ? album.getImageHdUrl() : album.getImageUrl();
			
			textViewArray[INDEX_TEXT_GENRE].setText(getString(R.string.boxplay_store_music_activity_neck_album_genre_title, album.getGenres().size() > 1 ? "s" : ""));
			recyclerViewArray[INDEX_LIST_GENRE].setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
			recyclerViewArray[INDEX_LIST_GENRE].setAdapter(genreTagViewAdapter = new GenreTagViewAdapter(album.getGenres()));
			
			textViewArray[INDEX_TEXT_SONG].setText(getString(R.string.boxplay_store_music_activity_body_album_song_title, album.getMusics().size() > 1 ? "s" : ""));
			recyclerViewArray[INDEX_LIST_SONG].setLayoutManager(new LinearLayoutManager(this));
			recyclerViewArray[INDEX_LIST_SONG].setAdapter(songViewAdapter = new SongViewAdapter(album.getMusics()));
		}
		
		if (groupForOtherAlbums != null && groupForOtherAlbums.getAlbums() != null) {
			List<?> otherAlbums = new ArrayList<>(groupForOtherAlbums.getAlbums());
			if (otherAlbums == null || otherAlbums.size() <= (0 + (albumToRemoveForOtherAlbums != null ? 1 : 0))) {
				cardViewArray[INDEX_FEET].setVisibility(View.GONE);
			} else {
				if (albumToRemoveForOtherAlbums != null) {
					otherAlbums.remove(albumToRemoveForOtherAlbums);
				}
				textViewArray[INDEX_TEXT_OTHER_ALBUM].setText(getString(musicElement instanceof MusicGroup ? R.string.boxplay_store_music_activity_feet_album_title : R.string.boxplay_store_music_activity_feet_album_other_title, otherAlbums.size() > 1 ? "s" : ""));
				recyclerViewArray[INDEX_LIST_OTHER_ALBUMS].setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
				recyclerViewArray[INDEX_LIST_OTHER_ALBUMS].setAdapter(albumViewAdapter = new AlbumViewAdapter((List<MusicElement>) otherAlbums));
			}
		}
		
		if (imageUrlForIcon != null) {
			BoxPlayApplication.getViewHelper().downloadToImageView(iconImageView, imageUrlForIcon);
		}
	}
	
	/*
	 * Genre
	 */
	class GenreTagViewAdapter extends RecyclerView.Adapter<GenreTagViewHolder> {
		private List<MusicGenre> list;
		
		public GenreTagViewAdapter(List<MusicGenre> list) {
			this.list = list;
		}
		
		@Override
		public GenreTagViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_music_genre_tag, viewGroup, false);
			return new GenreTagViewHolder(view);
		}
		
		@Override
		public void onBindViewHolder(GenreTagViewHolder viewHolder, int position) {
			MusicGenre item = list.get(position);
			viewHolder.bind(item);
		}
		
		@Override
		public int getItemCount() {
			return list.size();
		}
	}
	
	class GenreTagViewHolder extends RecyclerView.ViewHolder {
		private TextView genreTextView;
		
		public GenreTagViewHolder(View itemView) {
			super(itemView);
			
			genreTextView = (TextView) itemView.findViewById(R.id.item_music_genre_tag_textview_content);
		}
		
		public void bind(MusicGenre genre) {
			genreTextView.setText(BoxPlayApplication.getViewHelper().enumToStringCacheTranslation(genre));
		}
	}
	
	/*
	 * Song
	 */
	class SongViewAdapter extends RecyclerView.Adapter<SongViewHolder> {
		private List<MusicFile> list;
		
		public SongViewAdapter(List<MusicFile> list) {
			this.list = list;
		}
		
		@Override
		public SongViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_music_song, viewGroup, false);
			return new SongViewHolder(view);
		}
		
		@Override
		public void onBindViewHolder(SongViewHolder viewHolder, int position) {
			MusicFile item = list.get(position);
			viewHolder.bind(item);
		}
		
		@Override
		public int getItemCount() {
			return list.size();
		}
	}
	
	class SongViewHolder extends RecyclerView.ViewHolder {
		// private BadgeView badge;
		private View view;
		private View containerRelativeLayout;
		private TextView titleTextView, durationTextView;
		
		public SongViewHolder(View itemView) {
			super(itemView);
			view = itemView;
			containerRelativeLayout = (View) itemView.findViewById(R.id.item_music_song_relativelayout_container);
			// badge = new BadgeView(MusicActivity.this, containerRelativeLayout);
			titleTextView = (TextView) itemView.findViewById(R.id.item_music_song_textview_title);
			durationTextView = (TextView) itemView.findViewById(R.id.item_music_song_textview_duration);
		}
		
		@SuppressWarnings("deprecation")
		public void bind(final MusicFile music) {
			// badge.setText(getString(R.string.boxplay_store_music_activity_body_song_badge, music.getTrackId()));
			// badge.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
			// badge.show();
			
			titleTextView.setText(getString(R.string.boxplay_store_music_activity_body_song_item_title, music.getTrackId(), music.getTitle()));
			
			durationTextView.setText(music.formatDuration());
			
			if (music.isAvailable()) {
				view.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(final View view) {
						BoxPlayApplication.getHandler().post(new Runnable() {
							@Override
							public void run() {
								PopupMenu popupMenu = new PopupMenu(MusicActivity.this, view);
								popupMenu.getMenuInflater().inflate(R.menu.music_song_popup, popupMenu.getMenu());
								
								popupMenu.getMenu().findItem(R.id.music_song_popup_action_play).setEnabled(BoxPlayApplication.getViewHelper().isVlcInstalled());
								
								try {
									popupMenu.getMenu().findItem(R.id.music_song_popup_action_download).setEnabled(music.getParentGroup().isDownloadable());
								} catch (Exception exception) {
									; // NullPointerException
								}
								
								popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
									@Override
									public boolean onMenuItemClick(MenuItem item) {
										int id = item.getItemId();
										
										switch (id) {
											case R.id.music_song_popup_action_play: {
												BoxPlayApplication.getManagers().getMusicManager().playFile(music, getAdapterPosition(), true, true);
												break;
											}
											case R.id.music_song_popup_action_play_after: {
												int index = MusicController.getMusicController().getPlayingSongNumber() + 1;
												if (index > MusicController.getMusicController().getMusicPlaylist().size()) {
													index = 0;
												}
												MusicController.getMusicController().getMusicPlaylist().add(index, music);
												break;
											}
											case R.id.music_song_popup_action_add_to_queue: {
												MusicController.getMusicController().getMusicPlaylist().add(music);
												break;
											}
											case R.id.music_song_popup_action_download: {
												AndroidDownloader.askDownload(BoxPlayApplication.getBoxPlayApplication(), Uri.parse(music.getUrl()));
												break;
											}
										}
										
										switch (id) {
											case R.id.music_song_popup_action_play_after:
											case R.id.music_song_popup_action_add_to_queue:
												BoxPlayApplication.getManagers().getMusicManager().updateMusicInterface(music, MusicController.getMusicController().isSongPaused(), true);
											case R.id.music_song_popup_action_play:
												BoxPlayApplication.getManagers().getMusicManager().saveDatabase();
												break;
											
										}
										
										return false;
									}
								});
								
								MenuPopupHelper menuHelper = new MenuPopupHelper(MusicActivity.this, (MenuBuilder) popupMenu.getMenu(), view);
								menuHelper.setForceShowIcon(true);
								menuHelper.show();
								
								// popupMenu.show();
							}
						});
					}
				});
			} else {
				view.setBackgroundTintList(MusicActivity.this.getResources().getColorStateList(R.color.colorPanel));
			}
		}
	}
	
	/*
	 * Album
	 */
	class AlbumViewAdapter extends PageMusicStoreFragment.MusicListRowViewAdapter {
		public AlbumViewAdapter(List<MusicElement> list) {
			super(list);
		}
	}
	
	class MusicAlbumViewHolder extends PageMusicStoreFragment.MusicListRowViewHolder {
		public MusicAlbumViewHolder(View itemView) {
			super(itemView);
		}
	}
	
	public static void start(MusicElement musicElement) {
		BoxPlayApplication application = BoxPlayApplication.getBoxPlayApplication();
		
		Intent intent = new Intent(application, MusicActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(BUNDLE_KEY_MUSIC_ELEMENT_ITEM, (Serializable) musicElement);
		
		application.startActivity(intent);
	}
	
	public static MusicActivity getMusicActivity() {
		return INSTANCE;
	}
	
}