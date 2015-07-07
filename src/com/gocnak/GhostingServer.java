package com.gocnak;

import java.awt.*;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class GhostingServer {

    private static ServerSend serverSend = null;
    public static CopyOnWriteArraySet<User> userSet = new CopyOnWriteArraySet<>();
    public static boolean debug = false;
    public static int port = 5145;
    public static InetAddress machineIP = null;

    public static void main(String args[]) throws Exception {
        /**
         * TODO
         * launch options:
         * -ip "xxx.xx.xx.xxx"
         * -debug
         * -port xxxx
         */
        /*System.setProperty("java.net.preferIPv4Stack", "true");
        handleArgs(args);
        if (machineIP == null) machineIP = InetAddress.getLocalHost();
        init();*/
    }

    public static void init() {
        new InputListener().start();
        log("Starting server...");
        serverSend = new ServerSend();
        serverSend.start();
    }

    public static void handleArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-")) {
                String next = (i + 1 >= args.length ? null : args[i + 1]);
                switch (arg) {
                    case "debug":
                        debug = true;
                        break;
                    case "port":
                        int portRead = -1;
                        if (next != null) {
                            try {
                                portRead = Integer.parseInt(next);
                            } catch (Exception ignored) {
                            }
                        }
                        if (portRead != -1) port = portRead;
                        break;
                    case "ip":
                        if (next != null) {
                            try {
                                machineIP = InetAddress.getByName(next);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public static void log(Object message) {
        System.out.println(message.toString());
    }

    /**
     * This com.gocnak.thread handles user input in the command prompt.
     */
    public static class InputListener extends Thread {
        private static BufferedReader scanner;
        static boolean shouldRun = true;

        public InputListener() {
            scanner = new BufferedReader(new InputStreamReader(System.in));
        }

        @Override
        public void run() {
            while (shouldRun) {
                try {
                    String toHandle = scanner.readLine();
                    handleEvent(toHandle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        static void handleEvent(String message) {
            if (message == null || message.equals("")) return;
            message = message.toLowerCase();
            String[] split = message.split(" ");
            if (message.startsWith("kick")) {
                if (split.length > 1) {
                    User u = getUser(split[1]);
                    if (u != null) {
                        log("Kicking user " + u.getName() + "!");
                        UserEventType kicked = UserEventType.KICK_USER;
                        kicked.reason = "Kicked by server admin.";
                        u.addEvent(new UserEvent(u, kicked));
                    } else {
                        log("Could not find user " + split[1] + " !");
                    }
                } else {
                    log("Usage: \"kick (user)\" where (user) is the name of a user.");
                }
            } else if (message.startsWith("status")) {
                if (serverSend != null && serverSend.isAlive()) {
                    log("The server is currently running on " + machineIP.toString() + " with port " + port);
                    log("The server currently has " + userSet.size() + " user(s) on it:");
                    userSet.forEach(User::printUser);
                } else {
                    log("The server is currently NOT running! Try starting the server with \"restart\"!");
                    log("The server would be run on " + machineIP.toString() + " on port " + port);
                }
            } else if (message.startsWith("poll")) {
                if (split.length > 1) {
                    User u = getUser(split[1]);
                    if (u != null) {
                        u.printUser();
                    } else {
                        log("Could not find user \"" + split[1] + "\"!");
                    }
                } else {
                    log("Usage: \"poll (user)\" where (user) is the name of a user.");
                }
            } else if (message.startsWith("stop") || message.startsWith("quit")) {
                if (serverSend != null && serverSend.isAlive()) {
                    log("Shutting down server!");
                    UserEventType kicked = UserEventType.KICK_USER;
                    kicked.reason = "Server shutting down!";
                    userSet.stream().forEach(u -> u.addEvent(new UserEvent(u, kicked)));
                    log("Waiting for all users to disconnect...");
                    int count = 0;
                    while (!userSet.isEmpty()) {
                        try {
                            Thread.sleep(1000);
                            count++;
                            if (count > 10) { //waited 10+ seconds...
                                log("Some users did not disconnect; continuing with shutdown!");
                                break;
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    log("Everyone disconnected, shutting down server!");
                    ServerSend.shouldRun = false;
                    serverSend = null;
                } else {
                    log("Exit program? Y/N");
                }
            } else if (message.startsWith("restart")) {
                if (serverSend != null && serverSend.isAlive()) {
                    log("Server currently running! Try using \"stop\" first!");
                } else {
                    ServerSend.shouldRun = true;
                    serverSend = new ServerSend();
                    serverSend.start();
                }
            } else if (message.startsWith("ip")) {
                if (serverSend != null && serverSend.isAlive()) {
                    log("Cannot change the IP while the server is still running! Try using \"stop\" first!");
                } else {
                    try {
                        machineIP = InetAddress.getByName(split[1]);
                        log("IP changed to " + machineIP.toString() + " !");
                    } catch (Exception e) {
                        log("Could not change the IP!");
                        e.printStackTrace();
                    }
                }
            } else if (message.startsWith("port")) {
                if (serverSend != null && serverSend.isAlive()) {
                    log("Cannot change the port while the server is still running! Try using \"stop\" first!");
                } else {
                    try {
                        port = Integer.parseInt(split[1]);
                        log("Port changed to " + port + " !");
                    } catch (Exception e) {
                        log("Could not change the port!");
                        e.printStackTrace();
                    }
                }
            } else if (message.equalsIgnoreCase("y")) {
                shouldRun = false;
            }
            /**
             * TODO:
             *  - kick user
             *  - poll user (get info)
             *  - stop server
             *  - restart server (with a new socket IP?)
             *  - status (poll the server; how many connected etc)
             *  - ip [new IP]
             *  - port [new port]
             */
        }

        @Override
        public void interrupt() {
            try {
                scanner.close();
            } catch (Exception ignored) {
            }
            super.interrupt();
        }
    }

    /**
     * This com.gocnak.thread handles updating user run lines, as well as sending out the lines to users.
     */
    public static class ServerSend extends Thread {
        DatagramSocket socket = null;
        public static boolean shouldRun = true;

        public ServerSend() {
        }

        @Override
        public synchronized void start() {
            if (create()) {
                log("Started server!");
                super.start();
            } else {
                log("Could not start server, aborting!");
            }
        }

        public boolean create() {
            try {
                log("Creating the server on " + machineIP.toString() + " with port " + port + " !");
                socket = new DatagramSocket(port, machineIP);
                socket.setSoTimeout(5000);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        public void run() {
            while (shouldRun) {
                if (socket == null) break;
                byte[] buff = new byte[512];
                DatagramPacket request = new DatagramPacket(buff, buff.length);
                boolean rec = false;
                try {
                    socket.receive(request);
                    rec = true;
                } catch (Exception e) {
                    if (!(e instanceof SocketTimeoutException))
                        e.printStackTrace();
                }
                if (rec) {
                    DataCounterStream dcs = new DataCounterStream(request.getData(), new DataInputStream(new ByteArrayInputStream(request.getData())));
                    char indicator = dcs.getIndicator();
                    if (indicator == 'l') {
                        String name = dcs.getString();
                        User u = getUser(name);
                        //is the user even valid?
                        if (u != null) {
                            //update the map, velocity, and location
                            String map = dcs.getString();
                            if (u.getMap() == null || !u.getMap().equalsIgnoreCase(map)) {
                                u.setMap(map);
                            }
                            float vx = dcs.getFloat();
                            float vy = dcs.getFloat();
                            float vz = dcs.getFloat();
                            u.setVelocity(vx, vy, vz);
                            float x = dcs.getFloat();
                            float y = dcs.getFloat();
                            float z = dcs.getFloat();
                            u.setLocation(x, y, z);
                            //update the user with the other ghost data
                            sendExistingLocational(socket, request, u);
                            //add/remove new ghosts if there are any
                            u.completeEvents(socket, request);
                        }
                    } else if (indicator == 'c') {
                        String name = dcs.getString();
                        if (getUser(name) != null) {
                            UserEventType kicked = UserEventType.KICK_USER;
                            kicked.reason = "Change your gh_name to something unique, there already is a " + name + " on the server!";
                            sendKick(socket, request, null, kicked);
                        } else {
                            int trailLength = dcs.readUnsignedByte();
                            int trailRed = dcs.readUnsignedByte();
                            int trailGreen = dcs.readUnsignedByte();
                            int trailBlue = dcs.readUnsignedByte();
                            int ghostRed = dcs.readUnsignedByte();
                            int ghostGreen = dcs.readUnsignedByte();
                            int ghostBlue = dcs.readUnsignedByte();
                            User u = new User(name, trailLength, trailRed, trailGreen, trailBlue, ghostRed, ghostGreen, ghostBlue);
                            log("User received! " + name + " trail: " + trailLength + " colors: " + u.getGhostColor().toString());
                            sendExistingUsers(socket, request);
                            createUserEvent(u, UserEventType.NEW_USER);
                            addNewUser(u);
                        }
                    } else if (indicator == 'd') {
                        //TODO verify the sending packet -- will this actually be a problem?
                        String name = dcs.getString();
                        User u = getUser(name);
                        if (u != null) {
                            log(u.getName() + " has disconnected!");
                            createUserEvent(u, UserEventType.DISCONNECT);
                        }
                    }
                }
            }
            close();
        }

        @Override
        public void interrupt() {
            close();
            super.interrupt();
        }

        public void close() {
            if (socket != null) socket.close();
            userSet.clear();
            socket = null;
            log("Server shut down!");
        }
    }

    /**
     * Sends the current location data of all the other ghosts to a specified user.
     *
     * @param sock      The socket to send on.
     * @param recipient The packet initially received to send back to.
     * @param exclude   The user to exclude from the send.
     */
    public static void sendExistingLocational(DatagramSocket sock, DatagramPacket recipient, User exclude) {
        for (User toSend : userSet) {
            if (exclude.getName().equalsIgnoreCase(toSend.getName())) continue;
            sendRunLine(sock, recipient, toSend);
        }
    }

    /**
     * Sends the existing ghost data to a new user that has just connected.
     *
     * @param sock      The socket to send on.
     * @param recipient The packet initially received to send back to.
     */
    public static void sendExistingUsers(DatagramSocket sock, DatagramPacket recipient) {
        //the following sends the OTHER PEOPLE data to the NEW PERSON
        for (User existingUser : userSet) {
            sendGhostData(sock, recipient, existingUser);
        }
    }

    /**
     * Creates and sends a disconnect packet to a specified recipient.
     *
     * @param sock             The socket to send on.
     * @param pack             The packet initially received to send back to.
     * @param thatDisconnected The user that disconnected.
     */
    public static void sendDisconnect(DatagramSocket sock, DatagramPacket pack, User thatDisconnected) {
        byte[] buffer = new byte[512];
        //make the byte array
        ByteBuffer buf = ByteBuffer.wrap(buffer);
        buf.put((byte) 0x04);
        buf.putInt(thatDisconnected.getName().length());
        buf.put(thatDisconnected.getName().getBytes());
        //now send
        DatagramPacket d = new DatagramPacket(buf.array(), buf.array().length, pack.getAddress(), pack.getPort());
        try {
            sock.send(d);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a user's ghost data to a DatagramPacket recipient.
     *
     * @param sock      The socket to send on.
     * @param recipient The packet initially received to send back to.
     * @param toSend    The user to get data from to send.
     */
    public static void sendGhostData(DatagramSocket sock, DatagramPacket recipient, User toSend) {
        byte[] buffer = new byte[512];
        ByteBuffer buf = ByteBuffer.wrap(buffer);
        buf.put((byte) 0x00);
        buf.putInt(toSend.getName().length());
        buf.put(toSend.getName().getBytes());
        buf.put((byte) toSend.getTrailLength());
        Color trail = toSend.getTrailColor();
        Color ghost = toSend.getGhostColor();
        buf.put((byte) trail.getRed());
        buf.put((byte) trail.getGreen());
        buf.put((byte) trail.getBlue());
        buf.put((byte) ghost.getRed());
        buf.put((byte) ghost.getGreen());
        buf.put((byte) ghost.getBlue());
        DatagramPacket d = new DatagramPacket(buf.array(), buf.array().length, recipient.getAddress(), recipient.getPort());
        try {
            sock.send(d);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a run line to a specified recipient.
     *
     * @param sock      The socket to send on.
     * @param recipient The packet initially received to send back to.
     * @param toSend    The user to get data from to send.
     */
    public static void sendRunLine(DatagramSocket sock, DatagramPacket recipient, User toSend) {
        byte[] buffer = new byte[512];
        ByteBuffer buf = ByteBuffer.wrap(buffer);
        buf.put((byte) 0x01);
        String name = toSend.getName();
        buf.putInt(name.length());
        buf.put(name.getBytes());
        String map = toSend.getMap();
        if (map == null) {
            log("Map is null, setting to blank string!");
            map = "";
        }
        buf.putInt(map.length());
        buf.put(map.getBytes());
        Location p = toSend.getLoc();
        Velocity v = toSend.getVelocity();
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putFloat(v.getX());
        buf.putFloat(v.getY());
        buf.putFloat(v.getZ());
        buf.putFloat(p.getX());
        buf.putFloat(p.getY());
        buf.putFloat(p.getZ());
        //log("Sending " + name + " on " + map + " at (" + p.getX() + ", " + p.getY() + ", " + p.getZ() + ") to "
        // + recipient.getAddress().toString() + ":" + recipient.getPort());
        DatagramPacket d = new DatagramPacket(buf.array(), buf.array().length, recipient.getAddress(), recipient.getPort());
        try {
            sock.send(d);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Kicks a user from the server, then removes the user for all the other people.
     *
     * @param sock        The socket to send on.
     * @param recipient   The recipient of the kick.
     * @param beingKicked The recipient of the kick as a User object.
     * @param event       Used to pass the reason of why they're kicked to the kicked person.
     */
    public static void sendKick(DatagramSocket sock, DatagramPacket recipient, User beingKicked, UserEventType event) {
        byte[] buffer = new byte[512];
        ByteBuffer buf = ByteBuffer.wrap(buffer);
        buf.put((byte) 0x05);
        buf.putInt(event.reason.length());
        buf.put(event.reason.getBytes());
        DatagramPacket d = new DatagramPacket(buf.array(), buf.array().length, recipient.getAddress(), recipient.getPort());
        try {
            sock.send(d);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (beingKicked != null) createUserEvent(beingKicked, UserEventType.DISCONNECT);
    }

    /**
     * Creates an event based on a user either connecting or disconnecting.
     * <p>
     * This is required due to the by-request system in place of the send com.gocnak.thread.
     * If somebody were to join or leave, the existing users need to be notified the
     * next time they communicate with the server (by sending a location update packet).
     * <p>
     * If the user is connecting for the first time, they are sent the other people's ghost data in response.
     * The other users at the time of the connection need to be marked for special treatment (send the new user as well).
     * <p>
     * If the user is disconnecting, the user's name is marked, then the user is removed, and on the next packet update
     * for all the remaining users, they are told to remove the user's ghost.
     * <p>
     * TODO this can be updated to support things like RACE_FINISH etc
     *
     * @param eventUser The user the event is about.
     * @param event     The type of the event.
     */
    public static void createUserEvent(User eventUser, UserEventType event) {
        if (event == UserEventType.DISCONNECT) {
            userSet.remove(eventUser);
        }
        for (User u : userSet) {
            u.addEvent(new UserEvent(eventUser, event));
        }
    }

    enum UserEventType {
        NEW_USER,
        DISCONNECT,
        KICK_USER;

        String reason = "";
    }

    public static class UserEvent {
        User eventUser;
        UserEventType eventType;

        UserEvent(User user, UserEventType type) {
            eventUser = user;
            eventType = type;
        }
    }

    /**
     * Gets the user from the user set.
     *
     * @param name The name of the user to get.
     * @return The user object if it exists, otherwise null.
     */
    public static User getUser(String name) {
        for (User u : userSet) {
            if (u.getName().equalsIgnoreCase(name)) {
                return u;
            }
        }
        return null;
    }

    /**
     * Counts the users of a given name.
     *
     * @param name The name of the user.
     * @return The number of users with the given name.
     */
    public static int getUserCount(String name) {
        return (int) userSet.stream().filter(u -> u.getName().equalsIgnoreCase(name)).count();
    }

    /**
     * Adds a new user to the hash set.
     *
     * @param u The user to add.
     */
    public static synchronized void addNewUser(User u) {
        log("Adding user " + u.getName() + " !");
        userSet.add(u);
    }

    public static class User {
        private String name, map;
        private Velocity vel;
        private Location loc;
        private int trailLength;
        private Color ghostColor, trailColor;
        private CopyOnWriteArrayList<UserEvent> events;
        private long lastUpdate;
        private long ping = 0;

        public User(String name, int tl, int tr, int tg, int tb, int gr, int gg, int gb) {
            this.name = name;
            map = "";
            trailLength = tl;
            ghostColor = new Color(gr, gg, gb);
            trailColor = new Color(tr, tg, tb);
            loc = new Location(0, 0, 0);
            vel = new Velocity(0, 0, 0);
            events = new CopyOnWriteArrayList<>();
        }

        public void setLocation(float x, float y, float z) {
            ping = System.currentTimeMillis() - lastUpdate;
            lastUpdate = System.currentTimeMillis();
            loc.update(x, y, z);
        }

        public void setVelocity(float x, float y, float z) {
            vel.update(x, y, z);
        }

        public void setMap(String newMap) {
            map = newMap;
        }

        public String getName() {
            return name;
        }

        public String getMap() {
            return map;
        }

        public Location getLoc() {
            //if there's no update for 30 seconds or more, mark for deletion
            if (System.currentTimeMillis() - lastUpdate >= (1000 * 30)) {
                createUserEvent(this, UserEventType.DISCONNECT);
                log("Kicking " + name + " for inactivity!");
            }
            return loc;
        }

        public Velocity getVelocity() {
            return vel;
        }

        public Color getGhostColor() {
            return ghostColor;
        }

        public Color getTrailColor() {
            return trailColor;
        }

        public int getTrailLength() {
            return trailLength;
        }

        public void addEvent(UserEvent newEvent) {
            events.add(newEvent);
        }

        public boolean hasEvents() {
            return !events.isEmpty();
        }

        public void completeEvents(DatagramSocket sock, DatagramPacket pack) {
            if (!hasEvents()) return;
            Iterator<UserEvent> iterator = events.iterator();
            while (iterator.hasNext()) {
                UserEvent u = iterator.next();
                if (u.eventType == UserEventType.DISCONNECT) {
                    sendDisconnect(sock, pack, u.eventUser);
                } else if (u.eventType == UserEventType.NEW_USER) {
                    sendGhostData(sock, pack, u.eventUser);
                } else if (u.eventType == UserEventType.KICK_USER) {
                    sendKick(sock, pack, u.eventUser, u.eventType);
                    break;
                }
                iterator.remove();
            }
        }

        public void printUser() {
            System.out.printf("Name: %s%nCurrent map: %s%nTrail length: %d seconds%nGhost color: %s%nTrail color: %s%n%s%n%s%nHas events: %b%nPing: %d ms",
                    name, map, trailLength, ghostColor.toString(), trailColor.toString(), loc.toString(), vel.toString(), hasEvents(), (int) ping);
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof User) &&
                    ((User) obj).getGhostColor().equals(this.getGhostColor()) &&
                    ((User) obj).getName().equals(this.getName()) &&
                    ((User) obj).getMap().equals(this.getMap()) &&
                    ((User) obj).getTrailColor().equals(this.getTrailColor()) &&
                    ((User) obj).getTrailLength() == this.getTrailLength();
        }
    }

    public static class Velocity {
        private float vx, vy, vz;

        public Velocity(float x, float y, float z) {
            vx = x;
            vy = y;
            vz = z;
        }

        public void update(float x, float y, float z) {
            vx = x;
            vy = y;
            vz = z;
        }

        public float getX() {
            return vx;
        }

        public float getY() {
            return vy;
        }

        public float getZ() {
            return vz;
        }

        public String toString() {
            return String.format("Velocity: (%.3f, %.3f, %.3f)", vx, vy, vz);
        }
    }

    public static class Location {
        private float x, y, z;

        public Location(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public void update(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getZ() {
            return z;
        }

        public String toString() {
            return String.format("Location: (%.3f, %.3f, %.3f)", x, y, z);
        }
    }

    public static class DataCounterStream {
        private int totalBytesRead;
        private DataInputStream dis;
        private ByteBuffer buf;

        public DataCounterStream(byte[] buffer, DataInputStream dis) {
            totalBytesRead = 0;
            this.dis = dis;
            buf = ByteBuffer.wrap(buffer).asReadOnlyBuffer().order(ByteOrder.LITTLE_ENDIAN);
        }

        public char getIndicator() {
            try {
                int sizeInd = dis.readInt();
                totalBytesRead += 4;
                byte[] indicatorByte = new byte[sizeInd];
                totalBytesRead += dis.read(indicatorByte, 0, sizeInd);
                return (char) indicatorByte[0];
            } catch (Exception e) {
                return 0;
            }
        }

        public String getString() {
            try {
                int nameSize = dis.readInt();
                totalBytesRead += 4;
                byte[] stringToRead = new byte[nameSize];
                int read = dis.read(stringToRead, 0, nameSize);
                if (read >= 0) {
                    totalBytesRead += read;
                    return new String(stringToRead);
                } else {
                    return "";
                }
            } catch (Exception e) {
                return "";
            }
        }

        public float getFloat() {
            float toRet = buf.getFloat(totalBytesRead);
            totalBytesRead += 4;
            return toRet;
        }

        public int readUnsignedByte() {
            try {
                totalBytesRead += 8;
                return dis.readUnsignedByte();
            } catch (Exception e) {
                return -1;
            }
        }
    }
}