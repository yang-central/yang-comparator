package com.huawei.yang.comparator;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-06-01
 */
public abstract class YangCompareResultSerializer<T> {
    private YangCompareResult yangCompareResult;

    public YangCompareResultSerializer(YangCompareResult yangCompareResult){
        this.yangCompareResult = yangCompareResult;
    }

    public abstract T serialize();

    public abstract T serialize(boolean needChangeType, boolean needMetaPath, boolean needMeta,boolean needCompatibility);

    public YangCompareResult getYangCompareResult() {
        return yangCompareResult;
    }
    public static YangCompareResultSerializer getXmlSerializer(YangCompareResult yangCompareResult){
        return new YangCompareResultXmlSerializer(yangCompareResult);
    }
}
