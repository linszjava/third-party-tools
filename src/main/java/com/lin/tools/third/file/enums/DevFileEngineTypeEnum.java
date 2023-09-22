package com.lin.tools.third.file.enums;

import cn.hutool.core.util.ObjectUtil;
import com.lin.common.exception.CommonException;
import com.lin.tools.third.file.strategy.EngineContext;
import com.lin.tools.third.file.utils.AliyunEngineUtil;
import com.lin.tools.third.file.utils.LocalEngineUtil;
import com.lin.tools.third.file.utils.MinioEngineUtil;
import com.lin.tools.third.file.utils.TencentEngineUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>TODO</p>
 *
 * @author linsz
 * @version v1.0
 * @date 2023/9/4 20:44
 */
@AllArgsConstructor
@Getter
public enum DevFileEngineTypeEnum {

    /** 本地 */
    LOCAL("LOCAL"),

    /** 阿里云 */
    ALIYUN("ALIYUN"),

    /** 腾讯云 */
    TENCENT("TENCENT"),

    /** MINIO */
    MINIO("MINIO");


    private final String value;



    /**
     * 根据输入的enum类型，得到对应的EngineContext
     */
    public static EngineContext getEngineContext(String engine){
        engine = ObjectUtil.isNull(engine) ?"LOCAL" : engine;
        switch (engine){
            case "LOCAL":
                return new EngineContext(new LocalEngineUtil());
            case "TENCENT":
                return new EngineContext(new TencentEngineUtil());
            case "MINIO":
                return new EngineContext(new MinioEngineUtil());
            case "ALIYUN":
                return new EngineContext(new AliyunEngineUtil());
            default:
                throw new CommonException("输入的引擎类型不支持,{}",engine);
        }
    }

}
