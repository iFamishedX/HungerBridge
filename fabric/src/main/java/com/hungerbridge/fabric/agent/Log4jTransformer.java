package com.hungerbridge.fabric.agent;

import com.hungerbridge.fabric.OutputCapture;
import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class Log4jTransformer implements ClassFileTransformer {

    private static final String TARGET_CLASS = "org/apache/logging/log4j/core/Logger";
    private static final String TARGET_METHOD = "callAppenders";
    private static final String TARGET_DESC = "(Lorg/apache/logging/log4j/core/LogEvent;)V";

    @Override
    public byte[] transform(
            Module module,
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classfileBuffer
    ) {
        if (!TARGET_CLASS.equals(className)) {
            return null; // don't touch other classes
        }

        try {
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

            ClassVisitor cv = new ClassVisitor(Opcodes.ASM9, cw) {
                @Override
                public MethodVisitor visitMethod(
                        int access,
                        String name,
                        String descriptor,
                        String signature,
                        String[] exceptions
                ) {
                    MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                    if (name.equals(TARGET_METHOD) && descriptor.equals(TARGET_DESC)) {
                        return new MethodVisitor(Opcodes.ASM9, mv) {
                            @Override
                            public void visitCode() {
                                // Inject at method head:
                                // if (OutputCapture.isActive()) {
                                //   OutputCapture.add(event.getMessage().getFormattedMessage());
                                //   return;
                                // }

                                // Load LogEvent (arg0)
                                super.visitVarInsn(Opcodes.ALOAD, 1);

                                // Call OutputCapture.isActive()
                                super.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        "com/hungerbridge/fabric/OutputCapture",
                                        "isActive",
                                        "()Z",
                                        false
                                );

                                Label continueLabel = new Label();

                                // if (!isActive) goto continueLabel
                                super.visitJumpInsn(Opcodes.IFEQ, continueLabel);

                                // event.getMessage()
                                super.visitVarInsn(Opcodes.ALOAD, 1);
                                super.visitMethodInsn(
                                        Opcodes.INVOKEINTERFACE,
                                        "org/apache/logging/log4j/core/LogEvent",
                                        "getMessage",
                                        "()Lorg/apache/logging/log4j/message/Message;",
                                        true
                                );

                                // .getFormattedMessage()
                                super.visitMethodInsn(
                                        Opcodes.INVOKEINTERFACE,
                                        "org/apache/logging/log4j/message/Message",
                                        "getFormattedMessage",
                                        "()Ljava/lang/String;",
                                        true
                                );

                                // OutputCapture.add(String)
                                super.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        "com/hungerbridge/fabric/OutputCapture",
                                        "add",
                                        "(Ljava/lang/String;)V",
                                        false
                                );

                                // return;
                                super.visitInsn(Opcodes.RETURN);

                                // continueLabel:
                                super.visitLabel(continueLabel);

                                super.visitCode();
                            }
                        };
                    }
                    return mv;
                }
            };

            cr.accept(cv, 0);
            byte[] transformed = cw.toByteArray();
            System.out.println("[HungerBridgeAgent] Transformed Logger.callAppenders");
            return transformed;
        } catch (Throwable t) {
            t.printStackTrace();
            return null; // fall back to original class
        }
    }
}
