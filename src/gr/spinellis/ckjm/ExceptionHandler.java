/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author azanw
 */
package gr.spinellis.ckjm;

import org.apache.bcel.generic.*;

class ExceptionHandler {
    private final ClassVisitor classVisitor;
    private final ConstantPoolGen constantPool;

    public ExceptionHandler(ClassVisitor classVisitor, ConstantPoolGen constantPool) {
        this.classVisitor = classVisitor;
        this.constantPool = constantPool;
    }

    public void updateHandlers(CodeExceptionGen[] handlers) {
        for (CodeExceptionGen handler : handlers) {
            Type type = handler.getCatchType();
            if (type != null) {
                classVisitor.registerCoupling(type);
            }
        }
    }
}
