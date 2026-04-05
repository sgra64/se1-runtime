package application;

import runtime.SE1_Runtime;
import runtime.Runner;
import runtime.Runner.Accessors;


@Accessors(priority=0)
public class Application implements Runner {

    public static void main(String[] args) {
        SE1_Runtime.getInstance().startup(args);
    }

    @Override
    public void run(String[] args) {
        System.out.println(String.format("Hello, %s!", this.getClass().getSimpleName()));
    }
}
