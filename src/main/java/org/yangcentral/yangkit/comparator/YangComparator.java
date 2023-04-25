package org.yangcentral.yangkit.comparator;

import org.yangcentral.yangkit.base.YangElement;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.common.api.Namespace;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.*;

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

    public static List<SchemaNode> getModuleTreeNodes(MainModule module){
        List<SchemaNode> effectiveSchemaNodeChildren = new ArrayList<>();
        effectiveSchemaNodeChildren.addAll(getEffectiveSchemaNodeChildren(module));
        for(Augment augment: module.getAugments()){
            effectiveSchemaNodeChildren.addAll(getEffectiveSchemaNodeChildren(augment));
        }
        return effectiveSchemaNodeChildren;
    }
    public static List<SchemaNode> getEffectiveSchemaNodeChildren (SchemaNodeContainer schemaNodeContainer){
        List<SchemaNode> effectiveSchemaNodeChildren = new ArrayList<>();
        for(SchemaNode schemaNode:schemaNodeContainer.getSchemaNodeChildren()){
            if(schemaNode instanceof VirtualSchemaNode){
                VirtualSchemaNode virtualSchemaNode = (VirtualSchemaNode) schemaNode;
                effectiveSchemaNodeChildren.addAll(getEffectiveSchemaNodeChildren(virtualSchemaNode));
            } else {
                if(schemaNodeContainer instanceof YangStatement){
                    YangStatement parent = (YangStatement) schemaNodeContainer;
                    Namespace parentNs = parent.getContext().getNamespace();
                    Namespace childNs = schemaNode.getContext().getNamespace();
                    if(parentNs != null && childNs != null && parentNs.getUri().equals(childNs.getUri())){
                        effectiveSchemaNodeChildren.add(schemaNode);
                    }
                } else {
                    effectiveSchemaNodeChildren.add(schemaNode);
                }
            }


        }
        return effectiveSchemaNodeChildren;
    }

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
                YangStatement matchedRightSubStatement = CommonYangStatementComparator.searchStatement(leftSubStatement,rightSubStatements,foundStatements);
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
                if(CommonYangStatementComparator.contains(foundStatements,rightSubElement)){
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
            if(CommonYangStatementComparator.contains(foundModules,rightModule)){
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
    public Document outputXmlCompareResult(List<YangCompareResult> compareResults,CompareType compareType){
        return outputXmlCompareResult(compareResults,true,compareType);
    }
    public Document outputXmlCompareResult(List<YangCompareResult> compareResults,boolean needCompatible,CompareType compareType){
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

    public List<YangCompareResult> compare(CompareType compareType,String rule) throws DocumentException {
        if(rule != null){
            SAXReader reader = new SAXReader();
            Document document = reader.read(new File(rule));
            CompatibilityRules.getInstance().deserialize(document);
        }
        List<YangCompareResult> compareResults = new ArrayList<>();
        if(compareType == CompareType.STMT){
            compareResults = compareStatement();

        } else if (compareType == CompareType.TREE){
            List<Module> matched = new ArrayList<>();
            for(Module leftModule:leftContext.getModules()){
                if(!(leftModule instanceof MainModule)){
                    continue;
                }
                List<SchemaNode> leftSchemaNodes = getModuleTreeNodes((MainModule) leftModule);
                Module matchedModule = null;
                for(Module rightModule:rightContext.getModules()){
                    if(rightModule.getArgStr().equals(leftModule.getArgStr())){
                        matched.add(rightModule);
                        matchedModule = rightModule;
                        break;
                    }
                }
                List<SchemaNode> rightSchemaNodes = new ArrayList<>();
                if(null != matchedModule){
                    rightSchemaNodes.addAll(getModuleTreeNodes((MainModule) matchedModule));
                }
                compareResults.addAll(CommonYangStatementComparator.compareStatements(leftSchemaNodes,rightSchemaNodes
                        ,CommonYangStatementComparator.OPTION_ONLY_SCHEMA));
            }
            for(Module rightModule:rightContext.getModules()){
                if(matched.contains(rightModule)){
                    continue;
                }
                if(!(rightModule instanceof MainModule)){
                    continue;
                }
                compareResults.addAll(CommonYangStatementComparator.compareStatements(new ArrayList<>(),getModuleTreeNodes(
                                (MainModule) rightModule)
                        ,CommonYangStatementComparator.OPTION_ONLY_SCHEMA));
            }
        } else if(compareType == CompareType.COMPATIBLE_CHECK){
            compareResults = CommonYangStatementComparator.compareStatements(
                    leftContext.getModules(),rightContext.getModules(),CommonYangStatementComparator.OPTION_ALL
            );
        }
        return compareResults;
    }




}
