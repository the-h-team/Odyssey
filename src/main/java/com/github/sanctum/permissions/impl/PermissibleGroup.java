package com.github.sanctum.permissions.impl;

import com.github.sanctum.labyrinth.data.MemorySpace;
import com.github.sanctum.permissions.api.GroupAPI;
import com.github.sanctum.permissions.api.Permissible;
import java.util.List;
import java.util.function.Supplier;

public final class PermissibleGroup implements Permissible<String> {

	private final Supplier<String> information;

	public PermissibleGroup(String name) {
		this.information = () -> name;
	}

	@Override
	public Supplier<String> getAttachment() {
		return this.information;
	}

	@Override
	public boolean has(String perm) {
		return getNodes().contains(perm);
	}

	@Override
	public boolean has(String node, String world) {
		return false;
	}

	@Override
	public boolean give(String node) {
		if (has(node)) return false;
		MemorySpace m = GroupAPI.getInstance().getAtlas();
		return m.getNode("perms").getNode("group").getNode(getAttachment().get()).toPrimitive().getStringList().add(node);
	}

	@Override
	public boolean give(String node, String world) {
		return false;
	}

	@Override
	public boolean take(String node) {
		if (!has(node)) return false;
		MemorySpace m = GroupAPI.getInstance().getAtlas();
		return m.getNode("perms").getNode("group").getNode(getAttachment().get()).toPrimitive().getStringList().remove(node);
	}

	@Override
	public boolean take(String node, String world) {
		return false;
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
		return m.getNode("perms").getNode("group").getNode(getAttachment().get()).toPrimitive().getStringList();
	}

	@Override
	public List<String> getNodes(String world) {
		return null;
	}
}
