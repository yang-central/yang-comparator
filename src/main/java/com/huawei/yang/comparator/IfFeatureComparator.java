package com.huawei.yang.comparator;

import com.huawei.yang.model.api.stmt.IfFeature;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-06-15
 */
public class IfFeatureComparator extends CommonYangStatementComparator<IfFeature>{
    @Override
    protected CompatibilityInfo defaultCompatibility(IfFeature left, IfFeature right,
        CompatibilityRule.ChangeInfo changeInfo) {
        if(changeInfo == CompatibilityRule.ChangeInfo.ADDED){
            return new CompatibilityInfo(CompatibilityRule.Compatibility.NBC,"add a new if-feature,"
                + "it's non-backward-compatible.");
        } else if(changeInfo == CompatibilityRule.ChangeInfo.CHANGED){
            return new CompatibilityInfo(CompatibilityRule.Compatibility.UNKNOWN,
                "if-feature is changed, it's compatibility is unknown.");
        }
        return super.defaultCompatibility(left, right, changeInfo);
    }
}
