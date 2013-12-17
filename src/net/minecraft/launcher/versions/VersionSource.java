package net.minecraft.launcher.versions;

public enum VersionSource {
   LOCAL(new String[1]),
   REMOTE(new String[]{"http://s3.amazonaws.com/Minecraft.Download/"}),
   EXTRA(new String[]{"http://5.9.120.11/update/versions/", "http://ru-minecraft.org/update/tlauncher/extra/", "http://dl.dropboxusercontent.com/u/6204017/update/versions/"});

   private final String[] repos;
   private int selected;
   private boolean isSelected;

   private VersionSource(String[] repos) {
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
