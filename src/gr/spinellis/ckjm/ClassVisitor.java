/*
 * (C) Copyright 2005 Diomidis Spinellis
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package gr.spinellis.ckjm;

import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.apache.bcel.Repository;
import org.apache.bcel.Constants;
import org.apache.bcel.util.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.Modifier;

/**
 * Visit a class updating its Chidamber-Kemerer metrics.
 *
 * @see ClassMetrics
 * @version $Revision: 1.21 $
 * @author &lt;a href=&quot;http://www.spinellis.gr&quot;&gt;Diomidis Spinellis&lt;/a&gt;
 */
public class ClassVisitor extends org.apache.bcel.classfile.EmptyVisitor {
    /** The class being visited. */
    private JavaClass visitedClass;
    /** The class's constant pool. */
    private ConstantPoolGen constantPool;
    /** The class's fully qualified name. */
    private String classFullyQualifiedName;
    /** The container where metrics for all classes are stored. */
    private ClassMetricsContainer classMetricsContainer;
    /** The metrics for the class being visited. */
    private ClassMetrics classMetrics;
er>    /* Classes encountered.
     * Its cardinality is used for calculating the CBO.
     */
    private HashSet<String> efferentCoupledClasses = new HashSet<String>();
    /** Methods encountered.
     * Its cardinality is used for calculating the RFC.
     */
    private HashSet<String> responseSet = new HashSet<String>();
    /** Use of fields in methods.
     * Its contents are used for calculating the LCOM.
     * We use a Tree rather than a Hash to calculate the
     * intersection in O(n) instead of O(n*n).
     */
    ArrayList<TreeSet<String>> methodFieldUsage = new ArrayList<TreeSet<String>>();

    public ClassVisitor(JavaClass javaClass, ClassMetricsContainer classMetricsContainer) {
        visitedClass = javaClass;
        constantPool = new ConstantPoolGen(visitedClass.getConstantPool());
        this.classMetricsContainer = classMetricsContainer;
        classFullyQualifiedName = javaClass.getClassName();
        classMetrics = classMetricsContainer.getMetrics(classFullyQualifiedName);
    }

    /** Return the class's metrics container. */
    public ClassMetrics getMetrics() { return classMetrics; }

    public void start() {
        visitJavaClass(visitedClass);
    }

    /** Calculate the class's metrics based on its elements. */
    public void visitJavaClass(JavaClass javaClass) {
        String superClassName   = javaClass.getSuperclassName();
        String packageName = javaClass.getPackageName();

        classMetrics.setVisited();
        if (javaClass.isPublic())
            classMetrics.setPublic();
        ClassMetrics superClassMetrics = classMetricsContainer.getMetrics(superClassName);

        superClassMetrics.incNoc();
        try {
            classMetrics.setDit(javaClass.getSuperClasses().length);
        } catch( ClassNotFoundException ex) {
            System.err.println("Error obtaining all superclasses of " + javaClass);
        }
        registerCoupling(superClassName);

        String[] interfaceNames = javaClass.getInterfaceNames();
        /* Measuring decision: couple interfaces */
        for (int i = 0; i < interfaceNames.length; i++)
            registerCoupling(interfaceNames[i]);

        Field[] fields = javaClass.getFields();
        for(int i=0; i < fields.length; i++)
            fields[i].accept(this);

        Method[] methods = javaClass.getMethods();
        for(int i=0; i < methods.length; i++)
            methods[i].accept(this);
    }

    /** Add a given class to the classes we are coupled to */
    public void registerCoupling(String className) {
        /* Measuring decision: don't couple to Java SDK */
        if ((MetricsFilter.isJdkIncluded() ||
             !ClassMetrics.isJdkClass(className)) &&
            !classFullyQualifiedName.equals(className)) {
            efferentCoupledClasses.add(className);
            classMetricsContainer.getMetrics(className).addAfferentCoupling(classFullyQualifiedName);
        }
    }

    /* Add the type's class to the classes we are coupled to */
    public void registerCoupling(Type type) {
        registerCoupling(getClassName(type));
    }

    /* Add a given class to the classes we are coupled to */
    void registerFieldAccess(String className, String fieldName) {
        registerCoupling(className);
        if (className.equals(classFullyQualifiedName))
            methodFieldUsage.get(methodFieldUsage.size() - 1).add(fieldName);
    }

    /* Add a given method to our response set */
    void registerMethodInvocation(String className, String methodName, Type[] args) {
        registerCoupling(className);
        /* Measuring decision: calls to JDK methods are included in the RFC calculation */
        incrementRFC(className, methodName, args);
    }

    /** Called when a field access is encountered. */
    public void visitField(Field field) {
        registerCoupling(field.getType());
    }

    /** Called when encountering a method that should be included in the
        class's RFC. */
    private void incrementRFC(String className, String methodName, Type[] arguments) {
        String argumentList = Arrays.asList(arguments).toString();
        // remove [ ] chars from begin and end
        String args = argumentList.substring(1, argumentList.length() - 1);
        String signature = className + "." + methodName + "(" + args + ")";
        responseSet.add(signature);
    }

    /** Called when a method invocation is encountered. */
    public void visitMethod(Method method) {
        MethodGen methodGen = new MethodGen(method, visitedClass.getClassName(), constantPool);

        Type   resultType = methodGen.getReturnType();
        Type[] argTypes   = methodGen.getArgumentTypes();

        registerCoupling(methodGen.getReturnType());
        for (int i = 0; i < argTypes.length; i++)
            registerCoupling(argTypes[i]);

        String[] exceptions = methodGen.getExceptions();
        for (int i = 0; i < exceptions.length; i++)
            registerCoupling(exceptions[i]);

        /* Measuring decision: A class's own methods contribute to its RFC */
        incrementRFC(classFullyQualifiedName, method.getName(), argTypes);

        classMetrics.incWmc();
        if (Modifier.isPublic(method.getModifiers()))
            classMetrics.incNpm();
        methodFieldUsage.add(new TreeSet<String>());
        MethodVisitor methodVisitor = new MethodVisitor(methodGen, this);
        methodVisitor.start();
    }

    /** Return a class name associated with a type. */
    static String getClassName(Type type) {
        String typeString = type.toString();

        if (type.getType() <= Constants.T_VOID) {
            return "java.PRIMITIVE";
        } else if(type instanceof ArrayType) {
            ArrayType arrayType = (ArrayType)type;
            return getClassName(arrayType.getBasicType());
        } else {
            return typeString;
        }
    }

    /** Do final accounting at the end of the visit. */
    public void end() {
        classMetrics.setCbo(efferentCoupledClasses.size());
        classMetrics.setRfc(responseSet.size());
        /*
         * Calculate LCOM  as |P| - |Q| if |P| - |Q| > 0 or 0 otherwise
         * where
         * P = set of all empty set intersections
         * Q = set of all nonempty set intersections
         */
        int lcom = calculateLCOM();
        classMetrics.setLcom(lcom > 0 ? lcom : 0);
    }

    private int calculateLCOM() {
        int lcom = 0;
        for (int i = 0; i < methodFieldUsage.size(); i++)
            for (int j = i + 1; j < methodFieldUsage.size(); j++) {
                /* A shallow unknown-type copy is enough */
                TreeSet<?> intersection = (TreeSet<?>)methodFieldUsage.get(i).clone();
                intersection.retainAll(methodFieldUsage.get(j));
                if (intersection.size() == 0)
                    lcom++;
                else
                    lcom--;
            }
        return lcom;
    }
}
