import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashSet;

public class GhostingServer {


    public static HashSet<User> usersToRemove = new HashSet<>();
    public static HashSet<User> userSet = new HashSet<>();
    public static HashSet<User> usersWhoConnected = new HashSet<>();


    public static void main(String args[]) throws Exception {
        ServerReceive rec = new ServerReceive();
        ServerSend send = new ServerSend();
        send.start();
        rec.start();
    }

    public static void log(Object message) {
        System.out.println(message.toString());
    }


    public static class ServerSend extends Thread {

        DatagramSocket socket = null;


        public ServerSend() throws Exception {
            socket = new DatagramSocket(4446, InetAddress.getLocalHost());
        }

        @Override
        public synchronized void start() {
            System.out.println("Starting server...");
            super.start();
        }

        @Override
        public void run() {
            while (true) {
                byte[] buffer;
                HashSet<User> existingUsers = duplicateSet(userSet);
                HashSet<User> newUsers = duplicateSet(usersWhoConnected);
                HashSet<User> delete = duplicateSet(usersToRemove);
                if (!newUsers.isEmpty()) {//send new user request
                    for (User newUser : newUsers) {
                        //the following sends the NEW USER data to the OTHER PEOPLE
                        for (User existingUser : existingUsers) {
                            if (newUser.getName().equalsIgnoreCase(existingUser.getName())) continue;
                            buffer = new byte[512];
                            ByteBuffer buf = ByteBuffer.wrap(buffer);
                            buf.put((byte) 0x00);
                            buf.putInt(newUser.getName().length());
                            buf.put(newUser.getName().getBytes());
                            buf.put((byte) newUser.getTrailLength());
                            Color trail = newUser.getTrailColor();
                            Color ghost = newUser.getGhostColor();
                            buf.put((byte) trail.getRed());
                            buf.put((byte) trail.getGreen());
                            buf.put((byte) trail.getBlue());
                            buf.put((byte) ghost.getRed());
                            buf.put((byte) ghost.getGreen());
                            buf.put((byte) ghost.getBlue());
                            DatagramPacket d = new DatagramPacket(buf.array(), buf.array().length, existingUser.getInetAddress(), existingUser.getPort());
                            sendPacket(d);
                        }
                        //the following sends the OTHER PEOPLE data to the NEW PERSON
                        for (User existingUser : existingUsers) {
                            if (newUser.getName().equalsIgnoreCase(existingUser.getName())) continue;
                            buffer = new byte[512];
                            ByteBuffer buf = ByteBuffer.wrap(buffer);
                            buf.put((byte) 0x00);
                            buf.putInt(existingUser.getName().length());
                            buf.put(existingUser.getName().getBytes());
                            buf.put((byte) existingUser.getTrailLength());
                            Color trail = existingUser.getTrailColor();
                            Color ghost = existingUser.getGhostColor();
                            buf.put((byte) trail.getRed());
                            buf.put((byte) trail.getGreen());
                            buf.put((byte) trail.getBlue());
                            buf.put((byte) ghost.getRed());
                            buf.put((byte) ghost.getGreen());
                            buf.put((byte) ghost.getBlue());
                            DatagramPacket d = new DatagramPacket(buf.array(), buf.array().length, newUser.getInetAddress(), newUser.getPort());
                            sendPacket(d);
                        }
                    }
                    clearUsers();
                }
                if (!delete.isEmpty() && !existingUsers.isEmpty()) {
                    for (User u : delete) {
                        buffer = new byte[512];
                        //make the byte array
                        ByteBuffer buf = ByteBuffer.wrap(buffer);
                        buf.put((byte) 0x04);
                        buf.putInt(u.getName().length());
                        buf.put(u.getName().getBytes());
                        //now send
                        for (User existing : existingUsers) {
                            DatagramPacket d = new DatagramPacket(buf.array(), buf.array().length, existing.getInetAddress(), existing.getPort());
                            sendPacket(d);
                        }
                    }
                    clearRemove();
                }
                if (!existingUsers.isEmpty()) { //send run lines
                    for (User recipient : existingUsers) {
                        for (User toSend : existingUsers) {
                            if (recipient.getName().equalsIgnoreCase(toSend.getName())) continue;
                            buffer = new byte[512];
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
                            //log("Sending " + name + " on " + map + " at (" + p.getX() + ", " + p.getY() + ", " + p.getZ() + ") to " + recipient.getInetAddress().toString() + ":" + recipient.getPort());
                            DatagramPacket d = new DatagramPacket(buf.array(), buf.array().length, recipient.getInetAddress(), recipient.getPort());
                            sendPacket(d);
                        }
                    }
                }
                sleep(50);
            }
        }

        synchronized void clearRemove() {
            usersToRemove.clear();
        }

        synchronized void clearUsers() {
            usersWhoConnected.clear();
        }

