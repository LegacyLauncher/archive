package ru.turikhay.util.pastebin;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import net.minecraft.launcher.Http;
import org.apache.commons.lang3.StringUtils;

public class Paste {
   private static final URL POST_URL = Http.constantURL("http://pastebin.com/api/api_post.php");
   private String title;
   private String content;
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

   public final String getContent() {
      return this.content;
   }

   public final void setContent(String content) {
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
      Iterator var2 = this.listeners.iterator();

      PasteListener listener;
      while(var2.hasNext()) {
         listener = (PasteListener)var2.next();
         listener.pasteUploading(this);
      }

      try {
         this.result = this.doPaste();
      } catch (Throwable var4) {
         this.result = new PasteResult.PasteFailed(this, var4);
      }

      var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         listener = (PasteListener)var2.next();
         listener.pasteDone(this);
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
         query.put("api_paste_code", this.getContent());
         query.put("api_paste_private", this.getVisibility().getValue());
         query.put("api_paste_expire_date", this.getExpireDate().getValue());
         String answer = Http.performPost(POST_URL, query);
         if (answer.startsWith("http")) {
            return new PasteResult.PasteUploaded(this, new URL(answer));
         } else {
            throw new IOException("illegal answer: \"" + answer + '"');
         }
      }
   }
}
