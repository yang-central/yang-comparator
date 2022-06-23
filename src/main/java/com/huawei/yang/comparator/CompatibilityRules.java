package com.huawei.yang.comparator;

import org.dom4j.Document;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-06-09
 */
public class CompatibilityRules {
    private List<CompatibilityRule> compatibilityRules = new ArrayList<>();
    private static CompatibilityRules instance = new CompatibilityRules();

    private CompatibilityRules(){

    }

    public boolean addCompatibilityRule(CompatibilityRule rule){
        if(null != getCompatibilityRule(rule.getRuleId())){
            return false;
        }
        return compatibilityRules.add(rule);
    }

    public List<CompatibilityRule> getCompatibilityRules() {
        return compatibilityRules;
    }

    public CompatibilityRule getCompatibilityRule(String ruleId){
        for(CompatibilityRule rule:compatibilityRules){
            if(rule.getRuleId().equals(rule)){
                return rule;
            }
        }
        return null;
    }
    public static CompatibilityRules getInstance(){
        return instance;
    }
    public void deserialize(Document document){
        compatibilityRules.clear();
        Element root = document.getRootElement();
        List<Element> children = root.elements("rule");
        for(Element ruleElement:children){
            CompatibilityRule rule = CompatibilityRule.deserialize(ruleElement);
            addCompatibilityRule(rule);
        }
    }
    private boolean matchCondition(CompatibilityRule left, CompatibilityRule.ChangeInfo right){
        if(left.getCondition() == CompatibilityRule.ChangeInfo.ANY){
            return true;
        } else if(left.getCondition() == CompatibilityRule.ChangeInfo.IGNORE){
            return false;
        }
        if(left.getCondition() == right){
            return true;
        }
        if(left.getCondition() == CompatibilityRule.ChangeInfo.CHANGED
        && (right == CompatibilityRule.ChangeInfo.EXPAND
        || right == CompatibilityRule.ChangeInfo.REDUCE
        || right == CompatibilityRule.ChangeInfo.SEQUENCE_CHANGED
        || right == CompatibilityRule.ChangeInfo.INTEGER_TYPE_CHANGED)){
            if(left.getExceptConditions() != null && left.getExceptConditions().contains(right)){
                return false;
            }
            return true;
        }
        return false;
    }
    public CompatibilityRule searchRule(String statement,
        CompatibilityRule.ChangeInfo changeInfo){
        for(CompatibilityRule rule:compatibilityRules){
            if(rule.getStatements().contains(statement)){
                if(matchCondition(rule,changeInfo)){
                    return rule;
                }
            }
        }
        return null;
    }
}
