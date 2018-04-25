package com.tudou.common.resource;

import com.google.common.collect.Lists;
import com.tudou.common.util.A;
import com.tudou.common.util.LogUtil;
import org.springframework.core.io.Resource;

import java.util.Arrays;
import java.util.List;

public final class CollectResourceUtil {

    public static Resource[] resource(List<Resource[]> allModelResource) {
        List<Resource> resourceList = Lists.newArrayList();
        for (Resource[] resources : allModelResource) {
            // 将模块里面的 mybatis 配置文件都收集起来扫描进 spring 容器
            resourceList.addAll(Arrays.asList(resources));
        }
        if (LogUtil.ROOT_LOG.isDebugEnabled()) {
            LogUtil.ROOT_LOG.debug("mybatis load xml:({})", A.toStr(resourceList));
        }
        return resourceList.toArray(new Resource[resourceList.size()]);
    }
}
