package com.example.myinstallerhook;

import android.app.Activity;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.util.Enumeration;
import dalvik.system.DexFile;

public class HookMain implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.equals("com.android.packageinstaller"))
            return;

        XposedBridge.log("MyInstallerHook: PackageInstaller loaded");

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
                    XposedBridge.log("MyInstallerHook: Hooking Activity: " + className);

                    XposedHelpers.findAndHookMethod(clazz, "shouldShowChooser", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(false); // 自动跳过选择器
                            XposedBridge.log("MyInstallerHook: Chooser skipped in " + className);
                        }
                    });
                }
            }
        } catch (Throwable t) {
            XposedBridge.log("MyInstallerHook: Hook failed - " + t.getMessage());
        }
    }
}