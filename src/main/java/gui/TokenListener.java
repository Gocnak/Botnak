package gui;

import gui.forms.GUIAuthorizeAccount;
import gui.forms.GUIMain;
import util.Utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Gocnak on 5/10/2014.
 * Shoutouts to TDuva
 */
public class TokenListener extends Thread {

    private GUIAuthorizeAccount form;

    public TokenListener(GUIAuthorizeAccount field) {
        this.form = field;
    }

    @Override
    public void run() {
        Socket s = null;
        InputStreamReader irs = null;
        BufferedReader input = null;
        try (ServerSocket so = new ServerSocket(4447, 0, InetAddress.getLoopbackAddress())) {
            so.setSoTimeout(60000);
            s = so.accept();
            irs = new InputStreamReader(s.getInputStream());
            input = new BufferedReader(irs);
            String inputLine = input.readLine();
            String token = inputLine.replace("GET /token/", "").split(" ")[0];
            if (token.length() > 5) {
                form.oauthField.setText(token);
                form.statusPane.setText("Successfully obtained OAuth key! Click \"Close\" below to finish!");
                s.getOutputStream().write(makeResponse().getBytes("UTF-8"));
            } else {
                form.statusPane.setText("Failed to obtain OAuth key due to an unforeseen issue...");
            }
        } catch (Exception e) {
            form.statusPane.setText("Failed to obtain OAuth key due to Exception! Check System Logs for more info.");
            GUIMain.log(e);
        } finally {
            try {
                if (irs != null) irs.close();
                if (input != null) input.close();
                if (s != null) s.close();
            } catch (Exception e) {
                GUIMain.log(e);
            }
        }
    }

    private String makeResponse() {
        String content;
        try {
            // Read file to send back as content
            InputStream input = getClass().getResourceAsStream("token_received.html");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input));
            StringBuilder buffer = new StringBuilder();
            Utils.parseBufferedReader(bufferedReader, buffer, true);
            content = buffer.toString();
            input.close();
        } catch (Exception ex) {
            content = "<html><body>An error occurred (couldn't read file)</body></html>";
        }
        return makeHeader() + content;
    }

    private String makeHeader() {
        String header = "";
        header += "HTTP/1.0 200 OK\n";
        header += "Server: Botnak Auth Server\n";
        header += "Content-Type: text/html; charset=UTF-8\n\n";
        return header;
    }
}