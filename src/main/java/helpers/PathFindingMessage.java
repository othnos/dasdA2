package helpers;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PathFindingMessage {
    /**
     * Source conveyor
     */
    private String source;

    /**
     * Destination conveyor
     */
    private String destination;

    /**
     * Way-point conveyors from the source to the destination
     */
    private List path;

    /**
     * Action what should be done
     */
    private String action;

    /**
     * Message id
     */
    private Integer msgId;

    /**
     * Constructor
     * @param source_
     * @param destination_
     * @param action_
     * @throws Exception
     */
    public PathFindingMessage(String source_, String destination_,
                              String action_)
            throws Exception {
        source = source_;
        destination = destination_;
        action = action_;
        msgId = 0;
    }

    /**
     * Constructor with JSONObject data
     * @param data
     * @throws Exception
     */
    public PathFindingMessage(JSONObject data)
            throws Exception {
        source = data.get("source").toString();
        destination = data.get("destination").toString();

        path = new JSONArray();
        if (data.get("path") != null) {
            JSONArray pathFromData = (JSONArray) data.get("path");
            for (Object pathObj : pathFromData) {
                path.add(pathObj.toString());
            }
        }

        action = data.get("action").toString();

        msgId = Integer.parseInt(data.get("msgId").toString());
    }

    /**
     * Create message by getting object as JSON object and
     * set it as a string to the content
     */
    /*
    public void createMessage() {
        setContent(getAsJSONObject().toJSONString());
    }
    */


    /**
     * Get as JSON object
     * @return
     */
    public JSONObject getAsJSONObject() {
        JSONObject jsonMessage = new JSONObject();
        jsonMessage.put("source", source);
        jsonMessage.put("destination", destination);
        jsonMessage.put("path", path);
        jsonMessage.put("action", action);
        jsonMessage.put("msgId", msgId);

        return jsonMessage;
    }

    /// Setters

    public void setSource(String source_) {
        source = source_;
    }

    public void setDestination(String destination_) {
        destination = destination_;
    }

    public void setPath(JSONArray path_) {
        path = path_;
    }

    public void setMsgId(Integer msgId_) {
        msgId = msgId_;
    }

    public void setAction(String action_) {
        action = action_;
    }

    /// Getters

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public List getPath() {
        return path;
    }
}
