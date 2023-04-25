package org.yangcentral.yangkit.comparator;

import org.yangcentral.yangkit.model.api.stmt.YangStatement;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-06-14
 */
public class ValuePositionComparator extends CommonYangStatementComparator{
    @Override
    protected CompatibilityInfo defaultCompatibility(YangStatement left, YangStatement right,
        CompatibilityRule.ChangeInfo changeInfo) {
        if(changeInfo == CompatibilityRule.ChangeInfo.CHANGED){
            return new CompatibilityInfo(CompatibilityRule.Compatibility.NBC,"change value or position, it's non-backward-compatible.");
        }
        return super.defaultCompatibility(left, right, changeInfo);
    }
}
