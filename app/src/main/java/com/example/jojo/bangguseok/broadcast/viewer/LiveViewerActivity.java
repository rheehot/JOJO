package com.example.jojo.bangguseok.broadcast.viewer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jojo.bangguseok.R;
import com.example.jojo.bangguseok.chatting.ChatAdapter;
import com.example.jojo.bangguseok.chatting.ChatVO;
import com.example.jojo.bangguseok.login.FirebasePost_url;
import com.example.jojo.bangguseok.login.MainActivity;
import com.example.jojo.bangguseok.login.MyApplication;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.DebugTextViewHelper;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import android.media.MediaPlayer;



import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LiveViewerActivity extends AppCompatActivity {
    ArrayList<ChatVO> list = new ArrayList<>();
    ListView lv;
    Button btn;
    EditText edt;
    int imageID = R.drawable.profile;

    String id = "";
    String message = "chat";
    String room = "";

    VideoView videoView;
    VideoView videoView2;

    Query sortby;

    String num;

    AlertDialog.Builder builder;
    AlertDialog alertDialog;

    AlertDialog.Builder builder2;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    String listener1_finish="false";
    String dialog_exit="true";

    int vote_tmp;
    String winner="";

    ValueEventListener postListener1;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_video_viewer);


        MyApplication myApp1 = (MyApplication)getApplicationContext();

        videoView = (VideoView) findViewById(R.id.videoView);
        videoView2 = (VideoView) findViewById(R.id.videoView2);






        videoView.setVideoURI(Uri.parse(myApp1.getSend_url()));
        videoView2.setVideoURI(Uri.parse(myApp1.getGet_url()));

        videoView.start();
        videoView2.start();







        lv = findViewById(R.id.chat_list2);
        edt = findViewById(R.id.chat_message2);
        btn = findViewById(R.id.bnt_send2);

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference(message);

