package com.huawei.yang.comparator;

import org.yangcentral.yangkit.base.Cardinality;
import org.yangcentral.yangkit.base.YangElement;
import org.yangcentral.yangkit.base.YangStatementDef;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.schema.SchemaTreeType;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Choice;
import org.yangcentral.yangkit.model.api.stmt.ConfigSupport;
import org.yangcentral.yangkit.model.api.stmt.Container;
import org.yangcentral.yangkit.model.api.stmt.Default;
import org.yangcentral.yangkit.model.api.stmt.IdentifierRef;
import org.yangcentral.yangkit.model.api.stmt.IfFeature;
import org.yangcentral.yangkit.model.api.stmt.IfFeatureSupport;
import org.yangcentral.yangkit.model.api.stmt.Leaf;
import org.yangcentral.yangkit.model.api.stmt.LeafList;
import org.yangcentral.yangkit.model.api.stmt.MandatorySupport;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.MultiInstancesDataNode;
import org.yangcentral.yangkit.model.api.stmt.Must;
import org.yangcentral.yangkit.model.api.stmt.MustSupport;
import org.yangcentral.yangkit.model.api.stmt.OrderBy;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.SchemaNodeContainer;
import org.yangcentral.yangkit.model.api.stmt.Status;
import org.yangcentral.yangkit.model.api.stmt.StatusStmt;
import org.yangcentral.yangkit.model.api.stmt.SubModule;
import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;
import org.yangcentral.yangkit.model.api.stmt.Unique;
import org.yangcentral.yangkit.model.api.stmt.VirtualSchemaNode;
import org.yangcentral.yangkit.model.api.stmt.WhenSupport;
import org.yangcentral.yangkit.model.api.stmt.YangBuiltinStatement;
import org.yangcentral.yangkit.model.api.stmt.YangList;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.model.api.stmt.YangUnknown;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;
import org.yangcentral.yangkit.utils.file.FileUtil;
import org.yangcentral.yangkit.utils.xml.XmlWriter;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-05-19
 */
public class YangComparator {
    private YangSchemaContext leftContext;
    private YangSchemaContext rightContext;
    private CompatibilityRules compatibilityRules;

    public YangComparator(YangSchemaContext leftContext, YangSchemaContext rightContext) {
        this.leftContext = leftContext;
        this.rightContext = rightContext;
    }

    public CompatibilityRules getCompatibilityRules() {
        return compatibilityRules;
    }

    public void setCompatibilityRules(CompatibilityRules compatibilityRules) {
        this.compatibilityRules = compatibilityRules;
    }

