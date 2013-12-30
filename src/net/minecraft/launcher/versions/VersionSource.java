package net.minecraft.launcher.versions;

import com.turikhay.tlauncher.TLauncher;

public enum VersionSource {
   LOCAL((String[])null),
   REMOTE(TLauncher.getRemoteRepo()),
   EXTRA(TLauncher.getExtraRepo());

   private final String[] repos;
   private int selected;
   private boolean isSelected;

   private VersionSource() {
      this.repos = new String[0];
   }

   private VersionSource(String[] repos) {
      if (repos == null) {
         repos = new String[0];
      }

      this.repos = repos;
   }

   public int getSelected() {
      return this.selected;
   }

   public void setSelected(int pos) {
      if (!this.isSelectable()) {
         throw new IllegalStateException();
      } else {
         this.isSelected = true;
         this.selected = pos;
      }
   }

   public String getSelectedRepo() {
      return this.repos[this.selected];
   }

   public String getRepo(int pos) {
      return this.repos[pos];
   }

   public String[] getRepos() {
      return this.repos;
   }

   public int getRepoCount() {
      return this.repos.length;
   }

   public boolean isSelected() {
      return this.isSelected;
   }

   public boolean isSelectable() {
      return this.repos.length > 1;
   }
}
