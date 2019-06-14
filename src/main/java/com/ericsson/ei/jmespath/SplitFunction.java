package com.ericsson.ei.jmespath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;
import io.burt.jmespath.function.ArgumentConstraints;
import io.burt.jmespath.function.BaseFunction;
import io.burt.jmespath.function.FunctionArgument;

public class SplitFunction extends BaseFunction {

    public SplitFunction() {
        super(ArgumentConstraints.listOf(ArgumentConstraints.typeOf(JmesPathType.STRING),
                ArgumentConstraints.typeOf(JmesPathType.STRING)));
    }

    /**
     * This method separate a string parts based on a give separator. It is used for
     * extracting component of GAV from purl
     *
     * @param string
     *            A given string, in this case purl
     * @param separator
     *            Any separator pattern
     * @return string array
     */
    public String[] split(String string, String separator) {
        return string.split(separator);
    }

    /*
     * (non-Javadoc)
     *
     * @see io.burt.jmespath.function.BaseFunction#callFunction(io.burt.jmespath.
     * Adapter, java.util.List)
     *
     * Takes a string and separator string. Then apply the separator to separate
     * string and returns an arrays of separated jsonNode(s) for a match
     */
    @Override
    protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
        T string_arg = arguments.get(0).value();
        T separator_arg = arguments.get(1).value();
        String string = runtime.toString(string_arg);
        String separator = runtime.toString(separator_arg);
        String[] result = split(string, separator);

        List<JsonNode> resultList = new ArrayList<>();

        for (String val : result) {
            JsonNode valueNode = JsonNodeFactory.instance.textNode(val);
            resultList.add(valueNode);
        }
        return runtime.createArray((Collection<T>) resultList);
    }
}