        synchronized void sendPacket(DatagramPacket d) {
            try {
                socket.send(d);
            } catch (Exception ignored) {
                log(ignored.getMessage());
            }
        }

        void sleep(int millis) {
            try {
                Thread.sleep(millis);
            } catch (Exception ignored) {
            }
        }
    }


    public static synchronized User[] getUsers() {
        return userSet.toArray(new User[userSet.size()]);
    }

    public static class ServerReceive extends Thread {

        DatagramSocket socket = null;

        public ServerReceive() throws Exception {
            socket = new DatagramSocket(4445, InetAddress.getLocalHost());
        }

        @Override
        public synchronized void start() {
            super.start();
        }

        @Override
        public void run() {
            while (true) {//todo change to a boolean when the GUI is made
                try {
                    byte[] buf = new byte[512];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    DataCounterStream dcs = new DataCounterStream(buf, new DataInputStream(new ByteArrayInputStream(buf)));
                    char indicator = dcs.getIndicator();
                    switch (indicator) {
                        case 'c': {
                            String name = dcs.getString();
                            //TODO handle address changes (dynamic ips) and imposters (append the #)
                            int trailLength = dcs.readUnsignedByte();
                            int trailRed = dcs.readUnsignedByte();
                            int trailGreen = dcs.readUnsignedByte();
                            int trailBlue = dcs.readUnsignedByte();
                            int ghostRed = dcs.readUnsignedByte();
                            int ghostGreen = dcs.readUnsignedByte();
                            int ghostBlue = dcs.readUnsignedByte();
                            String color = "" + trailRed + "," + trailGreen + "," + trailBlue;
                            System.out.println("User received! " + name + " trail: " + trailLength + " colors: " + color);
                            User u = new User(name, packet.getAddress(), packet.getPort(), trailLength, trailRed, trailGreen, trailBlue, ghostRed, ghostGreen, ghostBlue);
                            addNewUser(u);
                            break;
                        }
                        case 'l': {
                            String name = dcs.getString();
                            User u = getUser(name);

                            //TODO handle address changes (dynamic ips) and or imposters (append the #)
                            if (u != null) {
                                //see the todo above
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
                            }
                            break;
                        }
                        case 'd': {
                            //TODO verify the sending packet
                            String name = dcs.getString();
                            User u = getUser(name);
                            if (u != null) {
                                log(u.getName() + " has disconnected!");
                                markUserForDeletion(name);
                            }
                            break;
                        }
                        default:
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static synchronized User getUser(String name) {
        for (User u : userSet) {
            if (u.getName().equalsIgnoreCase(name)) {
                return u;
            }
        }
        return null;
    }

    public static synchronized User removeUser(String name) {
        if (getUser(name) != null) {
            for (User u : userSet) {
                if (u.getName().equalsIgnoreCase(name)) {
                    userSet.remove(u);
                    return u;
                }
            }
        }
        return null;
    }

    public static synchronized void markUserForDeletion(String name) {
        User removed = removeUser(name);
        if (removed != null && !userSet.isEmpty()) {
            log("Marking " + name + " for deletion!");
            usersToRemove.add(removed);
        }
    }

    public static synchronized void addNewUser(User u) {
        log("Adding user " + u.getName() + " with IP: " + u.getInetAddress().toString() + ":" + u.getPort());
        usersWhoConnected.add(u);
        userSet.add(u);
    }

    public static synchronized HashSet<User> duplicateSet(HashSet<User> map) {
        HashSet<User> dupe = new HashSet<>();
        dupe.addAll(map);
        return dupe;
    }

    public static class User {

        String name;
        String map;
        Velocity vel;
        Location loc;
        int trailLength;
        Color ghostColor;
        Color trailColor;
        InetAddress inetAddress;
        int port;


        public User(String name, InetAddress address, int port, int tl, int tr, int tg, int tb, int gr, int gg, int gb) {
            this.name = name;
            map = "";
            inetAddress = address;
            this.port = port;
            trailLength = tl;
            ghostColor = new Color(gr, gg, gb);
            trailColor = new Color(tr, tg, tb);
            loc = new Location(0, 0, 0);
            vel = new Velocity(0, 0, 0);
        }

        public void setLocation(float x, float y, float z) {
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
            return loc;
        }

        public InetAddress getInetAddress() {
            return inetAddress;
        }

        public int getPort() {
            return port;
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
    }

    public static class Velocity {
        float vx, vy, vz;

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
    }

    public static class Location {

        float x, y, z;

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

    }


    public static class DataCounterStream {

        int totalBytesRead;
        DataInputStream dis;
        ByteBuffer buf;
        byte[] buffer;

        public DataCounterStream(byte[] buffer, DataInputStream dis) {
            totalBytesRead = 0;
            this.dis = dis;
            this.buffer = buffer;
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
