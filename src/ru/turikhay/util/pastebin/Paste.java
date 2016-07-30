package ru.turikhay.util.pastebin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import net.minecraft.launcher.Http;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.util.U;
import ru.turikhay.util.UrlEncoder;

public class Paste {
   private static final URL POST_URL = Http.constantURL("http://pastebin.com/api/api_post.php");
   private String title;
   private CharSequence content;
   private ExpireDate expires;
   private Visibility visibility;
   private final ArrayList listeners;
   private PasteResult result;

   public Paste() {
      this.expires = ExpireDate.ONE_WEEK;
      this.visibility = Visibility.NOT_LISTED;
      this.listeners = new ArrayList();
   }

   public final String getTitle() {
      return this.title;
   }

   public final void setTitle(String title) {
      this.title = title;
   }

   public final CharSequence getContent() {
      return this.content;
   }

   public final void setContent(CharSequence content) {
      this.content = content;
   }

   public final ExpireDate getExpireDate() {
      return this.expires;
   }

   public final Visibility getVisibility() {
      return this.visibility;
   }

   public void addListener(PasteListener listener) {
      this.listeners.add(listener);
   }

   public PasteResult paste() {
      Iterator var1 = this.listeners.iterator();

      PasteListener l;
      while(var1.hasNext()) {
         l = (PasteListener)var1.next();
         l.pasteUploading(this);
      }

      try {
         this.result = this.doPaste();
      } catch (Throwable var3) {
         U.log("Could not upload paste", var3);
         this.result = new PasteResult.PasteFailed(this, var3);
      }

      var1 = this.listeners.iterator();

      while(var1.hasNext()) {
         l = (PasteListener)var1.next();
         l.pasteDone(this);
      }

      return this.result;
   }

   private PasteResult.PasteUploaded doPaste() throws IOException {
      if (StringUtils.isEmpty(this.getContent())) {
         throw new IllegalArgumentException("content is empty");
      } else if (this.getVisibility() == null) {
         throw new NullPointerException("visibility");
      } else if (this.getExpireDate() == null) {
         throw new NullPointerException("expire date");
      } else {
         HashMap query = new HashMap();
         query.put("api_dev_key", "19a886bbdf6e11670d7f0a4e2dace1a5");
         query.put("api_option", "paste");
         query.put("api_paste_name", this.getTitle());
         query.put("api_paste_private", String.valueOf(this.getVisibility().getValue()));
         query.put("api_paste_expire_date", this.getExpireDate().getValue());
         HttpURLConnection connection = null;

         PasteResult.PasteUploaded var7;
         try {
            connection = (HttpURLConnection)POST_URL.openConnection(U.getProxy());
            connection.setConnectTimeout(U.getConnectionTimeout());
            connection.setReadTimeout(U.getReadTimeout());
            connection.setDoOutput(true);
            OutputStream output = connection.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(output);
            Iterator var5 = query.entrySet().iterator();

            while(var5.hasNext()) {
               Entry entry = (Entry)var5.next();
               writer.write(UrlEncoder.encode((String)entry.getKey()));
               writer.write("=");
               writer.write(StringUtils.isEmpty((CharSequence)entry.getValue()) ? "" : UrlEncoder.encode((String)entry.getValue()));
               writer.write("&");
            }

            writer.write("api_paste_code");
            writer.write("=");
            writer.flush();
            UrlEncoder.Encoder contentEncoder = (new UrlEncoder(this.getContent())).getEncoder();
            IOUtils.copy((InputStream)contentEncoder, (OutputStream)output);
            output.close();
            String response = IOUtils.toString(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            if (!response.startsWith("http")) {
               throw new IOException("illegal response: \"" + response + '"');
            }

            var7 = new PasteResult.PasteUploaded(this, new URL(response));
         } finally {
            if (connection != null) {
               connection.disconnect();
            }

         }

         return var7;
      }
   }
}
