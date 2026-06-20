package com.zombiesdots;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class ProfileManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String INDEX_FILE    = "profiles.json";
    private static final String SETTINGS_FILE = "settings.json";
    private static final String OFF_PROFILE   = "off";

    private final File configDir;
    private String activeProfile;
    private List<String> profiles;
    private final Map<String, List<MarkerData>> markerCache = new HashMap<>();

    private int customR = 255, customG = 0, customB = 255;  // default: magenta

    public ProfileManager(File configDir) {
        this.configDir = configDir;
        if (!configDir.exists()) configDir.mkdirs();
        loadIndex();
        loadSettings();
    }

    public int getCustomR() { return customR; }
    public int getCustomG() { return customG; }
    public int getCustomB() { return customB; }
    public int getCustomARGB() { return 0xFF000000 | (customR << 16) | (customG << 8) | customB; }

    public void setCustomColor(int r, int g, int b) {
        customR = r; customG = g; customB = b;
        saveSettings();
    }

    private void loadSettings() {
        File f = new File(configDir, SETTINGS_FILE);
        if (!f.exists()) return;
        try (Reader r = new FileReader(f)) {
            Settings s = GSON.fromJson(r, Settings.class);
            if (s != null && s.customColor != null && s.customColor.length() == 6) {
                customR = Integer.parseInt(s.customColor.substring(0, 2), 16);
                customG = Integer.parseInt(s.customColor.substring(2, 4), 16);
                customB = Integer.parseInt(s.customColor.substring(4, 6), 16);
            }
        } catch (Exception ignored) {}
    }

    private void saveSettings() {
        File f = new File(configDir, SETTINGS_FILE);
        try (Writer w = new FileWriter(f)) {
            Settings s = new Settings();
            s.customColor = String.format("%02X%02X%02X", customR, customG, customB);
            GSON.toJson(s, w);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadIndex() {
        File f = new File(configDir, INDEX_FILE);
        if (f.exists()) {
            try (Reader r = new FileReader(f)) {
                ProfileIndex idx = GSON.fromJson(r, ProfileIndex.class);
                if (idx != null) {
                    activeProfile = idx.activeProfile != null ? idx.activeProfile : "Default";
                    profiles = idx.profiles != null ? idx.profiles : defaultProfiles();
                    return;
                }
            } catch (IOException e) {
                // ignore, use defaults
            }
        }
        activeProfile = "Default";
        profiles = defaultProfiles();
        saveIndex();
    }

    private void saveIndex() {
        File f = new File(configDir, INDEX_FILE);
        try (Writer w = new FileWriter(f)) {
            ProfileIndex idx = new ProfileIndex();
            idx.activeProfile = activeProfile;
            idx.profiles = profiles;
            GSON.toJson(idx, w);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> defaultProfiles() {
        return new ArrayList<>(Arrays.asList(
                "Default", "DeadEnd", "BadBlood", "AlienArcade", "Prison", "Practice"
        ));
    }

    public String getActiveProfile() {
        return activeProfile;
    }

    public List<String> getProfiles() {
        return Collections.unmodifiableList(profiles);
    }

    public void setActiveProfile(String name) {
        activeProfile = name;
        saveIndex();
    }

    public boolean isOff() {
        return OFF_PROFILE.equalsIgnoreCase(activeProfile);
    }

    public void createProfile(String name) {
        if (!profiles.contains(name)) {
            profiles.add(name);
            saveIndex();
        }
    }

    public void deleteProfile(String name) {
        profiles.remove(name);
        markerCache.remove(name);
        File f = markerFile(name);
        if (f.exists()) f.delete();
        if (name.equals(activeProfile)) {
            activeProfile = profiles.isEmpty() ? "Default" : profiles.get(0);
        }
        saveIndex();
    }

    public void renameProfile(String oldName, String newName) {
        int idx = profiles.indexOf(oldName);
        if (idx >= 0) {
            profiles.set(idx, newName);
            List<MarkerData> markers = markerCache.remove(oldName);
            if (markers != null) markerCache.put(newName, markers);
            File oldFile = markerFile(oldName);
            if (oldFile.exists()) oldFile.renameTo(markerFile(newName));
            if (oldName.equals(activeProfile)) activeProfile = newName;
            saveIndex();
        }
    }

    public List<MarkerData> getMarkers(String profile) {
        if (OFF_PROFILE.equalsIgnoreCase(profile)) return Collections.emptyList();
        if (!markerCache.containsKey(profile)) {
            markerCache.put(profile, loadMarkers(profile));
        }
        return markerCache.get(profile);
    }

    public List<MarkerData> getActiveMarkers() {
        return getMarkers(activeProfile);
    }

    public void addMarker(MarkerData marker) {
        List<MarkerData> list = getMarkers(activeProfile);
        list.add(marker);
        saveMarkers(activeProfile, list);
    }

    public boolean removeClosestMarkerAt(int blockX, int blockY, int blockZ,
                                          double hitX, double hitY, double hitZ) {
        List<MarkerData> list = getMarkers(activeProfile);
        MarkerData best = null;
        double bestDist = Double.MAX_VALUE;
        for (MarkerData m : list) {
            if (!m.sameBlock(blockX, blockY, blockZ)) continue;
            double dx = m.hitX - hitX;
            double dy = m.hitY - hitY;
            double dz = m.hitZ - hitZ;
            double d = dx*dx + dy*dy + dz*dz;
            if (d < bestDist) { bestDist = d; best = m; }
        }
        if (best == null) return false;
        list.remove(best);
        saveMarkers(activeProfile, list);
        return true;
    }

    public int getMarkerCount(String profile) {
        return getMarkers(profile).size();
    }

    private List<MarkerData> loadMarkers(String profile) {
        File f = markerFile(profile);
        if (!f.exists()) return new ArrayList<>();
        try (Reader r = new FileReader(f)) {
            Type listType = new TypeToken<List<MarkerData>>() {}.getType();
            List<MarkerData> result = GSON.fromJson(r, listType);
            return result != null ? result : new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private void saveMarkers(String profile, List<MarkerData> markers) {
        File f = markerFile(profile);
        try (Writer w = new FileWriter(f)) {
            GSON.toJson(markers, w);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File markerFile(String profile) {
        String safeName = profile.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        return new File(configDir, "markers_" + safeName + ".json");
    }

    private static class ProfileIndex {
        String activeProfile;
        List<String> profiles;
    }

    private static class Settings {
        String customColor;
    }
}
