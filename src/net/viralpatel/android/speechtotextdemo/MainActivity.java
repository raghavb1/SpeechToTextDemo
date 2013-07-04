package net.viralpatel.android.speechtotextdemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("DefaultLocale")
public class MainActivity extends Activity {

	protected static final int RESULT_SPEECH = 1;


	private ImageButton btnSpeak;
	private TextView txtText1;
	private TextToSpeech tts;
	HttpClient httpclient = new DefaultHttpClient();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		txtText1 = (TextView) findViewById(R.id.txtText1);
		//txtText1.setText("");
		
		//txtText2 = (TextView) findViewById(R.id.txtText2);

		btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
		

		btnSpeak.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				
				//txtText1.setText("");
				//txtText2.setText("");
				
				Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
				
				

				try {
					startActivityForResult(intent, RESULT_SPEECH);
					
				} catch (ActivityNotFoundException a) {
					Toast t = Toast.makeText(getApplicationContext(),
							"Ops! Your device doesn't support Speech to Text",
							Toast.LENGTH_SHORT);
					t.show();
				}
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
	    
	    
		switch (requestCode) {
		case RESULT_SPEECH: {
			if (resultCode == RESULT_OK && null != data) {

				ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				
				
				
				String s=text.get(0);
				String p="http://napi.maluuba.com/v0/interpret?phrase="+URLEncoder.encode(s)+"&apikey=tHsREF4OYsH2gm9Yv7lmXmkurVmrYx2U";
				
				//txtText1.setBackgroundColor(Color.WHITE);
				
				//txtText1.setText(s+txtText1.getText()+"\n");
				
				HttpGet httpget = new HttpGet(p);
				HttpResponse response;
				 try 
			        { 
			          response = httpclient.execute(httpget);
			          if(response.getStatusLine().getStatusCode() == 200)
			          {
			            HttpEntity entity = response.getEntity();
			            if (entity != null) 
			            {
			            // A Simple JSON Response Read
			            InputStream instream = entity.getContent();
			            String jsonout=convertStreamToString(instream);
			            //txtText1.append("\n"+jsonout);
			            JSONObject mainObject = new JSONObject(jsonout);
			            String category = mainObject.getString("category").toLowerCase();
			            //String action=mainObject.getString("action");
			            JSONObject entities=mainObject.getJSONObject("entities");
			            //txtText1.append();
			            
			            
			            if(category.equals("calendar")){
			            	Calendar cal = Calendar.getInstance();
			            	//JSONObject time = (JSONObject) entities.getJSONArray("timeRange").get(0);
			            	Intent intent = new Intent(Intent.ACTION_EDIT);
			            	intent.setType("vnd.android.cursor.item/event");
			            	intent.putExtra("beginTime", cal.getTimeInMillis());
			            	//intent.putExtra("allDay", true);
			            	intent.putExtra("rrule", "FREQ=YEARLY");
			            	intent.putExtra("endTime", cal.getTimeInMillis()+60*60*1000);
			            	intent.putExtra("title", "Event");
			            	startActivity(intent);
			            	//txtText2.setText(category+" has been set");
			            }
			            else if(category.equals("text")){
			            	
			            	String body = (String) entities.getJSONArray("message").get(0);
			            	JSONObject contactName = (JSONObject) entities.getJSONArray("contacts").get(0);
			            	String num  = getPhoneNumber(contactName.getString("name"), this.getApplicationContext());
			                //String[] tokens = num.split(":");
			                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
			                sendIntent.putExtra("address",num);
			                sendIntent.putExtra("sms_body", body); 
			                sendIntent.setType("vnd.android-dir/mms-sms");
			                startActivity(sendIntent); 
			            }
			            else if(category.equals("music")){
			            	Intent intent = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
			            	startActivity(intent);
			            }			   
			            else if(category.equals("call")){	
				            JSONObject contactName = (JSONObject) entities.getJSONArray("contacts").get(0);
				            String num  = getPhoneNumber(contactName.getString("name"), this.getApplicationContext());
				            String uri = "tel:" + num.trim() ;
				            Intent intent = new Intent(Intent.ACTION_CALL);
				            intent.setData(Uri.parse(uri));
				            startActivity(intent);
			            }
			            else if(category.equals("knowledge") || category.equals("weather")){
			            	String know="http://afreerecharge.com/curly.php?q="+URLEncoder.encode(s);
							HttpGet httpknowget = new HttpGet(know);
							HttpResponse knowresponse;
					          knowresponse = httpclient.execute(httpknowget);
					          if(knowresponse.getStatusLine().getStatusCode() == 200)
					          {
					            HttpEntity knowentity = knowresponse.getEntity();
					            if (entity != null) 
					            {
					            // A Simple JSON Response Read
					            InputStream knowinstream = knowentity.getContent();
					            String knowout=convertStreamToString(knowinstream);
					            
					            /*ImageView iv = new ImageView(null);

								URL url = new URL(knowout);
					            InputStream content = (InputStream)url.getContent();
					            Drawable d = Drawable.createFromStream(content , "src"); 
					            iv.setImageDrawable(d);*/
					            
					            
					            //llayout.addView(iv);
					            txtText1.setText("Q : "+s+"\nA : "+knowout+"\n\n"+txtText1.getText());
					            //tts.speak(knowout.toString(), TextToSpeech.QUEUE_FLUSH, null);
					            }
					          }
			            }
			            else{
			            	txtText1.setText(s+"\nCategory : "+category+"\n\n"+txtText1.getText());
			            }
			            
			            //txtText2.append("--"+category+"--");
			         // Load the requested page converted to a string into a JSONObject.
			           
			            }
			          }
			        }
				 catch (IOException  ex) {
					 txtText1.setText("connection not established");
					    // thrown by line 80 - getContent();
					    // Connection was not established
					    //returnString = "Connection failed; " + ex.getMessage();
				 } catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 
				
				}
			}
			break;
		}

	}
	
	public String getPhoneNumber(String name, Context context) {
		String ret = null;
		String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" like'%" + name +"%'";
		String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER};
		Cursor c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,projection, selection, null, null);
		if (c.moveToFirst()) {
		    ret = c.getString(0);
		}
		c.close();
		if(ret==null)
		    ret = "Unsaved";
		return ret;
		}
	
	
	private static String convertStreamToString(InputStream is) 
    {
         BufferedReader reader = new BufferedReader(new InputStreamReader(is));
         StringBuilder sb = new StringBuilder();
         String line = null;
         try {
            while ((line = reader.readLine()) != null) {
             sb.append(line + "\n");
             }
            } catch (IOException e) 
            {
               e.printStackTrace();
             } finally {
                    try {
                        is.close();
                        } catch (IOException e) {
                        e.printStackTrace();
                      }
                }
                return sb.toString();
     }  
	}
