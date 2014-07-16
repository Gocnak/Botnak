package gui;

import util.Timer;

import java.awt.*;
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

    private AuthorizeAccountGUI field;
    private Timer timeoutTimer;

    public TokenListener(AuthorizeAccountGUI field) {
        timeoutTimer = new Timer(1000 * 60);
        this.field = field;
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
                    field.oAuthField.setText(token);
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            field.setState(Frame.ICONIFIED);
                            field.setState(Frame.NORMAL);
                            field.toFront();
                            field.repaint();
                        }
                    });
                    s.getOutputStream().write(makeResponse().getBytes("UTF-8"));
                    s.close();
                    so.close();
                    break;
                }
            } catch (Exception e) {
                GUIMain.log(e.getMessage());
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
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                buffer.append(line);
                buffer.append("\n");
            }
            content = buffer.toString();
            bufferedReader.close();
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
