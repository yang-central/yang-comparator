package com.huawei.yang.comparator;

import org.yangcentral.yangkit.model.api.stmt.Default;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-06-14
 */
public class DefaultComparator extends CommonYangStatementComparator<Default>{
    @Override
    protected CompatibilityInfo defaultCompatibility(Default left, Default right,
        CompatibilityRule.ChangeInfo changeInfo) {
        if(changeInfo == CompatibilityRule.ChangeInfo.DELETED
            || changeInfo == CompatibilityRule.ChangeInfo.CHANGED){
            return new CompatibilityInfo(CompatibilityRule.Compatibility.NBC,
                "delete or change default statement, it's non-backward-compatible");
        }
        return super.defaultCompatibility(left, right, changeInfo);
    }
}
