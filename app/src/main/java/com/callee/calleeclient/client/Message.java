package com.callee.calleeclient.client;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Message implements Parcelable {

    private JSONObject content = new JSONObject();

    public Message(Long id, String fromName, String toName, String fromEmail, String toEmail, Long timestamp, ToM type) {
        try {
            content.put("id", id);
            content.put("fromName", fromName);
            content.put("toName", toName);
            content.put("fromEmail", fromEmail);
            content.put("toEmail", toEmail);
            content.put("timestamp", timestamp);
            content.put("type", type);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Message(JSONObject source) throws JSONException {

        this((Long) source.getLong("id"), (String) source.get("fromName"), (String) source.get("toName"),
                (String) source.get("fromEmail"), (String) source.get("toEmail"),
                (Long) source.get("timestamp"), getType((String) source.get("type")));

        if (source.has("text")) {
            this.content.put("text", source.getString("text"));
        }

        if (source.has("lastUpdate")) {
            this.content.put("lastUpdate", source.getLong("lastUpdate"));
        }
    }

    public void addContent(JSONObject data) {
        content = data;
    }

    public void addLastUpdated(Long ts) {
        if (this.getType() == ToM.UPDATEREQUEST) {
            try {
                this.content.put("lastUpdate", ts);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void putText(String text) {
        try {
            this.content.put("text", text);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String toJSON() {
        return this.content.toString();
    }

    public String toString() {
        String str = ("ID: " + this.getId().toString() + "\n" +
                "Type: " + this.getType().toString() + "\n" +
                "From: " + this.getFromName() + " (" + this.getFromEmail() + ")\n" +
                "To: " + this.getToName() + " (" + this.getToEmail() + ")\n" +
                "Sent: " + this.getTimestamp()) + "\n" +
                "Text: " + this.getText();

        return str;
    }

    public JSONObject getContent() {
        return content;
    }

    public Long getId() {
        if (this.content.has("id")) {
            try {
                return (Long) this.content.get("id");
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } else return (long) -1;
    }

    public String getFromName() {
        if (this.content.has("fromName")) {
            try {
                return (String) this.content.get("fromName");
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } else return "";
    }

    public String getToName() {
        if (this.content.has("toName")) {
            try {
                return (String) this.content.get("toName");
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } else return "";
    }

    public String getFromEmail() {
        if (this.content.has("fromEmail")) {
            try {
                return (String) this.content.get("fromEmail");
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } else return "";
    }

    public String getToEmail() {
        if (this.content.has("toEmail")) {
            try {
                return (String) this.content.get("toEmail");
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } else return "";
    }

    public String getTimestamp() {
        if (this.content.has("timestamp")) {
            try {
                long ts = this.content.getLong("timestamp");
                return Long.toString(ts);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } else return "";
    }

    public ToM getType() {
        if (this.content.has("type")) {
            try {
                return (ToM) this.content.get("type");
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } else return null;
    }

    public String getText() {
        if (this.content.has("text")) {
            try {
                return (String) this.content.get("text");
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } else return "";
    }

    public Long getLastUpdate() {
        if (this.getType() == ToM.UPDATEREQUEST && this.content.has("lastUpdate")) {
            try {
                return this.content.getLong("lastUpdate");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void setId(Long id) {
        try {
            this.content.put("id", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setFromName(String fromName) {
        try {
            this.content.put("fromName", fromName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setToName(String toName) {
        try {
            this.content.put("toName", toName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setFromEmail(String fromEmail) {
        try {
            this.content.put("fromEmail", fromEmail);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setToEmail(String toEmail) {
        try {
            this.content.put("toEmail", toEmail);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setTimestamp(Long timestamp) {
        try {
            this.content.put("timestamp", timestamp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setType(ToM type) {
        try {
            this.content.put("type", type);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getDate() {
        return Message.getFormattedTime(Long.valueOf(this.getTimestamp()));
    }

    static public ToM getType(String type) {
        switch (type) {

            case "MESSAGE":
                return ToM.MESSAGE;
            case "MESSAGERESPONSE":
                return ToM.MESSAGERESPONSE;
            case "UPDATEREQUEST":
                return ToM.UPDATEREQUEST;
            case "UPDATERESPONSE":
                return ToM.UPDATERESPONSE;
            case "REGISTERUSER":
                return ToM.REGISTERUSER;
            case "REGISTERUSERRESPONSE":
                return ToM.REGISTERUSERRESPONSE;
            case "REGISTERCONFIRM":
                return ToM.REGISTERCONFIRM;
            case "REGISTERCONFIRMRESPONSE":
                return ToM.REGISTERCONFIRMRESPONSE;
            case "CONFIRMCONTACT":
                return ToM.CONFIRMCONTACT;

            default:return null;
        }
    }

    static public String getFormattedTime(Long timestamp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

        Date d = new Date(timestamp);
        return format.format(d);
    }

    //code for make Message parselable
    public Message(Parcel p) {
        String[] data = new String[8];

        p.readStringArray(data);
        this.setId(Long.parseLong(data[0]));
        this.setFromName(data[1]);
        this.setToName(data[2]);
        this.setFromEmail(data[3]);
        this.setToEmail(data[4]);
        this.setTimestamp(Long.parseLong(data[5]));
        this.setType(getType(data[6]));
        this.putText(data[7]);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{
                this.getId().toString(),
                this.getFromName(),
                this.getToEmail(),
                this.getFromEmail(),
                this.getToName(),
                this.getTimestamp(),
                this.getType().toString(),
                this.getText()
        });
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
}