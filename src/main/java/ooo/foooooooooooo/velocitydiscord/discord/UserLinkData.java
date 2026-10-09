package ooo.foooooooooooo.velocitydiscord.discord;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import ooo.foooooooooooo.velocitydiscord.LinkedPlayer;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class UserLinkData {

  private static Gson gson = new GsonBuilder()
    .setPrettyPrinting()
    .create();

  private static final File linkFile = new File("plugins/discord/LinkedUsers.json");

  private static Map<String, LinkedPlayer> links = new HashMap<>();

  public static void load() throws IOException {
    if (!linkFile.exists()) {
      links = new HashMap<>();
      return;
    }

    Type type = new TypeToken<HashMap<String, LinkedPlayer>>(){}.getType();
    FileReader fileReader = new FileReader(linkFile);
    links = gson.fromJson(fileReader, type);
    fileReader.close();
  }

  public static void saveAll() throws IOException {
    FileWriter fileWriter = new FileWriter(linkFile);
    gson.toJson(links, fileWriter);
    fileWriter.close();
  }

  public static void addAndSave(String minecraftUUID, String minecraftName, String discordUserID) throws IOException {
    links.put(minecraftUUID.toLowerCase(), new LinkedPlayer(minecraftName, discordUserID));
    saveAll();
  }

  public static @Nullable String removeFromUUIDAndSave(String minecraftUUID) throws IOException {
    String lowercasedUUID = minecraftUUID.toLowerCase();
    boolean removed = links.remove(lowercasedUUID) != null;
    if (removed) {
      saveAll();
      return lowercasedUUID;
    };
    return null;
  }

  public static @Nullable String removeFromNameAndSave(String minecraftName) throws IOException {
    String uuid = null;
    String lowercaseMinecraftName = minecraftName.toLowerCase();
    for (var link : links.entrySet()) {
      if (link.getValue().minecraftName.toLowerCase().equals(lowercaseMinecraftName)) {
        uuid = link.getKey();
      }
    }
    if (uuid == null) return null;
    return removeFromUUIDAndSave(uuid);
  }

  public static @Nullable LinkedPlayer getFromMinecraftUUID(String minecraftUUID) {
    return links.get(minecraftUUID);
  }

  public static @Nullable Map.Entry<String, LinkedPlayer> getFromDiscordUserID(String discordUserId) {
    for (var link : links.entrySet()) {
      if (link.getValue().discordId.equals(discordUserId)) {
        return link;
      }
    }
    return null;
  }
}
