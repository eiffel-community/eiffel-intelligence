package com.ericsson.ei.jmespath;

import com.github.wnameless.json.flattener.JsonFlattener;

import java.util.List;
import java.util.Map;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;
import io.burt.jmespath.function.ArgumentConstraints;
import io.burt.jmespath.function.BaseFunction;
import io.burt.jmespath.function.FunctionArgument;

public class IncompletePathContainsFunction extends BaseFunction {

    public IncompletePathContainsFunction() {
        super(ArgumentConstraints.listOf(ArgumentConstraints.typeOf(JmesPathType.OBJECT),
                ArgumentConstraints.typeOf(JmesPathType.STRING), ArgumentConstraints.typeOf(JmesPathType.STRING)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * io.burt.jmespath.function.BaseFunction#callFunction(io.burt.jmespath.
     * Adapter, java.util.List)
     * 
     * Takes a JSON object and a path with value. The path can contain only
     * parts of a path or the entire path but then function is redundant since 
     * JMESPath can already handle full paths in a JSON object. 
     * For example paths a.b.c.d.e and b.d.c.d.e are valid for
     * incomplete path c.e since both elements of the incomplete path are
     * contained in the example paths.
     * 
     * But the value should also match.
     */
    @Override
    protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
        T value1 = arguments.get(0).value();
        T value2 = arguments.get(1).value();
        T value3 = arguments.get(2).value();

        String object = runtime.toString(value1);
        String path = runtime.toString(value2);
        String pathValue = runtime.toString(value3);
        String[] pathPair = path.split(":");
        String pathKey = pathPair[0];
        String[] pathParts = pathKey.split("\\.");

        Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(object);

        for (Map.Entry<String, Object> entry : flattenJson.entrySet()) {
            String entryKey = entry.getKey();
            Object entryValue = entry.getValue();
            int lastPosition = 0;
            if (entryValue != null && entryValue.equals(pathValue)) {
                for (String pathPart : pathParts) {
                    int position = entryKey.indexOf(pathPart, lastPosition);
                    if (position > lastPosition) {
                        lastPosition = position;
                    } else {
                        // reset to start no complete sequence valid
                        lastPosition = 0;
                    }
                }
                // all path parts found and in right order
                if (lastPosition > 0)
                    return runtime.createBoolean(true);
            }
        }

        return runtime.createBoolean(false);
    }

}
