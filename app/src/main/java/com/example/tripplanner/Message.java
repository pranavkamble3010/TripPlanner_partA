package com.example.tripplanner;

import java.util.HashMap;
import java.util.Map;

public class Message {

    private String sender;
    //Firestore reads this annotation and fills it with the timestamp accordingly. follow https://stackoverflow.com/questions/54217229/how-to-save-timestamp-in-firestore
    //private @ServerTimestamp Date timestamp;
    private String timestamp;
    private String messageType;
    private String content;
    private String tripName;


    public Message(){

    }

    public Message(HashMap<String, Object> map){
        this.timestamp = map.get("timestamp").toString();
        this.messageType = map.get("messageType").toString();
        this.content = map.get("content").toString();
        this.sender = map.get("sender").toString();
        //this.tripName = map.get("tripName").toString();
    }

    public HashMap<String,Object> getMessageMap(){
       HashMap<String,Object> map = new HashMap<>();
        map.put("timestamp",this.timestamp);
        map.put("messageType",this.messageType);
        map.put("content",this.content);
        map.put("tripName",this.tripName);
        map.put("sender",this.sender);

        return map;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTripName() {
        return tripName;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Message{" +
                "sender='" + sender + '\'' +
                ", timestamp=" + timestamp +
                ", messageType='" + messageType + '\'' +
                ", content='" + content + '\'' +
                ", tripName='" + tripName + '\'' +
                '}';
    }
}
