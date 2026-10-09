package ooo.foooooooooooo.velocitydiscord;

import java.io.Serializable;

public class LinkedPlayer implements Serializable {
  public String minecraftName;
  public String discordId;

  public LinkedPlayer(String minecraftName, String discordId) {
    this.minecraftName = minecraftName;
    this.discordId = discordId;
  }
}
