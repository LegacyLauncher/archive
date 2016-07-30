package ru.turikhay.tlauncher.ui.support;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CharSequenceInputStream;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.explorer.FileExplorer;
import ru.turikhay.tlauncher.ui.frames.ProcessFrame;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.pastebin.Paste;
import ru.turikhay.util.pastebin.PasteResult;

public class SendInfoFrame extends ProcessFrame {
   private SupportFrame helpFrame;

   public SendInfoFrame() {
      this.setTitlePath("support.sending.title", new Object[0]);
      this.getHead().setText("support.sending.head");
      this.setIcon("communication.png");
      this.pack();
   }

   public final void setFrame(SupportFrame frame) {
      this.helpFrame = (SupportFrame)U.requireNotNull(frame);
      this.submit(new ProcessFrame.Process() {
         protected SendInfoFrame.SendInfoResponse get() throws Exception {
            Paste paste = new Paste();
            paste.setTitle("Diagnostic Log");
            paste.setContent(TLauncher.getLogger().getOutput());
            PasteResult result = paste.paste();
            if (result instanceof PasteResult.PasteUploaded) {
               return SendInfoFrame.this.new SendInfoResponse(((PasteResult.PasteUploaded)result).getURL().toString());
            } else if (result instanceof PasteResult.PasteFailed) {
               throw new IOException(((PasteResult.PasteFailed)result).getError());
            } else {
               throw new InternalError("unknown result type");
            }
         }
      });
   }

   protected void onSucceeded(ProcessFrame.Process process, SendInfoFrame.SendInfoResponse result) {
      super.onSucceeded(process, result);
      this.helpFrame.setResponse(result);
   }

   protected void onFailed(ProcessFrame.Process process, Exception e) {
      super.onFailed(process, e);
      U.log("Error sending diagnostic data", e);
      if (Alert.showLocQuestion("support.sending.error")) {
         Exception error;
         label71: {
            FileExplorer explorer;
            try {
               explorer = FileExplorer.newExplorer();
            } catch (Exception var14) {
               error = var14;
               break label71;
            }

            explorer.setSelectedFile(new File("diagnostic.log"));
            explorer.showSaveDialog(this);
            if (explorer.getSelectedFile() != null) {
               File file = explorer.getSelectedFile();
               FileOutputStream out = null;

               try {
                  IOUtils.copy((InputStream)(new CharSequenceInputStream(TLauncher.getLogger().getOutput(), "UTF-8")), (OutputStream)(out = new FileOutputStream(file)));
               } catch (Exception var12) {
                  error = var12;
                  break label71;
               } finally {
                  U.close(out);
               }

               Alert.showLocMessage("support.saving.success", file);
            }

            return;
         }

         Alert.showLocError("support.saving.error", error);
      }

   }

   public final class SendInfoResponse {
      private final String pastebinLink;

      SendInfoResponse(String pastebinLink) {
         this.pastebinLink = StringUtil.requireNotBlank(pastebinLink);
      }

      public final String getPastebinLink() {
         return this.pastebinLink;
      }
   }
}