    public List<YangCompareResult> compareSchemaNode(SchemaNode left,SchemaNode right){

        List<YangCompareResult> compareResults = new ArrayList<>();
        if(left instanceof TypedDataNode){
            TypedDataNode leftDataNode = (TypedDataNode) left;
            TypedDataNode rightDataNode = (TypedDataNode) right;
            if(!leftDataNode.getType().getRestriction().equals(rightDataNode.getType().getRestriction())){
                compareResults.addAll(compareStatement(leftDataNode.getType(),rightDataNode.getType(),true));
            }
            compareResults.addAll(compareStatement(leftDataNode.getUnits(),rightDataNode.getUnits()));
            if(left instanceof Leaf){
                Leaf leftLeaf = (Leaf) left;
                Leaf rightLeaf = (Leaf) right;
                compareResults.addAll(compareStatement(leftLeaf.getEffectiveDefault(),rightLeaf.getEffectiveDefault(),true));
            } else if(left instanceof LeafList){
                LeafList leftLeafList = (LeafList) left;
                LeafList rightLeafList = (LeafList) right;
                List<Default> leftDefaults = leftLeafList.getEffectiveDefaults();
                List<Default> rightDefaults = rightLeafList.getEffectiveDefaults();
                compareResults.addAll(compareStatement(leftDefaults,rightDefaults,true));
            }

        } else if(left instanceof Container){
            Container leftContainer = (Container) left;
            Container rightContainer = (Container) right;
            compareResults.addAll(compareStatement(leftContainer.getPresence(),rightContainer.getPresence()));
        } else if (left instanceof YangList){
            YangList leftList = (YangList) left;
            YangList rightList = (YangList) right;
            compareResults.addAll(compareStatement(leftList.getKey(),rightList.getKey(),true));
            List<Unique> leftUniques = leftList.getUniques();
            List<Unique> rightUniques = rightList.getUniques();
            compareResults.addAll(compareStatement(leftUniques,rightUniques,true));
        } else if (left instanceof Choice){
            Choice leftChoice = (Choice) left;
            Choice rightChoice = (Choice) right;
            compareResults.addAll(compareStatement(leftChoice.getDefault(),rightChoice.getDefault(),true));
        }
        if(left instanceof MustSupport){
            List<Must> leftMusts = ((MustSupport)left).getMusts();
            List<Must> rightMusts = ((MustSupport)right).getMusts();
            List<YangCompareResult> mustResults = compareStatement(leftMusts,rightMusts,true);
            if(mustResults.size() > 0){
                compareStatement(leftMusts,rightMusts,true);
                compareResults.addAll(mustResults);
            }

        }
        if(left instanceof IfFeatureSupport){
            List<IfFeature> LeftIfFeatures = ((IfFeatureSupport)left).getIfFeatures();
            List<IfFeature> rightIfFeatures = ((IfFeatureSupport)right).getIfFeatures();
            compareResults.addAll(compareStatement(LeftIfFeatures,rightIfFeatures,true));
        }
        if(left instanceof WhenSupport){
            WhenSupport leftWhenSupport = (WhenSupport) left;
            WhenSupport rightWhenSupport = (WhenSupport) right;
            compareResults.addAll(compareStatement(leftWhenSupport.getWhen(),rightWhenSupport.getWhen(),true));
        }
        if(left instanceof MultiInstancesDataNode){
            MultiInstancesDataNode leftDataNode = (MultiInstancesDataNode) left;
            MultiInstancesDataNode rightDataNode = (MultiInstancesDataNode) right;
            int leftMinElements = leftDataNode.getMinElements() == null?0:leftDataNode.getMinElements().getValue();
            int rightMinElements = rightDataNode.getMinElements() == null?0:rightDataNode.getMinElements().getValue();
            if(leftMinElements != rightMinElements){
                YangTreeMetaCompareResult yangTreeMetaCompareResult =
                    new YangTreeMetaCompareResult(right,"min-elements", ChangeType.MODIFY,
                        String.valueOf(leftMinElements),String.valueOf(rightMinElements));
                compareResults.add(yangTreeMetaCompareResult);
            }
            int leftMaxElements = leftDataNode.getMaxElements()==null?Integer.MAX_VALUE
                :leftDataNode.getMaxElements().isUnbounded()?Integer.MAX_VALUE:leftDataNode.getMaxElements().getValue();
            int rightMaxElements = rightDataNode.getMaxElements()==null?Integer.MAX_VALUE
                :rightDataNode.getMaxElements().isUnbounded()?Integer.MAX_VALUE:rightDataNode.getMaxElements().getValue();
            if(leftMaxElements != rightMaxElements){
                YangTreeMetaCompareResult yangTreeMetaCompareResult =
                    new YangTreeMetaCompareResult(right, "max-elements",ChangeType.MODIFY,
                        (leftMaxElements ==Integer.MAX_VALUE)?"unbounded":String.valueOf(leftMaxElements),
                        (rightMaxElements ==Integer.MAX_VALUE)?"unbounded":String.valueOf(rightMaxElements));
                compareResults.add(yangTreeMetaCompareResult);
            }
            if(left.isConfig() && right.isConfig()){
                OrderBy leftOrderedBy = OrderBy.SYSTEM;
                if(leftDataNode.getOrderedBy() != null){
                    leftOrderedBy = leftDataNode.getOrderedBy().getOrderedBy();
                }
                OrderBy rightOrderedBy = OrderBy.SYSTEM;
                if(rightDataNode.getOrderedBy() != null){
                    rightOrderedBy = rightDataNode.getOrderedBy().getOrderedBy();
                }
                if(leftOrderedBy != rightOrderedBy){
                    YangTreeMetaCompareResult yangTreeMetaCompareResult =
                        new YangTreeMetaCompareResult(right, "ordered-by",ChangeType.MODIFY,
                            leftOrderedBy.getOrderBy(),rightOrderedBy.getOrderBy());
                    compareResults.add(yangTreeMetaCompareResult);
                }

            }

        }
        if(left instanceof MandatorySupport && left.isMandatory() != right.isMandatory()){
            YangTreeMetaCompareResult yangTreeMetaCompareResult =
                new YangTreeMetaCompareResult(right, "mandatory",ChangeType.MODIFY,
                    String.valueOf(left.isMandatory()),String.valueOf(right.isMandatory()));
            compareResults.add(yangTreeMetaCompareResult);
        }
        if(left instanceof ConfigSupport && left.getSchemaTreeType() == SchemaTreeType.DATATREE
        && left.isConfig() != right.isConfig()){
            YangTreeMetaCompareResult yangTreeMetaCompareResult =
                new YangTreeMetaCompareResult(right, "config",ChangeType.MODIFY,
                    String.valueOf(left.isConfig()),String.valueOf(right.isConfig()));
            compareResults.add(yangTreeMetaCompareResult);
        }
        if(left.getEffectiveStatus() != right.getEffectiveStatus()){
            YangTreeMetaCompareResult yangTreeMetaCompareResult =
                new YangTreeMetaCompareResult(right, "status",ChangeType.MODIFY,
                    left.getEffectiveStatus().getStatus(),right.getEffectiveStatus().getStatus());
            compareResults.add(yangTreeMetaCompareResult);
        }

        List<YangCompareResult> unknownResults = compareStatement(left.getUnknowns(),right.getUnknowns(),true);
        if(unknownResults.size() > 0){
            compareResults.addAll(unknownResults);
        }

        return compareResults;
    }


