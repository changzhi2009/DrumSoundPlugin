package com.perfectpiano.drum.analogkit;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.util.ByteArrayBuffer;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.TypedValue;

import com.gamestar.pianoperfect.plugin.aidl.IClientCallback;
import com.gamestar.pianoperfect.plugin.aidl.TonePlugin.Stub;

public class DrumSoundPluginService extends Service {
	
	/**
	 * Select one sound type below and return in getInstrumentType() method.
	 */
	@SuppressWarnings("unused")
	private static final String PLUGIN_TYPE_FOR_KEYBOARD = "keyboard";	
	private static final String PLUGIN_TYPE_FOR_DRUM = "drumkit";
	
	/**
	 * Choose PLUGIN_TYPE_FOR_KEYBOARD.
	 */
	private static final String PluginType = PLUGIN_TYPE_FOR_DRUM;
	
	/**
	 * Drawable res id of icon design for Perfect Piano Instrument Switch Menu
	 */
	private static final int MenuIconResID = R.drawable.ic_launcher;
	
	/**
	 * Drawable res id of icon design for Perfect Piano tool bar
	 */
	private static final int ToolBarIconResID = R.drawable.ic_launcher;
	
	/**
	 * String res id of this instrument
	 */
	private static final int InstrumentNameID = R.string.ins_name;
	
	/**
	 * Raw sound files name in Assert. It should follow the order of the DrumPad screen of Perfect Piano.
	 * The order is: OPEN HI-HAT, RIDE CYMBAL, CABASA, CRASH-CYMBAL, CLOSE HI-HAT, LOW TOM, MID TOM, HIGH TOM, COWBELL, CLAP, SNARE, KICK DRUM.
	 * This size must be 12.
	 */
	private final static String[] SOUND_NAME_ARRAY = {
		"rock_openhh.wav",	"rock_ride.wav",	"rock_cabasa.wav",	"rock_crash_cymbal.wav",
		"rock_closehh.wav",	"rock_tom1.wav",	"rock_tom2.wav",	"rock_tom3.wav",
		"rock_cowbell.wav",	"rock_clap.wav",	"rock_snare.wav",	"rock_kick.wav"
	};
	
	/**
	 * This value means stop play the drum sound immediately after the finger up.
	 */
	private final static int STOP_PLAY_ON_NOTE_OFF = 0;
	/**
	 * This value means do not stop play the drum sound after the finger up. And wait for the sound file play back complete.
	 */
	private final static int NOT_STOP_PLAY_ON_NOTE_OFF = 1;
	
	/**
	 * Notice: This array doesn't work on Perfect Piano v5.8.3. It will work on a newer release.	 * 
	 * 
	 * This used to define how to play back a drum sound when touch a drum icon on Drum Pad。 The index must be the same as SOUND_NAME_ARRAY.
	 * For Example in below array: 
	 * 				rock_openhh.wav -> NOT_STOP_PLAY_ON_NOTE_OFF.
	 * 				rock_closehh.wav -> STOP_PLAY_ON_NOTE_OFF
	 */
	private static final int DrumPlayTypeArray[] = {
		NOT_STOP_PLAY_ON_NOTE_OFF, NOT_STOP_PLAY_ON_NOTE_OFF, NOT_STOP_PLAY_ON_NOTE_OFF, NOT_STOP_PLAY_ON_NOTE_OFF,
		STOP_PLAY_ON_NOTE_OFF, STOP_PLAY_ON_NOTE_OFF, STOP_PLAY_ON_NOTE_OFF, STOP_PLAY_ON_NOTE_OFF,
		NOT_STOP_PLAY_ON_NOTE_OFF, NOT_STOP_PLAY_ON_NOTE_OFF, NOT_STOP_PLAY_ON_NOTE_OFF, NOT_STOP_PLAY_ON_NOTE_OFF
	};
	
	
	private IBinder mBinder = new PluginServiceBinder();
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
	private static final int MAX_FILE_SIZE = 128;
	
