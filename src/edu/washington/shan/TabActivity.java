package edu.washington.shan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TabActivity extends Activity{
	
	private static final String TAG="TabActivity";
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_activity);
        
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String url = extras.getString(Constants.KEY_URL);

            if (url != null) {
                //mUrl.setText(url);
            	Log.v(TAG, "URL passed in: " + url);
            }
        }
        
        // wire the button
        Button button = (Button)findViewById(R.id.demo_activity_button1);
        button.setOnClickListener(buttonClickHandler);
    }
	
	private OnClickListener buttonClickHandler = new OnClickListener(){
		public void onClick(View view){
			Intent intent = new Intent(Constants.ADDNEWTAB_ACTION);
			sendBroadcast(intent);
		}
	};
}
