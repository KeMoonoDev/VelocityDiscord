package ooo.foooooooooooo.velocitydiscord.discord.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import ooo.foooooooooooo.velocitydiscord.PendingLinkedPlayer;
import ooo.foooooooooooo.velocitydiscord.VelocityDiscord;
import ooo.foooooooooooo.velocitydiscord.discord.Discord;
import ooo.foooooooooooo.velocitydiscord.discord.UserLinkData;

import java.util.Map;
import java.util.Objects;

public class LinkCommand implements ICommand {
  public static final String COMMAND_NAME = "link";

  private final Discord discord;

  public LinkCommand(Discord discord) {
    this.discord = discord;
  }

  @Override
  public void handle(SlashCommandInteraction interaction) {
    // Check bot's permissions
    if (VelocityDiscord.CONFIG.getDiscordConfig().setNicknames) {
      if (!Objects.requireNonNull(interaction.getMember()).getGuild().getSelfMember().hasPermission(Permission.NICKNAME_MANAGE)) {
        interaction.reply("I do not have permission to change your nickname. Please check my role permissions.").setEphemeral(true).queue();
        return;
      }
    }

    OptionMapping codeParam = interaction.getOption("code");

    if (codeParam == null) {
      interaction.reply("You must provide a code to link your account.").setEphemeral(true).queue();
      return;
    }

    String code = codeParam.getAsString();

     Map.Entry<String, PendingLinkedPlayer> pendingEntry = null;

    for (var entry : discord.getPendingLinkCodes().entrySet()) {
      if (entry.getValue().code.equals(code)) {
        pendingEntry = entry;
        break;
      }
    }

    if (pendingEntry != null) {
      String uuid = pendingEntry.getKey();
      PendingLinkedPlayer pendingLinkedPlayer = pendingEntry.getValue();
      System.out.println(interaction.getMember().getId() + " " + pendingLinkedPlayer.minecraftName);

      if (VelocityDiscord.CONFIG.getDiscordConfig().setNicknames) {
        Objects.requireNonNull(interaction.getGuild()).modifyNickname(interaction.getMember(), pendingLinkedPlayer.minecraftName).queue();
      }

      try {
        UserLinkData.addAndSave(uuid, pendingLinkedPlayer.minecraftName, interaction.getMember().getId());
      }
      catch (Exception e) {System.out.println("Error saving user link data");}

      System.out.println("Linked Discord user " + interaction.getUser().getAsTag() + " to Minecraft username: " + discord.getPendingLinkCodes().get(code));
      interaction.reply("Successfully linked your account with the code: " + code).setEphemeral(true).queue();
      discord.getPendingLinkCodes().remove(code);
    } else {
      interaction.reply("Invalid code. Please make sure you provide a valid code.").setEphemeral(true).queue();
    }
  }

  @Override
  public String description() {
    return "Link your account with a code.";
  }
}