	/**
	 * Save raw sound file from Asserts to the given directory in External Storage
	 * 
	 * @param dir: value pass from Perfect Piano Plugin Framework.
	 */
	private void savePluginSounds(String dir) {
		File dirFile = new File(dir);
		if (!dirFile.exists()) {
			dirFile.mkdir();
		}

		AssetManager mngr = getAssets();
		int length = SOUND_NAME_ARRAY.length;
		for(int i=0; i<length; i++) {
			String name = SOUND_NAME_ARRAY[i];
			try {
				File file = new File(dir + File.separator + name);
				if (!file.exists()) {
					InputStream path = mngr.open(name);
					BufferedInputStream bis = new BufferedInputStream(path,
							1024 * MAX_FILE_SIZE);
					ByteArrayBuffer baf = new ByteArrayBuffer(
							1024 * MAX_FILE_SIZE);
					// get the bytes one by one
					int current = 0;

					while ((current = bis.read()) != -1) {

						baf.append((byte) current);
					}
					byte[] data = baf.toByteArray();
					FileOutputStream fos;
					fos = new FileOutputStream(file);
					fos.write(data);
					fos.flush();
					fos.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private Bitmap getPluginMenuItemIcon() {
		Resources res = this.getResources();
		TypedValue value = new TypedValue();
		res.openRawResource(MenuIconResID, value);
	    BitmapFactory.Options opts = new BitmapFactory.Options();
	    opts.inTargetDensity = value.density;
	    return BitmapFactory.decodeResource(res, MenuIconResID, opts);
	}
	
	private Bitmap getPluginToolbarIcon() {
		Resources res = this.getResources();
		TypedValue value = new TypedValue();
		res.openRawResource(ToolBarIconResID, value);
	    BitmapFactory.Options opts = new BitmapFactory.Options();
	    opts.inTargetDensity = value.density;
	    return BitmapFactory.decodeResource(res, ToolBarIconResID, opts);
	}
	
	/**
	 * Please refer the comments in aidl file.
	 * 
	 * @author Administrator
	 *
	 */
	private final class PluginServiceBinder extends Stub{
		
		/**
		*	Mandatory Implemented Method.
		*	Get the tone type. This is a keyboard or Drum tone.
		*	Must return "keyboard" or "drumkit" in String.
		**/
		@Override
		public String getInstrumentType() throws RemoteException {
			// TODO Auto-generated method stub
			return PluginType;
		}
		
		/**
		*	Mandatory Implemented Method.
		*	Return Tone's display name
		**/
		@Override
		public String getToneName() throws RemoteException {
			// TODO Auto-generated method stub
			return getString(InstrumentNameID);
		}
		
		/**
		*	Mandatory Implemented Method.
		*	Return the icon showed in Tone selection menu.
		**/
		@Override
		public Bitmap getMenuItemIcon() throws RemoteException {
			// TODO Auto-generated method stub
			return getPluginMenuItemIcon();
		}
		
		/**
		*	Mandatory Implemented Method.
		*	Return the icon showed in Toolbar.
		**/
		@Override
		public Bitmap getToolbarIcon() throws RemoteException {
			// TODO Auto-generated method stub
			return getPluginToolbarIcon();
		}
		
		/**
		*	Mandatory Implemented Method.
		*	Return the sound src file name array. 
		**/
		@Override
		public String[] getToneFileNameArray() throws RemoteException {
			// TODO Auto-generated method stub
			return SOUND_NAME_ARRAY;
		}
		
		/**
		*	Mandatory Implemented Method.
		*	Inform the plugin to save sound files to sdcard. The directory must be /sdcard/PerfectPiano/plugin/packagename/. 
		*	Notice: The callback function "void pluginInitFinished();" define in IClientCallback.aidl must be called in the end of this function 
		*	after the initialization finished.
		**/
		@Override
		public void initPlugin(String pluginPath) throws RemoteException {
			savePluginSounds(pluginPath);
			initFinished();
		}

		/**
		*	Mandatory Implemented Method.
		*   For Keyboard:
		*		Return keyboard sound index array if "keyboard" type. More detail information please visit the developer website.
		*
		*	For Drum:
		*		This used to define how to play back a drum sound when touch a drum icon on Drum Pad。 The index must be the same as SOUND_NAME_ARRAY.
		*	 	Notice: This array doesn't work on Perfect Piano v5.8.3. It will work on a newer release.
		*
		*	www.revontuletsoft.com/developer.html
		**/
		@Override
		public int[] getKeyboardSoundsIndexArray() throws RemoteException {
			// TODO Auto-generated method stub
			return DrumPlayTypeArray;
		}

		/**
		*	Mandatory Implemented Method.
		*   Return null if "drumkit" type.
		*	Return keyboard sound quick choose array if "keyboard" type. More detail information please visit the developer website.
		*	www.revontuletsoft.com/developer.html
		**/
		@Override
		public int[] getKeyboardQuickArrayForChoosedIndexArray()
				throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}
 
		/**
		 * all sound files have been saved to the sdcard.
		 * notify the Perfect Piano App that the plugin initialization finished.
		 * 
		 */
	    public void initFinished() {
	        try {
	            mCallbacks.beginBroadcast();
	            // now for time being we will consider only one activity is bound to the service, so hardcode 0
	            mCallbacks.getBroadcastItem(0).pluginInitFinished();
	            mCallbacks.finishBroadcast();
	        } catch (RemoteException e) {
	            e.printStackTrace();
	        }
	    }
	    
	    final RemoteCallbackList<IClientCallback> mCallbacks = new RemoteCallbackList<IClientCallback>();
		
	    /**
		*	Mandatory Implemented Method.
		*	Helper method to register the above callback function. 
		**/
		@Override
		public void registerCallBack(IClientCallback cb) throws RemoteException {
			// TODO Auto-generated method stub
	        if(cb!=null){
	            mCallbacks.register(cb);
	        }
		}
		
		/**
		*	Optional Implemented Method.
		*	Return the program id in General MIDI Spec
		*	For example: 10 -> Music Box
		**/
		@Override
		public int getTone() throws RemoteException {
			return 25;
		}
		
		/**
		*	Mandatory Implemented Method.
		*	Return apk package name.
		**/
		@Override
		public String obtainPackageName() throws RemoteException {
			return getPackageName();
		} 
	}
}
