package spider.nitt.pptrem;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current feature session.
 */
public class Bluetoothfeature extends Activity {
    // Debugging
    private static final String TAG = "Bluetoothfeature";
    private static final boolean D = true;

    // Message types sent from the BluetoothfeatureService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothfeatureService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Layout Views
   // private TextView mTitle;
  //  private ListView mConversationView;
   // private EditText mOutEditText;
    //private Button mSendButton;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
   // private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the feature services
    private BluetoothfeatureService mfeatureService = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");

        
        
        // Set up the window layout
      //  requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
       // getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

        (findViewById(R.id.btn1)).setVisibility(View.GONE);
        (findViewById(R.id.btn2)).setVisibility(View.GONE);
        (findViewById(R.id.btn4)).setVisibility(View.GONE);
		
        
        // Set up the custom title
       // mTitle = (TextView) findViewById(R.id.title_left_text);
        //mTitle.setText(R.string.app_name);
       // mTitle = (TextView) findViewById(R.id.title_right_text);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupfeature() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the feature session
        } else {
            if (mfeatureService == null) setupfeature();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mfeatureService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mfeatureService.getState() == BluetoothfeatureService.STATE_NONE) {
              // Start the Bluetooth feature services
              mfeatureService.start();
            }
        }
    }

    private void setupfeature() {
        Log.d(TAG, "setupfeature()");

        // Initialize the BluetoothfeatureService to perform bluetooth connections
        mfeatureService = new BluetoothfeatureService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    @Override
    public synchronized void onPause() {
    	super.onPause();
    	if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
    	super.onStop();
    	try{
        	if (mfeatureService != null) mfeatureService.stop();
        }catch(Exception e){
        	// do nothing
        }
    	finish();
    	if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
        try{
        	if (mfeatureService != null) mfeatureService.stop();
        }catch(Exception e){
        	// do nothing
        }
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mfeatureService.getState() != BluetoothfeatureService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothfeatureService to write
            byte[] send = message.getBytes();
            mfeatureService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
//            mOutEditText.setText(mOutStringBuffer);
        }
    }

  
    // The Handler that gets information back from the BluetoothfeatureService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothfeatureService.STATE_CONNECTED:
                   // mTitle.setText(R.string.title_connected_to);
                   // mTitle.append(mConnectedDeviceName);
  //                  mConversationArrayAdapter.clear();
                    break;
                case BluetoothfeatureService.STATE_CONNECTING:
                   // mTitle.setText(R.string.title_connecting);
                    break;
                case BluetoothfeatureService.STATE_LISTEN:
                case BluetoothfeatureService.STATE_NONE:
                   // mTitle.setText(R.string.title_not_connected);
                    break;
                }
                break;
            case MESSAGE_WRITE:
                break;
            case MESSAGE_READ:
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                mfeatureService.connect(device);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a feature session
                setupfeature();
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.scan:
            // Launch the DeviceListActivity to see devices and do scan
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            return true;
        case R.id.discoverable:
            // Ensure this device is discoverable by others
            ensureDiscoverable();
            return true;
        }
        return false;
    }
    
    
    public void btn(View v){
    	switch(v.getId()){
    	case R.id.btn1:
    		sendMessage("L");
    		break;
    	case R.id.btn2:
    		sendMessage("R");
    		break;
    	case R.id.btn3:
    		sendMessage("S");
            (findViewById(R.id.btn1)).setVisibility(View.VISIBLE);
            (findViewById(R.id.btn2)).setVisibility(View.VISIBLE);
            (findViewById(R.id.btn4)).setVisibility(View.VISIBLE);
    		(findViewById(R.id.btn3)).setVisibility(View.GONE);
    		break;
    	case R.id.btn4:
    		sendMessage("E");
            (findViewById(R.id.btn1)).setVisibility(View.GONE);
            (findViewById(R.id.btn2)).setVisibility(View.GONE);
            (findViewById(R.id.btn4)).setVisibility(View.GONE);
    		(findViewById(R.id.btn3)).setVisibility(View.VISIBLE);
    		break;
    	case R.id.home:
    		Intent OpenAct = new Intent("spider.nitt.pptrem.MENU"); 
			startActivity(OpenAct);
    		break;
    	}
    }
    

}