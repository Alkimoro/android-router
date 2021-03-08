package cn.linked.router.api;

import android.app.Application;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import cn.linked.router.common.Const;
import cn.linked.router.common.MateData;
import cn.linked.router.common.RouteMappingLoader;

public class Router {
    private static final HashMap<String, MateData> map=new HashMap<>();

    private static ClassLoader classLoader;

    public static void initSync(Application application) {
        initInternal(application,false);
    }

    public static void initLoadLazy(Application application) {
        initInternal(application,true);
    }

    private static void initInternal(Application application,boolean lazy){
        if(application!=null) {
            classLoader = application.getClassLoader();
            for (int i = 0; i <= Const.MODULE_NUM; i++) {
                String generateFileName = Const.PACKAGE_PATH + "." + Const.GENERATE_FILE_PREFIX + i;
                try {
                    RouteMappingLoader loader = (RouteMappingLoader) classLoader.loadClass(generateFileName).newInstance();
                    loader.loadInto(map);
                    if (!lazy) {
                        for (Map.Entry<String, MateData> stringMateDataEntry : map.entrySet()) {
                            try {
                                classLoader.loadClass(stringMateDataEntry.getValue().getFullClassName());
                            }catch (ClassNotFoundException e) {
                                Log.w("Router", "class: "+stringMateDataEntry.getValue().getFullClassName()+" not found");
                            }
                        }
                    }
                } catch (IllegalAccessException | InstantiationException e) {
                    Log.e("Router", "初始化该class出错，ClassName：" + generateFileName);
                } catch (ClassNotFoundException ignored) { }
            }
            Log.i("Router", "Router初始化完毕");
        }else{
            Log.e("Router", "初始化Router失败，application为null");
        }
    }

    public static Class route(String path){
        if(path!=null&&!"".equals(path)){
            MateData mateData=map.get(path);
            if(mateData!=null&&classLoader!=null){
                try {
                    return classLoader.loadClass(mateData.getFullClassName());
                }catch (ClassNotFoundException e) {
                    Log.w("Router", "class: "+mateData.getFullClassName()+" not found");
                    return null;
                }
            }
        }
        return null;
    }
}
