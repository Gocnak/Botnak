package gui;

import gui.forms.GUIAuthorizeAccount;
import gui.forms.GUIMain;
import util.Timer;
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
    private Timer timeoutTimer;

    public TokenListener(GUIAuthorizeAccount field) {
        timeoutTimer = new Timer(1000 * 60);
        this.form = field;
    }

    @Override
    public void run() {
        while (!GUIMain.shutDown && timeoutTimer.isRunning()) {
            try {
                ServerSocket so = new ServerSocket(4447, 0, InetAddress.getLoopbackAddress());
                Socket s = so.accept();
                BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String inputLine = input.readLine();
                String token = inputLine.replace("GET /token/", "").split(" ")[0];
                if (token.length() > 5) {
                    form.oauthField.setText(token);
                    form.statusPane.setText("Successfully obtained OAuth key! Click \"Close\" below to finish!");
                    s.getOutputStream().write(makeResponse().getBytes("UTF-8"));
                    input.close();
                    s.close();
                    so.close();
                    break;
                }
            } catch (Exception e) {
                GUIMain.log(e);
                break;
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