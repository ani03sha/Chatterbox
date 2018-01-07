package org.anirudh.redquark.chatterbox.model;

/**
 * Model for one message
 */

public class Message {

    private String name;
    private String message;

    public Message(String name,String message){
        this.name = name;
        this.message = message;
    }

    public Message(){
        // necessary for Firebase's deserializer
    }

    public String getMessage() {
        return message;
    }

    public String getName() {
        return name;
    }
}
