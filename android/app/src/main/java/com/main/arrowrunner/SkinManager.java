package com.main.arrowrunner;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * High-Performance Local Data Persistence Manager.
 * Stores player coins, high score, unlocked skins, equipped skins, and theme settings safely in SharedPreferences.
 */
public class SkinManager {
    private static final String PREF_NAME = "arrow_runner_persistent_data";
    private static final String KEY_COINS = "total_coins";
    private static final String KEY_HIGH_SCORE = "high_score";
    private static final String KEY_THEME = "theme_mode"; // "dark" or "light"
    private static final String KEY_SFX_VOL = "sfx_volume";
    private static final String KEY_MUTED = "is_muted";
    private static final String KEY_VFX = "vfx_quality";
    
    private static final String KEY_EQUIPPED_ARROW = "eq_arrow";
    private static final String KEY_EQUIPPED_HAZARD = "eq_hazard";
    private static final String KEY_EQUIPPED_POINT = "eq_point";
    private static final String KEY_EQUIPPED_COIN = "eq_coin";
    private static final String KEY_UNLOCKED = "unlocked_items_json";

    public static class SkinItem {
        public String id;
        public String name;
        public String category; // "arrow", "hazard", "point", "coin"
        public int price;
        public int color;
        public int coreColor;
        public String icon;
        public String desc;

