package com.huawei.yang.comparator;

import org.yangcentral.yangkit.model.api.stmt.Reference;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-06-14
 */
public class ReferenceComparator extends CommonYangStatementComparator<Reference> {
    @Override
    protected CompatibilityInfo defaultCompatibility(Reference left, Reference right,
        CompatibilityRule.ChangeInfo changeInfo) {
        if(changeInfo == CompatibilityRule.ChangeInfo.DELETED){
            return new CompatibilityInfo(CompatibilityRule.Compatibility.NBC,"delete reference, it;s non-backward-compatible");
        }
        return super.defaultCompatibility(left, right, changeInfo);
    }
}
