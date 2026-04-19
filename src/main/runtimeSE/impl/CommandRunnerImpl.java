package runtimeSE.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.IntStream;

import runtimeSE.CommandRunner;
import runtimeSE.Logger;
import runtimeSE.CommandRunner.CommandRunnerInstance;
import runtimeSE.impl.KVArgsImpl.KVPair;


class CommandRunnerImpl {
    private static CommandRunnerImpl instance = null;
    private final static Logger log = Logger.getLogger(RuntimeSE_Impl.LoggerName);
    private Stack<String> tokens = new Stack<>();
    private List<KVPair> params = new ArrayList<>();
    private final StringBuilder sb = new StringBuilder();

    private CommandRunnerImpl() { }

    static CommandRunnerImpl getInstance() {
        return Optional.ofNullable(instance).orElseGet(() -> instance = new CommandRunnerImpl());
    }

    List<CommandRunnerInstance> create(List<CommandRunnerInstance> cri, CommandRunner runnable, String commands, String args) {
        cri = cri==null? new ArrayList<>() : cri;
        String[] splitArgs = commands==null? new String[] { } : commands.split(",");
        // for(String arg : args) {
        //     sb.append(arg).append(" ");
        // }
        if(args != null) {
            sb.append(args);
        }
        // 
        log.trace(String.format("%s: found splitting commands: '%s'", this.getClass().getSimpleName(), Arrays.toString(splitArgs)));
        // 
        this.tokens = tokenize(sb);
        int tokenNumber = this.tokens.size();
        this.params = parseParams(tokens).reversed();
        // 
        int[] r = { -1, -1 };   // shifting index pair
        // 
        for(var p : IntStream.range(0, params.size() + 1)
                .boxed()
                .filter(i -> i==params.size() || params.get(i).value()==null &&
                        Arrays.stream(splitArgs).anyMatch(arg ->
                            arg.trim().equalsIgnoreCase(params.get(i).key()))
                )
                .map(i -> {
                    int r0=r[0]; r[1]=r[0]; r[0]=i;     // shift indices
                    return tokenNumber==0 || i > 0? new int[] { r0, i-1 } : null;
                })
                .filter(p -> p != null)
                .toList())
        {
            boolean hasCmd = p[0] >= 0;
            boolean hasParams = p[0] < p[1];
            String cmd = hasCmd? params.get(p[0]).key() : "";
            // 
            // pass copy of params.subList(), otherwise ConcurrentModificationException is thrown
            List<KVPair> params_copy = hasParams? new ArrayList<>(params.subList(p[0] + 1, p[1] + 1)) : List.of();
            var kvargs = new KVArgsImpl(params_copy);
            // 
            log.info(String.format("%s, created runnable: %s.run(cmd='%s', args=[%s])", this.getClass().getSimpleName(), runnable.getClass().getName(), cmd, kvargs));
            // 
            cri.add(new CommandRunnerInstance(runnable, cmd, kvargs));
        }
        this.tokens.clear();
        this.params.clear();
        this.sb.setLength(0);
        return cri;
    }

    private List<KVPair> parseParams(Stack<String> tokens) {
        while( ! tokens.isEmpty()) {
            int top = tokens.size()-1;
            var e = tokens.pop();
            var e_1 = top-1 >= 0? tokens.get(top-1) : "";
            var e_2 = top-2 >= 0? tokens.get(top-2) : "";
            // 
            if( e.equals("=") && ! e_1.equals("=")) {
                // System.out.println(String.format("--> [%s %s %s]", e_1, e, "-"));
                params.add(new KVPair(e_1, ""));
                tokens.pop();
            // 
            } else if( ! e.equals("=") && e_1.equals("=") && ! e_2.equals("=")) {
                // System.out.println(String.format("--> [%s %s %s]", e_2, e_1, e));
                params.add(new KVPair(e_2, e));
                tokens.pop(); tokens.pop();
            // 
            } else {
                // System.out.println(String.format("--> [%s]", e));
                params.add(new KVPair(e, null));
            }
        }
        return params;
    }

    private enum States {COLLECT, DBLQUOTE, SGLQUOTE, BRACKETS};

    private Stack<String> tokenize(StringBuilder sb) {
        String args = sb.toString();
        sb.setLength(0);
        States state = States.COLLECT;
        for(int i=0; i <= args.length(); i++) {
            char c = i==args.length()? ' ' : args.charAt(i);
            if(c=='[' || c==']') {
                switch(state) {
                case DBLQUOTE: case SGLQUOTE: break;    // keep collecting within quotes
                case COLLECT: if(c=='[') { state = brk(state, States.BRACKETS); } break;
                case BRACKETS: if(c==']') { state = States.COLLECT; } break;
                }
                continue;   // don't append '[', ']'
            }
            if(c=='"' || c=='\'') {
                switch(state) {
                case COLLECT: state = c=='"'? brk(state, States.DBLQUOTE) : brk(state, States.SGLQUOTE); break;
                case DBLQUOTE: state = c=='"'? brk(state, States.COLLECT) : States.DBLQUOTE; break;
                case SGLQUOTE: state = c=='\''? brk(state, States.COLLECT) : States.SGLQUOTE; break;
                case BRACKETS: sb.append(c); break;
                }
            } else if(c==' ' || c=='\t' || c=='=') {
                if(state==States.DBLQUOTE || state==States.SGLQUOTE || state==States.BRACKETS) {
                    sb.append(c);   // collect white spaces within double/single quotes
                // } else if(state==States.BRACKETS) {
                } else {
                    brk(States.COLLECT, States.COLLECT);
                    if(c=='=') {
                        sb.append(c);
                        brk(States.COLLECT, States.COLLECT);
                    }
                }
            } else sb.append(c);
        }
        return this.tokens;
    }

    private States brk(States s, States next) {
        String tok = sb.toString();
        if(tok.length() > 0 || s != States.COLLECT && next==States.COLLECT /* include empty quoted String "" */) {
            // System.out.println(String.format("tok: [%s]", sb.toString()));
            tokens.push(tok);
            sb.setLength(0);
        }
        return next;
    }
}
