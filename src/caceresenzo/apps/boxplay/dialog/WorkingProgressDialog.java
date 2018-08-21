package caceresenzo.apps.boxplay.dialog;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import caceresenzo.apps.boxplay.R;

/**
 * Basic dialog progress, uncancellable, and updatable
 * 
 * @author Enzo CACERES
 */
public class WorkingProgressDialog {
	private Context context;
	
	private AlertDialog.Builder dialogBuilder;
	private AlertDialog dialog;
	
	private TextView statusTextView;
	
	/**
	 * Private constructor
	 */
	private WorkingProgressDialog(Context context) {
		this.context = context;
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		dialogBuilder = new AlertDialog.Builder(context);
		
		View dialogView = inflater.inflate(R.layout.dialog_progress_working, null);
		
		statusTextView = (TextView) dialogView.findViewById(R.id.dialog_progress_working_textview_status);
		
		dialogBuilder.setView(dialogView);
		dialogBuilder.setCancelable(false);
		
		dialog = dialogBuilder.create();
	}
	
	/**
	 * Open the dialog if not already open
	 */
	public void show() {
		if (!dialog.isShowing()) {
			dialog.show();
		}
	}
	
	/**
	 * Close/hide the dialog if already open
	 */
	public void hide() {
		if (dialog.isShowing()) {
			dialog.dismiss();
		}
	}
	
	/**
	 * Same function as {@link #update(int, Object...)} but without formatting arguments
	 * 
	 * @param ressourceId
	 *            String ressource id {@link R.string}
	 */
	public void update(int ressourceId) {
		update(context.getString(ressourceId));
	}
	
	/**
	 * Update the progres text with a string ressource and some formatting arguments
	 * 
	 * @param ressourceId
	 *            String ressource id {@link R.string}
	 * @param arguments
	 *            Array of argument used for formatting
	 */
	public void update(int ressourceId, Object... arguments) {
		update(context.getString(ressourceId, arguments));
	}
	
	/**
	 * Update progress text
	 * 
	 * @param text
	 *            New progress text
	 */
	public void update(String text) {
		statusTextView.setText(text);
	}
	
	/**
	 * Create a new instance of the {@link WorkingProgressDialog}
	 * 
	 * @return A new instance
	 */
	public static WorkingProgressDialog create(Context context) {
		return new WorkingProgressDialog(context);
	}
	
}