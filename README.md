# Project

###### 项目代码地址

https://github.com/yan0904/Project

###### 项目亮点

> 代码量

1. 本项目使用`java`语言进行编写，`jdk`版本`1.8`代码量为（不包括单元测试）：词法分析：730，LL语法分析器：550，LR语法分析器：740，语义分析器：480，考虑面向对象语言特性，根据功能抽象成类，使组件具有可拓展、可复用性

> 代码规范

2. 语言规范方面：本项目代码遵循`java`语言规范，并使用`IDEA`中的`SonarLint`插件对代码进行扫描，并根据扫描结果修改合规
3. 注释方面：采用`Javadoc`对类、方法、变量进行注释，并在一些复杂方法中加入注释，增强可读性

> 预测分析表实现

4. 本项目采用自动化方法构建LL预测分析表，语法规则可配置，修改语法规则后能针对新的规则进行分析

> 存储方式及使用的数据结构

5. 项目中充分考虑了数据结构的特点与变量的匹配程度，使用了多种类型的数据结构：

   + 输入`token`队列，符号队列 $\rightarrow$ 字符串链表

   + 产生式、终结符与数字映射、非终结符与数字映射 $\rightarrow$ 哈希表

   + 预测分析表  $\rightarrow$ 二维数组

   + 状态  $\rightarrow$ 堆栈

   + first集，follow集，终结符，非终结符  $\rightarrow$ 集合

   + 此外还使用了`Map`，`Pair`，`Queue`，`LinkedList`等数据结构

   + 将多种结构抽象为类如`Identifier`、`AssignExpr`，便于划分和识别

> 错误处理

6. 在Lab1中添加了额外错误处理信息

> 其他测试

7. 在Lab1中添加了其他情况时的`Junit`单元测试
8. 在Lab2中添加了关于文法是否属于LL文法的`Junit`单元测试
9. 在Lab3中添加了其他情况时的`Junit`单元测试
10. 在Lab4中添加了其他情况时的`Junit`单元测试
