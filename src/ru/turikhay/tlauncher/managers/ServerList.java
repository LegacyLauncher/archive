package ru.turikhay.tlauncher.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.minecraft.common.CompressedStreamTools;
import net.minecraft.common.NBTTagCompound;
import net.minecraft.common.NBTTagList;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.exceptions.ParseException;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.util.Reflect;
import ru.turikhay.util.U;

public class ServerList {
   private List list = new ArrayList();
   private static final List emptyUnmodifiableAllowedAccounts = Collections.unmodifiableList(new ArrayList());

   public void add(ServerList.Server server) {
      if (server == null) {
         throw new NullPointerException();
      } else {
         this.list.add(server);
      }
   }

   public boolean remove(ServerList.Server server) {
      if (server == null) {
         throw new NullPointerException();
      } else {
         return this.list.remove(server);
      }
   }

   public boolean contains(ServerList.Server server) {
      return this.list.contains(server);
   }

   public List getList() {
      return Collections.unmodifiableList(this.list);
   }

   public void save(File file) throws IOException {
      NBTTagList servers = new NBTTagList();
      Iterator var4 = this.list.iterator();

      while(var4.hasNext()) {
         ServerList.Server compound = (ServerList.Server)var4.next();
         servers.appendTag(compound.getNBT());
      }

      NBTTagCompound compound1 = new NBTTagCompound();
      compound1.setTag("servers", servers);
      CompressedStreamTools.safeWrite(compound1, file);
   }

   public String toString() {
      return "ServerList{" + this.list + "}";
   }

   public static ServerList loadFromFile(File file) throws IOException {
      ServerList serverList = new ServerList();
      List list = serverList.list;
      NBTTagCompound compound = CompressedStreamTools.read(file);
      if (compound == null) {
         return null;
      } else {
         NBTTagList servers = compound.getTagList("servers");

         for(int i = 0; i < servers.tagCount(); ++i) {
            list.add(ServerList.Server.loadFromNBT((NBTTagCompound)servers.tagAt(i)));
         }

         return serverList;
      }
   }

   public static ServerList sortLists(ServerList pref, ServerList add) {
      ServerList serverList = new ServerList();
      List list = serverList.list;
      List prefList = pref.list;
      List addList = add.list;
      serverList.list.addAll(prefList);
      Iterator var7 = addList.iterator();

      while(var7.hasNext()) {
         ServerList.Server server = (ServerList.Server)var7.next();
         if (!list.contains(server)) {
            list.add(server);
         }
      }

      return serverList;
   }

   private static String[] splitAddress(String address) {
      String[] array = StringUtils.split(address, ':');
      switch(array.length) {
      case 1:
         return new String[]{address, null};
      case 2:
         return new String[]{array[0], array[1]};
      default:
         throw new ParseException("split incorrectly");
      }
   }

   private static String validateNotBlank(String s, String name) throws ParseException {
      if (StringUtils.isBlank(s)) {
         throw new ParseException(name + " is blank");
      } else {
         return s;
      }
   }

   private static class ServerDeserializer implements JsonDeserializer {
      private ServerDeserializer() {
      }

      public ServerList.Server deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
         ServerList.Server server = new ServerList.Server();
         JsonObject root = json.getAsJsonObject();
         server.setName(root.getAsJsonPrimitive("name").getAsString());
         server.setAddress(root.getAsJsonPrimitive("address").getAsString());
         List versions = (List)context.deserialize(root.getAsJsonArray("versions"), (new TypeToken() {
         }).getType());
         server.versions.addAll(versions);
         if (root.has("allowedAccounts")) {
            List accountTypesStr = (List)context.deserialize(root.getAsJsonArray("allowedAccounts"), (new TypeToken() {
            }).getType());
            Iterator var9 = accountTypesStr.iterator();

            while(var9.hasNext()) {
               String accountType = (String)var9.next();
               server.allowAccountType((Account.AccountType)Reflect.parseEnum0(Account.AccountType.class, accountType));
            }
         }

         return server;
      }

