package com.github.sanctum.permissions.api;

import java.util.Set;

public interface PermissionReader {

	void read(String node);

	boolean has(String node);

	Set<String> getHistory();

}
