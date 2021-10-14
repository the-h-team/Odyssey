package com.github.sanctum.permissions.impl;

import com.github.sanctum.labyrinth.data.MemorySpace;
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
import org.bukkit.World;

public final class PermissibleGroup implements Permissible<String> {

	private final Supplier<String> information;
	private final GroupInheritance informationSupplier;
	private final World defaultWorld = Bukkit.getWorlds().get(0);

	public PermissibleGroup(String name) {
		this.information = () -> name;
		this.informationSupplier = new GroupInheritance() {

			private final Map<String, Set<String>> map = new HashMap<>();

			{
				Bukkit.getWorlds().forEach(w -> map.put(w.getName(), new HashSet<>()));
			}

			@Override
			public Permissible<String> getPrimary(String world) {
				return getSecondary(world).get(0);
			}

			@Override
			public List<Permissible<String>> getSecondary(String world) {
				GroupAPI API = GroupAPI.getInstance();
				return map.get(world).stream().map(API::getPermissible).collect(Collectors.toList());
			}

			@Override
			public boolean has(String permission) {
				boolean test = PermissibleGroup.this.has(permission);
				boolean anothertest = getPrimary(defaultWorld.getName()).has(permission);
				boolean yetanothertest = getSecondary(defaultWorld.getName()).stream().anyMatch(permissible -> permissible.has(permission));
				boolean thelasttest = getSecondary(defaultWorld.getName()).stream().anyMatch(s -> s.getInheritance().getSecondary(defaultWorld.getName()).stream().anyMatch(permissible -> permissible.has(permission)));
				return test || anothertest || yetanothertest || thelasttest;
			}

			@Override
			public boolean has(String permission, String world) {
				boolean test = PermissibleGroup.this.has(permission, world);
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

	@Override
	public String getAttachment() {
		return this.information.get();
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
		return m.getNode("perms").getNode(getAttachment()).getNode(world).toPrimitive().getStringList().add(node);
	}

	@Override
	public boolean take(String node) {
		return take(node, defaultWorld.getName());
	}

	@Override
	public boolean take(String node, String world) {
		if (!has(node, world)) return false;
		MemorySpace m = GroupAPI.getInstance().getAtlas();
		return m.getNode("perms").getNode(getAttachment()).getNode(world).toPrimitive().getStringList().remove(node);
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
		return m.getNode("perms").getNode(getAttachment()).getNode(world).toPrimitive().getStringList();
	}
}
