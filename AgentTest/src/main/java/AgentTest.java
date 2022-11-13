import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.Objects;

public class AgentTest {

    public static void agentmain(String agentArgs, Instrumentation inst) throws UnmodifiableClassException, ClassNotFoundException {


        Class[] classes = inst.getAllLoadedClasses();
        for(Class c : classes) {
            inst.addTransformer(new TransformerTest(), true);
            System.out.println("add class success");
            inst.retransformClasses(c);
            System.out.println("retransform success");
        }


        /*
        Class[] classes = inst.getAllLoadedClasses();
        for(Class c : classes) {
            System.out.println("searching");
            System.out.println(c.getName());
            if (c.getName().equalsIgnoreCase("Peoples")) {
                ClassDefinition def = new ClassDefinition(c, Objects.requireNonNull(TransformerTest
                        .getBytesFromFile("E:\\AgentTest\\target\\classes\\Peoples.class")));
                inst.redefineClasses(new ClassDefinition[]{def});
                System.out.println("redefineClasses success");
            }
        }*/



    }
}