package com.huawei.yang.comparator;

import org.dom4j.Attribute;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-06-01
 */
public class YangCompareResultXmlSerializer extends YangCompareResultSerializer<Element>{

    public YangCompareResultXmlSerializer(YangCompareResult yangCompareResult) {
        super(yangCompareResult);
    }

    private void serializeYangStatementCompareResult(YangStatementCompareResult yangStatementCompareResult,Element element,
        boolean needType,boolean needCompatibility){
        //change type
        if(needType){
            Element changeType = DocumentHelper.createElement("type");
            changeType.setText(yangStatementCompareResult.getChangeType().getName());
            element.add(changeType);
        }

        StringBuffer sb = new StringBuffer();
        sb.append("module:").append(yangStatementCompareResult.getModule().getArgStr()).append(" ");
        switch (yangStatementCompareResult.getChangeType()){
            case ADD:{
                //statement
                Attribute name = DocumentHelper.createAttribute(element,"text",
                    YangComparator.outputStatement(yangStatementCompareResult.getRight()));
                element.add(name);
                //position
                Element position = DocumentHelper.createElement("position");
                element.add(position);
                position.setText(sb.append(yangStatementCompareResult.getRight()
                    .getElementPosition().getLocation().getLocation()).toString());


                break;
            }
            case DELETE:{
                //statement
                Attribute name = DocumentHelper.createAttribute(element,"text",
                    YangComparator.outputStatement(yangStatementCompareResult.getLeft()));
                element.add(name);
                //position
                Element position = DocumentHelper.createElement("position");
                element.add(position);
                position.setText(sb.append(yangStatementCompareResult.getLeft()
                    .getElementPosition().getLocation().getLocation()).toString());
                break;
            }
            case MODIFY:{
                Attribute name = DocumentHelper.createAttribute(element,"text",
                    YangComparator.outputStatement(yangStatementCompareResult.getRight()));
                element.add(name);
                //position
                Element toPosition = DocumentHelper.createElement("position");
                element.add(toPosition);
                toPosition.setText(sb.toString() + yangStatementCompareResult.getRight().getElementPosition().getLocation().getLocation());

                Element from = DocumentHelper.createElement("previous");
                //statement
                Element fromStatement = DocumentHelper.createElement("statement");
                from.add(fromStatement);
                Attribute fromName = DocumentHelper.createAttribute(fromStatement,"text",
                        YangComparator.outputStatement(yangStatementCompareResult.getLeft()));
                fromStatement.add(fromName);
                //position
                Element fromPosition = DocumentHelper.createElement("position");
                from.add(fromPosition);
                fromPosition.setText(sb.toString() + yangStatementCompareResult.getLeft().getElementPosition().getLocation().getLocation());
                element.add(from);
                break;
            }
        }
        //compatible
        if(needCompatibility){
            Element compatibility = DocumentHelper.createElement("compatibility");
            element.add(compatibility);
            Attribute type = DocumentHelper.createAttribute(compatibility,"type",yangStatementCompareResult.getCompatibilityInfo().getCompatibility().getName());
            compatibility.add(type);
            if(yangStatementCompareResult.getCompatibilityInfo().getDescription() != null){
                Element description = DocumentHelper.createElement("description");
                compatibility.add(description);
                description.setText(yangStatementCompareResult.getCompatibilityInfo().getDescription());
            }
        }
    }

