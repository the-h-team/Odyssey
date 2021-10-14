package com.github.sanctum.permissions.api;

import com.github.sanctum.labyrinth.annotation.Ordinal;
import com.github.sanctum.labyrinth.command.CommandRegistration;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class PermissiveCommand extends Command {

	private final CommandContext context;

	protected PermissiveCommand(CommandContext context) {
		super(context.getLabel());
		this.context = context;
		if (!context.getDescription().isEmpty()) {
			setDescription(context.getDescription());
		}
		if (!context.getPermission().isEmpty()) {
			setPermission(context.getPermission());
		}
		if (!context.getUsage().isEmpty()) {
			setUsage(context.getUsage());
		}
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
		return context.tab((Player) sender, alias, args) != null ? context.tab((Player) sender, alias, args) : super.tabComplete(sender, alias, args);
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (!(sender instanceof Player)) {
			return context.console(sender, commandLabel, args);
		}
		return context.player((Player) sender, commandLabel, args);
	}

	public static class Impl extends PermissiveCommand {

		public Impl(CommandContext context) {
			super(context);
		}

		@Ordinal(420)
		void finish() {
			CommandRegistration.use(this);
		}

	}

}
