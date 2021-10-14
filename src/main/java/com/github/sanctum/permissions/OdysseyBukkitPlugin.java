package com.github.sanctum.permissions;

import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.api.TaskService;
import com.github.sanctum.labyrinth.data.Atlas;
import com.github.sanctum.labyrinth.data.AtlasMap;
import com.github.sanctum.labyrinth.data.Configurable;
import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.FileType;
import com.github.sanctum.labyrinth.data.LabyrinthUser;
import com.github.sanctum.labyrinth.data.MemorySpace;
import com.github.sanctum.labyrinth.data.Registry;
import com.github.sanctum.labyrinth.data.ServiceType;
import com.github.sanctum.labyrinth.interfacing.OrdinalProcedure;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.permissions.api.CommandContext;
import com.github.sanctum.permissions.api.GroupAPI;
import com.github.sanctum.permissions.api.Permissible;
import com.github.sanctum.permissions.api.PermissionReader;
import com.github.sanctum.permissions.api.PermissiveCommand;
import com.github.sanctum.permissions.impl.PermissibleGroup;
import com.github.sanctum.permissions.impl.PermissiblePlayer;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class OdysseyBukkitPlugin extends JavaPlugin implements GroupAPI {

	public static ServiceType<GroupAPI> SERVICE;
	private final Atlas MAP = new AtlasMap();
	private final PermissionReader READER = new PermissionReader() {

		final Set<String> set = new HashSet<>();

		@Override
		public void read(String node) {
			set.add(node);
		}

		@Override
		public boolean has(String node) {
			return set.contains(node);
		}

		@Override
		public Set<String> getHistory() {
			return Collections.unmodifiableSet(set);
		}
	};

	@Override
	public void onEnable() {

		SERVICE = new ServiceType<>(() -> this);
		LabyrinthProvider.getInstance().getServiceManager().load(SERVICE);

		new Registry<>(CommandContext.class)
				.pick("com.github.sanctum.permissions.command")
				.source(this)
				.operate(context -> getLogger().info("- Registered command " + '"' + "/" + context.getLabel() + '"'))
				.getData().stream().map(PermissiveCommand.Impl::new).forEach(impl -> OrdinalProcedure.process(impl, 420));

		for (World w : Bukkit.getWorlds()) {
			FileManager groups = FileList.search(this).get("Groups", "worlds/" + w.getName(), FileType.JSON);
			if (groups.getRoot().exists()) {
				for (String group : groups.getRoot().getKeys(false)) {
					List<String> permissions = groups.read(c -> c.getNode(group).getNode("permissions").toPrimitive().getStringList());
					List<String> inheritance = groups.read(c -> c.getNode(group).getNode("inheritance").toPrimitive().getStringList());
					PermissibleGroup g = new PermissibleGroup(group);
					getAtlas().getNode("perms").getNode(group).getNode(w.getName()).set(new ArrayList<String>());
					permissions.forEach(s -> g.give(s, w.getName()));
					inheritance.forEach(s -> g.getInheritance().give(s, w.getName()));
					getAtlas().getNode("entities").getNode(group).set(g);
				}
			} else {
				try {
					groups.getRoot().create();
				} catch (IOException e) {
					e.printStackTrace();
				}
				FileList.copy(getResource("Groups.data"), groups.getRoot().getParent());
				groups.getRoot().reload();
				for (String group : groups.getRoot().getKeys(false)) {
					List<String> permissions = groups.read(c -> c.getNode(group).getNode("permissions").toPrimitive().getStringList());
					List<String> inheritance = groups.read(c -> c.getNode(group).getNode("inheritance").toPrimitive().getStringList());
					PermissibleGroup g = new PermissibleGroup(group);
					getAtlas().getNode("perms").getNode(group).getNode(w.getName()).set(new ArrayList<String>());
					permissions.forEach(s -> g.give(s, w.getName()));
					inheritance.forEach(s -> g.getInheritance().give(s, w.getName()));
					getAtlas().getNode("entities").getNode(group).set(g);
				}
			}
			FileManager users = FileList.search(this).get("Users", "worlds/" + w.getName(), FileType.JSON);
			if (users.getRoot().exists()) {
				for (String user : users.getRoot().getKeys(false)) {
					List<String> permissions = users.read(c -> c.getNode(user).getNode("permissions").toPrimitive().getStringList());
					List<String> inheritance = users.read(c -> c.getNode(user).getNode("inheritance").toPrimitive().getStringList());
					PermissiblePlayer player = new PermissiblePlayer(LabyrinthProvider.getOfflinePlayers().stream().filter(p -> p.getId().toString().equals(user)).findFirst().get().toBukkit());
					getAtlas().getNode("perms").getNode(user).getNode(w.getName()).set(new ArrayList<String>());
					permissions.forEach(s -> player.give(s, w.getName()));
					inheritance.forEach(s -> player.getInheritance().give(s, w.getName()));
					getAtlas().getNode("entities").getNode(user).set(player);
				}
			} else {
				try {
					users.getRoot().create();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		LabyrinthProvider.getService(Service.TASK).getScheduler(TaskService.SYNCHRONOUS).wait(() -> {

			if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
				Bukkit.getServicesManager().register(Permission.class, new Permission() {
					@Override
					public String getName() {
						return OdysseyBukkitPlugin.this.getName();
					}

					@Override
					public boolean isEnabled() {
						return true;
					}

					@Override
					public boolean hasSuperPermsCompat() {
						return true;
					}

					@Override
					public boolean has(CommandSender sender, String permission) {
						return sender instanceof Player ? OdysseyBukkitPlugin.this.getPermissible((Player) sender).getInheritance().has(permission) : super.has(sender, permission);
					}

					@Override
					public boolean has(Player player, String permission) {
						return OdysseyBukkitPlugin.this.getPermissible(player).getInheritance().has(permission);
					}

					@Override
					@Deprecated
					public boolean playerHas(String world, String player, String permission) {
						return false;
					}

					@Override
					public boolean playerHas(String world, OfflinePlayer player, String permission) {
						return OdysseyBukkitPlugin.this.getPermissible(player).has(permission, world);
					}

					@Override
					@Deprecated
					public boolean playerAdd(String world, String player, String permission) {
						return false;
					}

					@Override
					public boolean playerAdd(String world, OfflinePlayer player, String permission) {
						return OdysseyBukkitPlugin.this.getPermissible(player).give(permission);
					}

					@Override
					@Deprecated
					public boolean playerRemove(String world, String player, String permission) {
						return false;
					}

					@Override
					public boolean playerRemove(String world, OfflinePlayer player, String permission) {
						return OdysseyBukkitPlugin.this.getPermissible(player).take(permission);
					}

					@Override
					public boolean playerInGroup(String world, OfflinePlayer player, String group) {
						return OdysseyBukkitPlugin.this.getPermissible(player).getInheritance().getPrimary(world).getAttachment().equals(group) || OdysseyBukkitPlugin.this.getPermissible(player).getInheritance().getSecondary(world).stream().anyMatch(p -> p.getAttachment().equals(group));
					}

					@Override
					public boolean playerAddGroup(String world, OfflinePlayer player, String group) {
						return OdysseyBukkitPlugin.this.getPermissible(player).getInheritance().give(group, world);
					}

					@Override
					public boolean playerRemoveGroup(String world, OfflinePlayer player, String group) {
						return OdysseyBukkitPlugin.this.getPermissible(player).getInheritance().take(group, world);
					}

					@Override
					public String[] getPlayerGroups(String world, OfflinePlayer player) {
						return OdysseyBukkitPlugin.this.getPermissible(player).getInheritance().getSecondary(world).stream().map(Permissible::getAttachment).toArray(String[]::new);
					}

					@Override
					public String getPrimaryGroup(String world, OfflinePlayer player) {
						return OdysseyBukkitPlugin.this.getPermissible(player).getInheritance().getPrimary(world).getAttachment();
					}

					@Override
					public boolean groupHas(String world, String group, String permission) {
						return OdysseyBukkitPlugin.this.getPermissible(group).has(permission, world);
					}

					@Override
					public boolean groupAdd(String world, String group, String permission) {
						return OdysseyBukkitPlugin.this.getPermissible(group).give(permission, world);
					}

					@Override
					public boolean groupRemove(String world, String group, String permission) {
						return OdysseyBukkitPlugin.this.getPermissible(group).take(permission, world);
					}

					@Override
					@Deprecated
					public boolean playerInGroup(String world, String player, String group) {
						return false;
					}

					@Override
					@Deprecated
					public boolean playerAddGroup(String world, String player, String group) {
						return false;
					}

					@Override
					@Deprecated
					public boolean playerRemoveGroup(String world, String player, String group) {
						return false;
					}

					@Override
					@Deprecated
					public String[] getPlayerGroups(String world, String player) {
						return new String[0];
					}

					@Override
					@Deprecated
					public String getPrimaryGroup(String world, String player) {
						return null;
					}

					@Override
					public String[] getGroups() {
						return getAtlas().getNode("entities")
								.getKeys(false)
								.stream().map(OdysseyBukkitPlugin.this::getPermissible)
								.filter(Objects::nonNull)
								.map(Permissible::getAttachment)
								.toArray(String[]::new);
					}

					@Override
					public boolean hasGroupSupport() {
						return true;
					}
				}, this, ServicePriority.High);
			}

		}, HUID.randomID().toString(), TimeUnit.SECONDS.toMillis(3));
	}

	@Override
	public void onDisable() {
		for (String id : getAtlas().getNode("perms").getKeys(false)) {
			for (World w : Bukkit.getWorlds()) {
				try {
					UUID owner = UUID.fromString(id);
					Permissible<LabyrinthUser> permissible = (Permissible<LabyrinthUser>) getAtlas().getNode("entities").getNode(id).get(Permissible.class);
					FileManager man = FileList.search(this).get("Users", "worlds/" + w.getName(), FileType.JSON);
					man.write(t -> t.set(owner.toString() + ".permissions", permissible.getNodes(w.getName()))
							.set(owner.toString() + ".inheritance", permissible.getInheritance().getSecondary(w.getName()).stream().map(Permissible::getAttachment).collect(Collectors.toList()))
							.set(owner.toString() + ".primary", permissible.getInheritance().getPrimary(w.getName()).getAttachment()));
				} catch (IllegalArgumentException e) {
					FileManager man = FileList.search(this).get("Groups", "worlds/" + w.getName(), FileType.JSON);
					Permissible<String> permissible = (Permissible<String>) getAtlas().getNode("entities").getNode(id).get(Permissible.class);
					man.write(t -> t.set(id + ".permissions", permissible.getNodes(w.getName())).set(id + ".inheritance", permissible.getInheritance().getSecondary(w.getName()).stream().map(Permissible::getAttachment).collect(Collectors.toList())));
				}
			}
		}
	}

	@Override
	public Atlas getAtlas() {
		return this.MAP;
	}

	@Override
	public Configurable getOptions() {
		FileList list = FileList.search(this);
		return list.get("Config", "Configuration").getRoot();
	}

	@Override
	public Permissible<LabyrinthUser> getPermissible(OfflinePlayer p) {
		MemorySpace m = getAtlas();
		PermissiblePlayer result = (PermissiblePlayer) m.getNode("entities").getNode(p.getUniqueId().toString()).get(Permissible.class);
		if (result != null) {
			if (p.isOnline()) {
				if (OrdinalProcedure.select(result, 24).cast(() -> PermissiblePlayer.Base.class) == null) {
					getLogger().info("- Permission base injection [" + result.getAttachment().getName() + "]");
					Field permissibleBase;
					try {
						permissibleBase = p.getPlayer().getClass().getSuperclass().getDeclaredField("perm");
					} catch (NoSuchFieldException noSuchFieldException) {
						throw new IllegalStateException("Unable to find field! Library changes detected.", noSuchFieldException);
					}
					permissibleBase.setAccessible(true);
					try {
						PermissiblePlayer.Base b = new PermissiblePlayer.Base(result);
						permissibleBase.set(p.getPlayer(), b);
						p.getPlayer().updateCommands();
						return result;
					} catch (Exception illegalAccessException) {
						throw new IllegalStateException("Unable to access field! Library changes detected.", illegalAccessException);
					}
				}
			}
			return result;
		}
		if (p.isOnline()) {
			Field permissibleBase;
			try {
				permissibleBase = p.getPlayer().getClass().getSuperclass().getDeclaredField("perm");
			} catch (NoSuchFieldException noSuchFieldException) {
				throw new IllegalStateException("Unable to find field! Library changes detected.", noSuchFieldException);
			}
			permissibleBase.setAccessible(true);
			try {
				PermissiblePlayer e = new PermissiblePlayer(p.getPlayer());
				m.getNode("entities").getNode(p.getPlayer().getUniqueId().toString()).set(e);
				Bukkit.getWorlds().forEach(w -> m.getNode("perms").getNode(p.getPlayer().getUniqueId().toString()).getNode(w.getName()).set(new ArrayList<String>()));
				PermissiblePlayer.Base b = new PermissiblePlayer.Base(e);
				permissibleBase.set(p.getPlayer(), b);
				p.getPlayer().updateCommands();
				return e;
			} catch (Exception illegalAccessException) {
				throw new IllegalStateException("Unable to access field! Library changes detected.", illegalAccessException);
			}
		}
		PermissiblePlayer e = new PermissiblePlayer(p.getPlayer());
		m.getNode("entities").getNode(p.getPlayer().getUniqueId().toString()).set(e);
		Bukkit.getWorlds().forEach(w -> m.getNode("perms").getNode(p.getPlayer().getUniqueId().toString()).getNode(w.getName()).set(new ArrayList<String>()));
		return e;
	}

	@Override
	public Permissible<LabyrinthUser> getPermissible(LabyrinthUser user) {
		return (PermissiblePlayer) getAtlas().getNode("entities").getNode(user.getId().toString()).get(Permissible.class);
	}

	@Override
	public Permissible<String> getPermissible(String name) {
		Permissible<String> test = (PermissibleGroup) getAtlas().getNode("entities").getNode(name).get(Permissible.class);
		String adjusted = String.valueOf(name.charAt(0)).toUpperCase() + name.substring(1);
		return test != null ? test : (PermissibleGroup) getAtlas().getNode("entities").getNode(adjusted).get(Permissible.class);
	}

	@Override
	public PermissionReader getReader() {
		return READER;
	}
}