//로그인한 아이디
        MyApplication myApp = (MyApplication)getApplicationContext();
        id = myApp.getname();
        room = getIntent().getStringExtra("room");

        final ChatAdapter adapter = new ChatAdapter(getApplicationContext(), R.layout.talklist, list, id);
        ((ListView) findViewById(R.id.chat_list2)).setAdapter(adapter);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (edt.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "내용을 입력하세요.", Toast.LENGTH_LONG).show();
                } else {
                    Date today = new Date();
                    SimpleDateFormat timeNow = new SimpleDateFormat("a K:mm");

                    StringBuffer sb = new StringBuffer(edt.getText().toString());
                    if (sb.length() >= 15) {
                        for (int i = 1; i <= sb.length() / 15; i++) {
                            sb.insert(15 * i, "\n");
                        }
                    }

//list.add(new ChatVO(R.drawable.profile1, id, sb.toString(), timeNow.format(today)));
//adapter.notifyDataSetChanged();

                    myRef.child(room).push().setValue(new ChatVO(R.drawable.profile, id, sb.toString(), timeNow.format(today)));
                    edt.setText("");
                }
            }
        });

        myRef.child(room).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ChatVO value = dataSnapshot.getValue(ChatVO.class); // 괄호 안 : 꺼낼 자료 형태
                list.add(value);
                adapter.notifyDataSetChanged();
                lv.setSelection(adapter.getCount() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        builder = new AlertDialog.Builder(this);

        builder2 = new AlertDialog.Builder(this);

        postListener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                int count = 1;


                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String key = postSnapshot.getKey();
                    FirebasePost_url get = postSnapshot.getValue(FirebasePost_url.class);
                    String[] info = {get.music_finish};


                   if(info[0].equals("true"))
                   {
                       if(count==2&&listener1_finish=="false")  //둘다 true일 경우 , 투표 한번작동되면 다시 ㄴㄴ
                       {
                           listener1_finish="true";

                           builder.setTitle("투표를 해주세요").setMessage("5초 후에 투표가 마감됩니다.");

                           builder.setPositiveButton("2번", new DialogInterface.OnClickListener(){
                               @Override
                               public void onClick(DialogInterface dialog, int id)
                               {
                                   ValueEventListener postListener3 = new ValueEventListener() {
                                       @Override
                                       public void onDataChange(DataSnapshot dataSnapshot) {

                                           int count=1;


                                           for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                               String key = postSnapshot.getKey();
                                               FirebasePost_url get = postSnapshot.getValue(FirebasePost_url.class);
                                               String[] info = {get.vote};

                                               if(count==2) {
                                                   String v = info[0];
                                                   int result= Integer.parseInt(v)+1;
                                                   databaseReference.child("URL").child("room" + num).child("url_2").child("vote").setValue(""+result);

                                               }


                                               count++;

                                           }


                                       }

                                       @Override
                                       public void onCancelled(DatabaseError databaseError) {
                                           Log.w("getFirebaseDatabase", "loadPost:onCancelled", databaseError.toException());
                                       }
                                   };

                                   String value = "room" + num;

                                   Query sortby2 = FirebaseDatabase.getInstance().getReference().child("URL").child(value);
                                   // sortbyAge.addValueEventListener(postListener);
                                   sortby2.addListenerForSingleValueEvent(postListener3);

                                   Toast.makeText(getApplicationContext(), "2번에게 투표했습니다.", Toast.LENGTH_SHORT).show();
                                   dialog_exit="false";

                               }
                           });

                           builder.setNeutralButton("1번", new DialogInterface.OnClickListener(){
                               @Override
                               public void onClick(DialogInterface dialog, int id)
                               {
                                   ValueEventListener postListener4 = new ValueEventListener() {
                                       @Override
                                       public void onDataChange(DataSnapshot dataSnapshot) {

                                           int count=1;


                                           for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                               String key = postSnapshot.getKey();
                                               FirebasePost_url get = postSnapshot.getValue(FirebasePost_url.class);
                                               String[] info = {get.vote};

                                               if(count==1) {
                                                   String v = info[0];
                                                   int result= Integer.parseInt(v)+1;
                                                   databaseReference.child("URL").child("room" + num).child("url_1").child("vote").setValue(""+result);

                                               }


                                               count++;

                                           }


                                       }

                                       @Override
                                       public void onCancelled(DatabaseError databaseError) {
                                           Log.w("getFirebaseDatabase", "loadPost:onCancelled", databaseError.toException());
                                       }
                                   };

                                   String value = "room" + num;

                                   Query sortby2 = FirebaseDatabase.getInstance().getReference().child("URL").child(value);
                                   // sortbyAge.addValueEventListener(postListener);
                                   sortby2.addListenerForSingleValueEvent(postListener4);

                                   Toast.makeText(getApplicationContext(), "1번에게 투표했습니다.", Toast.LENGTH_SHORT).show();
                                   dialog_exit="false";
                               }
                           });
                           alertDialog = builder.create();

                           alertDialog.show();


                           Handler delayHandler6 = new Handler();
                           delayHandler6.postDelayed(new Runnable() {
                               @Override
                               public void run() {
                                   if(dialog_exit=="true") {

                                       alertDialog.cancel();

                                       Toast toast = Toast.makeText(getApplicationContext(), "투표가 마감되었습니다.", Toast.LENGTH_LONG);
                                       toast.setGravity(Gravity.TOP | Gravity.LEFT, 350, 200);
                                       toast.show();
                                   }

                                   Handler delayHandler6 = new Handler();
                                   delayHandler6.postDelayed(new Runnable() {
                                       @Override
                                       public void run() {

                                           ValueEventListener postListener5 = new ValueEventListener() {
                                               @Override
                                               public void onDataChange(DataSnapshot dataSnapshot) {

                                                   int count=1;

                                                   for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                                       String key = postSnapshot.getKey();
                                                       FirebasePost_url get = postSnapshot.getValue(FirebasePost_url.class);
                                                       String[] info = { get.vote};

                                                       if(count==2)
                                                       {
                                                           if(vote_tmp>Integer.parseInt(info[0]))
                                                           {
                                                               winner="1번";
                                                           }
                                                           else if(vote_tmp<Integer.parseInt(info[0]))
                                                           {
                                                               winner="2번";
                                                           }

                                                       }

                                                       vote_tmp=Integer.parseInt(info[0]);
                                                       count++;

                                                   }


                                               }


                                               @Override
                                               public void onCancelled(DatabaseError databaseError) {
                                                   Log.w("getFirebaseDatabase", "loadPost:onCancelled", databaseError.toException());

                                               }
                                           };


                                           String value = "room" + num;
                                           // String sort_column_name = "get_url";
                                           Query sortbyAge = FirebaseDatabase.getInstance().getReference().child("URL").child(value);
                                           // sortbyAge.addValueEventListener(postListener);
                                           sortbyAge.addListenerForSingleValueEvent(postListener5);


                                           Handler delayHandler7 = new Handler();
                                           delayHandler7.postDelayed(new Runnable() {
                                               @Override
                                               public void run() {

                                                   if(winner.equals(""))
                                                   {
                                                       builder2.setTitle("").setMessage("수고하셨습니다. 무승부 입니다.");
                                                   }
                                                   else {
                                                       builder2.setTitle("").setMessage("수고하셨습니다. 우승자는 " +winner+" 입니다");
                                                   }


                                                   builder2.setPositiveButton("확인", new DialogInterface.OnClickListener(){
                                                       @Override
                                                       public void onClick(DialogInterface dialog, int id)
                                                       {

                                                       }
                                                   });

                                                   AlertDialog alertDialog = builder2.create();
                                                   alertDialog.show();

                                               }
                                           }, 1500);   //이거 나중에 바꾸기




                                       }
                                   }, 8000);   //이거 나중에 바꾸기


                               }
                           }, 7000);  //dialog가 아직 존재하면(투표를 끝까지안할경우) 다이아로그 취소

                       }
                       count++;

                   }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("getFirebaseDatabase", "loadPost:onCancelled", databaseError.toException());

            }
        };

        MyApplication myApp2 = (MyApplication)getApplicationContext();
        num=myApp2.getUrl_room();

        String value = "room" + num;
        // String sort_column_name = "get_url";
        sortby = FirebaseDatabase.getInstance().getReference().child("URL").child(value);
        // sortbyAge.addValueEventListener(postListener);
        sortby.addValueEventListener(postListener1);




    }


    public void room_exit(View view) {

        videoView.pause();
        videoView2.pause();

        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        sortby.removeEventListener(postListener1);
    }
}


