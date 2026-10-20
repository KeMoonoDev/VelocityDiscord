package ooo.foooooooooooo.velocitydiscord;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.PostCommandInvocationEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.server.ServerRegisteredEvent;
import com.velocitypowered.api.event.proxy.server.ServerUnregisteredEvent;
import com.velocitypowered.api.proxy.Player;
import ooo.foooooooooooo.velocitydiscord.discord.Discord;
import ooo.foooooooooooo.velocitydiscord.discord.UserLinkData;
import space.arim.libertybans.api.*;
import space.arim.libertybans.api.event.PardonEvent;
import space.arim.libertybans.api.event.PunishEvent;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class AdminLogsListener {
  private final Discord discord;

  public AdminLogsListener(Discord discord) {
    this.discord = discord;
  }

  @Subscribe
  public void onCommand(PostCommandInvocationEvent event) {
    if (event.getCommandSource() instanceof Player player) {
      if (player.getCurrentServer().isEmpty()) return;
      String serverName = player.getCurrentServer().get().getServerInfo().getName();
      var serverConfig = VelocityDiscord.CONFIG.getServerConfig(serverName);

      var channel = this.discord.loadChannel(serverConfig.getDiscordConfig().logsChannelId);

      channel.sendMessage("["+serverName+"] " + player.getUsername() + " ran the following command:\n`/" + event.getCommand() + "`").queue();
    }
  }

  @Subscribe
  public void onServerRegister(ServerRegisteredEvent event) {
    String serverName = event.registeredServer().getServerInfo().getName();
    var serverConfig = VelocityDiscord.CONFIG.getServerConfig(serverName);

    var channel = this.discord.loadChannel(serverConfig.getDiscordConfig().logsChannelId);

    channel.sendMessage("Server has been registered: " + serverName).queue();
  }

  @Subscribe
  public void onServerUnregister(ServerUnregisteredEvent event) {
    String serverName = event.unregisteredServer().getServerInfo().getName();
    var serverConfig = VelocityDiscord.CONFIG.getServerConfig(serverName);

    var channel = this.discord.loadChannel(serverConfig.getDiscordConfig().logsChannelId);

    channel.sendMessage("Server has been unregistered: " + serverName).queue();
  }

  @Subscribe
  public void onConnect(ServerConnectedEvent event) {
    var serverName = event.getServer().getServerInfo().getName();

    var username = event.getPlayer().getUsername();
    var previousServer = event.getPreviousServer();
    var previousServerName = previousServer.map(s -> s.getServerInfo().getName()).orElse(null);

    var serverConfig = VelocityDiscord.CONFIG.getServerConfig(serverName);
    var channel = this.discord.loadChannel(serverConfig.getDiscordConfig().logsChannelId);

    if (previousServerName != null) {
      channel.sendMessage("["+previousServerName+"] " + username + " left the game\n["+serverName+"] " + username + " joined the game").queue();
    } else {
      channel.sendMessage("["+serverName+"] " + username + " joined the game").queue();
    }
  }

  @Subscribe
  public void onDisconnect(DisconnectEvent event) {
    var currentServer = event.getPlayer().getCurrentServer();
    var username = event.getPlayer().getUsername();

    if (currentServer.isEmpty())
      return;

    String serverName = currentServer.get().getServerInfo().getName();
    var serverConfig = VelocityDiscord.CONFIG.getServerConfig(serverName);
    var channel = this.discord.loadChannel(serverConfig.getDiscordConfig().logsChannelId);

    channel.sendMessage("["+serverName+"] " + username + " left the game").queue();
  }

  public void onServerStart(String serverName) {
    var serverConfig = VelocityDiscord.CONFIG.getServerConfig(serverName);
    var channel = this.discord.loadChannel(serverConfig.getDiscordConfig().logsChannelId);
    channel.sendMessage(serverName + " has started!").queue();
  }

  public void onServerStop(String serverName) {
    var serverConfig = VelocityDiscord.CONFIG.getServerConfig(serverName);
    var channel = this.discord.loadChannel(serverConfig.getDiscordConfig().logsChannelId);
    channel.sendMessage(serverName + " has stopped!").queue();
  }


  public void onLink(String username, String uuid, String discordId) {
    var serverConfig = VelocityDiscord.CONFIG;
    var channel = this.discord.loadChannel(serverConfig.getDiscordConfig().logsChannelId);
    channel.sendMessage(username + " (" + uuid + ") has successfully linked to <@" + discordId + ">").queue();
  }

  public void onUnlink(String unlinker, String username, String uuid, String discordId) {
    var serverConfig = VelocityDiscord.CONFIG;
    var channel = this.discord.loadChannel(serverConfig.getDiscordConfig().logsChannelId);
    channel.sendMessage(unlinker + " has unlinked " + username + " (" + uuid + ") (Previously linked to <@" + discordId + ">)").queue();
  }

  public void onPunishment(PunishEvent punishEvent, LibertyBans libertyBans) {
    if (punishEvent.isCancelled()) return;

    var channel = this.discord.loadChannel(VelocityDiscord.CONFIG.getDiscordConfig().logsChannelId);
    withOperatorName(
      punishEvent.getDraftSanction().getOperator(),
      libertyBans,
      operatorName -> {
        withVictimName(
          punishEvent.getDraftSanction().getVictim(),
          libertyBans,
          victimName -> {
            var punishmentType = punishEvent.getDraftSanction().getType();
            var duration = punishEvent.getDraftSanction().getDuration();
            var reason = punishEvent.getDraftSanction().getReason();
            channel.sendMessage(
              "**" + operatorName + " has " + libertyBans.getFormatter().formatPunishmentTypeVerb(punishmentType).toLowerCase() + " " + victimName +
                "**\nDuration: " + libertyBans.getFormatter().formatDuration(duration) +
                "\nReason: " + reason
            ).queue();
          }
        );
      }
    );
  }

  public void onPardonment(PardonEvent pardonEvent, LibertyBans libertyBans) {
    if (pardonEvent.isCancelled()) return;

    var channel = this.discord.loadChannel(VelocityDiscord.CONFIG.getDiscordConfig().logsChannelId);
    withOperatorName(
      pardonEvent.getOperator(),
      libertyBans,
      operatorName -> {
        withVictimName(
          pardonEvent.getPardonedVictim(),
          libertyBans,
          victimName -> {
            var punishmentType = pardonEvent.getPunishmentType();
            channel.sendMessage(
              "**" + operatorName + " has un-" + libertyBans.getFormatter().formatPunishmentTypeVerb(punishmentType).toLowerCase() + " " + victimName + "**"
            ).queue();
          }
        );
      }
    );
  }

  public static void withOperatorName(Operator operator, LibertyBans libertyBans, Consumer<String> callback) {
    if (operator instanceof PlayerOperator playerOperator) {
      UUID uuid = playerOperator.getUUID();
      libertyBans.getUserResolver().lookupName(uuid).thenCompose(operatorName -> {
        if (operatorName.isEmpty()) {
          callback.accept(uuid.toString());
        } else {
          callback.accept(operatorName.get() + " (" + uuid + ")");
        }
        return null;
      });
    } else {
      callback.accept("Console");
    }
  }

  public static void withVictimName(Victim victim, LibertyBans libertyBans, Consumer<String> callback) {
    if (victim instanceof PlayerVictim playerVictim) {
      UUID uuid = playerVictim.getUUID();
      libertyBans.getUserResolver().lookupName(uuid).thenCompose(victimName -> {
        if (victimName.isEmpty()) {
          callback.accept(uuid.toString());
        } else {
          callback.accept(victimName.get() + " (" + uuid + ")");
        }
        return null;
      });
    } else if (victim instanceof CompositeVictim compositeVictim) {
      UUID uuid = compositeVictim.getUUID();
      libertyBans.getUserResolver().lookupName(uuid).thenCompose(victimName -> {
        if (victimName.isEmpty()) {
          callback.accept(uuid + " + " + compositeVictim.getAddress());
        } else {
          callback.accept(victimName.get() + " (" + uuid + ") + " + compositeVictim.getAddress());
        }
        return null;
      });
    } else if (victim instanceof AddressVictim addressVictim) {
      callback.accept(addressVictim.getAddress().toString());
    } else {
      callback.accept("Unknown victim type");
    }
  }
}
