package com.huawei.yang.comparator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.yangcentral.yangkit.compiler.Settings;
import org.yangcentral.yangkit.compiler.YangCompiler;
import org.yangcentral.yangkit.compiler.YangCompilerException;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.plugin.YangCompilerPlugin;
import org.yangcentral.yangkit.plugin.YangCompilerPluginParameter;
import org.yangcentral.yangkit.utils.file.FileUtil;
import org.yangcentral.yangkit.utils.xml.XmlWriter;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class YangComparatorPlugin implements YangCompilerPlugin {
    @Override
    public YangCompilerPluginParameter getParameter(Properties properties, String name, String value) throws YangCompilerException {
        if(!name.equals("old-yang")&& !name.equals("settings")
                && !name.equals("compare-type") && !name.equals("rule")
                && !name.equals("result")) {
            throw new YangCompilerException("unrecognized parameter:"+ name);
        }
        YangCompilerPluginParameter yangCompilerPluginParameter = new YangCompilerPluginParameter() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public Object getValue() throws YangCompilerException {
                if(name.equals("old-yang")|| name.equals("settings")
                        || name.equals("rule") || name.equals("result")){
                    Iterator<Map.Entry<Object,Object>> it = properties.entrySet().iterator();
                    String formatStr = value;
                    while (it.hasNext()){
                        Map.Entry<Object,Object> entry = it.next();
                        formatStr = formatStr.replaceAll("\\{"+entry.getKey()+"\\}", (String) entry.getValue());
                    }
                    return formatStr;
                }

                if(name.equals("compare-type")){
                    if(value.equals("stmt")){
                        return CompareType.STMT;
                    } else if(value.equals("tree")){
                        return CompareType.TREE;
                    } else if(value.equals("compatible-check")){
                        return CompareType.COMPATIBLE_CHECK;
                    }
                    throw new YangCompilerException("unrecognized value:"+value);
                }
                return null;
            }

        };
        return yangCompilerPluginParameter;
    }

    @Override
    public void run(YangSchemaContext yangSchemaContext, Settings settings,List<YangCompilerPluginParameter> list) throws YangCompilerException {
        YangCompiler yangCompiler = new YangCompiler();
        yangCompiler.setSettings((settings==null)?new Settings():settings);
        CompareType compareType = null;
        String oldYangPath = null;
        String rulePath = null;
        String resultPath = null;
        for(YangCompilerPluginParameter parameter:list){
            //System.out.println("para name="+parameter.getName() + " para value="+parameter.getValue());
            if(parameter.getName().equals("old-yang")){
                oldYangPath = (String) parameter.getValue();
                yangCompiler.setYang(new File(oldYangPath));
            } else if(parameter.getName().equals("settings")){
                String settingsPath = (String) parameter.getValue();
                try {
                    yangCompiler.setSettings(Settings.parse(FileUtil.readFile2String(settingsPath)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if(parameter.getName().equals("compare-type")){
                compareType = (CompareType) parameter.getValue();
            } else if(parameter.getName().equals("rule")){
                rulePath = (String) parameter.getValue();
            } else if(parameter.getName().equals("result")){
                resultPath = (String) parameter.getValue();
            }

        }
        if(oldYangPath == null){
            throw new YangCompilerException("missing mandatory parameter:old-yang");
        }
        if(compareType == null){
            throw new YangCompilerException("missing mandatory parameter:compare-type");
        }
        if(resultPath == null){
            throw new YangCompilerException("missing mandatory parameter:result");
        }
        YangSchemaContext oldSchemaContext = yangCompiler.buildSchemaContext();
        oldSchemaContext.validate();
        //System.out.println(oldSchemaContext.getValidateResult());
        YangComparator yangComparator = new YangComparator(oldSchemaContext,yangSchemaContext);
        try {
            List<YangCompareResult> results = yangComparator.compare(compareType,rulePath);
            boolean needCompatible = false;
            if(compareType == CompareType.COMPATIBLE_CHECK){
                needCompatible = true;
            }
            Document document = yangComparator.outputXmlCompareResult(results,needCompatible,compareType);
            //System.out.println(XmlWriter.transDom4jDoc2String(document));
            XmlWriter.writeDom4jDoc(document,resultPath);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        } catch (RuntimeException e){
            for(StackTraceElement traceElement:e.getStackTrace()){
                System.out.println(traceElement);
            }

        }

    }
}