    public List<YangCompareResult> compareChildrenTree(SchemaNode leftSchemaNode,SchemaNode rightSchemaNode){
        List<YangCompareResult> compareResults = new ArrayList<>();
        // if(!leftSchemaNode.equals(rightSchemaNode)){
        //     YangTreeCompareResult compareResult = new YangTreeCompareResult(leftSchemaNode.getSchemaPath(),ChangeType.MODIFY);
        //     compareResult.setLeft(leftSchemaNode);
        //     compareResult.setRight(rightSchemaNode);
        //     compareResults.add(compareResult);
        // }

        List<YangCompareResult> statementCompareResults = compareSchemaNode(leftSchemaNode,rightSchemaNode);
        if(statementCompareResults.size() > 0){
            YangTreeCompareResult compareResult = new YangTreeCompareResult(leftSchemaNode.getSchemaPath(),
                ChangeType.MODIFY);
            compareResult.setLeft(leftSchemaNode);
            compareResult.setRight(rightSchemaNode);
            compareResult.setMetaCompareResults(statementCompareResults);
            compareResults.add(compareResult);
        }
        if((leftSchemaNode instanceof SchemaNodeContainer) && (rightSchemaNode instanceof SchemaNodeContainer)){
            //children compare
            compareResults.addAll(compareChildrenTree((SchemaNodeContainer) leftSchemaNode,(SchemaNodeContainer)rightSchemaNode));
        }

        return compareResults;
    }

    public static List<SchemaNode> getEffectiveSchemaNodeChildren (SchemaNodeContainer schemaNodeContainer){
        List<SchemaNode> effectiveSchemaNodeChildren = new ArrayList<>();
        for(SchemaNode schemaNode:schemaNodeContainer.getSchemaNodeChildren()){
            if(schemaNode instanceof VirtualSchemaNode){
                VirtualSchemaNode virtualSchemaNode = (VirtualSchemaNode) schemaNode;
                effectiveSchemaNodeChildren.addAll(getEffectiveSchemaNodeChildren(virtualSchemaNode));
            }
            else {
                effectiveSchemaNodeChildren.add(schemaNode);
            }
        }
        return effectiveSchemaNodeChildren;
    }

    public List<YangCompareResult> compareChildrenTree(SchemaNodeContainer leftContainer,SchemaNodeContainer rightContainer){
        List<YangCompareResult> compareResults = new ArrayList<>();
        List<SchemaNode> foundSchemaNodes = new ArrayList<>();
        List<SchemaNode> leftSchemaNodes = getEffectiveSchemaNodeChildren(leftContainer);
        for(SchemaNode leftSchemaNode:leftSchemaNodes){
            if(!leftSchemaNode.isActive()){
                continue;
            }
            SchemaNode rightSchemaNode = rightContainer.getSchemaNodeChild(leftSchemaNode.getIdentifier());
            if(null == rightSchemaNode || !rightSchemaNode.getYangKeyword().equals(leftSchemaNode.getYangKeyword())){
                //not found
                YangTreeCompareResult compareResult = new YangTreeCompareResult(leftSchemaNode.getSchemaPath(),ChangeType.DELETE);
                compareResult.setLeft(leftSchemaNode);
            } else {
                compareResults.addAll(compareChildrenTree(leftSchemaNode,rightSchemaNode));
                foundSchemaNodes.add(rightSchemaNode);
            }
        }
        List<SchemaNode> rightSchemaNodes = getEffectiveSchemaNodeChildren(rightContainer);
        for(SchemaNode rightSchemaNode:rightSchemaNodes){
            if(!rightSchemaNode.isActive()){
                continue;
            }
            if(contains(foundSchemaNodes,rightSchemaNode)){
                continue;
            }
            YangTreeCompareResult compareResult = new YangTreeCompareResult(rightSchemaNode.getSchemaPath(),ChangeType.ADD);
            compareResult.setRight(rightSchemaNode);
            compareResults.add(compareResult);
        }
        return compareResults;
    }

    public List<YangCompareResult> compareTree(){
        List<YangCompareResult> compareResults = new ArrayList<>();
        List<Module> foundModules = new ArrayList<>();
        List<Module> modules = leftContext.getModules();
        for(Module module:modules){
            if(module instanceof SubModule){
                continue;
            }
            List<Module> rightModules = rightContext.getModule(module.getArgStr());
            if(rightModules == null || rightModules.size()==0){
                //not find
                List<SchemaNode> rootSchemaNodes = getEffectiveSchemaNodeChildren(module);
                for(SchemaNode rootSchemaNode:rootSchemaNodes){
                    YangTreeCompareResult compareResult = new YangTreeCompareResult(rootSchemaNode.getSchemaPath(),ChangeType.DELETE);
                    compareResult.setLeft(rootSchemaNode);
                    compareResults.add(compareResult);
                }
            } else {
                Module rightModule = rightModules.get(0);
                foundModules.add(rightModule);
                compareResults.addAll(compareChildrenTree(module,rightModule));
            }
        }
        for(Module rightModule:rightContext.getModules()){
            if(rightModule instanceof SubModule){
                continue;
            }
            if(contains(foundModules,rightModule)){
                continue;
            }
            List<SchemaNode> rootSchemaNodes = getEffectiveSchemaNodeChildren(rightModule);
            for(SchemaNode rootSchemaNode:rootSchemaNodes){
                YangTreeCompareResult compareResult = new YangTreeCompareResult(rootSchemaNode.getSchemaPath(),ChangeType.ADD);
                compareResult.setRight(rootSchemaNode);
                compareResults.add(compareResult);
            }
        }
        return compareResults;
    }

