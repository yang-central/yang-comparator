package org.yangcentral.yangkit.comparator;

import org.yangcentral.yangkit.model.api.stmt.Mandatory;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-06-14
 */
public class MandatoryComparator extends CommonYangStatementComparator<Mandatory> {
    @Override
    protected List<CompatibilityRule.ChangeInfo> getChangeInfo(Mandatory left, Mandatory right) {
        List<CompatibilityRule.ChangeInfo> changeInfos = new ArrayList<>();

        if(left.getValue() && !right.getValue()){
            changeInfos.add(CompatibilityRule.ChangeInfo.EXPAND);
        } else if(!left.getValue() && right.getValue()){
            changeInfos.add(CompatibilityRule.ChangeInfo.REDUCE);
        }
        if(super.getChangeInfo(left, right).contains(CompatibilityRule.ChangeInfo.SEQUENCE_CHANGED)){
            changeInfos.add(CompatibilityRule.ChangeInfo.SEQUENCE_CHANGED);
        }
        return changeInfos;
    }

    @Override
    protected CompatibilityInfo defaultCompatibility(Mandatory left, Mandatory right,
        CompatibilityRule.ChangeInfo changeInfo) {
        if(changeInfo == CompatibilityRule.ChangeInfo.REDUCE){
            return new CompatibilityInfo(CompatibilityRule.Compatibility.NBC,"mandatory is changed from false to true,"
                + "it's non-backward-compatible.");
        }
        return super.defaultCompatibility(left, right, changeInfo);
    }
}
