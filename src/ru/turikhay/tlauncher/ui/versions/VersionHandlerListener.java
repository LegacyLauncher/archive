package ru.turikhay.tlauncher.ui.versions;

import java.util.List;
import ru.turikhay.tlauncher.managers.VersionManager;

public interface VersionHandlerListener {
   void onVersionRefreshing(VersionManager var1);

   void onVersionRefreshed(VersionManager var1);

   void onVersionSelected(List var1);

   void onVersionDeselected();

   void onVersionDownload(List var1);
}
