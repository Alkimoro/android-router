package cn.linked.router.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dalvik.system.DexFile;

@Deprecated
public class ClassUtil {

    // 获取APP该Package下所有的ClassName
    public static Set<String> getClassNameByPackage(Context context, final String packageName) {
        final Set<String> classNames = new HashSet<>();
        List<String> paths = getSourcePaths(context);
        final CountDownLatch parserCtl = new CountDownLatch(paths.size());
        // 异步加载Dex的class
        for (final String path : paths) {
            RouterThreadPoolExecutor.getInstance().execute(() -> {
                DexFile dexfile = null;
                try {
                    if (path.endsWith(".zip")) {
                        // NOT use new DexFile(path), because it will throw "permission error in /data/dalvik-cache"
                        dexfile = DexFile.loadDex(path, path + ".tmp", 0);
                    } else {
                        dexfile = new DexFile(path);
                    }

                    Enumeration<String> dexEntries = dexfile.entries();
                    while (dexEntries.hasMoreElements()) {
                        String className = dexEntries.nextElement();
                        if (className.startsWith(packageName)) {
                            classNames.add(className);
                        }
                    }
                } catch (Throwable ignore) {
                    Log.e("Router", "扫描dex文件时出错", ignore);
                } finally {
                    if (null != dexfile) {
                        try {
                            dexfile.close();
                        } catch (Throwable ignore) {
                        }
                    }
                    parserCtl.countDown();
                }
            });
        }
        try {
            parserCtl.await();
        } catch (InterruptedException e) {
            Log.w("ClassUtil","异步获取ClassName中断，返回集合不完整");
        }
        return classNames;
    }
    // 获取该App 所有的dex文件路径
    public static List<String> getSourcePaths(Context context) {
        ApplicationInfo applicationInfo = null;
        List<String> sourcePaths = new ArrayList<>();
        try {
            applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);

            File sourceApk = new File(applicationInfo.sourceDir);

            sourcePaths.add(applicationInfo.sourceDir); // add the default apk path

            // the prefix of extracted file, ie: test.classes
            String extractedFilePrefix = sourceApk.getName() + ".classes";

            // 如果VM已经支持了MultiDex，就不要去Secondary Folder加载 Classesx.zip了，那里已经没有了
            // 通过是否存在sp中的multidex.version是不准确的，因为从低版本升级上来的用户，是包含这个sp配置的
            if (!isSupportMultiDex()) {
                // the total dex numbers
                int totalDexNumber = getMultiDexPreferences(context).getInt("dex.number", 1);
                File dexDir = new File(applicationInfo.dataDir, "code_cache" + File.separator + "secondary-dexes");

                for (int secondaryNumber = 2; secondaryNumber <= totalDexNumber; secondaryNumber++) {
                    // for each dex file, ie: test.classes2.zip, test.classes3.zip...
                    String fileName = extractedFilePrefix + secondaryNumber + ".zip";
                    File extractedFile = new File(dexDir, fileName);
                    if (extractedFile.isFile()) {
                        sourcePaths.add(extractedFile.getAbsolutePath());
                        // we ignore the verify zip part
                    } else {
                        Log.e("ClassUtil","缺少该文件'" + extractedFile.getPath() + "'");
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return sourcePaths;
    }
    private static SharedPreferences getMultiDexPreferences(Context context) {
        return context.getSharedPreferences("multidex.version", Context.MODE_PRIVATE |Context.MODE_MULTI_PROCESS);
    }
    // 是否支持MultiDex
    private static boolean isSupportMultiDex() {
        boolean isSupportMultiDex = false;
        String vmName = "'Android'";
        try {
            String versionString = System.getProperty("java.vm.version");
            if (versionString != null) {
                Matcher matcher = Pattern.compile("(\\d+)\\.(\\d+)(\\.\\d+)?").matcher(versionString);
                if (matcher.matches()) {
                    try {
                        int major = Integer.parseInt(matcher.group(1));
                        int minor = Integer.parseInt(matcher.group(2));
                        isSupportMultiDex = (major > 2) || ((major == 2) && (minor >= 1));
                    } catch (NumberFormatException ignore) {
                        // let isMultidexCapable be false
                    }
                }
            }
        } catch (Exception ignore) { }
        return isSupportMultiDex;
    }
}