        public SkinItem(String id, String name, String category, int price, int color, int coreColor, String icon, String desc) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.price = price;
            this.color = color;
            this.coreColor = coreColor;
            this.icon = icon;
            this.desc = desc;
        }
    }

    private final SharedPreferences prefs;
    private final Map<String, List<SkinItem>> categorySkins = new HashMap<>();

    public SkinManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        initSkinCatalog();
    }

    private void initSkinCatalog() {
        // 1. ARROW SKINS (10 Unique Ships)
        List<SkinItem> arrows = new ArrayList<>();
        arrows.add(new SkinItem("arrow_0", "Classic Stealth", "arrow", 0, 0xFF0288D1, 0xFF00E5FF, "🚀", "Standard Dual-Wing Jet"));
        arrows.add(new SkinItem("arrow_1", "Gold Vector", "arrow", 40, 0xFFF59E0B, 0xFFFFF59D, "🚀", "Polished Diamond Gold Vessel"));
        arrows.add(new SkinItem("arrow_2", "Cyber Razor", "arrow", 80, 0xFFEF4444, 0xFFFCA5A5, "🚀", "Curved Razor Blade Wing"));
        arrows.add(new SkinItem("arrow_3", "Quantum Pulsar", "arrow", 150, 0xFF8B5CF6, 0xFFDDD6FE, "🚀", "Quad-Wing Star Destroyer"));
        arrows.add(new SkinItem("arrow_4", "Toxic Viper", "arrow", 250, 0xFF10B981, 0xFFA7F3D0, "🚀", "Trident Hydrofoil Wing"));
        arrows.add(new SkinItem("arrow_5", "Cryo Hawk", "arrow", 400, 0xFF06B6D4, 0xFFCFF4FC, "🚀", "Glacial Crystal Hawk"));
        arrows.add(new SkinItem("arrow_6", "Solar Valkyrie", "arrow", 600, 0xFFEA580C, 0xFFFEF08A, "🚀", "Twin Sunfire Wing Vessel"));
        arrows.add(new SkinItem("arrow_7", "Void Shadow", "arrow", 850, 0xFF475569, 0xFFE2E8F0, "🚀", "Stealth Black Delta Wing"));
        arrows.add(new SkinItem("arrow_8", "Matrix Drone", "arrow", 1100, 0xFF84CC16, 0xFFD9F99D, "🚀", "Hexagonal Cyber Drone"));
        arrows.add(new SkinItem("arrow_9", "Titan Omega", "arrow", 1500, 0xFF38BDF8, 0xFFF59E0B, "🚀", "Heavy Triple-Shielded Omega"));
        categorySkins.put("arrow", arrows);

        // 2. HAZARD / BOMB SKINS (10 Unique Danger Models)
        List<SkinItem> hazards = new ArrayList<>();
        hazards.add(new SkinItem("bomb_0", "Crimson Spike", "hazard", 0, 0xFFEF4444, 0xFFFFFFFF, "▲", "Danger Red 8-Spike Mine"));
        hazards.add(new SkinItem("bomb_1", "Bio Core", "hazard", 30, 0xFF84CC16, 0xFFFFFFFF, "☣", "Toxic Green Biohazard Hex"));
        hazards.add(new SkinItem("bomb_2", "Plasma Trap", "hazard", 70, 0xFF8B5CF6, 0xFFFFFFFF, "🌀", "Swirling Violet Ring Core"));
        hazards.add(new SkinItem("bomb_3", "Nuclear Core", "hazard", 120, 0xFFF59E0B, 0xFFFFFFFF, "☢", "Yellow Fission Symbol Core"));
        hazards.add(new SkinItem("bomb_4", "Cryo Cluster", "hazard", 200, 0xFF06B6D4, 0xFFFFFFFF, "❄", "Jagged Ice Crystal Cluster"));
        hazards.add(new SkinItem("bomb_5", "Magma Saw", "hazard", 320, 0xFFEA580C, 0xFFFFFFFF, "🔥", "12-Tooth Rotating Sawblade"));
        hazards.add(new SkinItem("bomb_6", "Void Hole", "hazard", 500, 0xFF475569, 0xFFFFFFFF, "⚫", "Swirling Black Hole"));
        hazards.add(new SkinItem("bomb_7", "Glitch Box", "hazard", 750, 0xFF0288D1, 0xFFFFFFFF, "🎲", "3D Matrix Block"));
        hazards.add(new SkinItem("bomb_8", "Laser Pyramid", "hazard", 1000, 0xFFEF4444, 0xFFFFFFFF, "⚡", "Warning Laser Pyramid"));
        hazards.add(new SkinItem("bomb_9", "Skull Mine", "hazard", 1400, 0xFFE2E8F0, 0xFFFFFFFF, "💀", "Fiery Skull Danger Mine"));
        categorySkins.put("hazard", hazards);

        // 3. POINTS / ORB SKINS (10 Unique Score Models)
        List<SkinItem> points = new ArrayList<>();
        points.add(new SkinItem("orb_0", "Emerald Pearl", "point", 0, 0xFF10B981, 0xFFFFFFFF, "🔮", "Classic Green Pearl Sphere"));
        points.add(new SkinItem("orb_1", "Cyan Spark", "point", 30, 0xFF0288D1, 0xFFFFFFFF, "⚡", "4-Point Cyan Lightning Spark"));
        points.add(new SkinItem("orb_2", "Golden Star", "point", 70, 0xFFF59E0B, 0xFFFFFFFF, "★", "5-Point Yellow Star"));
        points.add(new SkinItem("orb_3", "Magenta Gem", "point", 120, 0xFFEC4899, 0xFFFFFFFF, "◆", "Octagonal Cut Ruby Gem"));
        points.add(new SkinItem("orb_4", "Violet Soul", "point", 200, 0xFF8B5CF6, 0xFFFFFFFF, "✦", "Swirling Spirit Ring"));
        points.add(new SkinItem("orb_5", "Fire Teardrop", "point", 320, 0xFFEA580C, 0xFFFFFFFF, "🔥", "Molten Flame Drop"));
        points.add(new SkinItem("orb_6", "Snow Crystal", "point", 500, 0xFF06B6D4, 0xFFFFFFFF, "❄", "Hexagonal Snow Crystal"));
        points.add(new SkinItem("orb_7", "Matrix Cube", "point", 750, 0xFF84CC16, 0xFFFFFFFF, "🟩", "Digital Green Data Node"));
        points.add(new SkinItem("orb_8", "Hyper Diamond", "point", 1000, 0xFFEF4444, 0xFFFFFFFF, "💎", "3D Cut Score Diamond"));
        points.add(new SkinItem("orb_9", "Cosmic Core", "point", 1400, 0xFF38BDF8, 0xFFFFFFFF, "🌌", "Swirling Galaxy Core"));
        categorySkins.put("point", points);

        // 4. COINS SKINS (10 Unique Currency Models)
        List<SkinItem> coins = new ArrayList<>();
        coins.add(new SkinItem("coin_0", "Gold Doubloon", "coin", 0, 0xFFF59E0B, 0xFFFFFFFF, "🪙", "Classic Metallic Gold Coin"));
        coins.add(new SkinItem("coin_1", "Cyber Credit", "coin", 30, 0xFF0288D1, 0xFFFFFFFF, "💳", "Neon Blue Hologram Credit"));
        coins.add(new SkinItem("coin_2", "Ruby Drop", "coin", 70, 0xFFEF4444, 0xFFFFFFFF, "🔻", "Deep Faceted Red Ruby"));
        coins.add(new SkinItem("coin_3", "Emerald Prism", "coin", 120, 0xFF10B981, 0xFFFFFFFF, "◆", "Emerald Cut Green Gem"));
        coins.add(new SkinItem("coin_4", "Sapphire Star", "coin", 200, 0xFF0288D1, 0xFFFFFFFF, "★", "6-Point Blue Star Medal"));
        coins.add(new SkinItem("coin_5", "Amethyst Shard", "coin", 320, 0xFF8B5CF6, 0xFFFFFFFF, "✦", "Purple Crystal Cluster"));
        coins.add(new SkinItem("coin_6", "Casino Chip", "coin", 500, 0xFF94A3B8, 0xFFFFFFFF, "🎰", "Striped Casino Token"));
        coins.add(new SkinItem("coin_7", "Crypto Coin", "coin", 750, 0xFF84CC16, 0xFFFFFFFF, "₿", "Glowing Bitcoin Medal"));
        coins.add(new SkinItem("coin_8", "Solar Medallion", "coin", 1000, 0xFFEA580C, 0xFFFFFFFF, "☀️", "Sunburst Flame Medal"));
        coins.add(new SkinItem("coin_9", "Quantum Relic", "coin", 1400, 0xFF38BDF8, 0xFFFFFFFF, "🌌", "Cosmic Orbital Relic"));
        categorySkins.put("coin", coins);
    }

    public List<SkinItem> getSkinsForCategory(String category) {
        List<SkinItem> list = categorySkins.get(category);
        return list != null ? list : categorySkins.get("arrow");
    }

    public SkinItem getSkin(String category, String id) {
        List<SkinItem> list = getSkinsForCategory(category);
        for (SkinItem item : list) {
            if (item.id.equals(id)) return item;
        }
        return list.get(0);
    }

    // Persisted SharedPreferences Getters & Setters
    public int getTotalCoins() {
        return prefs.getInt(KEY_COINS, 0);
    }

    public void addCoins(int amount) {
        int current = getTotalCoins();
        prefs.edit().putInt(KEY_COINS, current + amount).apply();
    }

    public boolean spendCoins(int amount) {
        int current = getTotalCoins();
        if (current >= amount) {
            prefs.edit().putInt(KEY_COINS, current - amount).apply();
            return true;
        }
        return false;
    }

    public int getHighScore() {
        return prefs.getInt(KEY_HIGH_SCORE, 0);
    }

    public boolean checkAndUpdateHighScore(int score) {
        if (score > getHighScore()) {
            prefs.edit().putInt(KEY_HIGH_SCORE, score).apply();
            return true;
        }
        return false;
    }

    public String getThemeMode() {
        return prefs.getString(KEY_THEME, "dark");
    }

    public void setThemeMode(String mode) {
        prefs.edit().putString(KEY_THEME, mode).apply();
    }

    public boolean isMuted() {
        return prefs.getBoolean(KEY_MUTED, false);
    }

    public void setMuted(boolean muted) {
        prefs.edit().putBoolean(KEY_MUTED, muted).apply();
    }

    public int getSfxVolume() {
        return prefs.getInt(KEY_SFX_VOL, 80);
    }

    public void setSfxVolume(int vol) {
        prefs.edit().putInt(KEY_SFX_VOL, vol).apply();
    }

    public String getVfxQuality() {
        return prefs.getString(KEY_VFX, "balanced");
    }

    public void setVfxQuality(String quality) {
        prefs.edit().putString(KEY_VFX, quality).apply();
    }

    public String getEquippedSkin(String category) {
        switch (category) {
            case "hazard": return prefs.getString(KEY_EQUIPPED_HAZARD, "bomb_0");
            case "point": return prefs.getString(KEY_EQUIPPED_POINT, "orb_0");
            case "coin": return prefs.getString(KEY_EQUIPPED_COIN, "coin_0");
            default: return prefs.getString(KEY_EQUIPPED_ARROW, "arrow_0");
        }
    }

    public void setEquippedSkin(String category, String skinId) {
        switch (category) {
            case "hazard": prefs.edit().putString(KEY_EQUIPPED_HAZARD, skinId).apply(); break;
            case "point": prefs.edit().putString(KEY_EQUIPPED_POINT, skinId).apply(); break;
            case "coin": prefs.edit().putString(KEY_EQUIPPED_COIN, skinId).apply(); break;
            default: prefs.edit().putString(KEY_EQUIPPED_ARROW, skinId).apply(); break;
        }
    }

    public boolean isSkinUnlocked(String category, String skinId) {
        if (skinId.endsWith("_0")) return true; // Default free skins
        Set<String> unlocked = getUnlockedSkinIds();
        return unlocked.contains(category + ":" + skinId);
    }

    public void unlockSkin(String category, String skinId) {
        Set<String> unlocked = new HashSet<>(getUnlockedSkinIds());
        unlocked.add(category + ":" + skinId);
        JSONArray array = new JSONArray(unlocked);
        prefs.edit().putString(KEY_UNLOCKED, array.toString()).apply();
    }

    private Set<String> getUnlockedSkinIds() {
        Set<String> set = new HashSet<>();
        String json = prefs.getString(KEY_UNLOCKED, "[]");
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                set.add(array.getString(i));
            }
        } catch (Exception ignored) {}
        return set;
    }
}
