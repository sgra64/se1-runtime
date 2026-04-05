package runtime.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import runtime.CommandRunner.KVArgs;
import runtime.Logger;


class KVArgsImpl implements KVArgs {

    record KVPair(String key, String value) {

        public String toString() {
            return value==null? String.format("%s", key) : String.format("%s=%s", key, value);
        }
    }

    private final static Logger log = Logger.getLogger(RuntimeSystem.LoggerName);

    private final List<KVPair> args;    // maintain args order
    // private final java.util.Map<String, KVPair> kvMap;    // fast lookup


    KVArgsImpl(List<KVPair> args) {
        this.args = args==null? List.of() : args;
        // this.kvMap = args==null? java.util.Map.of() :
        //     args.stream().collect(java.util.stream.Collectors.toMap(kvp -> kvp.key(), v -> v));
    }

    @Override
    public String value(String key, String... altValue) {
        return findKey(key).map(p -> p.value()).orElse(altValue.length > 0? altValue[0] : null);
    }

    @Override
    public int asInt(String key, Integer... altValue) {
        try {
            String value = value(key);
            return value != null? Integer.parseInt(value) : altValue.length > 0? altValue[0] : 0;
        } catch(NumberFormatException e) { }
        return -1;
    }

    @Override
    public double asDouble(String key, Double... altValue) {
        String value = value(key);
        try {
            return value != null? Double.parseDouble(value) : altValue.length > 0? altValue[0] : 0.0;
        } catch(NumberFormatException e) { }
        return -1;
    }

    @Override
    public boolean asBoolean(String key, Boolean... altValue) {
        String value = value(key);
        return value != null? value.equalsIgnoreCase("true") : altValue.length > 0? altValue[0] : false;
    }

    @Override
    public List<String> asList(String key) {
        String value = value(key);
        return value==null? List.of() : Arrays.stream(value.split(",")).map(e -> e.trim()).toList();
    }

    @Override
    public List<Integer> asIntList(String key) {
        return asList(key).stream()
            .map(e -> {
                Integer n = null;
                try {
                    return Optional.of(Integer.parseInt(e));
                } catch(NumberFormatException ex) {
                    log.error(String.format("%s, command line argument '%s=' has element '%s' that cannot be converted to Integer",
                        ex.getClass().getSimpleName(), key, e));
                }
                return Optional.ofNullable(n);
            })
            .flatMap(Optional::stream)
            .toList();
    }

    @Override
    public boolean hasKey(String key) { return findKey(key).isPresent(); }

    @Override
    public String toString() {
        return args.stream()
            .map(kv -> String.format("%s='%s'", kv.key(), kv.value()))
            .reduce("", (a, b) -> String.format("%s%s %s", a, a.length()==0? "" : ", ", b));
    }

    private Optional<KVPair> findKey(String key) {
        var k = args.stream().filter(kvp -> kvp.key().equalsIgnoreCase(key)).findFirst();
        if( ! k.isPresent()) {
            log.warn(String.format("key '%s' is not present in command line arguments", key));
        }
        return k;
    }
}
