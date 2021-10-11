package com.github.sanctum.permissions.api;

import java.util.List;
import java.util.function.Supplier;

public interface Permissible<T> {

	Supplier<T> getAttachment();

	boolean has(String node);

	boolean has(String node, String world);

	boolean give(String node);

	boolean give(String node, String world);

	boolean take(String node);

	boolean take(String node, String world);

	boolean inherit(Permissible<String> permissible);

	boolean inherit(Permissible<String> permissible, String world);

	List<String> getNodes();

	List<String> getNodes(String world);

}
