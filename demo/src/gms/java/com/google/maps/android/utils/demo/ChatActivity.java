package com.google.maps.android.utils.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ChatActivity extends AppCompatActivity {

    String journey_name;
    String cpns;
    String curr_id, roomname;
    private Socket mSocket;

    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    //private List<Message> MessageList;
    //private ChatBoxAdapter chatBoxAdapter;
    private EditText messagetxt;
    private TextView id_and_roomname;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);
        journey_name = getIntent().getStringExtra("journey_name");
        cpns = getIntent().getStringExtra("cpns");
        curr_id = getIntent().getStringExtra("curr_id");
        roomname = journey_name + " / " + cpns;

        recyclerView = findViewById(R.id.chatRecyclerView);
        messagetxt = findViewById(R.id.messageEdit);
        textView = findViewById(R.id.sendBtn);
        id_and_roomname = findViewById(R.id.id_and_roomname);
        id_and_roomname.setText("my id: " + curr_id + "    room: " + roomname);

        try {
            //connect to socket
            mSocket = IO.socket("http://192.249.18.166:80");
            mSocket.connect();

            initializeView(roomname, curr_id);

            //join room
            mSocket.emit("join_chat", curr_id, roomname);

            //if joined, get notification
            mSocket.on("user_join_notif", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String msg = (String) args[0];
                            Toast.makeText(ChatActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

            //when click send button, send new message to server
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //send data to server through socket io
                    String new_message = messagetxt.getText().toString();
                    mSocket.emit("new_message", roomname, curr_id, new_message);

                    ArrayList<String> send = new ArrayList<>();
                    send.add("send"); send.add("msg"); send.add(roomname); send.add(curr_id); send.add(new_message);

                    //store chat to database
                    //store_chat(send);

                    //visualize
                    messageAdapter.addItem(send);
                    recyclerView.smoothScrollToPosition(messageAdapter.getItemCount()-1);
                    messagetxt.setText("");
                }
            });

            //if received new message
            mSocket.on("updatechat", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //get data from server through socket io
                            JSONObject data = (JSONObject) args[0];
                            String roominfo = null;
                            try {
                                roominfo = data.getString("roomname");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            String userinfo = null;
                            try {
                                userinfo = data.getString("id");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            String message = null;
                            try {
                                message = data.getString("message");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            //should not be user's own chat
                            if(!userinfo.equals(curr_id)){
                                ArrayList<String> send = new ArrayList<>();
                                send.add("receive"); send.add("msg"); send.add(roomname); send.add(curr_id); send.add(message);

                                //store chat to database
                                //store_chat(send);

                                //visualize
                                messageAdapter.addItem(send);
                                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount()-1);
                            }
                        }
                    });
                }
            });
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    private void initializeView(String roomname, String curr_id) { //여기에서 데베에 쌓인 데이터 받아와서 다 출력해줘야 함!!!
        messageAdapter = new MessageAdapter(getLayoutInflater());
        recyclerView.setAdapter(messageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        show_chat(roomname, curr_id);
    }

//    private void store_chat(List<String> data){
//        HashMap<String, List<String>> map = new HashMap<>();
//
//        ArrayList<String> arrayList1 = new ArrayList<>();
//        arrayList1.add(data.get(2));
//        map.put("roomname", arrayList1);
//
//        ArrayList<String> arrayList2 = new ArrayList<>();
//        arrayList2.add(data.get(3));
//        map.put("id", arrayList2);
//
//        ArrayList<String> arrayList3 = new ArrayList<>();
//        arrayList3.add(data.get(4));
//        map.put("message", arrayList3);
//
//        Call<Void> call = LoginActivity.retrofitInterface.storeChat(map);
//
//        call.enqueue(new Callback<Void>() {
//            @Override
//            public void onResponse(Call<Void> call, Response<Void> response) {
//                if (response.code() == 200) { }
//            }
//
//            @Override
//            public void onFailure(Call<Void> call, Throwable t) {
//                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    private void show_chat(String roomname, String curr_id){
        HashMap<String, String> map = new HashMap<>();

        map.put("roomname", roomname);

        Call<ChatResult> call = LoginActivity.retrofitInterface.showChat(map);

        call.enqueue(new Callback<ChatResult>() {
            @Override
            public void onResponse(Call<ChatResult> call, Response<ChatResult> response) {

                if (response.code() == 200) {

                    ChatResult result = response.body();

                    List<String> id = result.getId();
                    List<String> message = result.getMessage();

                    for(int i=0; i<id.size(); i++){
                        ArrayList<String> data = new ArrayList<>();
                        if(curr_id.equals(id.get(i))){
                            data.add("send"); data.add("msg"); data.add(roomname); data.add(id.get(i)); data.add(message.get(i));
                        } else {
                            data.add("receive"); data.add("msg"); data.add(roomname); data.add(id.get(i)); data.add(message.get(i));
                        }
                        messageAdapter.addItem(data);
                        recyclerView.smoothScrollToPosition(messageAdapter.getItemCount()-1);
                    }

                } else if (response.code() == 400) {
                    Toast.makeText(getApplicationContext(), "Let's start our first chat!", Toast.LENGTH_SHORT).show();
                }

            }
            @Override
            public void onFailure(Call<ChatResult> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

}