package pt.ulisboa.tecnico.cmu.ubibike.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.util.List;

import pt.ulisboa.tecnico.cmu.ubibike.ApplicationContext;
import pt.ulisboa.tecnico.cmu.ubibike.R;
import pt.ulisboa.tecnico.cmu.ubibike.UbiBike;
import pt.ulisboa.tecnico.cmu.ubibike.adapters.MessageListAdapter;
import pt.ulisboa.tecnico.cmu.ubibike.managers.CipherManager;
import pt.ulisboa.tecnico.cmu.ubibike.peercommunication.Chat;
import pt.ulisboa.tecnico.cmu.ubibike.peercommunication.ChatMessage;
import pt.ulisboa.tecnico.cmu.ubibike.utils.DigitalSignature;
import pt.ulisboa.tecnico.cmu.ubibike.utils.JsonParser;

public class ChatFragment extends Fragment {

    public static final String ARGUMENT_KEY_GROUP_CHAT = "group_chat";
    public static final String ARGUMENT_KEY_USERNAME = "username";

    private List<ChatMessage> mMessages;
    private Chat mChat;
    private boolean mGroupChat;

    private ListView mListView;

    public ChatFragment() {
        // Required empty public constructor
    }

    private UbiBike getParentActivity(){
        return (UbiBike) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGroupChat = getArguments().getBoolean(ARGUMENT_KEY_GROUP_CHAT);

        if(mGroupChat) {
            mChat = ApplicationContext.getInstance().getNearbyPeerCommunication().getGroupChat().getChat();

            String groupOwner = ApplicationContext.getInstance().getNearbyPeerCommunication().
                                                                            getGroupChat().getOwner();
            if(groupOwner != null) {
                getParentActivity().getSupportActionBar().setTitle("Group hosted by '" + groupOwner + "'");
            }
            else{
                getParentActivity().getSupportActionBar().setTitle("Group chat");
            }
        }
        else{
            String username = getArguments().getString(ARGUMENT_KEY_USERNAME);
            mChat = ApplicationContext.getInstance().getNearbyPeerCommunication().
                    getIndividualChat(username);

            getParentActivity().getSupportActionBar().setTitle(username);

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversation, container, false);

        setViewElements(view);

        return view;
    }


    private void setViewElements(View view){

        mListView = (ListView) view.findViewById(R.id.list);
        ImageButton send_button = (ImageButton) view.findViewById(R.id.chat_send_button);
        final EditText message_editText = (EditText) view.findViewById(R.id.chat_message_editText);

        send_button.setClickable(false);
        send_button.setEnabled(false);

        send_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String message = message_editText.getText().toString();

                Toast.makeText(getParentActivity(), message, Toast.LENGTH_SHORT).show();

                String myUsername = ApplicationContext.getInstance().getData().getUsername();

                if(mGroupChat){
                    mChat.addNewMessage(new ChatMessage(false, myUsername, message));
                    updateMessagesView();
                }
                else{
                    mChat.addNewMessage(new ChatMessage(false, myUsername, message));
                }

                //TODO send
            }
        });


        mMessages = mChat.getAllMessages();

        MessageListAdapter messageListAdapter = new MessageListAdapter(getActivity(), mMessages, mGroupChat );
        mListView.setAdapter(messageListAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                TextView date;

                //getting right index to access the element. Because elements that are not totally
                // visible are deleted from viewGroup
                int index = position - mListView.getFirstVisiblePosition();

                //in case if element is not totally visible, android doesn't have it in memory
                // (is not in viewGroup)
                if (index > mListView.getLastVisiblePosition()) return;

                if (mMessages.get(position).isReceived()) {
                    date = (TextView) mListView.getChildAt(index).
                            findViewById(R.id.dateInBubble_textView);
                } else {
                    date = (TextView) mListView.getChildAt(index).
                            findViewById(R.id.dateOutBubble_textView);
                }

                if (date.getVisibility() == View.GONE) {
                    date.setVisibility(View.VISIBLE);
                } else {
                    date.setVisibility(View.GONE);
                }

                int shown = mListView.getLastVisiblePosition() -
                        mListView.getFirstVisiblePosition();

                if (index == shown) {
                    mListView.setSelection(mListView.getAdapter().getCount());
                }

            }
        });

    }

    private void sendPoints(long points) throws IOException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        String fromClientId = ApplicationContext.getInstance().getData().getUsername();
        PrivateKey privateKey = CipherManager.getPrivateKey();

        // ideias do jaquim
        // { from_client_id: 1, to_client_id : 2 , points : 230, points_source: "ride" points_source_id : "ride_client_id", nounce: 3}
        // { uid: 1 , username: "lol" , public_key : "string da chave", ttl : timestamp de validade }

        // TODO: hardcoded next variables
        String toClientId = "";
        String pointsSource = "";
        String pointsSourceId = "";
        int nounce = -1;
        int ttl = -1;

        JSONObject pointsTransactionData = JsonParser.buildPointsTransactionDataJson(
                fromClientId, toClientId, points, pointsSource, pointsSourceId, nounce, ttl);
        //TODO: buildPointsTransactionDataJson

        byte[] signature = DigitalSignature.signData(
                CipherManager.getSHA2Digest(
                        pointsTransactionData.toString().getBytes()), privateKey);

        String base64publicKey = ApplicationContext.getInstance().getData().getPublicToken(); // representing publicKey

        // create final json
        JSONObject pointsTransaction =
                JsonParser.buildPointsTransactionJson(
                        pointsTransactionData, signature, base64publicKey);

        String deviceName = toClientId; // TODO: is the same?

        // send points
        getParentActivity().wifiP2pSendMessageToPeer(deviceName, pointsTransaction.toString());
    }


    public void cleanMessageInput(){
        EditText message_editText = (EditText) getView().findViewById(R.id.chat_message_editText);
        message_editText.setText("");
    }

    public void updateMessagesView(){
        ((MessageListAdapter) mListView.getAdapter()).notifyDataSetChanged();
    }
}