    private List<YangStatement> getSubStatements(YangStatement statement){
        List<YangStatement> subStatements = new ArrayList<>();
        for(YangElement subElement :statement.getSubElements()){
            if(!(subElement instanceof YangStatement)){
                continue;
            }
            subStatements.add((YangStatement) subElement);
        }
        return subStatements;
    }
    private int calSimilarity(YangStatement src,YangStatement candidate){
        if(!src.getYangKeyword().equals(candidate.getYangKeyword())){
            return 0;
        }
        int similarity = 1;
        if(!src.equals(candidate)){
            return similarity;
        }
        similarity++;
        List<YangStatement> srcSubStatements = getSubStatements(src);
        List<YangStatement> candidateSubStatements = getSubStatements(candidate);
        if(srcSubStatements.size() != candidateSubStatements.size()){
            return similarity;
        }
        similarity++;
        List<YangStatement> matched = new ArrayList<>();
        for(YangStatement srcSubStatement:srcSubStatements){
            int maxSubSimilarity = 0;
            YangStatement maxsimliaritySubStatement = null;
            for(YangStatement candidateSubStatment:candidateSubStatements){
                if(contains(matched,candidateSubStatment)){
                    continue;
                }
                int subSimilarity = calSimilarity(srcSubStatement,candidateSubStatment);
                if(subSimilarity > maxSubSimilarity){
                    maxSubSimilarity = subSimilarity;
                    maxsimliaritySubStatement = candidateSubStatment;
                }
            }
            similarity+= maxSubSimilarity;
            matched.add(maxsimliaritySubStatement);
        }
        return similarity;

    }

    private boolean contains(List list,Object o){
        if(list == null ){
            return false;
        }
        for(Object src:list){
            if(src == o){
                return true;
            }
        }
        return false;
    }

    private YangStatement searchStatement(YangStatement statement,List<YangStatement> target,List<YangStatement> matched){
        if(null == target || target.size() == 0){
            return null;
        }
        List<YangStatement> matchedTargetStmts = new ArrayList<>();
        for(YangStatement rightSubStatement:target){
            if(contains(matched,rightSubStatement)){
                continue;
            }
            if(CommonYangStatementComparator.yangStatementIsEqual(statement,rightSubStatement)){
                matchedTargetStmts.add(rightSubStatement) ;
                continue;
            }
        }
        if(matchedTargetStmts.size() == 1){
            return matchedTargetStmts.get(0);
        } else if(matchedTargetStmts.size() >1){
            int maxSimilarity = 0;
            YangStatement maxSimilarStatement = null;
            for(YangStatement matchedTargetStmt:matchedTargetStmts){
                int similarity = calSimilarity(statement,matchedTargetStmt);
                if(similarity > maxSimilarity){
                    maxSimilarity = similarity;
                    maxSimilarStatement = matchedTargetStmt;
                }
            }
            return maxSimilarStatement;
        }

        YangStatementDef statementDef = statement.getContext().getYangSpecification()
            .getStatementDef(statement.getParentStatement().getYangKeyword());
        if(statementDef == null){
            if(statement.getParentStatement() instanceof YangUnknown){
                //unknown will be treated as cardinality with non-unbounded
                for(YangStatement rightSubStatement:target){
                    if(contains(matched,rightSubStatement)){
                        continue;
                    }
                    return rightSubStatement;
                }
                return null;
            }
            else {
                return null;
            }
        }

        Cardinality cardinality = statementDef.getSubStatementCardinality(statement.getYangKeyword());
        if(cardinality == null){
            if(statement instanceof YangUnknown){
                //unknown will be treated as cardinality with non-unbounded
                for(YangStatement rightSubStatement:target){
                    if(contains(matched,rightSubStatement)){
                        continue;
                    }
                    return rightSubStatement;
                }
            }
            return null;
        }
        if(cardinality.isUnbounded()){
            return null;
        }

        for(YangStatement rightSubStatement:target){
            if(contains(matched,rightSubStatement)){
                continue;
            }
            return rightSubStatement;
        }
        return null;
    }

//    private boolean yangStatementIsEqual(YangStatement left,YangStatement right){
//        if(left instanceof IdentifierRef && right instanceof IdentifierRef){
//            if(!left.getYangKeyword().equals(right.getYangKeyword())){
//                return false;
//            }
//            YangStatement leftRefStatement = ((IdentifierRef)left).getReferenceStatement();
//            YangStatement rightRefStatement = ((IdentifierRef)right).getReferenceStatement();
//            if(leftRefStatement != null && rightRefStatement != null && leftRefStatement.equals(rightRefStatement)){
//                return true;
//            } else if(leftRefStatement == null && rightRefStatement == null){
//                if(left.getArgStr().equals(right.getArgStr())){
//                    return true;
//                }
//                else {
//                    return false;
//                }
//            } else {
//                return false;
//            }
//        }
//        return left.equals(right);
//    }
    public List<YangCompareResult> compareStatement(YangStatement left,YangStatement right){
        return compareStatement(left,right,false);
    }
    public List<YangCompareResult> compareStatement(List<? extends YangElement> leftElements,
        List<? extends YangElement> rightElements,boolean exceptSchemaNode){
        List<YangCompareResult> compareResults = new ArrayList<>();
        List<YangStatement> foundStatements = new ArrayList<>();

        if(leftElements.size() > 0){
            for(YangElement subElement:leftElements){
                if(!(subElement instanceof YangStatement)){
                    continue;
                }
                YangStatement leftSubStatement = (YangStatement) subElement;
                if(exceptSchemaNode){
                    if(leftSubStatement instanceof SchemaNode){
                        continue;
                    }
                }

                List<YangStatement> rightSubStatements = new ArrayList<>();
                for(YangElement rightElement:rightElements){
                    if(!(rightElement instanceof YangStatement)){
                        continue;
                    }
                    if(leftSubStatement.getYangKeyword().equals(((YangStatement) rightElement).getYangKeyword())){
                        rightSubStatements.add((YangStatement) rightElement);
                    }
                }
                if(rightSubStatements.size()==0){
                    //no right statement, so change type is delete
                    YangStatementCompareResult yangStatementCompareResult = new YangStatementCompareResult(ChangeType.DELETE,leftSubStatement,null);
                    compareResults.add(yangStatementCompareResult);
                    continue;
                }
                YangStatement matchedRightSubStatement = searchStatement(leftSubStatement,rightSubStatements,foundStatements);
                if(null == matchedRightSubStatement){
                    YangStatementCompareResult yangStatementCompareResult = new YangStatementCompareResult(ChangeType.DELETE,leftSubStatement,null);
                    compareResults.add(yangStatementCompareResult);
                } else {
                    foundStatements.add(matchedRightSubStatement);
                    compareResults.addAll(compareStatement(leftSubStatement,matchedRightSubStatement,exceptSchemaNode));
                }
            }
        }
        if(rightElements.size() > 0){
            for(YangElement subElement:rightElements){
                if(!(subElement instanceof YangStatement)){
                    continue;
                }
                if(exceptSchemaNode){
                    if(subElement instanceof SchemaNode){
                        continue;
                    }
                }
                YangStatement rightSubElement = (YangStatement) subElement;
                if(contains(foundStatements,rightSubElement)){
                    continue;
                }
                YangStatementCompareResult yangStatementCompareResult = new YangStatementCompareResult(ChangeType.ADD,
                    null,rightSubElement);
                compareResults.add(yangStatementCompareResult);
            }
        }
        return compareResults;
    }
    public List<YangCompareResult> compareStatement(YangStatement left,YangStatement right,boolean exceptSchemaNode){
        List<YangCompareResult> compareResults = new ArrayList<>();
        if(left == null && right == null){
            return compareResults;
        }
        if(left == null && right != null){
            YangStatementCompareResult compareResult = new YangStatementCompareResult(ChangeType.ADD,left,right);
            compareResults.add(compareResult);
            return compareResults;
        }
        if(left != null && right == null){
            YangStatementCompareResult compareResult = new YangStatementCompareResult(ChangeType.DELETE,left,right);
            compareResults.add(compareResult);
            return compareResults;
        }

        if(!CommonYangStatementComparator.yangStatementIsEqual(left,right)){
            YangStatementCompareResult yangStatementCompareResult = new YangStatementCompareResult(ChangeType.MODIFY,
                left,right);
            compareResults.add(yangStatementCompareResult);
        }
        compareResults.addAll(compareStatement(left.getSubElements(),right.getSubElements(),exceptSchemaNode));
        return compareResults;
    }

