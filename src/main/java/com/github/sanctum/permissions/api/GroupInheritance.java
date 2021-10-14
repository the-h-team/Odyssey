package com.github.sanctum.permissions.api;

import java.util.List;

public interface GroupInheritance {

	Permissible<String> getPrimary(String world);

	List<Permissible<String>> getSecondary(String world);

	boolean has(String permission);

	boolean has(String permission, String world);

	boolean give(String group);

	boolean give(String group, String world);

	boolean take(String group);

	boolean take(String group, String world);

}
