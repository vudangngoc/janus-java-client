package net.edulive.janus.java_client.textroom;

import net.edulive.janus.java_client.JanusTransactionAbstractHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.CompletableFuture;

public class GetRoomsHandler extends JanusTransactionAbstractHandler {
    public GetRoomsHandler(CompletableFuture<JSONArray> result, String transactionId, long handleId) {
        super(result, transactionId, handleId);
    }

    @Override
    public boolean process(JSONObject janusMessage) {
        if(janusMessage.getString("janus").equals("success")){
            JSONObject data = janusMessage.getJSONObject("plugindata").getJSONObject("data");
            if(data.getString("textroom").equals("success")){
                this.getResult().complete(data.getJSONArray("list"));
                return false;
            }
        }
        this.getResult().complete(new JSONArray());
        return false;
    }
}
