package com.oracle.emeatech.callcentre.bulletinboard;

import java.io.Serializable;
import java.util.Date;

/**
 * A message to be posted to either a public or domain bulletinboard
 * @author ewan
 */
public class Bulletin implements Serializable
{
    private Date timestamp;
    private String senderId;
    private String contents;
    public static final int TIMESTAMP = 0, SENDER_ID = 1, CONTENTS = 2;

    public Bulletin() {}

    public Bulletin(Date timestamp, String senderId, String contents)
    {
        this.timestamp = timestamp;
        this.senderId = senderId;
        this.contents = contents;
    }

    /**
     * @return the timestamp
     */
    public Date getTimestamp()
    {
        return timestamp;
    }

    /**
     * @return the senderId
     */
    public String getSenderId()
    {
        return senderId;
    }

    /**
     * @return the contents
     */
    public String getContents()
    {
        return contents;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Sent @ ").append(this.getTimestamp()).append("\n");
        sb.append("From : ").append(this.getSenderId()).append("\n");
        sb.append(this.getContents());
        return sb.toString();
    }
}
