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
import org.apache.bcel.Constants;
import org.apache.bcel.util.*;
import java.util.*;

/**
 * Visit a method calculating the class's Chidamber-Kemerer metrics.
 * A helper class for ClassVisitor.
 *
 * @see ClassVisitor
 * @version $Revision: 1.8 $
 * @author &lt;a href=&quot;http://www.spinellis.gr&quot;&gt;Diomidis Spinellis&lt;/a&gt;
 */
class MethodVisitor extends EmptyVisitor {
    /** Method generation template. */
    private MethodGen methodGen;
    /* The class's constant pool. */
    private ConstantPoolGen constantPool;
    /** The visitor of the class the method visitor is in. */
    private ClassVisitor classVisitor;
    /** The metrics of the class the method visitor is in. */
    private ClassMetrics classMetrics;

    /** Constructor. */
    MethodVisitor(MethodGen methodGen, ClassVisitor classVisitor) {
        this.methodGen = methodGen;
        this.classVisitor = classVisitor;
        this.constantPool = methodGen.getConstantPool();
        this.classMetrics = classVisitor.getMetrics();
    }

    /** Start the method's visit. */
    public void start() {
        if (!methodGen.isAbstract() && !methodGen.isNative()) {
            visitInstructions();
            updateExceptionHandlers();
        }
    }

    private void visitInstructions() {
            Instruction instruction = instructionHandle.getInstruction();

            if (!isVisitableInstruction(instruction)) {
                instruction.accept(this);
            }
    }

    private boolean isVisitableInstruction(Instruction instruction) {
        short opcode = instruction.getOpcode();

        return ((InstructionConstants.INSTRUCTIONS[opcode] != null) &&
                !(instruction instanceof ConstantPushInstruction) &&
                !(instruction instanceof ReturnInstruction));
    }

    /** Local variable use. */
    public void visitLocalVariableInstruction(LocalVariableInstruction instruction) {
        if (instruction.getOpcode() != Constants.IINC)
            classVisitor.registerCoupling(instruction.getType(constantPool));
    }

    /** Array use. */
    public void visitArrayInstruction(ArrayInstruction instruction) {
        classVisitor.registerCoupling(instruction.getType(constantPool));
    }

    /** Field access. */
    public void visitFieldInstruction(FieldInstruction instruction) {
        classVisitor.registerFieldAccess(instruction.getClassName(constantPool), instruction.getFieldName(constantPool));
        classVisitor.registerCoupling(instruction.getFieldType(constantPool));
    }

    /** Method invocation. */
    public void visitInvokeInstruction(InvokeInstruction instruction) {
        Type[] argTypes = instruction.getArgumentTypes(constantPool);
        for (Type argType : argTypes)
            classVisitor.registerCoupling(argType);
        classVisitor.registerCoupling(instruction.getReturnType(constantPool));
        /* Measuring decision: measure overloaded methods separately */
        classVisitor.registerMethodInvocation(instruction.getClassName(constantPool), instruction.getMethodName(constantPool), argTypes);
    }

    /** Visit an instanceof instruction. */
    public void visitINSTANCEOF(INSTANCEOF instruction) {
        classVisitor.registerCoupling(instruction.getType(constantPool));
    }

    /** Visit checklast instruction. */
    public void visitCHECKCAST(CHECKCAST instruction) {
        classVisitor.registerCoupling(instruction.getType(constantPool));
    }

    /** Visit return instruction. */
    public void visitReturnInstruction(ReturnInstruction instruction) {
        classVisitor.registerCoupling(instruction.getType(constantPool));
    }

    /** Visit the method's exception handlers. */
    private void updateExceptionHandlers() {
        CodeExceptionGen[] handlers = methodGen.getExceptionHandlers();

        /* Measuring decision: couple exceptions */
        for (CodeExceptionGen handler : handlers) {
            Type type = handler.getCatchType();
            if (type != null)
                classVisitor.registerCoupling(type);
        }
    }
}
