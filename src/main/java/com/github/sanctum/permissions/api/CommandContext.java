package com.github.sanctum.permissions.api;

import com.github.sanctum.labyrinth.interfacing.EqualsOperator;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface CommandContext extends EqualsOperator {

	String getLabel();

	default String getDescription() {
		return "";
	}

	default String getUsage() {
		return "";
	}

	default String getPermission() {
		return "";
	}

	boolean player(Player p, String label, String[] args);

	boolean console(CommandSender sender, String label, String[] args);

	List<String> tab(Player p, String alias, String[] args);

}
