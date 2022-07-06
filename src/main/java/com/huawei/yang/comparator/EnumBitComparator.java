package com.huawei.yang.comparator;

import org.yangcentral.yangkit.model.api.stmt.YangStatement;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-06-14
 */
public class EnumBitComparator extends CommonYangStatementComparator{

    @Override
    protected CompatibilityInfo defaultCompatibility(YangStatement left, YangStatement right,
        CompatibilityRule.ChangeInfo changeInfo) {
        if(changeInfo == CompatibilityRule.ChangeInfo.DELETED){
            return new CompatibilityInfo(CompatibilityRule.Compatibility.NBC,
                "delete an enum, it's non-backward-compatible.");
        } else if(changeInfo == CompatibilityRule.ChangeInfo.ADDED){
            return new CompatibilityInfo(CompatibilityRule.Compatibility.BC,"add a new enum, it's backward-compatible.");
        }
        return super.defaultCompatibility(left, right, changeInfo);
    }
}
