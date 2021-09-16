package xyz.phanta.givemebackmyhp;

import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.*;

// for some reason, the PlayerEvent.LoadFromFile offered by forge does not expose the actual player NBT tag being loaded
// so we have to inject our own hook to deal with it :I
public class GiveMeBackMyHpClassTransformer implements IClassTransformer {

    private static final Logger LOGGER = LogManager.getLogger("GiveMeBackMyHp-Core");

    @Override
    public byte[] transform(String name, String transformedName, byte[] code) {
        if (!transformedName.equals("net.minecraft.entity.player.EntityPlayerMP")) {
            return code;
        }
        LOGGER.info("Injecting player deserialization hook...");
        ClassReader reader = new ClassReader(code);
        ClassWriter writer = new ClassWriter(reader, 0);
        reader.accept(new TransformClassEntityPlayerMP(Opcodes.ASM5, writer), 0);
        return writer.toByteArray();
    }

    private static class TransformClassEntityPlayerMP extends ClassVisitor {

        public TransformClassEntityPlayerMP(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (name.equals("func_70037_a") || name.equals("readEntityFromNBT")) {
                return new TransformMethodReadEntityFromNBT(
                        api, super.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

    }

    private static class TransformMethodReadEntityFromNBT extends MethodVisitor {

        public TransformMethodReadEntityFromNBT(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode == Opcodes.RETURN) {
                super.visitFieldInsn(Opcodes.GETSTATIC, "xyz/phanta/givemebackmyhp/GiveMeBackMyHp", "INSTANCE",
                        "Lxyz/phanta/givemebackmyhp/GiveMeBackMyHp;");
                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitVarInsn(Opcodes.ALOAD, 1);
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "xyz/phanta/givemebackmyhp/GiveMeBackMyHp", "onPlayerDeserialize",
                        "(Lnet/minecraft/entity/player/EntityPlayerMP;Lnet/minecraft/nbt/NBTTagCompound;)V", false);
                LOGGER.info("Hook injected successfully!");
            }
            super.visitInsn(opcode);
        }

    }

}
