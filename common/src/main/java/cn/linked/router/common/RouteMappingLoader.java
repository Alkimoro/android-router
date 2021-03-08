package cn.linked.router.common;

import java.util.Map;

public interface RouteMappingLoader {
    public void loadInto(Map<String,MateData> map);
}
