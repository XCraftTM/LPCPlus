package me.wikmor.lpcplus;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LPCPlus extends JavaPlugin implements Listener {

	private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
	private static final Pattern SECTION_HEX_PATTERN =
			Pattern.compile("(?i)§x§([a-f0-9])§([a-f0-9])§([a-f0-9])§([a-f0-9])§([a-f0-9])§([a-f0-9])");
	private static final Pattern LEGACY_CODE_PATTERN = Pattern.compile("(?i)[&§]([0-9A-FK-OR])");
	private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();
	private static final LegacyComponentSerializer AMPERSAND_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
	private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
	private static final String MESSAGE_PLACEHOLDER = "{message}";

	private static final Map<Character, String> LEGACY_TO_MINIMESSAGE = new HashMap<>();
	static {
		LEGACY_TO_MINIMESSAGE.put('0', "black");
		LEGACY_TO_MINIMESSAGE.put('1', "dark_blue");
		LEGACY_TO_MINIMESSAGE.put('2', "dark_green");
		LEGACY_TO_MINIMESSAGE.put('3', "dark_aqua");
		LEGACY_TO_MINIMESSAGE.put('4', "dark_red");
		LEGACY_TO_MINIMESSAGE.put('5', "dark_purple");
		LEGACY_TO_MINIMESSAGE.put('6', "gold");
		LEGACY_TO_MINIMESSAGE.put('7', "gray");
		LEGACY_TO_MINIMESSAGE.put('8', "dark_gray");
		LEGACY_TO_MINIMESSAGE.put('9', "blue");
		LEGACY_TO_MINIMESSAGE.put('a', "green");
		LEGACY_TO_MINIMESSAGE.put('b', "aqua");
		LEGACY_TO_MINIMESSAGE.put('c', "red");
		LEGACY_TO_MINIMESSAGE.put('d', "light_purple");
		LEGACY_TO_MINIMESSAGE.put('e', "yellow");
		LEGACY_TO_MINIMESSAGE.put('f', "white");
		LEGACY_TO_MINIMESSAGE.put('k', "obfuscated");
		LEGACY_TO_MINIMESSAGE.put('l', "bold");
		LEGACY_TO_MINIMESSAGE.put('m', "strikethrough");
		LEGACY_TO_MINIMESSAGE.put('n', "underlined");
		LEGACY_TO_MINIMESSAGE.put('o', "italic");
		LEGACY_TO_MINIMESSAGE.put('r', "reset");
	}

	private LuckPerms luckPerms;

	@Override
	public void onEnable() {
		// Load LuckPerms
		this.luckPerms = getServer().getServicesManager().load(LuckPerms.class);

		saveDefaultConfig();
		getServer().getPluginManager().registerEvents(this, this);
		Bukkit.getOnlinePlayers().forEach(this::updateTablistName);

		getLogger().info("✅ LPCPlus enabled (Spigot/Paper compatible)");
	}

	@Override
	public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command command,
							 final @NotNull String label, final String[] args) {
		if (args.length == 1 && "reload".equalsIgnoreCase(args[0])) {
			reloadConfig();
			Bukkit.getOnlinePlayers().forEach(this::updateTablistName);
			sender.sendMessage(colorize("&aLPCPlus has been reloaded."));
			return true;
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(final @NotNull CommandSender sender, final @NotNull Command command,
									  final @NotNull String alias, final String[] args) {
		if (args.length == 1) return Collections.singletonList("reload");
		return Collections.emptyList();
	}

	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent event) {
		updateTablistName(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChat(final AsyncChatEvent event) {
		final Player player = event.getPlayer();
		final CachedMetaData metaData = luckPerms.getPlayerAdapter(Player.class).getMetaData(player);
		final String group = metaData.getPrimaryGroup();

		final String format = buildFormat(player, metaData, group);
		final String rawMessage = PlainTextComponentSerializer.plainText().serialize(event.message());
		final Component messageComponent = applyMessageColors(player, rawMessage);
		final Component finalComponent = mergeFormatAndMessage(format, messageComponent);

		event.renderer(ChatRenderer.viewerUnaware((source, sourceDisplayName, message) -> finalComponent));
	}

	private Component mergeFormatAndMessage(final String format, final Component messageComponent) {
		final int index = format.indexOf(MESSAGE_PLACEHOLDER);
		if (index < 0) {
			return MINI_MESSAGE.deserialize(format).append(messageComponent);
		}

		final Component before = MINI_MESSAGE.deserialize(format.substring(0, index));
		final Component after = MINI_MESSAGE.deserialize(format.substring(index + MESSAGE_PLACEHOLDER.length()));
		return before.append(messageComponent).append(after);
	}

	private String buildFormat(Player player, CachedMetaData metaData, String group) {
		final String format = getConfig().getString(
				getConfig().getString("group-formats." + group) != null ?
						"group-formats." + group : "chat-format");

		return formatText(player, metaData, format == null ? "{name}: {message}" : format);
	}

	private String formatText(final Player player, final CachedMetaData metaData, String format) {
		format = format
				.replace("{prefix}", Optional.ofNullable(metaData.getPrefix()).orElse(""))
				.replace("{suffix}", Optional.ofNullable(metaData.getSuffix()).orElse(""))
				.replace("{prefixes}", String.join("", metaData.getPrefixes().values()))
				.replace("{suffixes}", String.join("", metaData.getSuffixes().values()))
				.replace("{world}", player.getWorld().getName())
				.replace("{name}", player.getName())
				.replace("{displayname}", MINI_MESSAGE.serialize(player.displayName()))
				.replace("{username-color}", Optional.ofNullable(metaData.getMetaValue("username-color")).orElse(""))
				.replace("{message-color}", Optional.ofNullable(metaData.getMetaValue("message-color")).orElse(""));

		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			format = PlaceholderAPI.setPlaceholders(player, format);
		}

		// Convert legacy '&' color/format codes and '&#RRGGBB' hex codes into MiniMessage
		// tags so that the whole format string (prefixes, suffixes, static text, etc.)
		// is rendered through MiniMessage, alongside any actual MiniMessage tags already
		// present in the config, prefixes or suffixes.
		return translateLegacyToMiniMessage(format);
	}

	private String translateLegacyToMiniMessage(final String message) {
		String result = HEX_PATTERN.matcher(message).replaceAll("<#$1>");
		final Matcher sectionHexMatcher = SECTION_HEX_PATTERN.matcher(result);
		final StringBuffer sectionHexBuffer = new StringBuffer(result.length());
		while (sectionHexMatcher.find()) {
			sectionHexMatcher.appendReplacement(sectionHexBuffer, "<#"
					+ sectionHexMatcher.group(1) + sectionHexMatcher.group(2) + sectionHexMatcher.group(3)
					+ sectionHexMatcher.group(4) + sectionHexMatcher.group(5) + sectionHexMatcher.group(6) + ">");
		}
		result = sectionHexMatcher.appendTail(sectionHexBuffer).toString();

		final Matcher matcher = LEGACY_CODE_PATTERN.matcher(result);
		final StringBuilder buffer = new StringBuilder(result.length() + 16);
		while (matcher.find()) {
			final String tag = LEGACY_TO_MINIMESSAGE.get(Character.toLowerCase(matcher.group(1).charAt(0)));
			matcher.appendReplacement(buffer, tag != null ? "<" + tag + ">" : Matcher.quoteReplacement(matcher.group()));
		}
		matcher.appendTail(buffer);
		return buffer.toString();
	}

	private void updateTablistName(final Player player) {
		if (!getConfig().getBoolean("tablist.enabled")) return;
		final CachedMetaData metaData = luckPerms.getPlayerAdapter(Player.class).getMetaData(player);
		updateTablistName(player, metaData);
	}

	private void updateTablistName(final Player player, final CachedMetaData metaData) {
		if (!getConfig().getBoolean("tablist.enabled")) {
			player.setPlayerListName(null);
			return;
		}
		final String format = getConfig().getString("tablist.format", "{prefix}{username-color}{name}{suffix}");
		player.setPlayerListName(MINI_MESSAGE.deserialize(formatText(player, metaData, format)));
	}

	private Component applyMessageColors(Player player, String message) {
		if (player.hasPermission("lpcplus.minimessage")) {
			try {
				return MINI_MESSAGE.deserialize(message);
			} catch (ParsingException ignored) {
				// Invalid MiniMessage syntax, fall back to legacy/plain handling below.
			}
		}

		if (player.hasPermission("lpcplus.colorcodes") && player.hasPermission("lpcplus.rgbcodes")) {
			return LEGACY_SERIALIZER.deserialize(colorize(translateHexColorCodes(message)));
		} else if (player.hasPermission("lpcplus.colorcodes")) {
			return LEGACY_SERIALIZER.deserialize(colorize(message));
		} else if (player.hasPermission("lpcplus.rgbcodes")) {
			return LEGACY_SERIALIZER.deserialize(translateHexColorCodes(message));
		} else {
			return Component.text(message);
		}
	}

	private String colorize(final String message) {
		return LEGACY_SERIALIZER.serialize(AMPERSAND_SERIALIZER.deserialize(message));
	}

	private String translateHexColorCodes(final String message) {
		final char colorChar = LegacyComponentSerializer.SECTION_CHAR;
		final Matcher matcher = HEX_PATTERN.matcher(message);
		final StringBuilder buffer = new StringBuilder(message.length() + 4 * 8);

		while (matcher.find()) {
			final String group = matcher.group(1);
			matcher.appendReplacement(buffer, colorChar + "x"
					+ colorChar + group.charAt(0) + colorChar + group.charAt(1)
					+ colorChar + group.charAt(2) + colorChar + group.charAt(3)
					+ colorChar + group.charAt(4) + colorChar + group.charAt(5));
		}
		return matcher.appendTail(buffer).toString();
	}
}