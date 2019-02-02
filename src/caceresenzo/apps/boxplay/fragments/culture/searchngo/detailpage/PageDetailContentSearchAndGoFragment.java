package caceresenzo.apps.boxplay.fragments.culture.searchngo.detailpage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.javiersantos.bottomdialogs.BottomDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.support.v4.view.AsyncLayoutInflater;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import caceresenzo.android.libs.dialog.DialogUtils;
import caceresenzo.android.libs.intent.CommonIntentUtils;
import caceresenzo.android.libs.internet.AdmAndroidDownloader;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.MangaChapterReaderActivity;
import caceresenzo.apps.boxplay.activities.SearchAndGoDetailActivity;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.dialog.WorkingProgressDialog;
import caceresenzo.apps.boxplay.fragments.BaseBoxPlayFragment;
import caceresenzo.libs.boxplay.common.extractor.ContentExtractionManager;
import caceresenzo.libs.boxplay.common.extractor.ContentExtractionManager.ExtractorType;
import caceresenzo.libs.boxplay.common.extractor.video.VideoContentExtractor;
import caceresenzo.libs.boxplay.common.extractor.video.VideoQualityContentExtractor;
import caceresenzo.libs.boxplay.common.extractor.video.base.BaseVideoContentExtractor;
import caceresenzo.libs.boxplay.common.extractor.video.base.BaseVideoContentExtractor.VideoContentExtractorProgressCallback;
import caceresenzo.libs.boxplay.common.extractor.video.model.VideoQuality;
import caceresenzo.libs.boxplay.culture.searchngo.content.video.IVideoContentProvider;
import caceresenzo.libs.boxplay.culture.searchngo.data.AdditionalResultData;
import caceresenzo.libs.boxplay.culture.searchngo.data.models.SimpleUrlData;
import caceresenzo.libs.boxplay.culture.searchngo.data.models.content.ChapterItemResultData;
import caceresenzo.libs.boxplay.culture.searchngo.data.models.content.VideoItemResultData;
import caceresenzo.libs.boxplay.culture.searchngo.data.models.content.completed.CompletedVideoItemResultData;
import caceresenzo.libs.boxplay.culture.searchngo.result.SearchAndGoResult;
import caceresenzo.libs.bytes.ByteFormat;
import caceresenzo.libs.databridge.ObjectWrapper;
import caceresenzo.libs.filesystem.FileUtils;
import caceresenzo.libs.network.Downloader;
import caceresenzo.libs.thread.implementations.WorkerThread;

/**
 * Content page for the {@link SearchAndGoDetailActivity}
 * 
 * @author Enzo CACERES
 */
public class PageDetailContentSearchAndGoFragment extends BaseBoxPlayFragment {
	
	/* Tag */
	public static final String TAG = PageDetailContentSearchAndGoFragment.class.getSimpleName();
	
	/* Constants */
	public static final String ACTION_STREAMING = "action.streaming";
	public static final String ACTION_DOWNLOAD = "action.download";
	
	/* Actual result */
	private SearchAndGoResult result;
	private List<AdditionalResultData> contents = new ArrayList<>();
	
	/* Views */
	private LinearLayout listLinearLayout;
	private ProgressBar progressBar;
	
	/* Dialog */
	private WorkingProgressDialog progressDialog;
	private DialogCreator dialogCreator;
	
	/* Worker */
	private VideoExtractionWorker videoExtractionWorker;
	
	/* Constructor */
	public PageDetailContentSearchAndGoFragment() {
		super();
		
		this.dialogCreator = new DialogCreator();
		
		this.videoExtractionWorker = new VideoExtractionWorker();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_culture_searchngo_activitypage_details, container, false);
		
		progressBar = (ProgressBar) view.findViewById(R.id.fragment_culture_searchngo_activitypage_details_progressbar_loading);
		
		listLinearLayout = (LinearLayout) view.findViewById(R.id.fragment_culture_searchngo_activitypage_details_linearlayout_list);
		
		progressBar.setVisibility(View.VISIBLE);
		listLinearLayout.setVisibility(View.GONE);
		
		ready();
		
