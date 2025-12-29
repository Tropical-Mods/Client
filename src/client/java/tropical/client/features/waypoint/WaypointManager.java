package tropical.client.features.waypoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Random;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class WaypointManager {
    private static final String waypointFileName = "waypoints.wp";
    private static File waypointFile = null;

    private static ArrayList<Waypoint> waypoints = new ArrayList<>();

    public static void initalize() {
        waypointFile = new File(waypointFileName);

        try {
            File newFile = new File(waypointFileName);
            newFile.createNewFile();
        } catch (Exception e) {
            System.out.println("Failed to create waypointFile, exiting waypoint startup...");
            return;
        }

        try {
            FileReader r = new FileReader(waypointFile);
            BufferedReader read = new BufferedReader(r);
            String line;
            while ((line = read.readLine()) != null) {
                Waypoint newWp = rowToWaypoint(line);
                if (newWp == null) {
                    System.out.println("failed to parse row: " + line);
                    continue;
                }

                waypoints.add(newWp);
            }

            read.close();
        } catch (Exception e) {
            System.out.println("Failed to load waypoint file into memory: " + e);
            waypoints.clear();
            return;
        }

        //addDebugWaypoints();
    }

    private static void addDebugWaypoints() {
        Random r = new Random();
        for (int i = 0; i < 30; i++) {
            float rc = (float) r.nextInt((100 - 10) + 1) + 10;
            Waypoint wp = new Waypoint("Debug" + String.valueOf(i), rc, rc, rc, "nether", i, "testing");
            waypoints.add(wp);
        }
    }

    public static void toggleScreen() {
        System.out.println(waypoints.size());
        Screen currentScreen = Minecraft.getInstance().screen;
        if (currentScreen instanceof WaypointScreen) {
            Minecraft.getInstance().setScreen(null);
            return;
        }

        Minecraft.getInstance().setScreen(new WaypointScreen(currentScreen));
    }

    @Nullable
    public static Waypoint updateWaypoint(Waypoint oldWp, Waypoint newWp) {
        if (oldWp.id != newWp.id) { return null; }

        int index = getWaypointIndex(oldWp);
        if (index == -1) { return null; }

        waypoints.set(index, newWp);

        try {
            saveCurrentStateToFile();
        } catch (Exception e) {
            return null; 
        }

        return waypoints.get(index);
    }

    @Nullable
    public static Waypoint removeWaypoint(Waypoint wp) {
        int index = getWaypointIndex(wp);
        if (index == -1) { return null; }

        wp = waypoints.remove(index);

        try {
            saveCurrentStateToFile();
        } catch (Exception e) {
            return null;
        }

        return wp;
    }

    public static Waypoint toggleWaypoint(Waypoint wp) {
        int index = getWaypointIndex(wp);
        if (index == -1) {
            return wp;
        }

        wp.enabled = !wp.enabled;

        try {
            saveCurrentStateToFile();
        } catch (Exception e) {
            wp.enabled = !wp.enabled;
        }

        return wp;
    }

    // minecraft.overworld
    // minecraft.the_nether
    // minecraft.the_end
    @Nullable
    public static String getCurrentDimensionString() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) { return null; }

        return client.player.level().dimension().identifier().toLanguageKey();
    }

    public static void dimensionKeyToString() {
        System.out.println(Minecraft.getInstance().player.level().dimension().identifier().toLanguageKey());
    }

    private static int getWaypointIndex(Waypoint wp) {
        int index = -1;
        for (int i = 0; i < waypoints.size(); i++) {
            if (waypoints.get(i).id != wp.id) continue;
            index = i;
            break;
        }

        return index;
    }

    public static ArrayList<Waypoint> getWaypoints() {
        return new ArrayList<>(waypoints);
    }

    public static String getWorldId() {
        Minecraft client = Minecraft.getInstance();
        String worldId;
        if (client.isSingleplayer()) {
            worldId = client.getSingleplayerServer().getServerDirectory()
                        .toString()
                        .replaceAll(" ", "_");

        } else {
            worldId = client.getCurrentServer().ip;
        }

        return worldId;
    }

    @Nullable
    public static Waypoint addWaypoint(String waypointName, String dimension, float x, float y, float z) {
        String worldId = getWorldId();

        int id = getYoungestId() + 1;
        Waypoint wp = new Waypoint(waypointName, x, y, z, dimension, id, worldId);
        waypoints.add(wp);

        try {
            saveCurrentStateToFile();
        } catch (Exception e) {
            return null;
        }

        return wp;
    }

    private static int getYoungestId() {
        int current = 0;
        for (int i = 0; i < waypoints.size(); i++) {
            if (waypoints.get(i).id > current) current = waypoints.get(i).id;
        }

        return current;
    }

    private static void saveCurrentStateToFile() throws Exception {
       FileWriter w = new FileWriter(waypointFile, false);
        for (Waypoint wp : waypoints) {
            String row = waypointToRow(wp);
            w.write(row);
        }

        w.close();
    }

    private static Waypoint updateWaypointFile(Waypoint oldWp, Waypoint newWp) throws Exception {
        BufferedReader read = new BufferedReader(new FileReader(waypointFile));
        StringBuffer fileContents = new StringBuffer();
        String line;
        while ((line = read.readLine()) != null) {
            fileContents.append(line);
            fileContents.append("\n");
        }
        read.close();

        String newContents = fileContents.toString();
        String oldWaypointLine = waypointToRow(oldWp);
        String newWaypointLine = waypointToRow(newWp);
        newContents = newContents.replace(oldWaypointLine, newWaypointLine);

        FileOutputStream fileOut = new FileOutputStream(waypointFile);
        fileOut.write(newContents.getBytes());
        fileOut.close();
        return newWp;
    }

    public static String waypointToRow(Waypoint wp) {
        return wp.owner + "|" + String.valueOf(wp.id) + "|" + wp.name + "|" + wp.dimension + "|" + 
                String.valueOf(wp.x) + "|" + String.valueOf(wp.y) + "|" + String.valueOf(wp.z) + "|" +
                String.valueOf(wp.enabled) + "|" + String.valueOf(wp.timestamp) + "\n";
    }

    @Nullable
    private static Waypoint rowToWaypoint(String row) {
        String[] parts = row.replaceAll("\n", "").split("\\|");
        System.out.println(parts.length);

        if (parts.length != 9) { return null; }

        String owner = parts[0];
        int id = Integer.parseInt(parts[1]);
        String name = parts[2];
        String dimension = parts[3];
        float x = Float.parseFloat(parts[4]);
        float y = Float.parseFloat(parts[5]);
        float z = Float.parseFloat(parts[6]);
        boolean enabled = Boolean.parseBoolean(parts[7]);
        long timestamp = Long.parseLong(parts[8]);

        Waypoint wp = new Waypoint(name, x, y, z, dimension, id, owner, enabled, timestamp);

        return wp;
    }
}
