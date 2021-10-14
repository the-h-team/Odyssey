package com.github.sanctum.permissions.impl;

import com.github.sanctum.labyrinth.annotation.Ordinal;
import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.FileType;
import com.github.sanctum.labyrinth.data.LabyrinthUser;
import com.github.sanctum.labyrinth.data.MemorySpace;
import com.github.sanctum.permissions.OdysseyBukkitPlugin;
import com.github.sanctum.permissions.api.GroupAPI;
import com.github.sanctum.permissions.api.GroupInheritance;
import com.github.sanctum.permissions.api.Permissible;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

public final class PermissiblePlayer implements Permissible<LabyrinthUser> {

	private final GroupInheritance informationSupplier;
	private final LabyrinthUser user;
	private final World defaultWorld = Bukkit.getWorlds().get(0);
	private Base base;

	public PermissiblePlayer(OfflinePlayer player) {
		this.user = LabyrinthUser.get(player.getName());
		this.informationSupplier = new GroupInheritance() {
			private final Map<String, String> primary = new HashMap<>();
			private final Map<String, Set<String>> map = new HashMap<>();

			{
				Bukkit.getWorlds().forEach(w -> {
					FileManager users = FileList.search(JavaPlugin.getProvidingPlugin(OdysseyBukkitPlugin.class)).get("Users", "worlds/" + w.getName(), FileType.JSON);
					FileManager groups = FileList.search(JavaPlugin.getProvidingPlugin(OdysseyBukkitPlugin.class)).get("Groups", "worlds/" + w.getName(), FileType.JSON);

					String test = users.read(c -> c.getNode(user.getId().toString()).getNode("primary").toPrimitive().getString());
					if (test == null || test.isEmpty() || test.equals("null")) {
						for (String group : groups.getRoot().getKeys(false)) {
							if (groups.getRoot().getNode(group).getNode("default").toPrimitive().getBoolean()) {
								primary.put(w.getName(), group);
								break;
							}
						}
					} else {
						primary.put(w.getName(), test);
					}
				});
				Bukkit.getWorlds().forEach(w -> map.put(w.getName(), new HashSet<>()));
			}

			@Override
			public Permissible<String> getPrimary(String world) {
				return GroupAPI.getInstance().getPermissible(primary.get(world));
			}

			@Override
			public List<Permissible<String>> getSecondary(String world) {
				GroupAPI API = GroupAPI.getInstance();
				return map.get(world).stream().map(API::getPermissible).collect(Collectors.toList());
			}

			@Override
			public boolean has(String permission) {
				boolean test = PermissiblePlayer.this.has(permission);
				boolean anothertest = getPrimary(defaultWorld.getName()).has(permission);
				boolean yetanothertest = getSecondary(defaultWorld.getName()).stream().anyMatch(permissible -> permissible.has(permission));
				boolean thelasttest = getSecondary(defaultWorld.getName()).stream().anyMatch(s -> s.getInheritance().getSecondary(defaultWorld.getName()).stream().anyMatch(permissible -> permissible.has(permission)));
				return test || anothertest || yetanothertest || thelasttest;
			}

			@Override
			public boolean has(String permission, String world) {
				boolean test = PermissiblePlayer.this.has(permission, world);
				boolean anothertest = getPrimary(world).has(permission, world);
				boolean yetanothertest = getSecondary(world).stream().anyMatch(permissible -> permissible.has(permission, world));
				boolean thelasttest = getSecondary(world).stream().anyMatch(s -> s.getInheritance().getSecondary(world).stream().anyMatch(permissible -> permissible.has(permission, world)));
				return test || anothertest || yetanothertest || thelasttest;
			}

			@Override
			public boolean give(String group) {
				return give(group, defaultWorld.getName());
			}

			@Override
			public boolean give(String group, String world) {
				Supplier<Boolean> supplier = () -> {
					Set<String> set = new HashSet<>();
					map.put(world, set);
					return map.get(world).add(group);
				};
				return map.get(world) != null ? map.get(world).add(group) : supplier.get();
			}

			@Override
			public boolean take(String group) {
				return take(group, defaultWorld.getName());
			}

			@Override
			public boolean take(String group, String world) {
				return map.get(world) != null && map.get(world).remove(group);
			}

		};
	}

	@Ordinal(24)
	Base getBase() {
		return base;
	}

	public PermissiblePlayer setBase(Base base) throws InstantiationException {
		if (this.base != null) throw new InstantiationException("The permission base can only be applied once!");
		this.base = base;
		return this;
	}

	@Override
	public LabyrinthUser getAttachment() {
		return user;
	}

	@Override
	public GroupInheritance getInheritance() {
		return informationSupplier;
	}

	@Override
	public boolean has(String perm) {
		return getNodes().contains(perm);
	}

	@Override
	public boolean has(String node, String world) {
		return getNodes(world).contains(node);
	}

	@Override
	public boolean give(String node) {
		return give(node, defaultWorld.getName());
	}

	@Override
	public boolean give(String node, String world) {
		if (has(node, world)) return false;
		MemorySpace m = GroupAPI.getInstance().getAtlas();
		return m.getNode("perms").getNode(getAttachment().getId().toString()).getNode(world).toPrimitive().getStringList().add(node);
	}

	@Override
	public boolean take(String node) {
		return take(node, defaultWorld.getName());
	}

	@Override
	public boolean take(String node, String world) {
		if (!has(node, world)) return false;
		MemorySpace m = GroupAPI.getInstance().getAtlas();
		return m.getNode("perms").getNode(getAttachment().getId().toString()).getNode(world).toPrimitive().getStringList().remove(node);
	}

	@Override
	public boolean inherit(Permissible<String> permissible) {
		return getInheritance().give(permissible.getAttachment());
	}

	@Override
	public boolean inherit(Permissible<String> permissible, String world) {
		return getInheritance().give(permissible.getAttachment(), world);
	}

	@Override
	public boolean deprive(Permissible<String> permissible) {
		return getInheritance().take(permissible.getAttachment());
	}

	@Override
	public boolean deprive(Permissible<String> permissible, String world) {
		return getInheritance().take(permissible.getAttachment(), world);
	}

	@Override
	public List<String> getNodes() {
		return getNodes(defaultWorld.getName());
	}

	@Override
	public List<String> getNodes(String world) {
		MemorySpace m = GroupAPI.getInstance().getAtlas();
		return m.getNode("perms").getNode(getAttachment().getId().toString()).getNode(world).toPrimitive().getStringList();
	}

	public static final class Base extends PermissibleBase {

		private final PermissiblePlayer p;

		public Base(PermissiblePlayer p) throws InstantiationException {
			super(p.getAttachment().toBukkit());
			p.setBase(this);
			this.p = p;
		}

		@Override
		public boolean hasPermission(String permission) {
			GroupAPI.getInstance().getReader().read(permission);
			if (p.getAttachment().toBukkit().isOp()) return true;
			return p.getInheritance().has("*") || p.getInheritance().has(permission) || super.hasPermission(permission);
		}

		@Override
		public boolean hasPermission(Permission perm) {
			GroupAPI.getInstance().getReader().read(perm.getName());
			if (p.getAttachment().toBukkit().isOp()) return true;
			return p.getInheritance().has("*") || p.getInheritance().has(perm.getName()) || super.hasPermission(perm);
		}

	}
}