		return view;
	}
	
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		
		Context targetContext = SearchAndGoDetailActivity.getSearchAndGoDetailActivity();
		if (targetContext == null) {
			targetContext = context;
		}
		
		progressDialog = WorkingProgressDialog.create(targetContext);
	}
	
	public void applyResult(SearchAndGoResult result, List<AdditionalResultData> additionals) {
		this.result = result;
		
		this.contents.clear();
		this.contents.addAll(additionals);
		
		listLinearLayout.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.GONE);
		
		createNextContentItemView(new AsyncLayoutInflater(context), additionals.iterator());
	}
	
	private void createNextContentItemView(final AsyncLayoutInflater asyncLayoutInflater, final Iterator<AdditionalResultData> contentIterator) {
		if (contentIterator.hasNext() && isContextValid()) {
			asyncLayoutInflater.inflate(R.layout.item_culture_searchandgo_activitypage_detail_content, listLinearLayout, new AsyncLayoutInflater.OnInflateFinishedListener() {
				@Override
				public void onInflateFinished(View view, int resid, ViewGroup parent) {
					if (!destroyed) {
						new ContentViewBinder(view).bind(contentIterator.next());
						
						parent.addView(view);
						
						createNextContentItemView(asyncLayoutInflater, contentIterator);
					}
				}
			});
		}
	}
	
	/**
	 * View holder for content item
	 * 
	 * @author Enzo CACERES
	 */
	class ContentViewBinder {
		
		/* Views */
		private View view;
		private TextView typeTextView, disabledTextView, contentTextView;
		private ImageView iconImageView, downloadImageView;
		
		/* Variables */
		private String bindedDataUrl;
		
		/* Constructor */
		public ContentViewBinder(View view) {
			this.view = view;
			
			typeTextView = (TextView) view.findViewById(R.id.item_culture_searchandgo_activitypage_detail_content_textview_type);
			disabledTextView = (TextView) view.findViewById(R.id.item_culture_searchandgo_activitypage_detail_content_textview_disabled);
			
			iconImageView = (ImageView) view.findViewById(R.id.item_culture_searchandgo_activitypage_detail_content_imageview_icon);
			contentTextView = (TextView) view.findViewById(R.id.item_culture_searchandgo_activitypage_detail_content_textview_content);
			downloadImageView = (ImageView) view.findViewById(R.id.item_culture_searchandgo_activitypage_detail_content_imageview_download);
		}
		
		public void bind(final AdditionalResultData additionalData) {
			typeTextView.setText(viewHelper.enumToStringCacheTranslation(additionalData.getType()));
			
			int targetRessourceId;
			boolean validType = true, hideDownload = true;
			switch (additionalData.getType()) {
				case ITEM_VIDEO: {
					targetRessourceId = R.drawable.icon_video_library_white_24dp;
					hideDownload = false;
					break;
				}
				
				case ITEM_CHAPTER: {
					targetRessourceId = R.drawable.icon_library_books_24dp;
					break;
				}
				
				default: {
					targetRessourceId = R.drawable.icon_close_black_24dp;
					validType = false;
					break;
				}
			}
			
			iconImageView.setImageResource(targetRessourceId);
			contentTextView.setText(additionalData.convert());
			downloadImageView.setVisibility(hideDownload ? View.GONE : View.VISIBLE);
			
			String url = null;
			boolean checkNeeded = false;
			if (additionalData.getData() instanceof SimpleUrlData && !(additionalData.getData() instanceof CompletedVideoItemResultData)) {
				url = ((SimpleUrlData) additionalData.getData()).getUrl();
				checkNeeded = true;
			}
			
			this.bindedDataUrl = url;
			
			if (url == null && checkNeeded) {
				view.setClickable(false);
				disabledTextView.setVisibility(View.VISIBLE);
			} else {
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
									videoExtractionWorker.applyData((VideoItemResultData) additionalData.getData(), action, ContentViewBinder.this).start();
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
		
		@SuppressWarnings("deprecation")
		public void removeDownload() {
			downloadImageView.setImageDrawable(getResources().getDrawable(R.drawable.icon_open_in_new_white_24dp));
			downloadImageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					if (bindedDataUrl != null) {
						CommonIntentUtils.openUrl(getActivity(), bindedDataUrl);
					} else {
						boxPlayApplication.toast(R.string.boxplay_culture_searchngo_extractor_open_in_brower_invalid_url).show();
					}
				}
			});
		}
	}
	
	/**
	 * Worker thread to extract video direct link
	 * 
	 * @author Enzo CACERES
	 */
	class VideoExtractionWorker extends WorkerThread {
		private VideoItemResultData videoItem;
		private String action;
		private ContentViewBinder viewBinder;
		
		private IVideoContentProvider videoContentProvider;
		private BaseVideoContentExtractor extractor;
		
		private String directUrl;
		
		@Override
		protected void execute() {
			handler.post(new Runnable() {
				@Override
				public void run() {
					progressDialog.update(R.string.boxplay_culture_searchngo_extractor_status_downloading_video_site_url, (videoItem instanceof CompletedVideoItemResultData) ? "<multiple>" : videoItem.getUrl());
				}
			});
			
			final List<String> compatibleVideoPageUrls = new ArrayList<>();
			
			for (String videoPageUrl : videoContentProvider.extractVideoPageUrl(videoItem)) {
				if (ContentExtractionManager.hasCompatibleExtractor(ExtractorType.VIDEO, videoPageUrl)) {
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
								terminate();
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
						boxPlayApplication.toast(R.string.boxplay_culture_searchngo_extractor_status_no_extractor_compatible).show();
						
						viewBinder.removeDownload();
					}
				});
			}
		}
		
		public void processUrl(String videoPageUrl) {
			try {
				extractor = (BaseVideoContentExtractor) ContentExtractionManager.getExtractorFromBaseUrl(ExtractorType.VIDEO, videoPageUrl);
				
				if (extractor == null) {
					throw new NullPointerException(String.format("ContentExtractor is null, site not supported? (page url: %s)", videoPageUrl));
				}
				
				VideoContentExtractorProgressCallback callback = new VideoContentExtractorProgressCallback() {
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
						terminate();
						
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
				};
				
				if (extractor instanceof VideoContentExtractor) {
					directUrl = ((VideoContentExtractor) extractor).extractDirectVideoUrl(videoPageUrl, callback);
				} else if (extractor instanceof VideoQualityContentExtractor) {
					final List<VideoQuality> qualities = ((VideoQualityContentExtractor) extractor).extractVideoQualities(videoPageUrl, callback);
					
					if (qualities != null && !qualities.isEmpty()) {
						VideoQuality targetVideoQuality;
						
						if (qualities.size() == 1) {
							targetVideoQuality = qualities.get(0);
						} else {
							lock();
							
							final ObjectWrapper<VideoQuality> videoQualityObjectWrapper = new ObjectWrapper<VideoQuality>(null);
							
							handler.post(new Runnable() {
								@Override
								public void run() {
									dialogCreator.showAvailableVideoQualitiesDialog(qualities, new VideoQualityDialogCallback() {
										@Override
										public void onClick(final int which) {
											videoQualityObjectWrapper.setValue(qualities.get(which));
											unlock();
										}
									}, new OnCancelListener() {
										@Override
										public void onCancel(DialogInterface dialog) {
											closeDialog();
											terminate();
										}
									});
								}
							});
							
							waitUntilUnlock();
							
							targetVideoQuality = videoQualityObjectWrapper.getValue();
						}
						
						if (targetVideoQuality != null) {
							directUrl = targetVideoQuality.getVideoUrl();
						}
					}
				} else {
					throw new IllegalStateException("Unhandled extractor type: " + extractor.getClass().getSimpleName());
				}
				
				closeDialog();
				
				if (directUrl == null) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							if (!isCancelled()) {
								boxPlayApplication.toast(R.string.boxplay_culture_searchngo_extractor_status_url_not_available).show();
							}
							
							viewBinder.removeDownload();
						}
					});
				} else {
					final String filename = String.format("%s - %s.mp4", FileUtils.replaceIllegalChar(result.getName()), FileUtils.replaceIllegalChar(videoItem.getName()));
					
					switch (action) {
						case ACTION_STREAMING: {
							managers.getVideoManager().openVLC(directUrl, result.getName() + "\n" + videoItem.getName());
							break;
						}
						
						case ACTION_DOWNLOAD: {
							if (isContextValid()) {
								final BottomDialog.Builder bottomDialogBuilder = new BottomDialog.Builder(context) //
										.setTitle(getString(R.string.boxplay_culture_searchngo_download_dialog_file_size_title)) //
										.setContent(getString(R.string.boxplay_culture_searchngo_download_dialog_file_size_message_loading, filename)) //
										.setPositiveText(R.string.boxplay_culture_searchngo_download_dialog_file_size_button_continue) //
										.onPositive(new BottomDialog.ButtonCallback() {
											@Override
											public void onClick(BottomDialog dialog) {
												AdmAndroidDownloader.askDownload(boxPlayApplication, directUrl, filename, viewHelper.isAdmEnabled());
												forceDestroy();
											}
										}) //
										.setNegativeText(R.string.boxplay_culture_searchngo_download_dialog_file_size_button_cancel); //
								
								final ObjectWrapper<BottomDialog> fileSizeBottomDialogObjectWrapper = new ObjectWrapper<BottomDialog>(null);
								
								lock();
								handler.post(new Runnable() {
									@Override
									public void run() {
										fileSizeBottomDialogObjectWrapper.setValue(bottomDialogBuilder.build());
										unlock();
									}
								});
								waitUntilUnlock();
								
								final BottomDialog fileSizeBottomDialog = fileSizeBottomDialogObjectWrapper.getValue();
								
								handler.post(new Runnable() {
									@Override
									public void run() {
										fileSizeBottomDialog.show();
									}
								});
								
								final String fileSize = ByteFormat.toHumanBytes(Downloader.getFileSize(directUrl));
								handler.post(new Runnable() {
									@Override
									public void run() {
										if (fileSizeBottomDialog.getBuilder().getBaseDialog().isShowing()) {
											fileSizeBottomDialog.getContentTextView().setText(getString(R.string.boxplay_culture_searchngo_download_dialog_file_size_message, filename, fileSize));
										}
									}
								});
							}
							break;
						}
						
						default: {
							throw new IllegalStateException("Unknown action: " + action);
						}
					}
				}
			} catch (Exception exception) {
				if (extractor != null) {
					extractor.notifyException(exception);
				} else {
					Log.e(TAG, "Can't print in the extractor's logger (null)", exception);
				}
				
				boxPlayApplication.toast(R.string.boxplay_culture_searchngo_extractor_error_failed_to_extract, exception.getLocalizedMessage());
			}
			
			if (managers.getDebugManager().openLogsAtExtractorEnd() && extractor != null) {
				DialogUtils.showDialog(BoxPlayApplication.getHandler(), getContext(), "Extraction logs", extractor.getLogger().getContent());
			}
		}
		
		@Override
		protected void done() {
			closeDialog();
		}
		
		@Override
		protected void cancel() {
			done();
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
		public VideoExtractionWorker applyData(VideoItemResultData videoItem, String action, ContentViewBinder viewBinder) {
			this.videoItem = videoItem;
			this.action = action;
			this.viewBinder = viewBinder;
			
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
		
		public void showAvailableVideoQualitiesDialog(List<VideoQuality> qualities, final VideoQualityDialogCallback callback, OnCancelListener onCancelListener) {
			AlertDialog.Builder builder = createBuilder();
			builder.setTitle(getString(R.string.boxplay_culture_searchngo_extractor_dialog_available_video_qualities));
			
			String[] itemArray = new String[qualities.size()];
			
			for (int i = 0; i < qualities.size(); i++) {
				itemArray[i] = qualities.get(i).getResolution();
			}
			
			builder.setItems(itemArray, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					callback.onClick(which);
				}
			});
			
			builder.setOnCancelListener(onCancelListener);
			
			builder.create().show();
		}
	}
	
	interface PossiblePlayerDialogCallback {
		void onClick(int which);
	}
	
	interface VideoQualityDialogCallback {
		void onClick(int which);
	}
	
}