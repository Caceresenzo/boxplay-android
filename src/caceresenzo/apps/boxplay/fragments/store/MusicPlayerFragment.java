package caceresenzo.apps.boxplay.fragments.store;

import java.util.Collections;

import com.sothree.slidinguppanel.ScrollableViewHelper;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import caceresenzo.android.libs.widget.itemtouchhelper.ItemTouchHelperAdapter;
import caceresenzo.android.libs.widget.itemtouchhelper.ItemTouchHelperListener;
import caceresenzo.android.libs.widget.itemtouchhelper.ItemTouchHelperViewHolder;
import caceresenzo.android.libs.widget.itemtouchhelper.OnStartDragListener;
import caceresenzo.android.libs.widget.itemtouchhelper.SimpleItemTouchHelperCallback;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.providers.media.music.MusicController;
import caceresenzo.apps.boxplay.providers.media.music.MusicControls;
import caceresenzo.apps.boxplay.providers.media.music.MusicService;
import caceresenzo.libs.boxplay.models.store.music.MusicFile;
import caceresenzo.libs.boxplay.models.store.music.MusicPlaylist;

@SuppressWarnings({ "deprecation", "unused" })
public class MusicPlayerFragment extends Fragment {
	
	private static MusicPlayerFragment FRAGMENT;
	
	private MusicController musicController;
	
	private SlidingUpPanelLayout slidingUpPanelLayout;
	private RelativeLayout controlsRelativeLayout;
	private ImageView iconImage;
	private ImageButton previousButton, playPausePanelButton, playPauseButton, nextPanelButton, nextButton;
	private TextView titleTextView, authorTextView;
	private RecyclerView recyclerView;
	private SeekBar progressSeekBar;
	
	private ItemTouchHelper itemTouchHelper;
	
	private Handler progressBarHandler;
	
	private boolean lockedProgresBar = false;
	
	public MusicPlayerFragment() {
		musicController = MusicController.getMusicController();
		
		progressBarHandler = new Handler() {
			private int oldMax = 0;
			
			@Override
			public void handleMessage(Message message) {
				super.handleMessage(message);
				
				try {
					int[] object = (int[]) message.obj;
					
					// if (oldMax != object[1]) {
					progressSeekBar.setMax(oldMax = object[1]);
					// }
					if (!lockedProgresBar) {
						progressSeekBar.setProgress(object[0]);
					}
				} catch (Exception exception) {
					;
				}
			}
		};
		
		MusicController.getMusicController().setProgressBarHandler(progressBarHandler);
	}
	
	public void attachSlidingUpPanelLayout(SlidingUpPanelLayout slidingUpPanelLayout) {
		this.slidingUpPanelLayout = slidingUpPanelLayout;
	}
	
