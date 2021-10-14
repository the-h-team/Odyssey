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

public class UserCommand implements CommandContext {

	@Override
	public String getLabel() {
		return "user";
	}

	@Override
	public String getDescription() {
		return "The base command for user permission/group control.";
	}

	@Override
	public String getUsage() {
		return "/user <permit, revoke, add, take, inheritance>";
	}

	@Override
	public String getPermission() {
		return "odyssey.user";
	}

	String notEnoughArgs() {
		return "&cNot enough arguments.";
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
				mailer.chat("&cUsage:&r /user permit <user> | [world/perm] nodes...").deploy();
			}
			if (equals(args[0], "revoke")) {
				mailer.chat(notEnoughArgs()).deploy();
				mailer.chat("&cUsage:&r /user revoke <user> | [world/perm] nodes...").deploy();
			}
			if (equals(args[0], "add")) {
				mailer.chat(notEnoughArgs()).deploy();
				mailer.chat("&cUsage:&r /user add <user> <group> | [world]").deploy();
			}
			if (equals(args[0], "take")) {
				mailer.chat(notEnoughArgs()).deploy();
				mailer.chat("&cUsage:&r /user take <user> <group> | [world]").deploy();
			}
		}

		if (args.length == 2) {
			if (equals(args[0], "permit")) {
				mailer.chat(notEnoughArgs()).deploy();
				mailer.chat("&cUsage:&r /user permit <user> | [world/perm] nodes...").deploy();
			}
			if (equals(args[0], "revoke")) {
				mailer.chat(notEnoughArgs()).deploy();
				mailer.chat("&cUsage:&r /user revoke <user> | [world/perm] nodes...").deploy();
			}
			if (equals(args[0], "add")) {
				mailer.chat(notEnoughArgs()).deploy();
				mailer.chat("&cUsage:&r /user add <user> <group> | [world]").deploy();
			}
			if (equals(args[0], "take")) {
				mailer.chat(notEnoughArgs()).deploy();
				mailer.chat("&cUsage:&r /user take <user> <group> | [world]").deploy();
			}
		}

		if (args.length == 3) {
			if (equals(args[0], "permit")) {
				LabyrinthUser user = LabyrinthUser.get(args[1]);
				if (user != null && user.isValid()) {
					Permissible<LabyrinthUser> perm = GroupAPI.getInstance().getPermissible(user);
					String node = args[2];
					if (!perm.give(node, p.getWorld().getName())) {
						mailer.chat("&cUser " + user.getName() + " already has direct access to node " + '"' + node + '"').deploy();
					} else {
						mailer.chat("&aPermission &f" + '"' + node + '"' + " &ahas been given to user &e" + user.getName()).deploy();
					}
				}
				return true;
			}
			if (equals(args[0], "revoke")) {
				LabyrinthUser user = LabyrinthUser.get(args[1]);
				if (user != null && user.isValid()) {
					Permissible<LabyrinthUser> perm = GroupAPI.getInstance().getPermissible(user);
					String node = args[2];
					if (!perm.take(node, p.getWorld().getName())) {
						mailer.chat("&cUser " + user.getName() + " doesn't have direct access to node " + '"' + node + '"').deploy();
					} else {
						mailer.chat("&aPermission &f" + '"' + node + '"' + " &ahas been taken from user &e" + user.getName()).deploy();
					}
				}
				return true;
			}
			if (equals(args[0], "add")) {
				LabyrinthUser user = LabyrinthUser.get(args[1]);
				if (user != null && user.isValid()) {
					Permissible<LabyrinthUser> perm = GroupAPI.getInstance().getPermissible(user);
					Permissible<String> group = GroupAPI.getInstance().getPermissible(args[2]);
					if (group == null) {
						mailer.chat(GroupCommand.groupNonExistent(args[2])).deploy();
						return true;
					}
					if (!perm.getInheritance().give(group.getAttachment(), p.getWorld().getName())) {
						mailer.chat("&cUser " + user.getName() + " already inherits from group " + '"' + group.getAttachment() + '"').deploy();
					} else {
						mailer.chat("&aGroup &f" + '"' + group.getAttachment() + '"' + " &ahas been given to user &e" + user.getName()).deploy();
					}
				}
				return true;
			}
			if (equals(args[0], "take")) {
				LabyrinthUser user = LabyrinthUser.get(args[1]);
				if (user != null && user.isValid()) {
					Permissible<LabyrinthUser> perm = GroupAPI.getInstance().getPermissible(user);
					Permissible<String> group = GroupAPI.getInstance().getPermissible(args[2]);
					if (group == null) {
						mailer.chat(GroupCommand.groupNonExistent(args[2])).deploy();
						return true;
					}
					if (!perm.getInheritance().take(group.getAttachment(), p.getWorld().getName())) {
						mailer.chat("&cUser " + user.getName() + " doesn't inherit from group " + '"' + group.getAttachment() + '"').deploy();
					} else {
						mailer.chat("&aGroup &f" + '"' + group.getAttachment() + '"' + " &ahas been revoked from user &e" + user.getName()).deploy();
					}
				}
				return true;
			}
		}

		if (args.length == 4) {
			if (equals(args[0], "permit")) {
				LabyrinthUser user = LabyrinthUser.get(args[1]);
				String world = args[2];
				if (Bukkit.getWorld(world) == null) {
					mailer.chat("&cWorld " + '"' + world + '"' + " doesn't exist.").deploy();
					return true;
				}
				if (user != null && user.isValid()) {
					Permissible<LabyrinthUser> perm = GroupAPI.getInstance().getPermissible(user);
					String node = args[3];
					if (!perm.give(node, world)) {
						mailer.chat("&cUser " + user.getName() + " already has direct access to node " + '"' + node + '"').deploy();
					} else {
						mailer.chat("&aPermission &f" + '"' + node + '"' + " &ahas been given to user &e" + user.getName()).deploy();
					}
				}
				return true;
			}
			if (equals(args[0], "revoke")) {
				LabyrinthUser user = LabyrinthUser.get(args[1]);
				String world = args[2];
				if (Bukkit.getWorld(world) == null) {
					mailer.chat("&cWorld " + '"' + world + '"' + " doesn't exist.").deploy();
					return true;
				}
				if (user != null && user.isValid()) {
					Permissible<LabyrinthUser> perm = GroupAPI.getInstance().getPermissible(user);
					String node = args[3];
					if (!perm.take(node, world)) {
						mailer.chat("&cUser " + user.getName() + " doesn't have direct access to node " + '"' + node + '"').deploy();
					} else {
						mailer.chat("&aPermission &f" + '"' + node + '"' + " &ahas been taken from user &e" + user.getName()).deploy();
					}
				}
				return true;
			}
			if (equals(args[0], "add")) {
				LabyrinthUser user = LabyrinthUser.get(args[1]);
				String world = args[3];
				if (Bukkit.getWorld(world) == null) {
					mailer.chat("&cWorld " + '"' + world + '"' + " doesn't exist.").deploy();
					return true;
				}
				if (user != null && user.isValid()) {
					Permissible<LabyrinthUser> perm = GroupAPI.getInstance().getPermissible(user);
					Permissible<String> group = GroupAPI.getInstance().getPermissible(args[2]);
					if (group == null) {
						mailer.chat(GroupCommand.groupNonExistent(args[2])).deploy();
						return true;
					}
					if (!perm.getInheritance().give(group.getAttachment(), world)) {
						mailer.chat("&cUser " + user.getName() + " already inherits from group " + '"' + group.getAttachment() + '"').deploy();
					} else {
						mailer.chat("&aGroup &f" + '"' + group.getAttachment() + '"' + " &ahas been given to user &e" + user.getName()).deploy();
					}
				}
				return true;
			}
			if (equals(args[0], "take")) {
				LabyrinthUser user = LabyrinthUser.get(args[1]);
				String world = args[3];
				if (Bukkit.getWorld(world) == null) {
					mailer.chat("&cWorld " + '"' + world + '"' + " doesn't exist.").deploy();
					return true;
				}
				if (user != null && user.isValid()) {
					Permissible<LabyrinthUser> perm = GroupAPI.getInstance().getPermissible(user);
					Permissible<String> group = GroupAPI.getInstance().getPermissible(args[2]);
					if (group == null) {
						mailer.chat(GroupCommand.groupNonExistent(args[2])).deploy();
						return true;
					}
					if (!perm.getInheritance().take(group.getAttachment(), world)) {
						mailer.chat("&cUser " + user.getName() + " doesn't inherit from group " + '"' + group.getAttachment() + '"').deploy();
					} else {
						mailer.chat("&aGroup &f" + '"' + group.getAttachment() + '"' + " &ahas been revoked from user &e" + user.getName()).deploy();
					}
				}
				return true;
			}
		}

		if (args.length > 4) {
			if (equals(args[0], "permit")) {
				LabyrinthUser user = LabyrinthUser.get(args[1]);
				String world = args[2];
				if (Bukkit.getWorld(world) == null) {
					if (user != null && user.isValid()) {
						Permissible<LabyrinthUser> perm = GroupAPI.getInstance().getPermissible(user);
						for (int i = 2; i < args.length; i++) {
							if (!perm.give(args[i], p.getWorld().getName())) {
								mailer.chat("&cUser " + user.getName() + " already has direct access to node " + '"' + args[i] + '"').deploy();
							} else {
								mailer.chat("&aPermission &f" + '"' + args[i] + '"' + " &ahas been given to user &e" + user.getName()).deploy();
							}
						}
					}
				} else {
					if (user != null && user.isValid()) {
						Permissible<LabyrinthUser> perm = GroupAPI.getInstance().getPermissible(user);
						for (int i = 3; i < args.length; i++) {
							if (!perm.give(args[i], world)) {
								mailer.chat("&cUser " + user.getName() + " already has direct access to node " + '"' + args[i] + '"').deploy();
							} else {
								mailer.chat("&aPermission &f" + '"' + args[i] + '"' + " &ahas been given to user &e" + user.getName()).deploy();
							}
						}
					}
				}

				return true;
			}
			if (equals(args[0], "revoke")) {
				LabyrinthUser user = LabyrinthUser.get(args[1]);
				String world = args[2];
				if (Bukkit.getWorld(world) == null) {
					if (user != null && user.isValid()) {
						Permissible<LabyrinthUser> perm = GroupAPI.getInstance().getPermissible(user);
						for (int i = 2; i < args.length; i++) {
							if (!perm.take(args[i], p.getWorld().getName())) {
								mailer.chat("&cUser " + user.getName() + " doesn't have direct access to node " + '"' + args[i] + '"').deploy();
							} else {
								mailer.chat("&aPermission &f" + '"' + args[i] + '"' + " &ahas been taken from user &e" + user.getName()).deploy();
							}
						}
					}
				} else {
					if (user != null && user.isValid()) {
						Permissible<LabyrinthUser> perm = GroupAPI.getInstance().getPermissible(user);
						for (int i = 2; i < args.length; i++) {
							if (!perm.take(args[i], world)) {
								mailer.chat("&cUser " + user.getName() + " doesn't have direct access to node " + '"' + args[i] + '"').deploy();
							} else {
								mailer.chat("&aPermission &f" + '"' + args[i] + '"' + " &ahas been taken from user &e" + user.getName()).deploy();
							}
						}
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
