package com.anatawa12.crashReportPublisher

import net.minecraft.launchwrapper.IClassTransformer
import org.objectweb.asm.*

class CrashReportPublisherClassTransformer : IClassTransformer {
    override fun transform(name: String?, transformedName: String?, basicClass: ByteArray?): ByteArray? {
        if (transformedName != "net.minecraft.crash.CrashReport") return basicClass
        val reader = ClassReader(basicClass!!)
        val cw = ClassWriter(0)
        reader.accept(ClassTransformer(cw), 0)
        return cw.toByteArray()
    }

    // mapping: func_147149_a:saveToFile
    // mapping: func_71502_e:getCompleteReport
    // mapping: field_71510_d:crashReportFile

    class ClassTransformer(cv: ClassVisitor?) : ClassVisitor(Opcodes.ASM5, cv) {
        override fun visitMethod(
            access: Int,
            name: String,
            desc: String,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor {
            if (desc == "(Ljava/io/File;)Z") {
                if (name == "func_147149_a")
                    return MethodTransformer(super.visitMethod(access, name, desc, signature, exceptions), true)
                else if (name == "saveToFile")
                    return MethodTransformer(super.visitMethod(access, name, desc, signature, exceptions), false)
            }
            return super.visitMethod(access, name, desc, signature, exceptions)
        }
    }

    class MethodTransformer(
        mv: MethodVisitor?,
        val isObf: Boolean
    ) : MethodVisitor(Opcodes.ASM5, mv) {
        private fun obf(srg: String, mcp: String) = if (isObf) srg else mcp

        override fun visitCode() {
            super.visitCode()
            visitVarInsn(Opcodes.ALOAD, 0)
            visitFieldInsn(
                Opcodes.GETFIELD,
                "net/minecraft/crash/CrashReport",
                obf("field_71510_d", "crashReportFile"),
                "Ljava/io/File;"
            )
            visitVarInsn(Opcodes.ALOAD, 1)
            visitVarInsn(Opcodes.ALOAD, 0)
            visitInvokeDynamicInsn(
                "get",
                "(Lnet/minecraft/crash/CrashReport;)Ljava/util/function/Supplier;",
                Handle(
                    Opcodes.H_INVOKESTATIC,
                    "java/lang/invoke/LambdaMetafactory",
                    "metafactory",
                    "(Ljava/lang/invoke/MethodHandles\$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;L" +
                            "java/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;" +
                            ")Ljava/lang/invoke/CallSite;",
                ),
                Type.getType("()Ljava/lang/Object;"),
                Handle(
                    Opcodes.H_INVOKEVIRTUAL,
                    "net/minecraft/crash/CrashReport",
                    obf("func_71502_e", "getCompleteReport"),
                    "()Ljava/lang/String;",
                ),
                Type.getType("()Ljava/lang/String;"),
            )
            visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "com/anatawa12/crashReportPublisher/CrashReportPublisherTweaker",
                "onSaveToFile",
                "(Ljava/io/File;Ljava/io/File;Ljava/util/function/Supplier;)V",
                false,
            )
        }

        override fun visitMaxs(maxStack: Int, maxLocals: Int) {
            super.visitMaxs(maxOf(maxStack, 3), maxLocals)
        }
    }
}
