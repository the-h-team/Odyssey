package com.github.sanctum.permissions.api;

import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.data.Atlas;
import com.github.sanctum.labyrinth.data.Configurable;
import com.github.sanctum.permissions.Groups;
import org.bukkit.OfflinePlayer;

public interface GroupAPI extends Service {


	static GroupAPI getInstance() {
		return LabyrinthProvider.getService(Groups.SERVICE);
	}

	Atlas getAtlas();

	Configurable getOptions();

	Permissible<GroupInformation> getPermissible(OfflinePlayer player);

	Permissible<String> getPermissible(String groupName);

	PermissionReader getReader();

}
