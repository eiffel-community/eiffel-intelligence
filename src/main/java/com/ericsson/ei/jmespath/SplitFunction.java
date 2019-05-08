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

    public String[] split(String string, String separator) {
        return string.split(separator);
    }

    @Override
    protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
        // TODO Auto-generated method stub
        T value1 = arguments.get(0)
                            .value();
        T value2 = arguments.get(1)
                            .value();
        String val1 = runtime.toString(value1);
        String val2 = runtime.toString(value2);
        String[] result = split(val1, val2);

        List<JsonNode> resultList = new ArrayList<>();

        for (String val : result) {
            JsonNode valueNode = JsonNodeFactory.instance.textNode(val);
            resultList.add(valueNode);
        }
        return runtime.createArray((Collection<T>) resultList);
    }
}
