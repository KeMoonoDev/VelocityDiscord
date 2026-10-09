package ooo.foooooooooooo.velocitydiscord.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import ooo.foooooooooooo.velocitydiscord.VelocityDiscord;
import ooo.foooooooooooo.velocitydiscord.discord.UserLinkData;

import java.util.UUID;


public class UnlinkCommand {
  public static LiteralArgumentBuilder<CommandSource> create() {
    return BrigadierCommand
      .literalArgumentBuilder("unlink")
      .then(BrigadierCommand
        .requiredArgumentBuilder("minecraft_username_or_uuid", StringArgumentType.word())
        .requires(source -> source.hasPermission("discord.unlink"))
        .executes(UnlinkCommand::execute));
  }

  private static int execute(CommandContext<CommandSource> source) {
    var discord = VelocityDiscord.getDiscord();

    if (discord == null) {
      source.getSource().sendPlainMessage("Plugin not initialized");
      return 0;
    }

    var username_or_uuid = source.getArgument("minecraft_username_or_uuid", String.class);

    String removedUUID = null;

    try {
      removedUUID = UserLinkData.removeFromUUIDAndSave(username_or_uuid);
      if (removedUUID == null) {
        removedUUID = UserLinkData.removeFromNameAndSave(username_or_uuid);
      }
    }
    catch (Exception e) {
      source.getSource().sendPlainMessage("Error executing command: " + e);
    }

    if (removedUUID == null) {
      source.getSource().sendPlainMessage("Couldn't find and unlink player " + username_or_uuid);
    } else {
      source.getSource().sendPlainMessage("Successfully unlinked player " + username_or_uuid);
      var maybePlayer = VelocityDiscord.SERVER.getPlayer(UUID.fromString(removedUUID));
      maybePlayer.ifPresent(player -> player.disconnect(MiniMessage.miniMessage().deserialize(VelocityDiscord.CONFIG.getMinecraftConfig().discordUnlinkKickMessage)));
    }

    return Command.SINGLE_SUCCESS;
  }
}
