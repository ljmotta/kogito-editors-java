package org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack;

import org.jboss.errai.ioc.rebind.ioc.codegen.*;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.UndefinedMethodException;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class UnresolvedMethodCall extends AbstractCallElement {
    private String methodName;
    private Object[] parameters;

    public UnresolvedMethodCall(String methodName, Object[] parameters) {
        this.methodName = methodName;
        this.parameters = parameters;
    }

    public void handleCall(CallWriter writer, Context context, Statement statement) {
        CallParameters callParams = CallParameters.fromStatements(GenUtil.generateCallParameters(context, parameters));

        MetaClass[] parameterTypes = callParams.getParameterTypes();
        MetaMethod method = statement.getType().getBestMatchingMethod(methodName, parameterTypes);
        if (method == null)
            throw new UndefinedMethodException(methodName, parameterTypes);

        statement = new MethodInvocation(method, callParams);

        nextOrReturn(writer, context, statement);
    }
}
