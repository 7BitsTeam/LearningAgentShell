import javassist.*;
import javassist.bytecode.stackmap.TypeData;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.lang.instrument.IllegalClassFormatException;

public class TransformerTest implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

        /*
        if (!className.equalsIgnoreCase("Peoples")) {
            return null;
        }
        return getBytesFromFile("E:\\AgentTest\\target\\classes\\Peoples.class");
        */

        if(!className.equalsIgnoreCase("Peoples")){
            return null;
        }


        ClassPool classPool = ClassPool.getDefault();
        classPool.appendClassPath(new LoaderClassPath(loader));
        CtClass ctClass = null;
        try {
            ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
        } catch (IOException e) {
            e.printStackTrace();
        }
        CtMethod ctm= null;
        try {
            ctm = ctClass.getDeclaredMethod("say");
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        StringBuilder codeBuilder = new StringBuilder()
                .append("System.out.println(\"world\");").append("\n")
                ;
        String beforeCode= codeBuilder.toString();
        try {
            ctm.insertAfter(beforeCode);
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }
        try {
            return ctClass.toBytecode();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] getBytesFromFile(String fileName) {
        File file = new File(fileName);
        try  {
            InputStream is = new FileInputStream(file);
            long length = file.length();
            byte[] bytes = new byte[(int) length];

            // Read in the bytes
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length
                    && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }

            if (offset < bytes.length) {
                throw new IOException("Could not completely read file "
                        + file.getName());
            }
            is.close();
            return bytes;
        } catch (Exception e) {
            System.out.println("error occurs in _ClassTransformer!"
                    + e.getClass().getName());
            return null;
        }

    }
}