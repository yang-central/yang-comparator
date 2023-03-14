package com.huawei.yang.comparator.app;

import com.huawei.yang.comparator.CompareType;
import com.huawei.yang.comparator.YangComparator;
import com.huawei.yang.comparator.YangCompareResult;
import org.dom4j.DocumentException;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;
import org.yangcentral.yangkit.utils.xml.XmlWriter;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class YangComparatorRunner {
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
            if(outputFile.getParentFile() != null && !outputFile.getParentFile().exists()){
                outputFile.getParentFile().mkdirs();
            }
            outputFile.createNewFile();
        }
        String compareTypeStr = args[typeBegin];
        String rule = null;
        String filter = null;
        if(compareTypeStr.equalsIgnoreCase("-compatible-check")
                || compareTypeStr.equalsIgnoreCase("-tree")){
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
        //System.out.println(leftValidatorResult);

        YangSchemaContext rightSchemaContext = YangYinParser.parse(rightYangDir,rightDepDir,rightCap);
        rightSchemaContext.validate();

        YangComparator comparator = new YangComparator(leftSchemaContext,rightSchemaContext);
        CompareType compareType = null;
        if(compareTypeStr.equalsIgnoreCase("-stmt")){
            compareType = CompareType.STMT;
        } else if (compareTypeStr.equalsIgnoreCase("-tree")){
            compareType = CompareType.TREE;
        } else if(compareTypeStr.equalsIgnoreCase("-compatible-check")){
            compareType = CompareType.COMPATIBLE_CHECK;
        }

        List<YangCompareResult> compareResults = comparator.compare(compareType,rule);
        boolean needCompatible = false;
        if(compareType == CompareType.COMPATIBLE_CHECK){
            needCompatible = true;
        }
        XmlWriter.writeDom4jDoc(comparator.outputXmlCompareResult(compareResults,needCompatible,compareType),output);
    }

}
