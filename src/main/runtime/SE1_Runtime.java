package runtime;

import java.util.Properties;


public interface SE1_Runtime {

    static SE1_Runtime getInstance() {
        return runtime.impl.RuntimeSystem.getInstance();
    }

    SE1_Runtime startup(String[] args);

    Properties properties();

    Logger logger();
}
