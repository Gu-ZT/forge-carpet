package net.cjsah.mod.carpet.script.language;

import net.cjsah.mod.carpet.script.Expression;
import net.cjsah.mod.carpet.script.argument.FunctionArgument;
import net.cjsah.mod.carpet.script.exception.ExitStatement;
import net.cjsah.mod.carpet.script.exception.InternalExpressionException;
import net.cjsah.mod.carpet.script.value.BooleanValue;
import net.cjsah.mod.carpet.script.value.NumericValue;
import net.cjsah.mod.carpet.script.value.ThreadValue;
import net.cjsah.mod.carpet.script.value.Value;

public class Threading
{
    public static void apply(Expression expression)
    {
        expression.addFunctionWithDelegation("task", -1, false, false, (c, t, expr, tok, lv) ->
        {
            if (lv.size() == 0)
                throw new InternalExpressionException("'task' requires at least function to call as a parameter");
            FunctionArgument functionArgument = FunctionArgument.findIn(c, expression.module, lv, 0, false, true);
            ThreadValue thread = new ThreadValue(Value.NULL, functionArgument.function, expr, tok, c, functionArgument.checkedArgs());
            Thread.yield();
            return thread;
        });

        expression.addFunctionWithDelegation("task_thread", -1, false, false, (c, t, expr, tok, lv) ->
        {
            if (lv.size() < 2)
                throw new InternalExpressionException("'task' requires at least function to call as a parameter");
            Value queue = lv.get(0);
            FunctionArgument functionArgument = FunctionArgument.findIn(c, expression.module, lv, 1, false, true);
            ThreadValue thread = new ThreadValue(queue, functionArgument.function, expr, tok, c, functionArgument.checkedArgs());
            Thread.yield();
            return thread;
        });


        expression.addContextFunction("task_count", -1, (c, t, lv) ->
                (lv.size() > 0)? new NumericValue(c.host.taskCount(lv.get(0))):new NumericValue(c.host.taskCount()));

        expression.addUnaryFunction("task_value", (v) ->
        {
            if (!(v instanceof ThreadValue))
                throw new InternalExpressionException("'task_value' could only be used with a task value");
            return ((ThreadValue) v).getValue();
        });

        expression.addUnaryFunction("task_join", (v) ->
        {
            if (!(v instanceof ThreadValue))
                throw new InternalExpressionException("'task_join' could only be used with a task value");
            return ((ThreadValue) v).join();
        });

        expression.addLazyFunction("task_dock", 1, (c, t, lv) ->
        {
            // pass through placeholder
            // implmenetation should dock the task on the main thread.
            return lv.get(0);
        });

        expression.addUnaryFunction("task_completed", (v) ->
        {
            if (!(v instanceof ThreadValue))
                throw new InternalExpressionException("'task_completed' could only be used with a task value");
            return BooleanValue.of(((ThreadValue) v).isFinished());
        });

        // lazy cause expr is evaluated in the same type
        expression.addLazyFunction("synchronize", (c, t, lv) ->
        {
            if (lv.size() == 0) throw new InternalExpressionException("'synchronize' require at least an expression to synchronize");
            Value lockValue = Value.NULL;
            int ind = 0;
            if (lv.size() == 2)
            {
                lockValue = lv.get(0).evalValue(c);
                ind = 1;
            }
            synchronized (c.host.getLock(lockValue))
            {
                Value ret = lv.get(ind).evalValue(c, t);
                return (_c, _t) -> ret;
            }
        });

        // lazy since exception expression is very conditional
        expression.addLazyFunction("sleep", (c, t, lv) ->
        {
            long time = lv.isEmpty()?0L:NumericValue.asNumber(lv.get(0).evalValue(c)).getLong();
            boolean interrupted = false;
            try
            {
                if (Thread.interrupted()) interrupted = true;
                if (time > 0) Thread.sleep(time);
                Thread.yield();
            }
            catch (InterruptedException ignored)
            {
                interrupted = true;
            }
            if (interrupted)
            {
                Value exceptionally = Value.NULL;
                if (lv.size() > 1)
                {
                    exceptionally = lv.get(1).evalValue(c);
                }
                throw new ExitStatement(exceptionally);
            }
            return (cc, tt) -> new NumericValue(time); // pass through for variables
        });
    }
}
