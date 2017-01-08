package ru.turikhay.tlauncher.updater;

import com.google.gson.*;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.LangConfiguration;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.util.IntegerArray;
import ru.turikhay.util.Reflect;
import ru.turikhay.util.U;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class Notices {
    private final Map<String, Notices.NoticeList> map = new HashMap();
    private final Map<String, Notices.NoticeList> unmodifiable;

    public Notices() {
        unmodifiable = Collections.unmodifiableMap(map);
    }

    public final Map<String, Notices.NoticeList> getMap() {
        return unmodifiable;
    }

    protected final Map<String, Notices.NoticeList> map() {
        return map;
    }

    public final Notices.NoticeList getByName(String name) {
        return map.get(name);
    }

    protected void add(Notices.NoticeList list) {
        if (list == null) {
            throw new NullPointerException("list");
        } else {
            map.put(list.name, list);
        }
    }

    protected void add(String listName, Notices.Notice notice) {
        if (notice == null) {
            throw new NullPointerException("notice");
        } else {
            Notices.NoticeList list = map.get(listName);
            boolean add = list == null;
            if (add) {
                list = new Notices.NoticeList(listName);
            }

            list.add(notice);
            if (add) {
                add(list);
            }

        }
    }

    public String toString() {
        return getClass().getSimpleName() + map;
    }

    private static String parseImage(String image) {
        if (image == null) {
            return null;
        } else if (image.startsWith("data:image")) {
            return image;
        } else {
            URL url = Images.getRes(image, false);
            return url == null ? null : url.toString();
        }
    }

    public static class Deserializer implements JsonDeserializer<Notices> {
        public Notices deserialize(JsonElement root, Type type, JsonDeserializationContext context) throws JsonParseException {
            try {
                return deserialize0(root);
            } catch (Exception var5) {
                U.log("Cannot parse notices:", var5);
                return new Notices();
            }
        }

        private Notices deserialize0(JsonElement root) throws JsonParseException {
            Notices notices = new Notices();
            JsonObject rootObject = root.getAsJsonObject();
            Iterator var5 = rootObject.entrySet().iterator();

            label55:
            while (var5.hasNext()) {
                Entry notice = (Entry) var5.next();
                String listName = (String) notice.getKey();
                JsonArray ntArray = ((JsonElement) notice.getValue()).getAsJsonArray();
                Iterator var9 = ntArray.iterator();

                while (true) {
                    JsonObject ntObj;
                    Pattern pattern;
                    do {
                        if (!var9.hasNext()) {
                            continue label55;
                        }

                        JsonElement elem = (JsonElement) var9.next();
                        ntObj = elem.getAsJsonObject();
                        if (!ntObj.has("version")) {
                            break;
                        }

                        String notice1 = ntObj.get("version").getAsString();
                        pattern = Pattern.compile(notice1);
                    } while (!pattern.matcher(String.valueOf(TLauncher.getVersion())).matches());

                    Notices.Notice notice3 = new Notices.Notice();
                    notice3.setContent(ntObj.get("content").getAsString());
                    notice3.setSize(IntegerArray.parseIntegerArray(ntObj.get("size").getAsString(), 'x').toArray());
                    if (ntObj.has("id")) {
                        notice3.setId(ntObj.get("id").getAsInt());
                    }

                    if (ntObj.has("chance")) {
                        notice3.setChance(ntObj.get("chance").getAsInt());
                    }

                    if (ntObj.has("type")) {
                        notice3.setType(Reflect.parseEnum(NoticeType.class, ntObj.get("type").getAsString()));
                    }

                    if (ntObj.has("image")) {
                        notice3.setImage(ntObj.get("image").getAsString());
                    }

                    notices.add(listName, notice3);
                }
            }

            if (!TLauncher.getBrand().equals("Legacy") && notices.getByName(Locale.US.toString()) != null) {
                List<Notice> universalList = notices.getByName(Locale.US.toString()).getList();
                for (Locale locale : LangConfiguration.getAvailableLocales()) {
                    if (locale.equals(Locale.US)) {
                        continue;
                    }
                    if (notices.getByName(locale.toString()) == null) {
                        for (Notice notice : universalList) {
                            notices.add(locale.toString(), notice);
                        }
                    }
                }
            } else {
                if (notices.getByName("uk_UA") == null && notices.getByName("ru_RU") != null) {
                    var5 = notices.getByName("ru_RU").getList().iterator();

                    while (var5.hasNext()) {
                        Notices.Notice notice2 = (Notices.Notice) var5.next();
                        notices.add("uk_UA", notice2);
                    }
                }
            }

            return notices;
        }
    }

    public static class Notice {
        private String content;
        private int id;
        private int chance = 100;
        private Notices.NoticeType type;
        private int[] size;
        private String image;

        public Notice() {
            type = Notices.NoticeType.NOTICE;
            size = new int[2];
        }

        public final int getId() {
            return id;
        }

        public final void setId(int id) {
            this.id = id;
        }

        public final int getChance() {
            return chance;
        }

        public final void setChance(int chance) {
            if (chance >= 1 && chance <= 100) {
                this.chance = chance;
            } else {
                throw new IllegalArgumentException("illegal chance: " + chance);
            }
        }

        public final String getContent() {
            return content;
        }

        public final void setContent(String content) {
            if (StringUtils.isBlank(content)) {
                throw new IllegalArgumentException("content is empty or is null");
            } else {
                this.content = content;
            }
        }

        public final Notices.NoticeType getType() {
            return type;
        }

        public final void setType(Notices.NoticeType type) {
            this.type = type;
        }

        public final int[] getSize() {
            return size.clone();
        }

        public final void setSize(int[] size) {
            if (size == null) {
                throw new NullPointerException();
            } else if (size.length != 2) {
                throw new IllegalArgumentException("illegal length");
            } else {
                setWidth(size[0]);
                setHeight(size[1]);
            }
        }

        public final int getWidth() {
            return size[0];
        }

        public final void setWidth(int width) {
            if (width < 1) {
                throw new IllegalArgumentException("width must be greater than 0");
            } else {
                size[0] = width;
            }
        }

        public final int getHeight() {
            return size[1];
        }

        public final void setHeight(int height) {
            if (height < 1) {
                throw new IllegalArgumentException("height must be greater than 0");
            } else {
                size[1] = height;
            }
        }

        public final String getImage() {
            return image;
        }

        public final void setImage(String image) {
            this.image = StringUtils.isBlank(image) ? null : Notices.parseImage(image);
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(getClass().getSimpleName()).append("{").append("size=").append(size[0]).append('x').append(size[1]).append(';').append("chance=").append(chance).append(';').append("content=\"");
            if (content.length() < 50) {
                builder.append(content);
            } else {
                builder.append(content.substring(0, 46)).append("...");
            }

            builder.append("\";").append("image=");
            if (image != null && image.length() > 24) {
                builder.append(image.substring(0, 22)).append("...");
            } else {
                builder.append(image);
            }

            builder.append('}');
            return builder.toString();
        }
    }

    public static class NoticeList {
        private final String name;
        private final List<Notices.Notice> list = new ArrayList();
        private final List<Notices.Notice> unmodifiable;
        private final Notices.Notice[] chances;
        private int totalChance;

        public NoticeList(String name) {
            unmodifiable = Collections.unmodifiableList(list);
            chances = new Notices.Notice[100];
            totalChance = 0;
            if (name == null) {
                throw new NullPointerException("name");
            } else if (name.isEmpty()) {
                throw new IllegalArgumentException("name is empty");
            } else {
                this.name = name;
            }
        }

        public final String getName() {
            return name;
        }

        public final List<Notices.Notice> getList() {
            return unmodifiable;
        }

        protected final List<Notices.Notice> list() {
            return list;
        }

        public final Notices.Notice getRandom() {
            return chances[(new Random()).nextInt(100)];
        }

        protected void add(Notices.Notice notice) {
            if (notice == null) {
                throw new NullPointerException();
            } else if (totalChance + notice.chance > 100) {
                throw new IllegalArgumentException("chance overflow: " + (totalChance + notice.chance));
            } else {
                list.add(notice);
                Arrays.fill(chances, totalChance, totalChance + notice.chance, notice);
                totalChance += notice.chance;
            }
        }

        public String toString() {
            return getClass().getSimpleName() + list();
        }
    }

    public enum NoticeType {
        NOTICE(false),
        WARNING(false),
        AD_SERVER,
        AD_YOUTUBE,
        AD_OTHER;

        private final boolean advert;

        NoticeType(boolean advert) {
            this.advert = advert;
        }

        NoticeType() {
            this(true);
        }

        public boolean isAdvert() {
            return advert;
        }
    }
}
