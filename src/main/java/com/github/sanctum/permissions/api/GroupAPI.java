package com.github.sanctum.permissions.api;

import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.data.Atlas;
import com.github.sanctum.labyrinth.data.Configurable;
import com.github.sanctum.labyrinth.data.LabyrinthUser;
import com.github.sanctum.permissions.OdysseyBukkitPlugin;
import org.bukkit.OfflinePlayer;

public interface GroupAPI extends Service {


	static GroupAPI getInstance() {
		return LabyrinthProvider.getService(OdysseyBukkitPlugin.SERVICE);
	}

	Atlas getAtlas();

	Configurable getOptions();

	Permissible<LabyrinthUser> getPermissible(OfflinePlayer player);

	Permissible<LabyrinthUser> getPermissible(LabyrinthUser user);

	Permissible<String> getPermissible(String groupName);

	PermissionReader getReader();

}
