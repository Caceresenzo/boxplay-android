package caceresenzo.apps.boxplay.fragments.other.about;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.libs.boxplay.models.server.ServerHosting;

public class PageAboutHostingFragment extends Fragment {
	
	private static PageAboutHostingFragment INSTANCE;
	
	private List<ServerHosting> servers;
	
	private RecyclerView recyclerView;
	
	public PageAboutHostingFragment() {
		INSTANCE = this;
		
		this.servers = new ArrayList<ServerHosting>();
		updateServers();
	}
	
	public void updateServers() {
		if (BoxPlayApplication.getManagers().getServerManager() != null) {
			this.servers.addAll(BoxPlayApplication.getManagers().getServerManager().getServerHostings());
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		INSTANCE = this;
		
		View view = inflater.inflate(R.layout.fragment_about_hosting, container, false);
		
		recyclerView = (RecyclerView) view.findViewById(R.id.fragment_about_hosting_recyclerview_host_list);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		recyclerView.setAdapter(new ServerHostingViewAdapter(this.servers));
		
		return view;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		INSTANCE = null;
	}
	
	class ServerHostingViewAdapter extends RecyclerView.Adapter<ServerViewHolder> {
		private List<ServerHosting> list;
		
		public ServerHostingViewAdapter(List<ServerHosting> list) {
			this.list = list;
		}
		
		@Override
		public ServerViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_about_hosting_server, viewGroup, false);
			return new ServerViewHolder(view);
		}
		
		@Override
		public void onBindViewHolder(ServerViewHolder viewHolder, int position) {
			ServerHosting item = list.get(position);
			viewHolder.bind(item);
		}
		
		@Override
		public int getItemCount() {
			return list.size();
		}
	}
	
	class ServerViewHolder extends RecyclerView.ViewHolder {
		private TextView nameTextView, descriptionTextView;
		private ImageView imageImageView;
		
		public ServerViewHolder(View itemView) {
			super(itemView);
			
			nameTextView = (TextView) itemView.findViewById(R.id.item_about_hosting_textview_name);
			descriptionTextView = (TextView) itemView.findViewById(R.id.item_about_hosting_textview_description);
			imageImageView = (ImageView) itemView.findViewById(R.id.item_about_hosting_imageview_image);
		}
		
		public void bind(ServerHosting server) {
			String locale = BoxPlayApplication.getBoxPlayApplication().getLocaleString();
			
			nameTextView.setText(server.getDisplayTranslation(locale));
			descriptionTextView.setText(server.getDescriptionTranslation(locale));
			
			String imageUrl = null;
			if (server.getImageUrl() != null) {
				imageUrl = server.getImageUrl();
			} else if (server.getIconUrl() != null) {
				imageUrl = server.getIconUrl();
			}
			
			if (imageUrl != null) {
				BoxPlayApplication.getViewHelper().downloadToImageView(imageImageView, imageUrl);
			}
		}
	}
	
	public static PageAboutHostingFragment getPageAboutPageFragment() {
		return INSTANCE;
	}
}