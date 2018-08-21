package caceresenzo.apps.boxplay.fragments.culture.searchngo.detailpage;

import java.util.ArrayList;
import java.util.List;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import caceresenzo.android.libs.dialog.DialogUtils;
import caceresenzo.android.libs.internet.AndroidDownloader;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.MangaChapterReaderActivity;
import caceresenzo.apps.boxplay.activities.SearchAndGoDetailActivity;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.dialog.WorkingProgressDialog;
import caceresenzo.apps.boxplay.helper.ViewHelper;
import caceresenzo.apps.boxplay.managers.DebugManager;
import caceresenzo.apps.boxplay.managers.SearchAndGoManager;
import caceresenzo.libs.boxplay.common.extractor.video.VideoContentExtractor;
import caceresenzo.libs.boxplay.common.extractor.video.VideoContentExtractor.VideoContentExtractorProgressCallback;
import caceresenzo.libs.boxplay.culture.searchngo.content.video.IVideoContentProvider;
import caceresenzo.libs.boxplay.culture.searchngo.data.AdditionalResultData;
import caceresenzo.libs.boxplay.culture.searchngo.data.models.content.ChapterItemResultData;
import caceresenzo.libs.boxplay.culture.searchngo.data.models.content.VideoItemResultData;
import caceresenzo.libs.boxplay.culture.searchngo.result.SearchAndGoResult;
import caceresenzo.libs.bytes.ByteFormat;
import caceresenzo.libs.databridge.ObjectWrapper;
import caceresenzo.libs.filesystem.FileUtils;
import caceresenzo.libs.network.Downloader;
import caceresenzo.libs.thread.HelpedThread;

/**
 * Content page for the {@link SearchAndGoDetailActivity}
 * 
 * @author Enzo CACERES
 */
public class PageDetailContentSearchAndGoFragment extends Fragment {
	
	private static final String TAG = PageDetailContentSearchAndGoFragment.class.getSimpleName();
	
	public static final String ACTION_STREAMING = "action.streaming";
	public static final String ACTION_DOWNLOAD = "action.download";
	
	/* Managers */
	private BoxPlayApplication boxPlayApplication;
	private Handler handler;
	private ViewHelper viewHelper;
	private SearchAndGoManager searchAndGoManager;
	private DebugManager debugManager;
	
	private boolean uiReady = false;
	
	/* Actual result */
	private SearchAndGoResult result;
	private List<AdditionalResultData> contents = new ArrayList<>();
	
	/* Views */
	private RecyclerView recyclerView;
	private ProgressBar progressBar;
	
	private ContentViewAdapter adapter;
	
	/* Dialog */
	private WorkingProgressDialog progressDialog;
	private DialogCreator dialogCreator;
	
	/* Worker */
	private VideoExtractionWorker videoExtractionWorker;
	
	public PageDetailContentSearchAndGoFragment() {
		this.boxPlayApplication = BoxPlayApplication.getBoxPlayApplication();
		this.handler = BoxPlayApplication.getHandler();
		this.viewHelper = BoxPlayApplication.getViewHelper();
		this.searchAndGoManager = BoxPlayApplication.getManagers().getSearchAndGoManager();
		this.debugManager = BoxPlayApplication.getManagers().getDebugManager();
		
		this.dialogCreator = new DialogCreator();
		
		this.videoExtractionWorker = new VideoExtractionWorker();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_culture_searchngo_activitypage_details, container, false);
		
		this.progressDialog = WorkingProgressDialog.create(SearchAndGoDetailActivity.getSearchAndGoDetailActivity());
		
		progressBar = (ProgressBar) view.findViewById(R.id.fragment_culture_searchngo_activitypage_details_progressbar_loading);
		
		recyclerView = (RecyclerView) view.findViewById(R.id.fragment_culture_searchngo_activitypage_details_recyclerview_list);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		recyclerView.setAdapter(adapter = new ContentViewAdapter(contents));
		recyclerView.setHasFixedSize(true);
		recyclerView.setNestedScrollingEnabled(false);
		
