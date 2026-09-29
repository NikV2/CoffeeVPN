package me.nik.coffeevpn.files;

import me.nik.coffeevpn.CoffeeVPN;
import me.nik.coffeevpn.files.commentedfiles.CommentedFileConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Config {

    private static final String[] HEADER = new String[]{
            "+----------------------------------------------------------------------------------------------+",
            "|                                                                                              |",
            "|                                            CoffeeVPN                                         |",
            "|                                                                                              |",
            "|                               Discord: https://discord.gg/m7j2Y9H                            |",
            "|                                                                                              |",
            "|                                           Author: Nik                                        |",
            "|                                                                                              |",
            "+----------------------------------------------------------------------------------------------+"
    };

    private final CoffeeVPN plugin;
    private CommentedFileConfiguration configuration;
    private static boolean exists;

    public Config(CoffeeVPN plugin) {
        this.plugin = plugin;
    }

    public void setup() {

        File configFile = new File(this.plugin.getDataFolder(), "config.yml");

        exists = configFile.exists();

        boolean setHeaderFooter = !exists;

        boolean changed = setHeaderFooter;

        this.configuration = CommentedFileConfiguration.loadConfiguration(this.plugin, configFile);

        if (setHeaderFooter) {
            this.configuration.addComments(HEADER);
        }

        for (Setting setting : Setting.values()) {

            setting.reset();

            changed |= setting.setIfNotExists(this.configuration);
        }

        if (changed) this.configuration.save();

        for (Setting setting : Setting.values()) setting.loadValue();
    }

    public void reset() {
        for (Setting setting : Setting.values()) setting.reset();
    }

    /**
     * @return the config.yml as a CommentedFileConfiguration
     */
    public CommentedFileConfiguration getConfig() {
        return this.configuration;
    }

    public enum Setting {
        MESSAGE("message", "&cVPN Usage is prohibited in this server!", "The message that players that try to join with a VPN/Proxy will see"),
        CACHE_CLEANUP("cache_cleanup", 30, "How often should we clear the connection cache? (In minutes)"),
        WHITELISTED_PLAYERS("whitelisted_players",
                Arrays.asList("Player1", "Player2"),
                "Player names listed below will be allowed to join using a VPN/Proxy"),

        PROVIDERS("providers", "", "VPN Providers", "Feel free to add your own by following the default format"),

        PROVIDERS_PROXYCHECKIO_URL("providers.proxycheckio.url", "https://proxycheck.io/v2/%ip%?key=%key%&vpn=1", true, "The url for this provider", "Placeholders: %ip% (The player's IP), %key% (The API Key)"),
        PROVIDERS_PROXYCHECKIO_API_KEY("providers.proxycheckio.api_key", "", true, "The API Key", "You can get one for free online"),
        PROVIDERS_PROXYCHECKIO_CHECK("providers.proxycheckio.check", "\"proxy\": \"yes\"", true, "If the answer we receive from the provider contains the text below, It will be considered as positive (VPN/Proxy)"),

        PROVIDERS_HACKMYIP_URL("providers.hackmyip.url", "https://hackmyip.com/api/lookup?ip=%ip%", true, "The url for this provider", "Placeholders: %ip% (The player's IP), %key% (The API Key)"),
        PROVIDERS_HACKMYIP_API_KEY("providers.hackmyip.api_key", "", true, "The API Key", "You can get one for free online"),
        PROVIDERS_HACKMYIP_CHECK("providers.hackmyip.check", "\"proxy\":true", true, "If the answer we receive from the provider contains the text below, It will be considered as positive (VPN/Proxy)");

        private final String key;
        private final Object defaultValue;
        private boolean excluded;
        private final String[] comments;
        private Object value = null;

        Setting(String key, Object defaultValue, String... comments) {
            this.key = key;
            this.defaultValue = defaultValue;
            this.comments = comments != null ? comments : new String[0];
        }

        Setting(String key, Object defaultValue, boolean excluded, String... comments) {
            this.key = key;
            this.defaultValue = defaultValue;
            this.comments = comments != null ? comments : new String[0];
            this.excluded = excluded;
        }

        /**
         * Gets the setting as a boolean
         *
         * @return The setting as a boolean
         */
        public boolean getBoolean() {
            this.loadValue();
            return (boolean) this.value;
        }

        public String getKey() {
            return this.key;
        }

        /**
         * @return the setting as an int
         */
        public int getInt() {
            this.loadValue();
            return (int) this.getNumber();
        }

        /**
         * @return the setting as a short
         */
        public short getShort() {
            this.loadValue();
            return (short) this.getNumber();
        }

        /**
         * @return the setting as a long
         */
        public long getLong() {
            this.loadValue();
            return (long) this.getNumber();
        }

        /**
         * @return the setting as a double
         */
        public double getDouble() {
            this.loadValue();
            return this.getNumber();
        }

        /**
         * @return the setting as a float
         */
        public float getFloat() {
            this.loadValue();
            return (float) this.getNumber();
        }

        /**
         * @return the setting as a String
         */
        public String getString() {
            this.loadValue();
            return String.valueOf(this.value);
        }

        private double getNumber() {
            if (this.value instanceof Integer) {
                return (int) this.value;
            } else if (this.value instanceof Short) {
                return (short) this.value;
            } else if (this.value instanceof Byte) {
                return (byte) this.value;
            } else if (this.value instanceof Float) {
                return (float) this.value;
            } else if (this.value instanceof Long) {
                return (long) this.value;
            }

            return (double) this.value;
        }

        /**
         * @return the setting as a string list
         */
        @SuppressWarnings("unchecked")
        public List<String> getStringList() {
            this.loadValue();
            return (List<String>) this.value;
        }

        private boolean setIfNotExists(CommentedFileConfiguration fileConfiguration) {
            this.loadValue();

            if (exists && this.excluded) return false;

            if (fileConfiguration.get(this.key) == null) {
                List<String> comments = Stream.of(this.comments).collect(Collectors.toList());
                if (this.defaultValue != null) {
                    fileConfiguration.set(this.key, this.defaultValue, comments.toArray(new String[0]));
                } else {
                    fileConfiguration.addComments(comments.toArray(new String[0]));
                }

                return true;
            }

            return false;
        }

        /**
         * Resets the cached value
         */
        public void reset() {
            this.value = null;
        }

        /**
         * @return true if this setting is only a section and doesn't contain an actual value
         */
        public boolean isSection() {
            return this.defaultValue == null;
        }

        /**
         * Loads the value from the config and caches it if it isn't set yet
         */
        public void loadValue() {
            if (this.value != null) return;
            this.value = CoffeeVPN.getInstance().getConfiguration().getConfig().get(this.key);
        }
    }
}