package ru.turikhay.tlauncher.minecraft;

import net.minecraft.common.CompressedStreamTools;
import net.minecraft.common.NBTTagCompound;
import net.minecraft.common.NBTTagList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public class NBTServer extends Server {
    private static final Logger LOGGER = LogManager.getLogger(NBTServer.class);

    private boolean hideAddress;
    private int acceptTextures;

    public NBTServer(Server server) {
        setName(server.getName());
        setFullAddress(server.getFullAddress());

        if (server instanceof NBTServer) {
            NBTServer nbtServer = (NBTServer) server;
            hideAddress = nbtServer.hideAddress;
            acceptTextures = nbtServer.acceptTextures;
        }
    }

    private NBTServer() {
    }

    public NBTTagCompound getNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("name", getName());
        compound.setString("ip", getFullAddress());
        compound.setBoolean("hideAddress", hideAddress);
        if (acceptTextures != 0) {
            compound.setBoolean("acceptTextures", acceptTextures == 1);
        }

        return compound;
    }

    private static NBTServer getNBTServer(NBTTagCompound nbt) {
        NBTServer server = new NBTServer();
        server.setName(nbt.getString("name"));
        server.setFullAddress(nbt.getString("ip"));
        server.hideAddress = nbt.getBoolean("hideAddress");

        if (nbt.hasKey("acceptTextures")) {
            server.acceptTextures = nbt.getBoolean("acceptTextures") ? 1 : -1;
        }

        return server;
    }

    public static LinkedHashSet<NBTServer> loadSet(File file) throws IOException {
        LinkedHashSet<NBTServer> set = new LinkedHashSet<>();
        NBTTagCompound compound = CompressedStreamTools.read(file);

        if (compound == null) {
            return set;
        }

        NBTTagList servers = compound.getTagList("servers");
        for (int i = 0; i < servers.tagCount(); ++i) {
            NBTServer nbtServer;

            try {
                nbtServer = getNBTServer((NBTTagCompound) servers.tagAt(i));
            } catch (RuntimeException rE) {
                LOGGER.warn("Could not parse server from NBT file {}", file, rE);
                continue;
            }

            set.add(nbtServer);
        }

        return set;
    }

    public static void saveSet(Set<NBTServer> set, File file) throws IOException {
        NBTTagList servers = new NBTTagList();

        for (NBTServer server : set) {
            servers.appendTag(server.getNBT());
        }

        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag("servers", servers);

        CompressedStreamTools.safeWrite(compound, file);
    }
}
