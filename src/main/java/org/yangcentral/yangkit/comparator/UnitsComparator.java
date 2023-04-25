package org.yangcentral.yangkit.comparator;

import org.yangcentral.yangkit.model.api.stmt.Units;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-06-14
 */
public class UnitsComparator extends CommonYangStatementComparator<Units>{


    @Override
    protected CompatibilityInfo defaultCompatibility(Units left, Units right,
        CompatibilityRule.ChangeInfo changeInfo) {
        if(changeInfo == CompatibilityRule.ChangeInfo.DELETED
        || changeInfo == CompatibilityRule.ChangeInfo.CHANGED){
            return new CompatibilityInfo(CompatibilityRule.Compatibility.NBC,"delete or change units, it's non-backward-compatible.");
        }
        return super.defaultCompatibility(left, right, changeInfo);
    }
}
