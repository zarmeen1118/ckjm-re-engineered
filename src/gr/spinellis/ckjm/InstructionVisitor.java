/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gr.spinellis.ckjm;
import org.apache.bcel.Constants;


import org.apache.bcel.generic.*;

class InstructionVisitor extends EmptyVisitor {
    private final ClassVisitor classVisitor;
    private final ConstantPoolGen constantPool;

    public InstructionVisitor(ClassVisitor classVisitor, ConstantPoolGen constantPool) {
        this.classVisitor = classVisitor;
        this.constantPool = constantPool;
    }

    @Override
    public void visitLocalVariableInstruction(LocalVariableInstruction instruction) {
        if (instruction.getOpcode() != Constants.IINC)
            classVisitor.registerCoupling(instruction.getType(constantPool));
    }

    @Override
    public void visitArrayInstruction(ArrayInstruction instruction) {
        classVisitor.registerCoupling(instruction.getType(constantPool));
    }

    @Override
    public void visitFieldInstruction(FieldInstruction instruction) {
        classVisitor.registerFieldAccess(instruction.getClassName(constantPool), instruction.getFieldName(constantPool));
        classVisitor.registerCoupling(instruction.getFieldType(constantPool));
    }

    @Override
    public void visitInvokeInstruction(InvokeInstruction instruction) {
        Type[] argTypes = instruction.getArgumentTypes(constantPool);
        for (Type argType : argTypes)
            classVisitor.registerCoupling(argType);
        classVisitor.registerCoupling(instruction.getReturnType(constantPool));
        classVisitor.registerMethodInvocation(instruction.getClassName(constantPool), instruction.getMethodName(constantPool), argTypes);
    }

    @Override
    public void visitINSTANCEOF(INSTANCEOF instruction) {
        classVisitor.registerCoupling(instruction.getType(constantPool));
    }

    @Override
    public void visitCHECKCAST(CHECKCAST instruction) {
        classVisitor.registerCoupling(instruction.getType(constantPool));
    }

    @Override
    public void visitReturnInstruction(ReturnInstruction instruction) {
        classVisitor.registerCoupling(instruction.getType(constantPool));
    }
}

