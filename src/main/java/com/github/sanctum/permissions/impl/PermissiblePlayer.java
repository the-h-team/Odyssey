package com.github.sanctum.permissions.impl;

import com.github.sanctum.labyrinth.annotation.Ordinal;
import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.FileType;
import com.github.sanctum.labyrinth.data.LabyrinthUser;
import com.github.sanctum.labyrinth.data.MemorySpace;
import com.github.sanctum.permissions.Groups;
import com.github.sanctum.permissions.api.GroupAPI;
import com.github.sanctum.permissions.api.GroupInformation;
import com.github.sanctum.permissions.api.Permissible;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

public final class PermissiblePlayer implements Permissible<GroupInformation> {

	private final Supplier<GroupInformation> player;
	private final World defaultWorld = Bukkit.getWorlds().get(0);
	private Base base;

	public PermissiblePlayer(OfflinePlayer player) {
		this.player = () -> new GroupInformation() {

			private final LabyrinthUser user;
			private final String primary;
			private final Map<String, Set<String>> map = new HashMap<>();

			{
				this.user = LabyrinthUser.get(player.getName());
				this.primary = FileList.search(JavaPlugin.getProvidingPlugin(Groups.class)).get("Users", "worlds/" + defaultWorld.getName(), FileType.JSON).read(c -> c.getNode(user.getId().toString()).getNode("primary").toPrimitive().getString());
			}

			@Override
			public Permissible<String> getPrimary() {
				return GroupAPI.getInstance().getPermissible(primary);
			}

			@Override
			public List<Permissible<String>> getSecondary() {
				GroupAPI API = GroupAPI.getInstance();
				return map.values().stream().reduce((stringSetEntry, stringSetEntry2) -> {
					Set<String> newSet = new HashSet<>(stringSetEntry);
					newSet.addAll(stringSetEntry2);
					return newSet;
				}).flatMap(strings -> Optional.of(strings.stream().map(API::getPermissible).collect(Collectors.toList()))).get();
			}

			@Override
			public boolean has(String permission) {
				boolean test = PermissiblePlayer.this.has(permission);
				boolean anothertest = getPrimary().has(permission);
				boolean yetanothertest = getSecondary().stream().anyMatch(permissible -> permissible.has(permission));
				return test || anothertest || yetanothertest;
			}

			@Override
			public boolean has(String permission, String world) {
				boolean test = PermissiblePlayer.this.has(permission, world);
				boolean anothertest = getPrimary().has(permission, world);
				boolean yetanothertest = getSecondary().stream().anyMatch(permissible -> permissible.has(permission, world));
				return test || anothertest || yetanothertest;
			}

			@Override
			public boolean give(String group) {
				return map.get(defaultWorld.getName()) != null ? map.get(defaultWorld.getName()).add(group) : map.put(defaultWorld.getName(), new HashSet<>()).add(group);
			}

			@Override
			public boolean give(String group, String world) {
				return map.get(world) != null ? map.get(world).add(group) : map.put(world, new HashSet<>()).add(group);
			}

			@Override
			public boolean take(String group) {
				return map.get(defaultWorld.getName()) != null ? map.get(defaultWorld.getName()).remove(group) : map.put(defaultWorld.getName(), new HashSet<>()).remove(group);
			}

			@Override
			public boolean take(String group, String world) {
				return map.get(world) != null ? map.get(world).remove(group) : map.put(world, new HashSet<>()).remove(group);
			}

			@Override
			public LabyrinthUser getUser() {
				return this.user;
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
	public Supplier<GroupInformation> getAttachment() {
		return player;
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
		if (has(node)) return false;
		MemorySpace m = GroupAPI.getInstance().getAtlas();
		return m.getNode("perms").getNode(player.get().getUser().getId().toString()).getNode(defaultWorld.getName()).toPrimitive().getStringList().add(node);
	}

	@Override
	public boolean give(String node, String world) {
		if (has(node)) return false;
		MemorySpace m = GroupAPI.getInstance().getAtlas();
		return m.getNode("perms").getNode(player.get().getUser().getId().toString()).getNode(world).toPrimitive().getStringList().add(node);
	}

	@Override
	public boolean take(String node) {
		if (!has(node)) return false;
		MemorySpace m = GroupAPI.getInstance().getAtlas();
		return m.getNode("perms").getNode(player.get().getUser().getId().toString()).getNode(defaultWorld.getName()).toPrimitive().getStringList().remove(node);
	}

	@Override
	public boolean take(String node, String world) {
		if (!has(node)) return false;
		MemorySpace m = GroupAPI.getInstance().getAtlas();
		return m.getNode("perms").getNode(player.get().getUser().getId().toString()).getNode(world).toPrimitive().getStringList().remove(node);
	}

	@Override
	public boolean inherit(Permissible<String> permissible) {
		return false;
	}

	@Override
	public boolean inherit(Permissible<String> permissible, String world) {
		return false;
	}

	@Override
	public List<String> getNodes() {
		MemorySpace m = GroupAPI.getInstance().getAtlas();
		return m.getNode("perms").getNode(player.get().getUser().getId().toString()).getNode(defaultWorld.getName()).toPrimitive().getStringList();
	}

	@Override
	public List<String> getNodes(String world) {
		MemorySpace m = GroupAPI.getInstance().getAtlas();
		return m.getNode("perms").getNode(player.get().getUser().getId().toString()).getNode(world).toPrimitive().getStringList();
	}

	public static final class Base extends PermissibleBase {

		private final PermissiblePlayer p;

		public Base(PermissiblePlayer p) {
			super(p.getAttachment().get().getUser().toBukkit());
			this.p = p;
		}

		@Override
		public boolean hasPermission(String permission) {
			GroupAPI.getInstance().getReader().read(permission);
			if (isOp()) return true;
			return p.has("*") || p.has(permission) || super.hasPermission(permission);
		}

		@Override
		public boolean hasPermission(Permission perm) {
			GroupAPI.getInstance().getReader().read(perm.getName());
			if (isOp()) return true;
			return p.has("*") || p.has(perm.getName()) || super.hasPermission(perm);
		}

	}
}
