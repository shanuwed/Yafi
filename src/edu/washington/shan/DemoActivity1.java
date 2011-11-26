package edu.washington.shan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class DemoActivity1 extends Activity{
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_activity);
        
        // wire the button
        Button button = (Button)findViewById(R.id.demo_activity_button1);
        button.setOnClickListener(buttonHandler);
    }
	
	private OnClickListener buttonHandler = new OnClickListener(){
		public void onClick(View view){
			//Intent intent = new Intent();
			//intent.setAction("edu.washington.shan.MYACTION");
			//sendBroadcast(intent);
		}
	};
}
