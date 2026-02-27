package net.legacylauncher.mods;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class ModsManager {
    
    private static final Logger logger = LoggerFactory.getLogger(ModsManager.class);
    private static final String CURSEFORGE_API_URL = "https://api.curseforge.com/v1";
    private static final String MODRINTH_API_URL = "https://api.modrinth.com/v2";
    
    private final OkHttpClient httpClient;
    private final Gson gson;
    
    public ModsManager() {
        this.httpClient = new OkHttpClient();
        this.gson = new Gson();
    }
    
    /**
     * Search for mods on CurseForge
     * @param gameId Game ID (1 for Minecraft)
     * @param categoryId Category ID (optional)
     * @param searchFilter Search term
     * @param apiKey CurseForge API key
     * @return List of mods
     */
    public List<CurseForgeMod> searchCurseForgeMods(int gameId, Integer categoryId, String searchFilter, String apiKey) throws IOException {
        StringBuilder urlBuilder = new StringBuilder(CURSEFORGE_API_URL)
                .append("/mods/search")
                .append("?gameId=").append(gameId);
        
        if (categoryId != null) {
            urlBuilder.append("&categoryId=").append(categoryId);
        }
        
        if (searchFilter != null && !searchFilter.isEmpty()) {
            urlBuilder.append("&searchFilter=").append(searchFilter);
        }
        
        Request request = new Request.Builder()
                .url(urlBuilder.toString())
                .addHeader("Accept", "application/json")
                .addHeader("x-api-key", apiKey)
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response.code());
            }
            
            String responseBody = response.body().string();
            Type listType = new TypeToken<CurseForgeResponse<List<CurseForgeMod>>>(){}.getType();
            CurseForgeResponse<List<CurseForgeMod>> curseForgeResponse = gson.fromJson(responseBody, listType);
            
            return curseForgeResponse.data;
        }
    }
    
    /**
     * Search for mods on Modrinth
     * @param query Search term
     * @param facets Search filters in JSON array format
     * @return List of mods
     */
    public List<ModrinthMod> searchModrinthMods(String query, String facets) throws IOException {
        StringBuilder urlBuilder = new StringBuilder(MODRINTH_API_URL)
                .append("/search");
        
        boolean hasParams = false;
        if (query != null && !query.isEmpty()) {
            urlBuilder.append("?query=").append(query);
            hasParams = true;
        }
        
        if (facets != null && !facets.isEmpty()) {
            urlBuilder.append(hasParams ? "&" : "?")
                    .append("facets=").append(facets);
        }
        
        Request request = new Request.Builder()
                .url(urlBuilder.toString())
                .addHeader("Accept", "application/json")
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response.code());
            }
            
            String responseBody = response.body().string();
            Type listType = new TypeToken<ModrinthResponse<List<ModrinthMod>>>(){}.getType();
            ModrinthResponse<List<ModrinthMod>> modrinthResponse = gson.fromJson(responseBody, listType);
            
            return modrinthResponse.hits;
        }
    }
    
    // Data classes for API responses
    
    public static class CurseForgeResponse<T> {
        public T data;
        public int pagination;
    }
    
    public static class CurseForgeMod {
        public int id;
        public String name;
        public String slug;
        public String summary;
        public int downloadCount;
        public boolean isAvailable;
        public String websiteUrl;
        public String thumbnailUrl;
    }
    
    public static class ModrinthResponse {
        public List<ModrinthMod> hits;
        public int offset;
        public int limit;
        public int total_hits;
    }
    
    public static class ModrinthMod {
        public String project_id;
        public String title;
        public String description;
        public String slug;
        public String project_type;
        public String team;
        public List<String> categories;
        public String client_side;
        public String server_side;
        public int downloads;
        public String icon_url;
        public String color;
    }
}