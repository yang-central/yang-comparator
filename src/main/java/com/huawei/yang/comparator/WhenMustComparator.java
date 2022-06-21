package com.huawei.yang.comparator;

import com.huawei.yang.model.api.stmt.YangStatement;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-06-14
 */
public class WhenMustComparator extends CommonYangStatementComparator{
    @Override
    protected CompatibilityInfo defaultCompatibility(YangStatement left, YangStatement right,
        CompatibilityRule.ChangeInfo changeInfo) {
        if(changeInfo == CompatibilityRule.ChangeInfo.ADDED){
            return new CompatibilityInfo(CompatibilityRule.Compatibility.NBC,"add a new when/must, it's non-backward-compatible.") ;
        } else if (changeInfo == CompatibilityRule.ChangeInfo.CHANGED){
            return new CompatibilityInfo(CompatibilityRule.Compatibility.UNKNOWN,"change a when/must, the compatibility is unknown.");
        }
        return super.defaultCompatibility(left, right, changeInfo);
    }
}