    public List<YangCompareResult> compareStatement(){
        List<YangCompareResult> compareResults = new ArrayList<>();
        List<Module> foundModules = new ArrayList<>();
        List<Module> modules = leftContext.getModules();
        for(Module module:modules){
            List<Module> rightModules = rightContext.getModule(module.getArgStr());
            if(rightModules == null || rightModules.size()==0){
                //not find
                YangStatementCompareResult yangStatementCompareResult = new YangStatementCompareResult(ChangeType.DELETE,
                    module,null);
                compareResults.add(yangStatementCompareResult);
            } else {
                Module rightModule = rightModules.get(0);
                foundModules.add(rightModule);
                compareResults.addAll(compareStatement(module,rightModule));
            }
        }
        for(Module rightModule:rightContext.getModules()){
            if(rightModule instanceof SubModule){
                continue;
            }
            if(contains(foundModules,rightModule)){
                continue;
            }
            YangStatementCompareResult yangStatementCompareResult = new YangStatementCompareResult(ChangeType.ADD,null,rightModule);
            compareResults.add(yangStatementCompareResult);
        }
        return compareResults;
    }

    public Map<String,List<YangCompareResult>> resortYangCompareResult(List<YangCompareResult> results){
        Map<String,List<YangCompareResult>> map = new HashMap<>();
        for(YangCompareResult yangCompareResult:results){
            String module = yangCompareResult.getModule().getArgStr();
            List<YangCompareResult> mapResults = map.get(module);
            if(null == mapResults){
                mapResults = new ArrayList<>();
                map.put(module,mapResults);
            }
            mapResults.add(yangCompareResult);

        }
        return map;
    }
    private String outputCompareResult(List<YangCompareResult> compareResults){
        Map<String,List<YangCompareResult>> resortedResults = resortYangCompareResult(compareResults);
        StringBuffer sb = new StringBuffer();
        Iterator<Map.Entry<String,List<YangCompareResult>>> it = resortedResults.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String,List<YangCompareResult>> entry = it.next();
            sb.append("module:").append(entry.getKey()).append("\n");
            sb.append("\t").append("added:\n");
            List<YangCompareResult> value = entry.getValue();
            for(YangCompareResult result: value){
                if(result.getChangeType() != ChangeType.ADD){
                    continue;
                }
                sb.append(result.getChangeDescription("\t\t")).append("\n");
            }
            sb.append("\t").append("deleted:\n");
            for(YangCompareResult result: value){
                if(result.getChangeType() != ChangeType.DELETE){
                    continue;
                }
                sb.append(result.getChangeDescription("\t\t")).append("\n");
            }
            sb.append("\t").append("changed:\n");
            for(YangCompareResult result: value){
                if(result.getChangeType() != ChangeType.MODIFY){
                    continue;
                }
                sb.append(result.getChangeDescription("\t\t")).append("\n");
            }
        }
        return sb.toString();
    }
    private Document outputXmlCompareResult(List<YangCompareResult> compareResults,CompareType compareType){
        return outputXmlCompareResult(compareResults,true,compareType);
    }
    private Document outputXmlCompareResult(List<YangCompareResult> compareResults,boolean needCompatible,CompareType compareType){
        Map<String,List<YangCompareResult>> resortedResults = resortYangCompareResult(compareResults);
        Element root = DocumentHelper.createElement("modules");
        Document document = DocumentHelper.createDocument(root);
        Iterator<Map.Entry<String,List<YangCompareResult>>> it = resortedResults.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String,List<YangCompareResult>> entry = it.next();
            List<YangCompareResult> value = entry.getValue();
            if(value.size() == 0){
                continue;
            }
            Element moduleElement = DocumentHelper.createElement("module");
            root.add(moduleElement);
            Attribute name = DocumentHelper.createAttribute(moduleElement,"name",entry.getKey());
            moduleElement.add(name);
            if(compareType == CompareType.COMPATIBLE_CHECK || compareType== CompareType.STMT){
                Element addedStmts = DocumentHelper.createElement("added-statements");
                moduleElement.add(addedStmts);
                Element deletedStmts = DocumentHelper.createElement("deleted-statements");
                moduleElement.add(deletedStmts);
                Element changedStmts = DocumentHelper.createElement("changed-statements");
                moduleElement.add(changedStmts);
                for(YangCompareResult result: value){
                    if(!(result instanceof YangStatementCompareResult)){
                        continue;
                    }
                    if(result.getChangeType() != ChangeType.ADD){
                        continue;
                    }
                    addedStmts.add((Element) YangCompareResultSerializer.getXmlSerializer(result).serialize(false,false,false,needCompatible));
                }
                for(YangCompareResult result: value){
                    if(!(result instanceof YangStatementCompareResult)){
                        continue;
                    }
                    if(result.getChangeType() != ChangeType.DELETE){
                        continue;
                    }
                    deletedStmts.add((Element) YangCompareResultSerializer.getXmlSerializer(result).serialize(false,false,false,needCompatible));
                }
                for(YangCompareResult result: value){
                    if(!(result instanceof YangStatementCompareResult)){
                        continue;
                    }
                    if(result.getChangeType() != ChangeType.MODIFY){
                        continue;
                    }
                    changedStmts.add((Element) YangCompareResultSerializer.getXmlSerializer(result).serialize(false,false,false,needCompatible));
                }
            }

            if(compareType == CompareType.COMPATIBLE_CHECK || compareType == CompareType.TREE){
                Element addedPaths = DocumentHelper.createElement("added-paths");
                moduleElement.add(addedPaths);
                Element deletedPaths = DocumentHelper.createElement("deleted-paths");
                moduleElement.add(deletedPaths);
                Element changedPaths = DocumentHelper.createElement("changed-paths");
                moduleElement.add(changedPaths);

                for(YangCompareResult result: value){
                    if(!(result instanceof YangTreeCompareResult)){
                        continue;
                    }
                    if(result.getChangeType() != ChangeType.ADD){
                        continue;
                    }
                    addedPaths.add((Element) YangCompareResultSerializer.getXmlSerializer(result).serialize(false,false,false,needCompatible));
                }
                for(YangCompareResult result: value){
                    if(!(result instanceof YangTreeCompareResult)){
                        continue;
                    }
                    if(result.getChangeType() != ChangeType.DELETE){
                        continue;
                    }
                    deletedPaths.add((Element) YangCompareResultSerializer.getXmlSerializer(result).serialize(false,false,false,needCompatible));
                }
                for(YangCompareResult result: value){
                    if(!(result instanceof YangTreeCompareResult)){
                        continue;
                    }
                    if(result.getChangeType() != ChangeType.MODIFY){
                        continue;
                    }
                    changedPaths.add((Element) YangCompareResultSerializer.getXmlSerializer(result).serialize(false,false,true,needCompatible));
                }
                Element obsoleted = DocumentHelper.createElement("obsoleted-paths");
                moduleElement.add(obsoleted);
                for(YangCompareResult result: value){
                    if(!(result instanceof YangTreeCompareResult)){
                        continue;
                    }
                    if(result.getChangeType() != ChangeType.MODIFY){
                        continue;
                    }
                    YangTreeCompareResult yangTreeCompareResult = (YangTreeCompareResult) result;
                    if(yangTreeCompareResult.getMetaCompareResults().size() == 0){
                        continue;
                    }
                    YangTreeMetaCompareResult find = null;
                    for(YangCompareResult metaCompareResult:yangTreeCompareResult.getMetaCompareResults()){
                        YangStatementCompareResult yangStatementCompareResult = (YangStatementCompareResult) metaCompareResult;
                        if(yangStatementCompareResult.getRight() instanceof StatusStmt){
                            StatusStmt right = (StatusStmt) yangStatementCompareResult.getRight();
                            if(right.getStatus() == Status.OBSOLETE){
                                obsoleted.add(
                                    (Element) YangCompareResultSerializer.getXmlSerializer(result).serialize(false,false,false,false));
                            }
                        }
                    }

                }
                Element deprecated = DocumentHelper.createElement("deprecated-paths");
                moduleElement.add(deprecated);
                for(YangCompareResult result: value){
                    if(!(result instanceof YangTreeCompareResult)){
                        continue;
                    }
                    if(result.getChangeType() != ChangeType.MODIFY){
                        continue;
                    }
                    YangTreeCompareResult yangTreeCompareResult = (YangTreeCompareResult) result;
                    if(yangTreeCompareResult.getMetaCompareResults().size() == 0){
                        continue;
                    }
                    YangTreeMetaCompareResult find = null;
                    for(YangCompareResult metaCompareResult:yangTreeCompareResult.getMetaCompareResults()){
                        YangStatementCompareResult yangStatementCompareResult = (YangStatementCompareResult) metaCompareResult;
                        if(yangStatementCompareResult.getRight() instanceof StatusStmt){
                            StatusStmt right = (StatusStmt) yangStatementCompareResult.getRight();
                            if(right.getStatus() == Status.DEPRECATED){
                                obsoleted.add(
                                    (Element) YangCompareResultSerializer.getXmlSerializer(result).serialize(false,false,false,false));
                            }
                        }
                    }

                }
            }
        }
        return document;
    }

    public static String outputStatement(YangStatement statement){
        StringBuffer sb = new StringBuffer();
        if(statement instanceof YangBuiltinStatement){
            sb.append(statement.getYangKeyword().getLocalName() );
        }
        else {
            YangUnknown unknown = (YangUnknown) statement;
            sb.append(unknown.getKeyword() );
        }
        if(statement.getArgStr() != null){
            sb.append(" ").append(statement.getArgStr());
        }
        return sb.toString();
    }

    /**
     * usage: -left --y {yang file or dir]} [--dep {dependency file or dir}] [--cap {capabilities.xml}]
     *        -right --y {yang file or dir]} [--dep {dependency file or dir}] [--cap {capabilities.xml}]
     *        -o {output.xml}
     *        {-tree | -stmt | -compatible-check [--rule rule.xml ]}
     * @param args
     * @throws DocumentException
     * @throws IOException
     * @throws YangParserException
     */

    public static void main(String[] args) throws DocumentException, IOException, YangParserException {
        int leftBeginPos = -1;
        int rightBeginPos = -1;
        int outBegin = -1;
        int typeBegin = -1;
        for(int i =0;i < args.length;i++){
            String arg = args[i];
            if(arg.equalsIgnoreCase("-left")){
                leftBeginPos = i;
            } else if (arg.equalsIgnoreCase("-right")){
                rightBeginPos = i;
            } else if (arg.equalsIgnoreCase("-o")) {
                outBegin = i;
            } else if(arg.equalsIgnoreCase("-tree")
                || arg.equalsIgnoreCase("-stmt")
                || arg.equalsIgnoreCase("-compatible-check")){
                typeBegin = i;
            }
        }
        if(leftBeginPos != 0){
            System.out.println("no left argument or left argument is not the 1st argument.");
            System.out.println("usage: -left --y {yang file or dir]} [--dep {dependency file or dir}] [--cap {capabilities.xml}]\n"
                             + "       -right --y {yang file or dir]} [--dep {dependency file or dir}] [--cap {capabilities.xml}]\n"
                             + "        -o {output.xml}\n"
                             + "        {-tree | -stmt [--filter filter.xml] | -compatible-check [--rule rule.xml] [--filter filter.xml]}");
            return;
        }
        if(rightBeginPos == -1){
            System.out.println("no right argument.");
            System.out.println("usage: -left --y {yang file or dir]} [--dep {dependency file or dir}] [--cap {capabilities.xml}]\n"
                + "       -right --y {yang file or dir]} [--dep {dependency file or dir}] [--cap {capabilities.xml}]\n"
                + "        -o {output.xml}\n"
                + "        {-tree | -stmt [--filter filter.xml] | -compatible-check [--rule rule.xml] [--filter filter.xml]}");
            return;
        }
        if(outBegin == -1){
            System.out.println("no output argument.");
            System.out.println("usage: -left --y {yang file or dir]} [--dep {dependency file or dir}] [--cap {capabilities.xml}]\n"
                + "       -right --y {yang file or dir]} [--dep {dependency file or dir}] [--cap {capabilities.xml}]\n"
                + "        -o {output.xml}\n"
                + "        {-tree | -stmt [--filter filter.xml] | -compatible-check [--rule rule.xml] [--filter filter.xml]}");
            return;
        }
        if(typeBegin == -1){
            System.out.println("no compare type argument, it should be '-tree' or '-stmt' or '-compatible-check' ");
            System.out.println("usage: -left --y {yang file or dir]} [--dep {dependency file or dir}] [--cap {capabilities.xml}]\n"
                + "       -right --y {yang file or dir]} [--dep {dependency file or dir}] [--cap {capabilities.xml}]\n"
                + "        -o {output.xml}\n"
                + "        {-tree | -stmt [--filter filter.xml] | -compatible-check [--rule rule.xml] [--filter filter.xml]}");
            return;
        }
        String leftYangDir = null;
        String leftDepDir = null;
        String leftCap = null;
        for(int i = leftBeginPos;i< rightBeginPos;i++){
            String arg = args[i];
            if(arg.equalsIgnoreCase("--y")){
                leftYangDir = args[i+1];
            } else if(arg.equalsIgnoreCase("--dep")){
                leftDepDir = args[i+1];
            } else if(arg.equalsIgnoreCase("--cap")){
                leftCap = args[i+1];
            }
        }
        if(leftYangDir == null){
            System.out.println("left yang file should be specified using --y argument.");
            System.out.println("usage: -left --y {yang file or dir]} [--dep {dependency file or dir}] [--cap {capabilities.xml}]\n"
                + "       -right --y {yang file or dir]} [--dep {dependency file or dir}] [--cap {capabilities.xml}]\n"
                + "        -o {output.xml}\n"
                + "        {-tree | -stmt [--filter filter.xml] | -compatible-check [--rule rule.xml] [--filter filter.xml]}");
            return;
        }
        String rightYangDir = null;
        String rightDepDir = null;
        String rightCap = null;
        for(int i = rightBeginPos;i< outBegin;i++){
            String arg = args[i];
            if(arg.equalsIgnoreCase("--y")){
                rightYangDir = args[i+1];
            } else if(arg.equalsIgnoreCase("--dep")){
                rightDepDir = args[i+1];
            } else if(arg.equalsIgnoreCase("--cap")){
                rightCap = args[i+1];
            }
        }
        if(rightYangDir == null){
            System.out.println("right yang file should be specified using --y argument.");
            System.out.println("usage: -left --y {yang file or dir]} [--dep {dependency file or dir}] [--cap {capabilities.xml}]\n"
                + "       -right --y {yang file or dir]} [--dep {dependency file or dir}] [--cap {capabilities.xml}]\n"
                + "        -o {output.xml}\n"
                + "        {-tree | -stmt [--filter filter.xml] | -compatible-check [--rule rule.xml] [--filter filter.xml]}");
            return;
        }
        String output = args[outBegin + 1];
        File outputFile = new File(output);
        if(!outputFile.exists()){
            outputFile.createNewFile();
        }
        String compareType = args[typeBegin];
        String rule = null;
        String filter = null;
        if(compareType.equalsIgnoreCase("-compatible-check")
          || compareType.equalsIgnoreCase("-tree")){
            for(int i = typeBegin;i < args.length;i++){
                String arg = args[i];
                if(arg.equalsIgnoreCase("--rule")){
                    rule = args[i+1];
                } else if(arg.equalsIgnoreCase("--filter")){
                    filter = args[i+1];
                }
            }
        }
        YangSchemaContext leftSchemaContext = YangYinParser.parse(leftYangDir,leftDepDir,leftCap);
        ValidatorResult leftValidatorResult = leftSchemaContext.validate();

        YangSchemaContext rightSchemaContext = YangYinParser.parse(rightYangDir,rightDepDir,rightCap);
        rightSchemaContext.validate();
        if(rule != null){
            SAXReader reader = new SAXReader();
            Document document = reader.read(new File(rule));
            CompatibilityRules.getInstance().deserialize(document);
        }

        YangComparator comparator = new YangComparator(leftSchemaContext,rightSchemaContext);
        List<YangCompareResult> compareResults = new ArrayList<>();
        if(compareType.equalsIgnoreCase("-stmt")){
            compareResults = comparator.compareStatement();
            XmlWriter.writeDom4jDoc(comparator.outputXmlCompareResult(compareResults,false,CompareType.STMT),output);
        } else if (compareType.equalsIgnoreCase("-tree")){
            compareResults = CommonYangStatementComparator.compareStatements(
                getEffectiveSchemaNodeChildren(leftSchemaContext),getEffectiveSchemaNodeChildren(rightSchemaContext),false
            );
            XmlWriter.writeDom4jDoc(comparator.outputXmlCompareResult(compareResults,false,CompareType.TREE),output);
        } else if(compareType.equalsIgnoreCase("-compatible-check")){
            compareResults = CommonYangStatementComparator.compareStatements(
                leftSchemaContext.getModules(),rightSchemaContext.getModules(),false
            );
            XmlWriter.writeDom4jDoc(comparator.outputXmlCompareResult(compareResults,CompareType.COMPATIBLE_CHECK),output);
        }


    }
}