    private void serializeYangTreeCompareResult(YangTreeCompareResult yangTreeCompareResult,Element element,boolean needType,boolean needMeta,
        boolean needCompatibility){

        //change type
        if(needType){
            Element changeType = DocumentHelper.createElement("type");
            changeType.setText(yangTreeCompareResult.getChangeType().getName());
            element.add(changeType);
        }

        //schema-path
        if(yangTreeCompareResult.getSchemaPath() != null){
            Attribute link = DocumentHelper.createAttribute(element,"link",yangTreeCompareResult.getSchemaPath().toString());
            element.add(link);
        } else {
            System.out.println(yangTreeCompareResult.getChangeDescription("  "));
        }

        //position
        Element position = DocumentHelper.createElement("position");
        StringBuffer sb = new StringBuffer();
        sb.append("module:").append(yangTreeCompareResult.getModule().getArgStr()).append(" ");
        switch (yangTreeCompareResult.getChangeType()){
            case ADD:
            case MODIFY:{
                if(yangTreeCompareResult.getRight().getElementPosition() == null){
                    break;
                }
                sb.append(yangTreeCompareResult.getRight().getElementPosition().getLocation().getLocation());
                position.setText(sb.toString());
                break;
            }
            case DELETE:{
                sb.append(yangTreeCompareResult.getLeft().getElementPosition().getLocation().getLocation());
                position.setText(sb.toString());
                break;
            }
        }
        element.add(position);
        if(needCompatibility){
            //compatible
            Element compatibility = DocumentHelper.createElement("compatibility");
            element.add(compatibility);
            Attribute type = DocumentHelper.createAttribute(compatibility,"type",yangTreeCompareResult.getCompatibilityInfo().getCompatibility().getName());
            compatibility.add(type);
            if(yangTreeCompareResult.getCompatibilityInfo().getDescription() != null){
                Element description = DocumentHelper.createElement("description");
                compatibility.add(description);
                description.setText(yangTreeCompareResult.getCompatibilityInfo().getDescription());
            }
        }

        //meta difference
        if(needMeta){
            if(yangTreeCompareResult.getMetaCompareResults().size() > 0){
                Element metaDiff = DocumentHelper.createElement("metas");
                for(YangCompareResult yangCompareResult: yangTreeCompareResult.getMetaCompareResults()){
                    metaDiff.add(new YangCompareResultXmlSerializer(yangCompareResult).serialize(true,false,true,needCompatibility));
                }
                element.add(metaDiff);
            }
        }
    }
    private void serializeYangTreeMetaCompareResult(YangTreeMetaCompareResult yangTreeMetaCompareResult,Element element,
        boolean needType, boolean needPath){
        //change type
        if(needType){
            Element changeType = DocumentHelper.createElement("type");
            changeType.setText(yangTreeMetaCompareResult.getChangeType().getName());
            element.add(changeType);
        }
        if(needPath){
            Attribute link = DocumentHelper.createAttribute(element,"link",yangTreeMetaCompareResult.getTarget().getSchemaPath().toString());
            element.add(link);
        }
        //meta
        Attribute name = DocumentHelper.createAttribute(element, "name", yangTreeMetaCompareResult.getMeta());
        element.add(name);
        switch (yangTreeMetaCompareResult.getChangeType()){
            case ADD:{
                Element value = DocumentHelper.createElement("value");
                value.setText(yangTreeMetaCompareResult.getRight());
                element.add(value);
                break;
            }
            case DELETE:{
                Element value = DocumentHelper.createElement("value");
                value.setText(yangTreeMetaCompareResult.getLeft());
                element.add(value);
                break;
            }
            case MODIFY:{
                Element from = DocumentHelper.createElement("from");
                from.setText(yangTreeMetaCompareResult.getLeft());
                element.add(from);
                Element to = DocumentHelper.createElement("to");
                to.setText(yangTreeMetaCompareResult.getRight());
                element.add(to);
                break;
            }
        }
    }
    @Override
    public Element serialize() {
        return serialize(true,false,true,true);
    }

    @Override
    public Element serialize(boolean needChangeType, boolean needMetaPath, boolean needMeta,boolean needCompatibility) {
        YangCompareResult yangCompareResult = getYangCompareResult();
        Element element = null;
        if(yangCompareResult instanceof YangStatementCompareResult){
            YangStatementCompareResult yangStatementCompareResult = (YangStatementCompareResult) yangCompareResult;
            element = DocumentHelper.createElement("statement");
            serializeYangStatementCompareResult(yangStatementCompareResult,element,needChangeType,needCompatibility);
        } else if(yangCompareResult instanceof YangTreeCompareResult){
            YangTreeCompareResult yangTreeCompareResult = (YangTreeCompareResult) yangCompareResult;
            element = DocumentHelper.createElement("path");
            serializeYangTreeCompareResult(yangTreeCompareResult,element,needChangeType,needMeta,needCompatibility);
        } else if (yangCompareResult instanceof YangTreeMetaCompareResult){
            YangTreeMetaCompareResult yangTreeMetaCompareResult = (YangTreeMetaCompareResult) yangCompareResult;
            element = DocumentHelper.createElement("meta");
            serializeYangTreeMetaCompareResult(yangTreeMetaCompareResult,element,needChangeType,needMetaPath);
        }
        return element;
    }

}
