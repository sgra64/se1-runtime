package application;

import java.util.Arrays;
import java.util.List;

import runtime.CommandRunner;
import runtime.Runner;
import runtime.Runner.Accessors;


@Accessors(priority=1)
public class Demo_1 implements Runner, CommandRunner {

    // @lombok.Getter
    // @lombok.AllArgsConstructor
    // @lombok.experimental.Accessors(fluent=true)
    // public static class XYZ {
    //     private int x;
    //     private int y;
    //     private int z;
    // }

    @Override
    public void run(String[] args) {
        // 
        System.out.println(String.format("Hello, %s! -- args: %s", this.getClass().getSimpleName(),
            Arrays.stream(args)
                // .map(a -> String.format("[%s]", a))
                .reduce("", (a, b) -> String.format("%s%s%s", a, (a.length() > 0? ", " : ""), b))));
        // 
        // for(var cri : CommandRunner.create(this, "sum, findFirst, yellow, green, red", args)) {
        //     this.run(cri.cmd(), cri.kvargs());
        // }
        CommandRunner.run(this, "sum, findFirst, yellow, green, red", args);
    }

    @Override
    public void run(String command, KVArgs kvargs) {
        // 
        System.out.println(String.format(" --> '%s' -- args: %s", command, kvargs));
        // 
        String x = kvargs.value("x");
        int y = kvargs.asInt("y");
        List<Integer> z = kvargs.asIntList("z");
        // 
        System.out.println(String.format("x: %s", x));
        System.out.println(String.format("y: %d", y));
        System.out.println(String.format("z: %s, sum: %d", z, z.stream().reduce(0, (a, b) -> a + b)));
    }
}
