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
import com.github.sanctum.labyrinth.data.MemorySpace;
import com.github.sanctum.labyrinth.data.ServiceType;
import com.github.sanctum.labyrinth.interfacing.OrdinalProcedure;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.permissions.api.GroupAPI;
import com.github.sanctum.permissions.api.GroupInformation;
import com.github.sanctum.permissions.api.Permissible;
import com.github.sanctum.permissions.api.PermissionReader;
import com.github.sanctum.permissions.impl.PermissibleGroup;
import com.github.sanctum.permissions.impl.PermissiblePlayer;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class Groups extends JavaPlugin implements GroupAPI {

	public static final ServiceType<GroupAPI> SERVICE = new ServiceType<>(() -> (GroupAPI) JavaPlugin.getProvidingPlugin(Groups.class));
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
	public void onLoad() {
		LabyrinthProvider.getInstance().getServiceManager().load(SERVICE);
	}

	@Override
	public void onEnable() {

		for (World w : Bukkit.getWorlds()) {
			FileManager groups = FileList.search(this).get("Groups", "worlds/" + w.getName(), FileType.JSON);
			for (String group : groups.getRoot().getKeys(false)) {
				List<String> permissions = groups.read(c -> c.getNode(group).getNode("permissions").toPrimitive().getStringList());
				PermissibleGroup g = new PermissibleGroup(group);
				permissions.forEach(s -> g.give(s, w.getName()));
				getAtlas().getNode("entities").getNode(group).set(g);
			}
		}

		LabyrinthProvider.getService(Service.TASK).getScheduler(TaskService.SYNCHRONOUS).wait(() -> {

			if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
				Bukkit.getServicesManager().register(Permission.class, new Permission() {
					@Override
					public String getName() {
						return Groups.this.getName();
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
						if (sender instanceof Player) return Groups.this.getPermissible((Player)sender).has(permission);
						return super.has(sender, permission);
					}

					@Override
					public boolean has(Player player, String permission) {
						return Groups.this.getPermissible(player).has(permission);
					}

					@Override
					@Deprecated
					public boolean playerHas(String world, String player, String permission) {
						return false;
					}

					@Override
					public boolean playerHas(String world, OfflinePlayer player, String permission) {
						return Groups.this.getPermissible(player).has(permission, world);
					}

					@Override
					@Deprecated
					public boolean playerAdd(String world, String player, String permission) {
						return false;
					}

					@Override
					public boolean playerAdd(String world, OfflinePlayer player, String permission) {
						return Groups.this.getPermissible(player).give(permission);
					}

					@Override
					@Deprecated
					public boolean playerRemove(String world, String player, String permission) {
						return false;
					}

					@Override
					public boolean playerRemove(String world, OfflinePlayer player, String permission) {
						return Groups.this.getPermissible(player).take(permission);
					}

					@Override
					public boolean playerInGroup(String world, OfflinePlayer player, String group) {
						return Groups.this.getPermissible(player).getAttachment().get().getPrimary().getAttachment().get().equals(group) || Groups.this.getPermissible(player).getAttachment().get().getSecondary().stream().anyMatch(p -> p.getAttachment().get().equals(group));
					}

					@Override
					public boolean playerAddGroup(String world, OfflinePlayer player, String group) {
						return Groups.this.getPermissible(player).getAttachment().get().give(group, world);
					}

					@Override
					public boolean playerRemoveGroup(String world, OfflinePlayer player, String group) {
						return Groups.this.getPermissible(player).getAttachment().get().take(group, world);
					}

					@Override
					public String[] getPlayerGroups(String world, OfflinePlayer player) {
						return Groups.this.getPermissible(player).getAttachment().get().getSecondary().stream().map(Permissible::getAttachment).map(Supplier::get).toArray(String[]::new);
					}

					@Override
					public String getPrimaryGroup(String world, OfflinePlayer player) {
						return Groups.this.getPermissible(player).getAttachment().get().getPrimary().getAttachment().get();
					}

					@Override
					public boolean groupHas(String world, String group, String permission) {
						return Groups.this.getPermissible(group).has(permission, world);
					}

					@Override
					public boolean groupAdd(String world, String group, String permission) {
						return Groups.this.getPermissible(group).give(permission, world);
					}

					@Override
					public boolean groupRemove(String world, String group, String permission) {
						return Groups.this.getPermissible(group).take(permission, world);
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
								.stream().map(Groups.this::getPermissible)
								.filter(Objects::nonNull).map(Permissible::getAttachment)
								.map(Supplier::get).toArray(String[]::new);
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
				List<String> perms = getAtlas().getNode("perms").getNode(id).getNode(w.getName()).toPrimitive().getStringList();
				try {
					FileManager man = FileList.search(this).get("Users", "worlds/" + w.getName(), FileType.JSON);
					UUID owner = UUID.fromString(id);
					man.write(t -> t.set(owner.toString() + ".permissions", perms));
				} catch (IllegalArgumentException e) {
					// its a group not a person.\
					FileManager man = FileList.search(this).get("Groups", "worlds/" + w.getName(), FileType.JSON);
					man.write(t -> t.set(id + ".permissions", perms));
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
	public Permissible<GroupInformation> getPermissible(OfflinePlayer p) {
		MemorySpace m = getAtlas();
		PermissiblePlayer result = (PermissiblePlayer) m.getNode("entities").getNode(p.getUniqueId().toString()).get(Permissible.class);
		if (result != null) {
			if (p.isOnline()) {
				if (OrdinalProcedure.select(result, 24).cast(() -> PermissiblePlayer.Base.class) == null) {
					Field permissibleBase;
					try {
						permissibleBase = p.getPlayer().getClass().getSuperclass().getDeclaredField("perm");
					} catch (NoSuchFieldException noSuchFieldException) {
						throw new IllegalStateException("Unable to find field! Library changes detected.", noSuchFieldException);
					}
					permissibleBase.setAccessible(true);
					try {
						PermissiblePlayer.Base b = new PermissiblePlayer.Base(result);
						result.setBase(b);
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
				PermissiblePlayer.Base b = new PermissiblePlayer.Base(e);
				e.setBase(b);
				permissibleBase.set(p.getPlayer(), b);
				p.getPlayer().updateCommands();
				return e;
			} catch (Exception illegalAccessException) {
				throw new IllegalStateException("Unable to access field! Library changes detected.", illegalAccessException);
			}
		}
		PermissiblePlayer e = new PermissiblePlayer(p.getPlayer());
		m.getNode("entities").getNode(p.getPlayer().getUniqueId().toString()).set(e);
		return e;
	}

	@Override
	public Permissible<String> getPermissible(String name) {
		return (PermissibleGroup) getAtlas().getNode("entities").getNode(name).get(Permissible.class);
	}

	@Override
	public PermissionReader getReader() {
		return READER;
	}
}
