package com.huawei.yang.comparator;

import org.yangcentral.yangkit.model.api.stmt.Augment;

import java.util.ArrayList;
import java.util.List;

public class AugmentComparator extends SchemaNodeComparator<Augment>{
    @Override
    public List<YangCompareResult> compare(Augment left, Augment right) {
        if(left != null){
            if(left.getEffectiveSchemaNodeChildren().size()==0){
                left = null;
            }
        }
        if(right != null){
            if(right.getEffectiveSchemaNodeChildren().size()==0){
                right = null;
            }
        }
        List<YangCompareResult> compareResults = new ArrayList<>();
        if(left == null && right == null){
            return compareResults;
        }
        return compareChildren(left,right);
    }
}
