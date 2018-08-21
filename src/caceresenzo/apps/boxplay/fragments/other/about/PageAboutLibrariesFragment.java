package caceresenzo.apps.boxplay.fragments.other.about;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;

public class PageAboutLibrariesFragment extends Fragment {
	
	private RecyclerView recyclerView;
	
	private final List<LibraryItem> libraries;
	
	public PageAboutLibrariesFragment() {
		this.libraries = new ArrayList<LibraryItem>();
		
		this.libraries.add(new LibraryItem("TapTargetView", "KeepSafe", "https://github.com/KeepSafe/TapTargetView"));
		this.libraries.add(new LibraryItem("changeloglib", "gabrielemariotti", "https://github.com/gabrielemariotti/changeloglib"));
		this.libraries.add(new LibraryItem("UCE-Handler", "RohitSurwase", "https://github.com/RohitSurwase/UCE-Handler"));
		this.libraries.add(new LibraryItem("ExpandableLayout", "KyoSherlock", "https://github.com/KyoSherlock/ExpandableLayout"));
		this.libraries.add(new LibraryItem("image-loader", "yuriy-budiyev", "https://github.com/yuriy-budiyev/image-loader"));
		this.libraries.add(new LibraryItem("android-about-page", "medyo", "https://github.com/medyo/android-about-page"));
		this.libraries.add(new LibraryItem("StyleableToast", "Muddz", "https://github.com/Muddz/StyleableToast"));
		this.libraries.add(new LibraryItem("android-support-v7-appcompat", "Google"));
		this.libraries.add(new LibraryItem("android-support-v4", "Google"));
		this.libraries.add(new LibraryItem("android-support-fragment", "Google"));
		this.libraries.add(new LibraryItem("android-support-design", "Google"));
		this.libraries.add(new LibraryItem("android-support-v7-cardview", "Google"));
		this.libraries.add(new LibraryItem("android-support-v7-recyclerview", "Google"));
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_about_libraries, container, false);
		
		recyclerView = (RecyclerView) view.findViewById(R.id.fragment_about_libraries_recyclerview_list);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		recyclerView.setAdapter(new LibraryViewAdapter(this.libraries));
		recyclerView.setHasFixedSize(true);
		recyclerView.setNestedScrollingEnabled(false);
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		recyclerView.requestFocus();
	}
	
	class LibraryViewAdapter extends RecyclerView.Adapter<LibraryViewHolder> {
		private List<LibraryItem> list;
		
		public LibraryViewAdapter(List<LibraryItem> list) {
			this.list = list;
		}
		
		@Override
		public LibraryViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_about_libraries_cardview, viewGroup, false);
			return new LibraryViewHolder(view);
		}
		
		@Override
		public void onBindViewHolder(LibraryViewHolder viewHolder, int position) {
			LibraryItem item = list.get(position);
			viewHolder.bind(item);
		}
		
		@Override
		public int getItemCount() {
			return list.size();
		}
	}
	
	class LibraryViewHolder extends RecyclerView.ViewHolder {
		private TextView contentTextView;
		
		private int indexOf = 0;
		
		public LibraryViewHolder(View itemView) {
			super(itemView);
			
			contentTextView = (TextView) itemView.findViewById(R.id.item_about_libraries_layout_textview_content);
			
			itemView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View view) {
					try {
						if (libraries.get(indexOf++).getUrl() == null) {
							BoxPlayApplication.getBoxPlayApplication().toast(getString(R.string.boxplay_other_about_libraries_error_no_url)).show();
						}
					} catch (Exception exception) {
						;
					}
				}
			});
		}
		
		public void bind(LibraryItem libraryItem) {
			contentTextView.setText(getString(R.string.boxplay_other_about_libraries_format, libraryItem.getName(), libraryItem.getAuthor()));
		}
	}
	
	class LibraryItem {
		private String name, author, url;
		
		public LibraryItem(String name, String author) {
			this(name, author, null);
		}
		
		public LibraryItem(String name, String author, String url) {
			this.name = name;
			this.author = author;
			this.url = url;
		}
		
		public String getName() {
			return name;
		}
		
		public String getAuthor() {
			return author;
		}
		
		public String getUrl() {
			return url;
		}
	}
	
}