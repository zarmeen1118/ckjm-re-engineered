package gr.spinellis.ckjm;

import org.apache.bcel.generic.*;

class MethodVisitor extends EmptyVisitor {
    private final MethodGen methodGen;
    private final ClassVisitor classVisitor;
    private final InstructionVisitor instructionVisitor;
    private final ExceptionHandler exceptionHandler;

    public MethodVisitor(MethodGen methodGen, ClassVisitor classVisitor) {
        this.methodGen = methodGen;
        this.classVisitor = classVisitor;
        ConstantPoolGen constantPool = methodGen.getConstantPool();
        this.instructionVisitor = new InstructionVisitor(classVisitor, constantPool);
        this.exceptionHandler = new ExceptionHandler(classVisitor, constantPool);
    }

    public void start() {
        if (!methodGen.isAbstract() && !methodGen.isNative()) {
            visitInstructions();
            updateExceptionHandlers();
        }
    }

    private void visitInstructions() {
        for (
            InstructionHandle instructionHandle = methodGen.getInstructionList().getStart();
            instructionHandle != null;
            instructionHandle = instructionHandle.getNext()
        ) {
            Instruction instruction = instructionHandle.getInstruction();
            if (!isVisitableInstruction(instruction)) {
                instruction.accept(instructionVisitor);
            }
        }
    }

    private boolean isVisitableInstruction(Instruction instruction) {
        short opcode = instruction.getOpcode();
        return (InstructionConstants.INSTRUCTIONS[opcode] != null) &&
               !(instruction instanceof ConstantPushInstruction) &&
               !(instruction instanceof ReturnInstruction);
    }

    private void updateExceptionHandlers() {
        CodeExceptionGen[] handlers = methodGen.getExceptionHandlers();
        exceptionHandler.updateHandlers(handlers);
    }
}
