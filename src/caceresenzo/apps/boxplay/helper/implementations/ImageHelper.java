package caceresenzo.apps.boxplay.helper.implementations;

import java.util.HashMap;
import java.util.Map;

import com.budiyev.android.imageloader.ImageLoader;
import com.budiyev.android.imageloader.ImageRequest;

import android.graphics.drawable.ColorDrawable;
import android.widget.ImageView;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.helper.AbstractHelper;
import caceresenzo.apps.boxplay.helper.HelperManager;

public class ImageHelper extends AbstractHelper {
	
	/* Managers */
	private ImageLoader imageLoader;
	
	/* Constructor */
	public ImageHelper(BoxPlayApplication boxPlayApplication) {
		super(boxPlayApplication);
	}
	
	@Override
	public void initialize(HelperManager helperManager) {
		super.initialize(helperManager);
		
		imageLoader = ImageLoader.builder(boxPlayApplication) //
				.storageCache(52428800L) // 50 mb
				.build();
	}
	
	/**
	 * Get a {@link RequestBuilder} to download image to a {@link ImageView} and chose settings.
	 * 
	 * @param imageView
	 *            Target {@link ImageView}.
	 * @param url
	 *            Target url.
	 * @return A {@link RequestBuilder} instance.
	 */
	public RequestBuilder download(ImageView imageView, String url) {
		return new RequestBuilder(imageLoader, imageView, url);
	}
	
	/**
	 * Clear the cache.
	 */
	public void clearImageCache() {
		imageLoader.clearMemoryCache();
		imageLoader.clearStorageCache();
		imageLoader.clearAllCaches();
	}
	
	public static class RequestBuilder {
		
		/* Variables */
		private final ImageLoader imageLoader;
		private final ImageView imageView;
		private final String url;
		private Map<String, Object> headers;
		private int[] newSize;
		
		/* Constructor */
		private RequestBuilder(ImageLoader imageLoader, ImageView imageView, String url) {
			this.imageLoader = imageLoader;
			this.imageView = imageView;
			this.url = url;
		}
		
		/**
		 * Set custom headers to your request.
		 * 
		 * @param headers
		 *            Target headers.
		 * @return this for method chaining (fluent API).
		 */
		public RequestBuilder headers(Map<String, Object> headers) {
			this.headers = headers;
			
			return this;
		}
		
		/**
		 * Add a header key-value to the request headers.
		 * 
		 * @param key
		 *            Target key to add.
		 * @param value
		 *            Corresponding value.
		 * @return this for method chaining (fluent API).
		 */
		public RequestBuilder header(String key, Object value) {
			if (headers == null) {
				headers = new HashMap<>();
			}
			
			headers.put(key, value);
			
			return this;
		}
		
		/**
		 * Set a resized value for the downloaded image.
		 * 
		 * @param newWidth
		 *            New image width size.
		 * @param newHeight
		 *            New image height size.
		 * @return this for method chaining (fluent API).
		 */
		public RequestBuilder resize(int newWidth, int newHeight) {
			newSize = new int[] { newWidth, newHeight };
			
			return this;
		}
		
		/**
		 * Validate the request and start downloading the image (added to a queue).
		 */
		public void validate() {
			if (imageView == null) {
				return;
			}
			
			if (url == null) {
				imageView.setImageDrawable(new ColorDrawable(color(R.color.colorError)));
				return;
			}
			
			ImageRequest<String> loader = imageLoader //
					.from(url) //
					.errorDrawable(new ColorDrawable(color(R.color.colorError))) //
					.httpHeaders(headers); //
			
			if (newSize != null) {
				loader.size(newSize[0], newSize[1]);
			}
			
			loader.load(imageView);
		}
		
	}
	
}