      // $FF: synthetic method
      ServerDeserializer(Object x0) {
         this();
      }
   }

   public static class Server {
      private String name;
      private List versions = new ArrayList();
      private String ip;
      private String port;
      private String address;
      private boolean hideAddress;
      private int acceptTextures;
      private List allowedAccounts;
      @Expose
      private List unmodifiableAllowedAccounts;
      private static Gson gson;

      public Server() {
         this.unmodifiableAllowedAccounts = ServerList.emptyUnmodifiableAllowedAccounts;
      }

      public String getName() {
         return this.name;
      }

      public void setName(String name) {
         this.name = name;
      }

      public List getVersions() {
         return Collections.unmodifiableList(this.versions);
      }

      public void addVersion(String version) {
         this.versions.add(version);
      }

      public void setIp(String ip) {
         this.ip = ip;
         this.updateAddress();
      }

      public void setPort(String port) {
         this.port = port;
         this.updateAddress();
      }

      protected void updateAddress() {
         this.address = this.ip + (this.port == null ? "" : ":" + this.port);
      }

      public String getAddress() {
         return this.address;
      }

      public void setAddress(String address) {
         if (address == null) {
            this.ip = null;
            this.port = null;
         } else {
            String[] split = ServerList.splitAddress(address);
            this.ip = split[0];
            this.port = split[1];
         }

         this.address = address;
      }

      public void allowAccountType(Account.AccountType... types) {
         Objects.requireNonNull(types);
         Account.AccountType[] var5 = types;
         int var4 = types.length;

         for(int var3 = 0; var3 < var4; ++var3) {
            Account.AccountType type = var5[var3];
            Objects.requireNonNull(type);
         }

         if (this.allowedAccounts == null) {
            this.allowedAccounts = new ArrayList(types.length);
            this.unmodifiableAllowedAccounts = Collections.unmodifiableList(this.allowedAccounts);
         }

         Collections.addAll(this.allowedAccounts, types);
      }

      public boolean isAccountTypeAllowed(Account.AccountType type) {
         return this.allowedAccounts == null || this.allowedAccounts.size() == 0 || this.allowedAccounts.contains(Objects.requireNonNull(type));
      }

      public List getAllowedAccountTypeList() {
         return this.unmodifiableAllowedAccounts;
      }

      public boolean equals(Object obj) {
         if (obj == null) {
            return false;
         } else if (!(obj instanceof ServerList.Server)) {
            return false;
         } else {
            ServerList.Server server = (ServerList.Server)obj;
            return server.getAddress().equals(this.getAddress());
         }
      }

      public String toString() {
         return "{'" + this.name + "', '" + this.address + "', " + this.versions + "}";
      }

      public NBTTagCompound getNBT() {
         NBTTagCompound compound = new NBTTagCompound();
         compound.setString("name", this.name);
         compound.setString("ip", this.address);
         compound.setBoolean("hideAddress", this.hideAddress);
         if (this.acceptTextures != 0) {
            compound.setBoolean("acceptTextures", this.acceptTextures == 1);
         }

         return compound;
      }

      public static ServerList.Server loadFromNBT(NBTTagCompound nbt) {
         ServerList.Server server = new ServerList.Server();
         server.setName(nbt.getString("name"));
         server.setAddress(nbt.getString("ip"));
         server.hideAddress = nbt.getBoolean("hideAddress");
         if (nbt.hasKey("acceptTextures")) {
            server.acceptTextures = nbt.getBoolean("acceptTextures") ? 1 : -1;
         }

         return server;
      }

      public static ServerList.Server parseFromString(String s) throws ParseException {
         if (s == null) {
            throw new NullPointerException();
         } else {
            try {
               return parseJsonString(s);
            } catch (RuntimeException var4) {
               try {
                  return parsePlainString(s);
               } catch (RuntimeException var3) {
                  U.log("Could not parse server string", s, var4, var3);
                  return null;
               }
            }
         }
      }

      private static ServerList.Server parsePlainString(String s) throws ParseException {
         String[] array = StringUtils.split(s, ';');
         if (array.length != 4) {
            throw new ParseException("split incorrectly");
         } else {
            String ip = ServerList.validateNotBlank(array[0], "ip");
            String port = ServerList.validateNotBlank(array[1], "port");
            String version = ServerList.validateNotBlank(array[2], "version");
            String name = ServerList.validateNotBlank(array[3], "name");
            ServerList.Server server = new ServerList.Server();
            server.setIp(ip);
            server.setPort(port);
            server.addVersion(version);
            server.setName(name);
            return server;
         }
      }

      private static ServerList.Server parseJsonString(String s) throws ParseException {
         if (gson == null) {
            gson = (new GsonBuilder()).registerTypeAdapter(ServerList.Server.class, new ServerList.ServerDeserializer()).create();
         }

         try {
            return (ServerList.Server)gson.fromJson(s, ServerList.Server.class);
         } catch (RuntimeException var2) {
            throw new ParseException("could not parse json server string", var2);
         }
      }
   }
}