		progressBar.setVisibility(View.VISIBLE);
		recyclerView.setVisibility(View.GONE);
		
		uiReady = true;
		
		return view;
	}
	
	public void applyResult(SearchAndGoResult result, List<AdditionalResultData> additionals) {
		this.result = result;
		
		this.contents.clear();
		this.contents.addAll(additionals);
		
		if (adapter != null) {
			adapter.notifyDataSetChanged();
			
			recyclerView.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
		}
	}
	
	/**
	 * Tell if the ui has been {@link View#findViewById(int)} and all view are ready to use
	 * 
	 * @return If the {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has finished
	 */
	public boolean isUiReady() {
		return uiReady;
	}
	
	/**
	 * View adapter for content list item
	 * 
	 * @author Enzo CACERES
	 */
	class ContentViewAdapter extends RecyclerView.Adapter<ContentViewHolder> {
		private List<AdditionalResultData> list;
		
		public ContentViewAdapter(List<AdditionalResultData> list) {
			this.list = list;
		}
		
		@Override
		public ContentViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_culture_searchandgo_activitypage_detail_content, viewGroup, false);
			return new ContentViewHolder(view);
		}
		
		@Override
		public void onBindViewHolder(ContentViewHolder viewHolder, int position) {
			AdditionalResultData item = list.get(position);
			viewHolder.bind(item);
		}
		
		@Override
		public int getItemCount() {
			return list.size();
		}
	}
	
	/**
	 * View holder for content item
	 * 
	 * @author Enzo CACERES
	 */
	class ContentViewHolder extends RecyclerView.ViewHolder {
		private View view;
		private TextView typeTextView, contentTextView;
		private ImageView iconImageView, downloadImageView;
		
		public ContentViewHolder(View itemView) {
			super(itemView);
			
			view = itemView;
			
			typeTextView = (TextView) itemView.findViewById(R.id.item_culture_searchandgo_activitypage_detail_content_textview_type);
			
			iconImageView = (ImageView) itemView.findViewById(R.id.item_culture_searchandgo_activitypage_detail_content_imageview_icon);
			contentTextView = (TextView) itemView.findViewById(R.id.item_culture_searchandgo_activitypage_detail_content_textview_content);
			downloadImageView = (ImageView) itemView.findViewById(R.id.item_culture_searchandgo_activitypage_detail_content_imageview_download);
		}
		
		public void bind(final AdditionalResultData additionalData) {
			typeTextView.setText(viewHelper.enumToStringCacheTranslation(additionalData.getType()));
			
			int targetRessourceId;
			boolean validType = true, hideDownload = true;
			switch (additionalData.getType()) {
				case ITEM_VIDEO: {
					targetRessourceId = R.drawable.icon_video_library_light;
					hideDownload = false;
					break;
				}
				
				case ITEM_CHAPTER: {
					targetRessourceId = R.drawable.icon_books_library_light;
					break;
				}
				
				default: {
					targetRessourceId = R.drawable.icon_close;
					validType = false;
					break;
				}
			}
			
			iconImageView.setImageResource(targetRessourceId);
			contentTextView.setText(additionalData.convert());
			downloadImageView.setVisibility(hideDownload ? View.GONE : View.VISIBLE);
			
			if (validType) {
				OnClickListener onClickListener = new OnClickListener() {
					@Override
					public void onClick(View view) {
						if (videoExtractionWorker.isRunning()) {
							boxPlayApplication.toast("ExtractionWorker is busy").show();
							return;
						}
						
						videoExtractionWorker = new VideoExtractionWorker();
						
						String action = ACTION_STREAMING;
						if (view.equals(downloadImageView)) {
							action = ACTION_DOWNLOAD;
						}
						
						switch (additionalData.getType()) {
							case ITEM_VIDEO: {
								videoExtractionWorker.applyData((VideoItemResultData) additionalData.getData(), action).start();
								progressDialog.show();
								break;
							}
							
							case ITEM_CHAPTER: {
								MangaChapterReaderActivity.start((ChapterItemResultData) additionalData.getData());
								break;
							}
							
							default: {
								throw new IllegalStateException(); // Impossible to reach
							}
						}
						
					}
				};
				
				view.setOnClickListener(onClickListener);
				downloadImageView.setOnClickListener(onClickListener);
			}
		}
	}
	
	/**
	 * Worker thread to extract video direct link
	 * 
	 * @author Enzo CACERES
	 */
	class VideoExtractionWorker extends HelpedThread {
		private VideoItemResultData videoItem;
		private String action;
		
		private IVideoContentProvider videoContentProvider;
		private VideoContentExtractor extractor;
		
		private String directUrl;
		
		@Override
		protected void onRun() {
			handler.post(new Runnable() {
				@Override
				public void run() {
					progressDialog.update(R.string.boxplay_culture_searchngo_extractor_status_downloading_video_site_url, videoItem.getUrl());
				}
			});
			
			final List<String> compatibleVideoPageUrls = new ArrayList<>();
			
			for (String videoPageUrl : videoContentProvider.extractVideoPageUrl(videoItem)) {
				if (searchAndGoManager.hasCompatibleExtractor(videoPageUrl)) {
					compatibleVideoPageUrls.add(videoPageUrl);
				}
			}
			
			final ObjectWrapper<String> urlObjectWrapper = new ObjectWrapper<>(null);
			
			if (videoContentProvider.hasMoreThanOnePlayer() && compatibleVideoPageUrls.size() > 1) {
				lock();
				
				handler.post(new Runnable() {
					@Override
					public void run() {
						dialogCreator.showPossiblePlayersDialog(compatibleVideoPageUrls, new PossiblePlayerDialogCallback() {
							@Override
							public void onClick(final int which) {
								urlObjectWrapper.setValue(compatibleVideoPageUrls.get(which));
								unlock();
							}
						}, new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog) {
								closeDialog();
								cancel();
							}
						});
					}
				});
				
				waitUntilUnlock();
				
				if (!isCancelled() && urlObjectWrapper.getValue() != null) {
					processUrl(urlObjectWrapper.getValue());
				}
			} else if (compatibleVideoPageUrls.size() == 1) {
				processUrl(compatibleVideoPageUrls.get(0));
			} else {
				handler.post(new Runnable() {
					@Override
					public void run() {
						progressDialog.update(R.string.boxplay_culture_searchngo_extractor_status_no_extractor_compatible);
					}
				});
			}
		}
		
		public void processUrl(String videoPageUrl) {
			try {
				extractor = (VideoContentExtractor) searchAndGoManager.getExtractorFromBaseUrl(videoPageUrl);
				
				if (extractor == null) {
					throw new NullPointerException(String.format("ContentExtractor is null, site not supported? (page url: %s)", videoPageUrl));
				}
				
				directUrl = extractor.extractDirectVideoUrl(videoPageUrl, new VideoContentExtractorProgressCallback() {
					@Override
					public void onDownloadingUrl(final String targetUrl) {
						handler.post(new Runnable() {
							@Override
							public void run() {
								progressDialog.update(R.string.boxplay_culture_searchngo_extractor_status_downloading_url, targetUrl);
							}
						});
					}
					
					@Override
					public void onFileNotAvailable() {
						handler.post(new Runnable() {
							@Override
							public void run() {
								boxPlayApplication.toast(R.string.boxplay_culture_searchngo_extractor_status_file_not_available).show();
							}
						});
					}
					
					@Override
					public void onExtractingLink() {
						handler.post(new Runnable() {
							@Override
							public void run() {
								progressDialog.update(R.string.boxplay_culture_searchngo_extractor_status_extracting_link);
							}
						});
					}
					
					@Override
					public void onFormattingResult() {
						handler.post(new Runnable() {
							@Override
							public void run() {
								progressDialog.update(R.string.boxplay_culture_searchngo_extractor_status_formatting_result);
							}
						});
					}
				});
				
				closeDialog();
				
				final String filename = String.format("%s %s.mp4", FileUtils.remplaceIllegalChar(result.getName()), videoItem.getName());
				
				switch (action) {
					case ACTION_STREAMING: {
						BoxPlayApplication.getManagers().getVideoManager().openVLC(directUrl, result.getName() + "\n" + videoItem.getName());
						break;
					}
					
					case ACTION_DOWNLOAD: {
						final String fileSize = ByteFormat.toHumanBytes(Downloader.getFileSize(directUrl));
						
						lock();
						handler.post(new Runnable() {
							@Override
							public void run() {
								unlock();
								
								dialogCreator.showFileSizeDialog(filename, fileSize, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										Log.e(getClass().getSimpleName(), "Downloading file: " + filename);
										AndroidDownloader.askDownload(boxPlayApplication, Uri.parse(directUrl), filename);
									}
								});
							}
						});
						
						waitUntilUnlock();
						break;
					}
					
					default: {
						throw new IllegalStateException();
					}
				}
			} catch (Exception exception) {
				if (extractor != null) {
					extractor.notifyException(exception);
				} else {
					Log.e(TAG, "Can't print in the extractor's logger (null)", exception);
				}
				
				BoxPlayApplication.getBoxPlayApplication().toast(R.string.boxplay_culture_searchngo_extractor_error_failed_to_extract, exception.getLocalizedMessage());
			}
			
			if (debugManager.openLogsAtExtractorEnd() && extractor != null) {
				DialogUtils.showDialog(BoxPlayApplication.getHandler(), getContext(), "Extraction logs", extractor.getLogger().getContent());
			}
		}
		
		@Override
		protected void onFinished() {
			closeDialog();
		}
		
		@Override
		protected void onCancelled() {
			closeDialog();
		}
		
		private void closeDialog() {
			handler.post(new Runnable() {
				@Override
				public void run() {
					progressDialog.update("");
					progressDialog.hide();
				}
			});
		}
		
		/**
		 * Initialize worker thread local data
		 * 
		 * @param videoItem
		 *            Target {@link VideoItemResultData} that will be extracted
		 * @param action
		 * @return Itself, now call {@link #start()}
		 */
		public VideoExtractionWorker applyData(VideoItemResultData videoItem, String action) {
			this.videoItem = videoItem;
			this.action = action;
			
			videoContentProvider = videoItem.getVideoContentProvider();
			
			return this;
		}
	}
	
	class DialogCreator {
		private AlertDialog.Builder createBuilder() {
			return new AlertDialog.Builder(getActivity());
		}
		
		public void showPossiblePlayersDialog(List<String> players, final PossiblePlayerDialogCallback callback, OnCancelListener onCancelListener) {
			AlertDialog.Builder builder = createBuilder();
			builder.setTitle(getString(R.string.boxplay_culture_searchngo_extractor_dialog_possible_player));
			
			String[] queryArray = new String[players.size()];
			
			for (int i = 0; i < players.size(); i++) {
				queryArray[i] = players.get(i);
			}
			
			builder.setItems(queryArray, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					callback.onClick(which);
				}
			});
			
			builder.setOnCancelListener(onCancelListener);
			
			builder.create().show();
		}
		
		public void showFileSizeDialog(String file, String size, DialogInterface.OnClickListener onContinueListener) {
			AlertDialog.Builder builder = createBuilder();
			
			builder.setTitle(getString(R.string.boxplay_culture_searchngo_download_dialog_file_size_title));
			builder.setMessage(getString(R.string.boxplay_culture_searchngo_download_dialog_file_size_message, file, size));
			
			builder.setPositiveButton(getString(R.string.boxplay_culture_searchngo_download_dialog_file_size_button_continue), onContinueListener);
			builder.setNegativeButton(getString(R.string.boxplay_culture_searchngo_download_dialog_file_size_button_cancel), null);
			
			builder.create().show();
		}
	}
	
	interface PossiblePlayerDialogCallback {
		void onClick(int which);
	}
	
}