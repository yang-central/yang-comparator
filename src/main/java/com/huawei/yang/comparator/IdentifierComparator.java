package com.huawei.yang.comparator;

import org.yangcentral.yangkit.model.api.stmt.YangStatement;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-06-15
 */
public class IdentifierComparator extends CommonYangStatementComparator{
    @Override
    protected CompatibilityInfo defaultCompatibility(YangStatement left, YangStatement right,
        CompatibilityRule.ChangeInfo changeInfo) {
        if(changeInfo == CompatibilityRule.ChangeInfo.DELETED){
            return new CompatibilityInfo(CompatibilityRule.Compatibility.NBC,"the statement is deleted, "
                + "it's non-backward-compatible.");
        }
        return super.defaultCompatibility(left, right, changeInfo);
    }
}
