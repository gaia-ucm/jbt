*******
License
*******

JBT is released under the Apache License, Version 2.0.

****************************
What's Java Behaviour Trees?
****************************

JBT is a Java framework for building and running behaviour trees. In the past few years, behaviour trees have been widely accepted as a tool for defining the behaviour of video games characters. However, to the best of our knowledge, there is no free-software Java implementation of such concept. With JBT we intend to provide a solid framework to build and run behaviour trees in Java.

JBT has two main parts. On the one hand, there is the JBT Core (it is the Eclipse SDK project under the "./JBTCore" directory), which implements all the classes needed to create and run behaviour trees. JBT Core basically lets the user create behaviour trees in pure Java and then run them. In order to ease the task of creating behaviour trees, JBT Core includes several tools that automatize the process of creating behaviour trees. In particular, it can create the Java source code of a behaviour tree from its description in an XML format. By doing so, the user of this framework basically has to worry only about defining behaviour trees in XML files and implementing the low level actions and conditions that his trees will use, which are domain-dependant (that is, they depend on the game being played).

On the other hand, there is the JBT Editor (which is composed of two Eclipse SDK projects under the "./JBTEditor" directory). The JBT Editor is a GUI application that can be used for defining behaviour trees, and then exporting them into XML files in the format that the JBT Core understands. The JBT Editor offers a set of standard nodes for building behaviour trees. It includes nodes such as sequences, parallels, decorators, etc. For low level actions and conditions, the user can provide their conceptual definition through Make Me Play Me (MMPM) domain files (for more information on MMPM, see the Sourceforge page of the project "Darmok 2"). The JBT Editor is an Eclipse RCP application. You must use Eclipse SDK in order to run it.

JBT implements a behaviour tree model which is mainly based on that of the book "Artificial Intelligence for Games", second edition, by Ian Millington and John Funge. JBT also includes the concept of "guard" and static and dynamic priority lists, which make use of guards. JBT behaviour trees are driven by ticks, which means that, in order for them to have CPU time, they need to be externally ticked. By following this pattern, the user can control how much CPU time the behaviour tree consumes.

***********************
For more information...
***********************

For more information on JBT, see the user's guide, which is located under the directory "./UserGuide". It contains the documentation itself along with the Latex source files.

*********************
Directories structure
*********************

./Documentation: contains the project's documentation, explaining what JBT is and how the framework works. It also contains the source code of the documentation, which is written in Latex. Images are in several formats, including ODG.

./JBTCore: contains the main framework of JBT, that is, the set of classes that are needed in order to use it. However, it is encouraged to use the JBT Editor, placed in "./JBTEditor". This project is structured as an Eclipse project, so we encourage the user to use Eclipse SDK when working on it.

./JBTEditor: contains the JBT Editor, a GUI application that lets the user define behaviour trees and export them into XML files that are easily handled by JBT. When creating behaviour trees, the user should use this application. This is an Eclipse RCP application, so Eclipse SDK must be used to run it. In order to run the application, just open the file "bteditor.product" with the "Product Configuration Editor" (right click on the file, then "Open With -> Product Configuration Editor"), and in the "Overview" page, click on "Launch an Eclipse application".

./UserGuide: contains the user's guide. It also contains the source code of the user's guide, which is written in Latex. Images are in several formats, including ODG.

*******************
Binary distribution
*******************

If you just want to get the compiled version of JBT or its documentation, go to our SourceForge download page:

https://sourceforge.net/projects/jbt/files/?source=navbar

