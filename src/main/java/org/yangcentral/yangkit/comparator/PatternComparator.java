package org.yangcentral.yangkit.comparator;

import org.yangcentral.yangkit.model.api.stmt.type.Pattern;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-06-14
 */
public class PatternComparator extends CommonYangStatementComparator<Pattern> {


    @Override
    protected CompatibilityInfo defaultCompatibility(Pattern left, Pattern right,
        CompatibilityRule.ChangeInfo changeInfo) {
        if(changeInfo == CompatibilityRule.ChangeInfo.ADDED){
            return new CompatibilityInfo(CompatibilityRule.Compatibility.NBC,
                "add a pattern, it's non-backward-compatible.");
        } else if(changeInfo == CompatibilityRule.ChangeInfo.CHANGED){
            return new CompatibilityInfo(CompatibilityRule.Compatibility.UNKNOWN,
                "change a pattern, the compatibility is unknown.");
        }
        return super.defaultCompatibility(left, right, changeInfo);
    }
}
