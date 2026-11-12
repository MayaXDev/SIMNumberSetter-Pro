package de.robv.android.xposed;

import java.lang.reflect.Member;

public abstract class XC_MethodHook {

    public class Unhook {
        public Member getHookedMethod() {
            return null;
        }

        public XC_MethodHook getCallback() {
            return XC_MethodHook.this;
        }

        public void unhook() {}
    }

    public static class MethodHookParam {
        public Object[] args;
        public Object result;
        public Throwable throwable;
        public Object thisObject;

        public Object getResult() {
            return result;
        }

        public void setResult(Object result) {
            this.result = result;
        }

        public boolean hasThrowable() {
            return throwable != null;
        }
    }

    public void beforeHookedMethod(MethodHookParam param) throws Throwable {}
    public void afterHookedMethod(MethodHookParam param) throws Throwable {}

}