	public void notifyPlaylistUpdate(boolean moveTop) {
		if (!isAdded()) {
			return;
		}
		
		updateVisibility();
		
		recyclerView.getAdapter().notifyDataSetChanged();
		if (moveTop) {
			((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(MusicController.getMusicController().getPlayingSongNumber(), 0);
		}
	}
	
	public void notifyUpdate(MusicFile music, boolean pause) {
		notifyUpdate(music, pause, false);
	}
	
	public void notifyUpdate(MusicFile music, boolean pause, boolean itemJustAdded) {
		if (!isAdded()) {
			return;
		}
		
		if (itemJustAdded) {
			notifyPlaylistUpdate(false);
		}
		// Drawable playPauseIcon = getResources().getDrawable(pause ? R.drawable.icon_play : R.drawable.icon_pause); // Drawable is not usable 2 times
		playPausePanelButton.setImageDrawable(getResources().getDrawable(pause ? R.drawable.icon_play : R.drawable.icon_pause));
		playPauseButton.setImageDrawable(getResources().getDrawable(pause ? R.drawable.icon_play : R.drawable.icon_pause));
		
		if (music != null && (!itemJustAdded || getPlaylist().isEmpty())) {
			BoxPlayApplication.getViewHelper().downloadToImageView(iconImage, music.getBestImageUrl());
			titleTextView.setText(music.getTitle());
			authorTextView.setText(formatMusicDescription(music));
		}
	}
	
	public void hidePlayer() {
		if (slidingUpPanelLayout != null) {
			slidingUpPanelLayout.setPanelHeight(0);
			if (slidingUpPanelLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
				slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
			}
		}
	}
	
	public void showPlayer() {
		if (slidingUpPanelLayout != null) {
			slidingUpPanelLayout.setPanelHeight((int) getResources().getDimension(R.dimen.music_player_panel_height));
		}
	}
	
	public void updateVisibility() {
		if (musicController.getMusicPlaylist().isEmpty()) {
			hidePlayer();
		} else {
			showPlayer();
		}
	}
	
	public void updatePanelButtonVisibility() {
		int newVisibility = slidingUpPanelLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED) ? View.GONE : View.VISIBLE;
		
		playPausePanelButton.setVisibility(newVisibility);
		nextPanelButton.setVisibility(newVisibility);
	}
	
	public void updatePanelButtonAlpha(float offset) {
		int alpha = (int) (255 - (offset * 255));
		
		playPausePanelButton.getBackground().setAlpha(alpha);
		playPausePanelButton.setAlpha(alpha);
		
		nextPanelButton.getBackground().setAlpha(alpha);
		nextPanelButton.setAlpha(alpha);
	}
	
	public void lockButton(boolean lock) {
		// BoxPlayApplication.getBoxPlayApplication().getSupportActionBar().setTitle("locked: " + lock);
		previousButton.setClickable(lock);
		playPausePanelButton.setClickable(lock);
		playPauseButton.setClickable(lock);
		nextPanelButton.setClickable(lock);
		nextButton.setClickable(lock);
	}
	
	public void lockControls(boolean lock) {
		if (lock) {
			progressSeekBar.setVisibility(View.VISIBLE); // Just in case
		}
		
		// lockButton(lock);
		progressSeekBar.setIndeterminate(lock);
		// progressSeekBar.getThumb().mutate().setAlpha(!lock ? 255 : 0);
		// progressSeekBar.setThumb(lock ? null : new SeekBar(getContext()).getThumb()); // Fuck i gave up, i cant fine default ressource for restoring old drawable
		// BoxPlayApplication.getBoxPlayApplication().getSupportActionBar().setTitle("alpha: " + (!lock ? 255 : 0));
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_music_player, container, false);
		
		controlsRelativeLayout = (RelativeLayout) view.findViewById(R.id.fragment_music_player_relativelayout_container_controls);
		
		iconImage = (ImageView) view.findViewById(R.id.fragment_music_player_imageview_panel_icon);
		titleTextView = (TextView) view.findViewById(R.id.fragment_music_player_textview_panel_title);
		authorTextView = (TextView) view.findViewById(R.id.fragment_music_player_textview_panel_author);
		
		previousButton = (ImageButton) view.findViewById(R.id.fragment_music_player_imagebutton_previous);
		playPausePanelButton = (ImageButton) view.findViewById(R.id.fragment_music_player_imagebutton_panel_pause_play);
		playPauseButton = (ImageButton) view.findViewById(R.id.fragment_music_player_imagebutton_pause_play);
		nextPanelButton = (ImageButton) view.findViewById(R.id.fragment_music_player_imagebutton_panel_next);
		nextButton = (ImageButton) view.findViewById(R.id.fragment_music_player_imagebutton_next);
		
		previousButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				MusicControls.getMusicControls().previousControl(getActivity());
			}
		});
		
		OnClickListener onClickPlayPauseListener = new OnClickListener() {
			@Override
			public void onClick(View view) {
				MusicControls.getMusicControls().playPauseControl(getActivity());
			}
		};
		playPausePanelButton.setOnClickListener(onClickPlayPauseListener);
		playPauseButton.setOnClickListener(onClickPlayPauseListener);
		
		OnClickListener onClickNextListener = new OnClickListener() {
			@Override
			public void onClick(View view) {
				MusicControls.getMusicControls().nextControl(getActivity());
			}
		};
		nextPanelButton.setOnClickListener(onClickNextListener);
		nextButton.setOnClickListener(onClickNextListener);
		
		recyclerView = (RecyclerView) view.findViewById(R.id.fragment_music_player_recyclerview_list);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		recyclerView.setAdapter(new MusicViewAdapter());
		
		ItemTouchHelper.Callback itemTouchHelperCallback = new SimpleItemTouchHelperCallback((ItemTouchHelperAdapter) recyclerView.getAdapter(), new ItemTouchHelperListener() {
			@Override
			public void onReallyMoved(int fromPosition, int toPosition) {
				BoxPlayApplication.getManagers().getMusicManager().saveDatabase();
			}
		});
		itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
		itemTouchHelper.attachToRecyclerView(recyclerView);
		
		progressSeekBar = (SeekBar) view.findViewById(R.id.fragment_music_player_seekbar_progressbar);
		progressSeekBar.setVisibility(View.GONE);
		progressSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				lockedProgresBar = false;
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				lockedProgresBar = true;
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				MusicService musicService = MusicService.getMusicService();
				if (musicService != null && fromUser) {
					musicService.getMediaPlayer().seekTo(progress);
				}
			}
		});
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if (slidingUpPanelLayout != null) {
			slidingUpPanelLayout.setScrollableViewHelper(new ScrollableViewHelper());
			slidingUpPanelLayout.setScrollableView(recyclerView);
			slidingUpPanelLayout.setDragView(controlsRelativeLayout);
			
			slidingUpPanelLayout.addPanelSlideListener(new PanelSlideListener() {
				@Override
				public void onPanelStateChanged(View panel, PanelState previousState, PanelState newState) {
					updatePanelButtonVisibility();
				}
				
				@Override
				public void onPanelSlide(View panel, float slideOffset) {
					updatePanelButtonAlpha(slideOffset);
				}
			});
		}
		
		updateVisibility();
	}
	
	class MusicViewAdapter extends RecyclerView.Adapter<MusicViewHolder> implements ItemTouchHelperAdapter {
		
		private final OnStartDragListener<MusicViewHolder> dragStartListener;
		
		public MusicViewAdapter() {
			dragStartListener = new OnStartDragListener<MusicViewHolder>() {
				@Override
				public void onStartDrag(MusicViewHolder viewHolder) {
					itemTouchHelper.startDrag(viewHolder);
				}
			};
		}
		
		@Override
		public MusicViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_music_player_track, viewGroup, false);
			return new MusicViewHolder(view);
		}
		
		@Override
		public void onBindViewHolder(final MusicViewHolder viewHolder, int position) {
			final MusicFile music = getPlaylist().get(position);
			viewHolder.bind(music);
			
			viewHolder.reorderImageView.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View view, MotionEvent event) {
					if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
						dragStartListener.onStartDrag(viewHolder);
					}
					return false;
				}
			});
		}
		
		@Override
		public int getItemCount() {
			return getPlaylist().size();
		}
		
		@Override
		public boolean onItemMove(int fromPosition, int toPosition) {
			Collections.swap(getPlaylist(), fromPosition, toPosition);
			notifyItemMoved(fromPosition, toPosition);
			
			if (musicController.getPlayingSongNumber() == fromPosition) {
				musicController.setPlayingSongNumber(toPosition);
			}
			
			return false;
		}
		
		@Override
		public void onItemDismiss(int position) {
			getPlaylist().remove(position);
			notifyItemRemoved(position);
			
			BoxPlayApplication.getManagers().getMusicManager().saveDatabase();
			
			if (musicController.getPlayingSongNumber() == position) {
				MusicControls.getMusicControls().pauseControl(getContext());
			}
			
			if (musicController.getMusicPlaylist().size() == 0) {
				updateVisibility();
				
				if (MusicService.isRunning()) {
					MusicService.destroyService(getContext());
				}
			}
		}
	}
	
	class MusicViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
		private View view;
		private ImageView artworkImageView, /* moreImageView, */ reorderImageView;
		private TextView artistTextView, titleTextView, durationTextView;
		
		public MusicViewHolder(View itemView) {
			super(itemView);
			
			view = itemView;
			
			artworkImageView = (ImageView) itemView.findViewById(R.id.item_music_player_track_imageview_track_view_artwork);
			artistTextView = (TextView) itemView.findViewById(R.id.item_music_player_track_textview_track_view_artist);
			titleTextView = (TextView) itemView.findViewById(R.id.item_music_player_track_textview_track_view_title);
			durationTextView = (TextView) itemView.findViewById(R.id.item_music_player_track_textview_track_view_duration);
			// moreImageView = (ImageView) itemView.findViewById(R.id.item_music_player_track_imageview_track_more);
			reorderImageView = (ImageView) itemView.findViewById(R.id.item_music_player_track_imageview_track_reorder);
		}
		
		public void bind(final MusicFile music) {
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					BoxPlayApplication.getManagers().getMusicManager().playFile(music, getAdapterPosition(), false, true);
				}
			});
			
			artistTextView.setText(formatMusicDescription(music));
			titleTextView.setText(music.getTitle());
			durationTextView.setText(music.formatDuration());
			
			if (music.getImageUrl() != null) {
				BoxPlayApplication.getViewHelper().downloadToImageView(artworkImageView, music.getImageUrl());
			}
			
			int colorRessource = 0;
			if (musicController.getPlayingSongNumber() == getAdapterPosition()) {
				colorRessource = R.color.colorAccent;
			} else {
				colorRessource = R.color.white;
			}
			reorderImageView.setImageTintList(getResources().getColorStateList(colorRessource));
		}
		
		@Override
		public void onItemSelected() {
			
		}
		
		@Override
		public void onItemClear() {
			
		}
	}
	
	private String formatMusicDescription(MusicFile music) {
		if (music == null) {
			return "";
		}
		
		String parentTitle = "";
		if (music.getParentAlbum() != null) {
			parentTitle = music.getParentAlbum().getTitle();
		}
		
		return getString(R.string.boxplay_player_music_track_description, parentTitle, music.formatAuthor());
	}
	
	private MusicPlaylist getPlaylist() {
		return musicController.getMusicPlaylist();
	}
	
	public SeekBar getTrackProgressBar() {
		return progressSeekBar;
	}
	
	public static MusicPlayerFragment getPlayerFragment() {
		if (FRAGMENT == null) {
			FRAGMENT = new MusicPlayerFragment();
		}
		
		return FRAGMENT;
	}
}