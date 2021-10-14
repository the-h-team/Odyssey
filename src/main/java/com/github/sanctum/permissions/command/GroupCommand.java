package com.github.sanctum.permissions.command;

import com.github.sanctum.labyrinth.data.LabyrinthUser;
import com.github.sanctum.labyrinth.library.Mailer;
import com.github.sanctum.permissions.api.CommandContext;
import com.github.sanctum.permissions.api.GroupAPI;
import com.github.sanctum.permissions.api.Permissible;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GroupCommand implements CommandContext {

	@Override
	public String getLabel() {
		return "group";
	}

	@Override
	public String getDescription() {
		return "The base command for group management.";
	}

	@Override
	public String getUsage() {
		return "/user <permit, revoke, add, take, inheritance>";
	}

	@Override
	public String getPermission() {
		return "odyssey.group";
	}

	String notEnoughArgs() {
		return "&cNot enough arguments.";
	}

	static String groupNonExistent(String group) {
		return "&cGroup " + group + " doesn't exist";
	}

	@Override
	public boolean player(Player p, String label, String[] args) {

		if (!p.hasPermission(getPermission())) return true;

		Mailer mailer = Mailer.empty(p);
		if (args.length == 0) {
			mailer.chat(notEnoughArgs()).deploy();
			mailer.chat("&cUsage:&r " + getUsage()).deploy();
		}

		if (args.length == 1) {
			if (equals(args[0], "permit")) {
				mailer.chat(notEnoughArgs()).deploy();
				mailer.chat("&cUsage:&r /group permit <group> | [world/perm] nodes...").deploy();
			}
			if (equals(args[0], "revoke")) {
				mailer.chat(notEnoughArgs()).deploy();
				mailer.chat("&cUsage:&r /group revoke <group> | [world/perm] nodes...").deploy();
			}
		}

		if (args.length == 2) {
			if (equals(args[0], "permit")) {
				mailer.chat(notEnoughArgs()).deploy();
				mailer.chat("&cUsage:&r /group permit <group> | [world/perm] nodes...").deploy();
			}
			if (equals(args[0], "revoke")) {
				mailer.chat(notEnoughArgs()).deploy();
				mailer.chat("&cUsage:&r /group revoke <group> | [world/perm] nodes...").deploy();
			}
		}

		if (args.length == 3) {
			if (equals(args[0], "permit")) {
				Permissible<String> perm = GroupAPI.getInstance().getPermissible(args[1]);
				String node = args[2];
				if (perm != null) {
					if (!perm.give(node, p.getWorld().getName())) {
						mailer.chat("&cGroup " + args[1] + " already has direct access to node " + '"' + node + '"').deploy();
					} else {
						mailer.chat("&aPermission &f" + '"' + node + '"' + " &ahas been given to group &e" + args[1]).deploy();
					}
				} else {
					mailer.chat(groupNonExistent(args[1])).deploy();
				}
				return true;
			}
			if (equals(args[0], "revoke")) {
				Permissible<String> perm = GroupAPI.getInstance().getPermissible(args[1]);
				String node = args[2];
				if (perm != null) {
					if (!perm.take(node, p.getWorld().getName())) {
						mailer.chat("&cGroup " + args[1] + " doesn't have direct access to node " + '"' + node + '"').deploy();
					} else {
						mailer.chat("&aPermission &f" + '"' + node + '"' + " &ahas been taken from group &e" + args[1]).deploy();
					}
				} else {
					mailer.chat(groupNonExistent(args[1])).deploy();
				}
				return true;
			}
		}

		if (args.length == 4) {
			if (equals(args[0], "permit")) {
				String world = args[2];
				if (Bukkit.getWorld(world) == null) {
					mailer.chat("&cWorld " + '"' + world + '"' + " doesn't exist.").deploy();
					return true;
				}
				Permissible<String> perm = GroupAPI.getInstance().getPermissible(args[1]);
				String node = args[3];
				if (perm != null) {
					if (!perm.give(node, world)) {
						mailer.chat("&cGroup " + args[1]+ " already has direct access to node " + '"' + node + '"').deploy();
					} else {
						mailer.chat("&aPermission &f" + '"' + node + '"' + " &ahas been given to group &e" + args[1]).deploy();
					}
				} else {
					mailer.chat(groupNonExistent(args[1])).deploy();
				}
				return true;
			}
			if (equals(args[0], "revoke")) {
				String world = args[2];
				if (Bukkit.getWorld(world) == null) {
					mailer.chat("&cWorld " + '"' + world + '"' + " doesn't exist.").deploy();
					return true;
				}
				Permissible<String> perm = GroupAPI.getInstance().getPermissible(args[1]);
				String node = args[3];
				if (perm != null) {
					if (!perm.take(node, world)) {
						mailer.chat("&cGroup " + args[1] + " doesn't have direct access to node " + '"' + node + '"').deploy();
					} else {
						mailer.chat("&aPermission &f" + '"' + node + '"' + " &ahas been taken from group &e" + args[1]).deploy();
					}
				} else {
					mailer.chat(groupNonExistent(args[1])).deploy();
				}
				return true;
			}
		}

		if (args.length > 4) {
			if (equals(args[0], "permit")) {
				String world = args[2];
				if (Bukkit.getWorld(world) == null) {
					Permissible<String> perm = GroupAPI.getInstance().getPermissible(args[1]);
					if (perm != null) {
						for (int i = 2; i < args.length; i++) {
							if (!perm.give(args[i], p.getWorld().getName())) {
								mailer.chat("&cGroup " + args[1] + " already has direct access to node " + '"' + args[i] + '"').deploy();
							} else {
								mailer.chat("&aPermission &f" + '"' + args[i] + '"' + " &ahas been given to group &e" + args[1]).deploy();
							}
						}
					} else {
						mailer.chat(groupNonExistent(args[1])).deploy();
					}
				} else {
					Permissible<String> perm = GroupAPI.getInstance().getPermissible(args[1]);
					if (perm != null) {
						for (int i = 3; i < args.length; i++) {
							if (!perm.give(args[i], world)) {
								mailer.chat("&cGroup " + args[1] + " already has direct access to node " + '"' + args[i] + '"').deploy();
							} else {
								mailer.chat("&aPermission &f" + '"' + args[i] + '"' + " &ahas been given to group &e" + args[1]).deploy();
							}
						}
					} else {
						mailer.chat(groupNonExistent(args[1])).deploy();
					}
				}

				return true;
			}
			if (equals(args[0], "revoke")) {
				String world = args[2];
				if (Bukkit.getWorld(world) == null) {
					Permissible<String> perm = GroupAPI.getInstance().getPermissible(args[1]);
					if (perm != null) {
						for (int i = 2; i < args.length; i++) {
							if (!perm.take(args[i], p.getWorld().getName())) {
								mailer.chat("&cGroup " + args[1] + " doesn't have direct access to node " + '"' + args[i] + '"').deploy();
							} else {
								mailer.chat("&aPermission &f" + '"' + args[i] + '"' + " &ahas been taken from group &e" + args[1]).deploy();
							}
						}
					} else {
						mailer.chat(groupNonExistent(args[1])).deploy();
					}
				} else {
					Permissible<String> perm = GroupAPI.getInstance().getPermissible(args[1]);
					if (perm != null) {
						for (int i = 2; i < args.length; i++) {
							if (!perm.take(args[i], world)) {
								mailer.chat("&cGroup " + args[1] + " doesn't have direct access to node " + '"' + args[i] + '"').deploy();
							} else {
								mailer.chat("&aPermission &f" + '"' + args[i] + '"' + " &ahas been taken from group &e" + args[1]).deploy();
							}
						}
					} else {
						mailer.chat(groupNonExistent(args[1])).deploy();
					}
				}
				return true;
			}
		}

		return true;
	}

	@Override
	public boolean console(CommandSender sender, String label, String[] args) {
		return false;
	}

	@Override
	public List<String> tab(Player p, String alias, String[] args) {
		return null;
	}
}
