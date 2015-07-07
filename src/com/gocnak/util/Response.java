package com.gocnak.util;

/**
 * Created by Nick on 1/2/2015.
 * <p>
 * This class was created because a lot of the Utils classes need to be
 * a bit more in depth on the results of their methods than just returning "true"
 */
public class Response {

    private boolean isSuccessful = false; //defaults to failed
    private String responseText = "";

    public Response() {
        //default, blank response
    }

    public void wasSuccessful() {
        this.isSuccessful = true;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    public String getResponseText() {
        return responseText;
    }

    /**
     * @return If the method was successfully completed or not.
     */
    public boolean isSuccessful() {
        return isSuccessful;
    }
}