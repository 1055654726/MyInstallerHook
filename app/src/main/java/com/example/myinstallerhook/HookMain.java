package com.example.myinstallerhook;

import android.app.Activity;
import io.github.libxposed.api.LXposedHookLoadPackage;
import io.github.libxposed.api.XC_MethodHook;
import io.github.libxposed.api.XposedHelpers;
import io.github.libxposed.api.callbacks.XC_LoadPackage;

import java.util.Enumeration;
import dalvik.system.DexFile;

public class HookMain implements LXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        // 只针对 PackageInstaller
        if (!lpparam.packageName.equals("com.android.packageinstaller"))
            return;

        XC_LoadPackage.log("MyInstallerHook: PackageInstaller loaded");

        try {
            DexFile dexFile = new DexFile(lpparam.appInfo.sourceDir);
            for (Enumeration<String> classNames = dexFile.entries(); classNames.hasMoreElements(); ) {
                String className = classNames.nextElement();
                Class<?> clazz;
                try {
                    clazz = lpparam.classLoader.loadClass(className);
                } catch (Throwable t) {
                    continue;
                }

                if (Activity.class.isAssignableFrom(clazz)) {
                    XC_LoadPackage.log("MyInstallerHook: Hooking Activity: " + className);

                    XposedHelpers.findAndHookMethod(clazz, "shouldShowChooser", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(false); // 自动跳过选择器
                            XC_LoadPackage.log("MyInstallerHook: Chooser skipped in " + className);
                        }
                    });
                }
            }
        } catch (Throwable t) {
            XC_LoadPackage.log("MyInstallerHook: Hook failed - " + t.getMessage());
        }
    }
}
