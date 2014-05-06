package ru.turikhay.tlauncher.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.common.CompressedStreamTools;
import net.minecraft.common.NBTTagCompound;
import net.minecraft.common.NBTTagList;

public class ServerList {
	private List<Server> list;

	public ServerList() {
		this.list = new ArrayList<Server>();
	}

	public void add(Server server) {
		if (server == null)
			throw new NullPointerException();

		list.add(server);
	}

	public boolean remove(Server server) {
		if (server == null)
			throw new NullPointerException();

		return list.remove(server);
	}

	public boolean contains(Server server) {
		return list.contains(server);
	}

	public boolean isEmpty() {
		return list.isEmpty();
	}

	public List<Server> getList() {
		return Collections.unmodifiableList(list);
	}

	public void save(File file) throws IOException {
		NBTTagList servers = new NBTTagList();

		for (Server server : list)
			servers.appendTag(server.getNBT());

		NBTTagCompound compound = new NBTTagCompound();
		compound.setTag("servers", servers);

		CompressedStreamTools.safeWrite(compound, file);
	}

	@Override
	public String toString() {
		return "ServerList{" + list.toString() + "}";
	}

	public static ServerList loadFromFile(File file) throws IOException {
		ServerList serverList = new ServerList();
		List<Server> list = serverList.list;

		NBTTagCompound compound = CompressedStreamTools.read(file);
		if (compound == null)
			return serverList;

		NBTTagList servers = compound.getTagList("servers");

		for (int i = 0; i < servers.tagCount(); i++) {
			list.add(Server.loadFromNBT((NBTTagCompound) servers.tagAt(i)));
		}

		return serverList;
	}

	public static class Server {
		private String name;
		private String version;
		private String address;

		private boolean hideAddress;
		private int acceptTextures;

		public String getName() {
			return name;
		}

		public String getVersion() {
			return version;
		}

		public String getAddress() {
			return address;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (!(obj instanceof Server))
				return false;

			Server server = (Server) obj;

			return server.address.equals(address);
		}

		@Override
		public String toString() {
			return "{'" + name + "', '" + address + "', '" + version + "'}";
		}

		public static Server loadFromNBT(NBTTagCompound nbt) {
			Server server = new Server();

			server.name = nbt.getString("name");
			server.address = nbt.getString("ip");
			server.hideAddress = nbt.getBoolean("hideAddress");
			if (nbt.hasKey("acceptTextures"))
				server.acceptTextures = nbt.getBoolean("acceptTextures") ? 1
						: -1;

			return server;
		}

		public NBTTagCompound getNBT() {
			NBTTagCompound compound = new NBTTagCompound();

			compound.setString("name", name);
			compound.setString("ip", address);
			compound.setBoolean("hideAddress", hideAddress);
			if (acceptTextures != 0)
				compound.setBoolean("acceptTextures", acceptTextures == 1);

			return compound;
		}
	}

	public static ServerList sortLists(ServerList pref, ServerList add) {
		ServerList serverList = new ServerList();

		List<Server> list = serverList.list, prefList = pref.list, addList = add.list;
		serverList.list.addAll(prefList);

		for (Server server : addList)
			if (!list.contains(server))
				list.add(server);

		return serverList;
	}
}
