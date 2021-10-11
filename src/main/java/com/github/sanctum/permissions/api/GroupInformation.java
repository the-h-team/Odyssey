package com.github.sanctum.permissions.api;

import com.github.sanctum.labyrinth.data.LabyrinthUser;
import java.util.List;

public interface GroupInformation {

	Permissible<String> getPrimary();

	List<Permissible<String>> getSecondary();

	boolean has(String permission);

	boolean has(String permission, String world);

	boolean give(String group);

	boolean give(String group, String world);

	boolean take(String group);

	boolean take(String group, String world);

	LabyrinthUser getUser();

}
