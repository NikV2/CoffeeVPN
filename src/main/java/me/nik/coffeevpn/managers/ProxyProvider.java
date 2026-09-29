package me.nik.coffeevpn.managers;

import me.nik.coffeevpn.utils.ChatUtils;

import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ProxyProvider {

    private final String providerUrl, stringCheck;

    public ProxyProvider(String providerUrl, String key, String stringCheck) {
        this.providerUrl = providerUrl.replace("%key%", key);
        this.stringCheck = stringCheck;
    }

    public boolean check(String address) {

        String response = null;

        try {
            final URLConnection connection = new URL(this.providerUrl.replace("%ip%", address)).openConnection();
            connection.addRequestProperty("User-Agent", "Mozilla/4.0");

            try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.toString())) {
                scanner.useDelimiter("\\A");
                response = scanner.hasNext() ? scanner.next() : "";
            }
        } catch (Exception ex) {
            ChatUtils.log("The provider " + this.providerUrl + " threw an error, Please ensure you're using an API Key!");
        }

        return response != null && response.contains(this.stringCheck);
    }
}