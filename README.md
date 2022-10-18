# yang-comparator
yang comparator is a tool which can compare two versions of yang releases. It can help users to identify the differences of the two versions.

Yang comparator provides three main functions:compare statements, compare tree and check the compatibility between two versions.

### compare statements
compare the statements of the two yang release versions. It will identify the statements which are added,changed, or deleted for every yang files between the previous version and current version.
These differences are all textual differences , not the effective differences. 
For example:

previous statements:
    
leaf foo {
      
      type string;
}
    
   current statements:

leaf foo {

      type string;
      mandatory false;
}
    
The difference will be "mandatory false" is added, although the previous leaf foo is 'mandatory false' by default.

### compare tree
  compare the schema tree of the two yang release versions. It will identify which schema node paths are added,changed or deleted, and which schema node paths are changed to be deprecated or obsolete.
  These differences are effective differences. 
  All config  are missed will be treated to be default value, 
  all status are missed will be treated to be current, 
  all mandatory are missed will be treated to be false,
  all min-elements are missed will be treated to be 0, 
  all max-elements are missed will be treated to be unbounded, 
  all ordered-by are missed will be treated to be system,etc.

  For example:

  previous statements:

  leaf foo {

      type string;

  }

  current statements:

  leaf foo {

      type string;
      mandatory true;

  }

  The difference will be changed from false to true , although the previous statements have no mandatory statement.
### check compatibility
  This function will output the compatibility results after comparing the two yang release versions. 
  It allows users to define their own compatible-check rules, if no user's rule is provide, it will use default rule.
  
  compatible-check rule is an XML file, every rule MUST be defined by the XML tags listed below:

* rules: the root element of the rule XML file.
* rule: a container for rule, it will define a compatibility rule.
* rule-id: the identifier for a rule.
* statements: the statements what the rule is applied.
* statement: the statement what the rule is applied.
* condition: the change type of statement to be matched. The change type maybe the values listed below:
    1. [x] added: any sub statement is added.
    2. [x] deleted: any sub statement is deleted.
    3. [x] changed: the meaning has been changed,for example, builtin-type changed for type,value changed for enumeration.
    4. [x] mandatory-added: add mandatory schema node.
    5. [x] sequence-changed: sequence-changed.
    6. [x] expand: expand the scope, for range,it means larger range, for length, it means larger length, for fraction-digits,
         it means a lower value, for min-elements, it means a lower value, for max-elements, it means a higher value,
         for mandatory, it means from true to false, for config, it means from false to true
         for unique, it means one or more attributes are deleted.
    7. [x] reduce: reduce the scope, for range,it means smaller range, for length, it means smaller length, for fraction-digits,
       it means a higher value, for min-elements, it means a higher value, for max-elements, it means a lower value,
       for mandatory, it means from false to true, for config, it means from true to false,
       for unique, it means new attributes are added.
    8. [x] integer-type-changed: for example type from int8 to int16,it is treated non-backward-compatible by default.
    9. [x] any: match any changes
    10. [x] ignore: ignore any changes, it means backward-compatibility for any changes.
  
* except-condition: condition what will be not matched.
* compatible: compatibility conclusion, non-backward-compatible or backward-compatible are accepted value.
* description: the description of this rule.

## Installation
### Prerequisites
* JDK or JRE 1.8 or above

### Obtain code
git clone https://github.com/HuaweiDatacomm/yang-comparator.git

### build code
mvn clean install

it will generate yang-comparator-1.0-SNAPSHOT.jar and libs directory under the directory target

copy yang-comparator-1.0-SNAPSHOT.jar and libs to anywhere in your computer.

## Usage:
java -jar yang-comparator-1.0-SNAPSHOT.jar _arguments_

arguments:

-left --y {yang file or dir]} [--dep {dependency file or dir}] [--cap {capabilities.xml} ]

-right --y {yang file or dir]} [--dep {dependency file or dir}] [--cap {capabilities.xml}]

-o {output file, xml format} 

{-tree | -stmt | -compatible-check [--rule rule.xml ]}

### Example:
download 8.20.10 and 8.21.0 versions yang files of network-router from https://github.com/Huawei/yang
 and copy to example/yang

cd example and execute the commands:

get statement difference:

java -jar yang-comparator-1.0-SNAPSHOT.jar -left --y yang/8.20.10 -right --y yang/8.21.0 -o out/diff_stmt.xml -stmt

get schema node path difference:

java -jar yang-comparator-1.0-SNAPSHOT.jar -left --y yang/8.20.10 -right --y yang/8.21.0 -o out/diff_tree.xml -tree

get compatibility result:

java -jar yang-comparator-1.0-SNAPSHOT.jar -left --y yang/8.20.10 -right --y yang/8.21.0 -o out/compatibility.xml -compatible-check

get compatibility with rule result:

java -jar yang-comparator-1.0-SNAPSHOT.jar -left --y yang/8.20.10 -right --y yang/8.21.0 -o out/compatibility_rule.xml -compatible-check --rule rules.xml





    
