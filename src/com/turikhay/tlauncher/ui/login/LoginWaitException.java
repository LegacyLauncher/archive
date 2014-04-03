package com.turikhay.tlauncher.ui.login;

public class LoginWaitException extends LoginException {
   private static final long serialVersionUID = -6606669907221918306L;
   private final LoginWaitException.LoginWaitTask waitTask;

   public LoginWaitException(String reason, LoginWaitException.LoginWaitTask loginWaitTask) {
      super(reason);
      if (loginWaitTask == null) {
         throw new NullPointerException();
      } else {
         this.waitTask = loginWaitTask;
      }
   }

   public LoginWaitException.LoginWaitTask getWaitTask() {
      return this.waitTask;
   }

   public interface LoginWaitTask {
      void runTask() throws LoginException;
   }
}
