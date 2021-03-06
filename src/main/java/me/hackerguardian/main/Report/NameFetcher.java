package me.hackerguardian.main.Report;

import com.google.common.collect.ImmutableList;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

public class NameFetcher implements Callable<Map<UUID, String>> {
    private static final String PROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private final org.json.simple.parser.JSONParser jsonParser = new JSONParser();
    private final List<UUID> uuids;
    public NameFetcher(List<UUID> uuids) {
        this.uuids = (List<UUID>)ImmutableList.copyOf(uuids);
    }
    @SuppressWarnings({"lgtm [java/input-resource-leak]"})
    public Map<UUID, String> call() throws Exception {
        Map<UUID, String> uuidStringMap = new HashMap<>();
        for (UUID uuid : this.uuids) {
            HttpURLConnection connection = (HttpURLConnection)(new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString().replace("-", ""))).openConnection();
            org.json.simple.JSONObject response = (JSONObject)this.jsonParser.parse(new InputStreamReader(connection.getInputStream()));
            String name = (String)response.get("name");
            if (name == null)
                continue;
            String cause = (String)response.get("cause");
            String errorMessage = (String)response.get("errorMessage");
            if (cause != null && cause.length() > 0)
                throw new IllegalStateException(errorMessage);
            uuidStringMap.put(uuid, name);
            connection.disconnect();
        }
        return uuidStringMap;
    }
}