package smack.test.com.smacktest;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import android.os.Handler;
import java.util.logging.LogRecord;

public class ActivityPrincipal extends ActionBarActivity {

    XMPPTCPConnection connection;
    TextView estadoConexion;
    TextView estadoLoggin;
    TextView mensajeRecibido;
    EditText edMensaje;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_principal);


        XMPPConnectionTask connectionTask = new XMPPConnectionTask();
        connectionTask.execute();

        try{
            connection = connectionTask.get();
        }catch (InterruptedException | ExecutionException e){
            Log.e("XMPP", "Exception: " + e.getMessage());
        }

        estadoConexion = (TextView) findViewById(R.id.estadoConexion);
        estadoLoggin = (TextView) findViewById(R.id.estadoLogin);
        mensajeRecibido = (TextView) findViewById(R.id.mensajeRecibido);
        edMensaje = (EditText) findViewById(R.id.enviarMensaje);

        estadoConexion.setText(String.valueOf(connection.isConnected()));
        estadoLoggin.setText(String.valueOf(connection.isAuthenticated()));

        Roster roster = Roster.getInstanceFor(connection);
        Collection<RosterEntry> entries = roster.getEntries();

        for (RosterEntry entry : entries) {
            Log.i("XMPP", entry.getUser());
        }

        ChatManager chatManager = ChatManager.getInstanceFor(connection);

        chatManager.addChatListener(new ChatManagerListener() {
            @Override
            public void chatCreated(Chat chat, boolean createdLocally) {
                chat.addMessageListener(new ChatMessageListener() {
                    @Override
                    public void processMessage(Chat chat, Message message) {
                        Log.i("XMPP", message.toString());
                        android.os.Message androidMessage = handler.obtainMessage();
                        Bundle msgData = new Bundle();
                        msgData.putString("body", message.getBody());
                        androidMessage.setData(msgData);
                        androidMessage.sendToTarget();
                    }
                });
            }
        });

    }

    public void sendMessage(View view){

        ChatManager chatmanager = ChatManager.getInstanceFor(connection);

        Chat newChat = chatmanager.createChat("pablo@indrainventari", new ChatMessageListener(){
            @Override
            public void processMessage(Chat chat, Message message) {
                Log.i("XMPP", message.getBody());
            }
        });

        try {
            newChat.sendMessage(String.valueOf(edMensaje.getText()));
        } catch (SmackException.NotConnectedException e) {
            Log.i("XMPP", e.getMessage());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected Handler handler = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {
            Log.i("XMPPHandler", msg.getData().toString());
            Bundle msgData = msg.getData();
            mensajeRecibido.setText(msgData.getString("body"));
        }
    };


    private class XMPPConnectionTask extends AsyncTask<Void, Void, XMPPTCPConnection>{

        @Override
        protected XMPPTCPConnection doInBackground(Void... params) {
            return SmackXMPPConnection.getConnection();
        }
    }

}
