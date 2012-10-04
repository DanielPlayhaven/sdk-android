package com.playhaven.src.publishersdk.content;

import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Configuration;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.playhaven.src.utils.PHStringUtil;

/** Represents some abstract content from the advertising servers. 
 * It implements the Parcelable class to ensure
 * that we can pass between activities via the broadcast manager.
 * Note: in the future we may want to simply extend JSONObject? Would be cleaner... 
 */
public class PHContent implements Parcelable {
	public enum TransitionType {
		Unknown, 
		Modal, 
		Dialog
	};

	public static final String PARCEL_NULL = "null";
	
	public TransitionType transition = TransitionType.Modal;
	
	public String closeURL;

	public JSONObject context;

	public Uri url;

	public double closeButtonDelay;
	
	public boolean preloaded = false;

	private HashMap<String, JSONObject> frameDict = new HashMap<String, JSONObject>();

	/** Regular simple constructor */
	public PHContent() {
		closeButtonDelay = 10.0;
		transition = TransitionType.Modal;
	}

	/** Create new PHContent from server json representation */
	public PHContent(JSONObject dict) {
		fromJSON(dict);
	}
	
	/** Creates from Parcel*/
	public PHContent(Parcel in) {
		String transition_str = in.readString();
		if (transition_str != null)
			if ( ! transition_str.equals(PARCEL_NULL))
				transition = TransitionType.valueOf(transition_str);
			
		closeURL = in.readString();
		
		if (closeURL != null && closeURL.equals(PARCEL_NULL))
			closeURL = null;
		
		try {
			String context_str = in.readString();
			if (context_str != null)
				if ( ! context_str.equals(PARCEL_NULL))
					context = new JSONObject(context_str);
			
		} catch (JSONException e) {
			PHStringUtil.log("Error hydrating PHContent JSON context from Parcel: "+e.getLocalizedMessage());
		}
		
		String url_str = in.readString();
		if (url_str != null)
			if ( ! url_str.equals(PARCEL_NULL))
				url = Uri.parse(url_str);
			
		closeButtonDelay = in.readDouble();
		
		preloaded = (in.readByte() == 1);
		
		Bundle frameBundle = in.readBundle();
		if (frameBundle != null) {
			for (String key : frameBundle.keySet()) {
				try {
					frameDict.put(key, new JSONObject(frameBundle.getString(key)));
				} catch (JSONException e) {
					PHStringUtil.log("Error deserializing frameDict from bundle in PHContent");
				}
				
			}
		}
		
	}

	/**
	 * Required Static creator (factory) for loading ourselves from a parcel.
	 */
	public static final Parcelable.Creator<PHContent> CREATOR = 
			new Parcelable.Creator<PHContent>() {
				public PHContent createFromParcel(Parcel in) {
					return new PHContent(in);
				}
				
				public PHContent[] newArray(int size) {
					return new PHContent[size];
				}
			};
			
	/** Attempts to load properties from the specified JSONObject. It loads
	 * what parameters it can from the JSON dictionary. Clients who use
	 * PHContent should always check that it contains their minimum required
	 * set of parameters because it often won't.
	 * @param The server's response (JSON)
	 */
	public void fromJSON(JSONObject dict) {
		try {
				Object frame 			= dict.opt			 	("frame");
				String url 		  		= dict.optString	 	("url");
				String transition 		= dict.optString	 	("transition");
				this.closeButtonDelay 	= dict.optDouble	 	("close_delay");
				this.closeURL 			= dict.optString	 	("close_ping");
				
				
				frameDict.clear();
				if (frame instanceof String)
					//TODO: don't like this at all, JSONObjects are annoying to work with
					frameDict.put((String) frame, new JSONObject(String.format("{\"%s\" : \"%s\"}", frame, frame)));
				else if (frame instanceof JSONObject)
					setFrameDict((JSONObject) frame);

				
				this.url = ((url.compareTo("") != 0) ? Uri.parse(url) : null);

				
				JSONObject context = dict.optJSONObject("context");
				
				if ( ! JSONObject.NULL.equals(context) && 
					   context.length() > 0				 )
				
					this.context = context;
				
				
				if (transition.compareTo("") != 0) {
					if (transition.equals("PH_MODAL"))
						this.transition = TransitionType.Modal;
					else if (transition.equals("PH_DIALOG"))
						this.transition = TransitionType.Dialog;
					else
						this.transition = null;
				}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void setFrameDict(JSONObject frame) {
		frameDict.clear();
		
		try {
			//Note: warning is pointless..'keys' is an iterator of Strings
			@SuppressWarnings("unchecked")
			Iterator<String> keys = frame.keys();
			
			while (keys.hasNext()) {
				String key = keys.next();
				// could be JSONObject or String
				frameDict.put(key, (JSONObject)frame.get(key));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	//TODO: this thing seems a bit overkill (server should really just send -1 for all values if fullscreen)
	public RectF getFrame(int orientation) {
		String orientKey = (orientation == Configuration.ORIENTATION_LANDSCAPE ? "PH_LANDSCAPE"	: "PH_PORTRAIT");
		
		if (frameDict.containsKey("PH_FULLSCREEN")) {
			// create infinitely large values to signify screen fill
			return new RectF(0,0, Integer.MAX_VALUE, Integer.MAX_VALUE);
		} else if (frameDict.containsKey(orientKey)) {
			float x,y,w,h;
			x = 0f;
			y = 0f;
			w = 0f;
			h = 0f;

			//TODO: needs work, what if it's null or if framedict is invalid
			JSONObject dict = (JSONObject)frameDict.get(orientKey);
			if (dict != null) {
				x = (float)dict.optDouble("x");
				y = (float)dict.optDouble("y");
				w = (float)dict.optDouble("w");
				h = (float)dict.optDouble("h");
				return new RectF(x, y, x + w, y + h);
			}

		}

		return new RectF(0.0f,0.0f,0.0f,0.0f);
	}
	
	@Override
	public String toString() {
		String formattedJson = "(NULL)";
		try {
			formattedJson = context.toString(2);
		} catch (JSONException e) {
			e.printStackTrace();
			formattedJson = "(NULL)"; 
		}
		
		
		String message = String.format(
										"Close URL: %s - Close Delay: %.1f - URL: %s\n" +
										"\n" +
										"---------------------------------\n" +
										"JSON Context: %s", 
										closeURL, closeButtonDelay, url, formattedJson
						);
		return message;
	}
	
	
	
	////////////////////////////////////////////////////////
	/////////////////// Parcelable Methods /////////////////
	
	@Override
	public int describeContents() {
		return 0; // no files descriptors..
	}
	
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(transition != null ? transition.name() : PARCEL_NULL);
		out.writeString(closeURL != null ? closeURL : PARCEL_NULL );
		out.writeString(context != null ? context.toString() : PARCEL_NULL);
		out.writeString(url != null ? url.toString() : PARCEL_NULL);
		out.writeDouble(closeButtonDelay);
		out.writeByte((byte) (preloaded ? 1 : 0));
		
		if (frameDict != null) {
			// convert JSONObject to string representation
			Bundle frameBundle = new Bundle();
			for (String key : frameDict.keySet()) {
				frameBundle.putString(key, frameDict.get(key).toString());
			}
			out.writeBundle(frameBundle);
		}		
	}
}
