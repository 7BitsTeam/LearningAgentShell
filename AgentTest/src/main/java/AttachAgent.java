import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.File;
import java.util.List;


public class AttachAgent {

    public static void main(String[] args) throws Exception {

        VirtualMachine                 vm;
        List<VirtualMachineDescriptor> vmList;

        String agentFile = new File( "E:\\AgentTest\\target\\AgentTest-1.0-SNAPSHOT-jar-with-dependencies.jar").getCanonicalPath();
        System.out.println(agentFile);
        try {
            vmList = VirtualMachine.list();
            for (VirtualMachineDescriptor vmd : vmList) {
                System.out.println(vmd.displayName());

                if (vmd.displayName().contains("Main") || "".equals(vmd.displayName())) {
                    vm = VirtualMachine.attach(vmd);

                    if (null != vm) {
                        vm.loadAgent(agentFile);
                        System.out.println("MemoryShell has been injected.");
                        vm.detach();
                        return;
                    }
                }

            }

            System.out.println("No Tomcat Virtual Machine found.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}