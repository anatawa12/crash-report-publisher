package com.anatawa12.crashReportPublisher;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;

public class CrashReportPublisherClassTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (!"net.minecraft.crash.CrashReport".equals(transformedName)) return basicClass;
        ClassReader reader = new ClassReader(basicClass);
        ClassWriter cw = new ClassWriter(0);
        reader.accept(new ClassTransformer(cw), 0);
        return cw.toByteArray();
    }

    // mapping: func_147149_a:saveToFile
    // mapping: func_71502_e:getCompleteReport
    // mapping: field_71510_d:crashReportFile

    static class ClassTransformer extends ClassVisitor {
        ClassTransformer (ClassVisitor cv) {
            super(Opcodes.ASM5, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if ("(Ljava/io/File;)Z".equals(desc)) {
                if ("func_147149_a".equals(name))
                    return new MethodTransformer(super.visitMethod(access, name, desc, signature, exceptions), true);
                else if ("saveToFile".equals(name))
                    return new MethodTransformer(super.visitMethod(access, name, desc, signature, exceptions), false);
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
    }

    static class MethodTransformer extends MethodVisitor {
        final boolean isObf;

        MethodTransformer(MethodVisitor mv, boolean isObf) {
            super(Opcodes.ASM5, mv);
            this.isObf = isObf;
        }

        private String obf(String srg, String mcp) {
            return isObf ? srg : mcp;
        }

        @Override
        public void visitCode() {
            super.visitCode();
            visitVarInsn(Opcodes.ALOAD, 0);
            visitFieldInsn(
                Opcodes.GETFIELD,
                "net/minecraft/crash/CrashReport",
                obf("field_71510_d", "crashReportFile"),
                "Ljava/io/File;"
            );
            visitVarInsn(Opcodes.ALOAD, 1);
            visitVarInsn(Opcodes.ALOAD, 0);
            visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "net/minecraft/crash/CrashReport",
                obf("func_71502_e", "getCompleteReport"),
                "()Ljava/lang/String;",
                false
            );
            visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "com/anatawa12/crashReportPublisher/CrashReportPublisherTweaker",
                "onSaveToFile",
                "(Ljava/io/File;Ljava/io/File;Ljava/lang/String;)V",
                false
            );
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            super.visitMaxs(Math.max(maxStack, 3), maxLocals);
        }
    }
}
