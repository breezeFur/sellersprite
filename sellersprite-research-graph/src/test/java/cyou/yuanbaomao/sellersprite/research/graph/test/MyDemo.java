package cyou.yuanbaomao.sellersprite.research.graph.test;

import com.alibaba.cloud.ai.graph.*;

import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.Optional;

import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

//@SpringBootTest
public class MyDemo {

    String name = "name";
    String originName = "originName";
    String age = "age";
    String node1 = "node1";
    String node2 = "node2";

    @Test
    public void test1() throws GraphStateException {


        StateGraph stateGraph = new StateGraph("test", () -> Map.of(name, new ReplaceStrategy()
                , age, new ReplaceStrategy(),originName,new ReplaceStrategy()));

        stateGraph.addNode(node1, node_async(this::nameNodeZhangsan));
        stateGraph.addNode(node2, node_async(this::ageNode10));
        stateGraph.addEdge(StateGraph.START, node1);
        stateGraph.addEdge(node1, node2);
        stateGraph.addEdge(node2,StateGraph.END);


        CompiledGraph compile = stateGraph.compile();
        OverAllState allState = compile.invoke(Map.of(name, "狗子")).orElseThrow();
        for (Map.Entry<String, Object> stringObjectEntry : allState.data().entrySet()) {
            System.out.println(stringObjectEntry.toString());
        }
    }


    public Map<String,Object> nameNodeZhangsan(OverAllState agentState){
        String nameOrgin = agentState.value(name, String.class).orElseThrow(() -> new RuntimeException("没有初始姓名"));
        return Map.of(name,"张三",originName,nameOrgin);
    }

    public Map<String,Object> ageNode10(OverAllState agentstae){
        return Map.of(age,100);
    }



    String SCORE = "score";
    String REPAIR_COUNT = "repairCount";
    String DECISION = "decision";
    String RESULT = "result";

    String APPROVED = "APPROVED";
    String REJECT = "REJECT";
    String REPAIR = "REPAIR";


    String INSPECT_NODE = "inspect";
    String FINISH_NODE = "finish";
    String REPAIR_NODE = "repair";


    public Map<String, KeyStrategy> strategy1(){
        return Map.of(SCORE,new ReplaceStrategy(),REPAIR_COUNT,new ReplaceStrategy(),DECISION,new ReplaceStrategy(),RESULT,new ReplaceStrategy());
    }

    public  Map<String, Object> inspect(OverAllState state){
        Integer score = state.value(SCORE, Integer.class).orElseThrow();
        if (score>=60) {
            System.out.println("分数: "+score + "成功结束");
            return  Map.of(DECISION,APPROVED);
        }
        Integer repairCount = state.value(REPAIR_COUNT, Integer.class).orElse(0);

        if (repairCount>=3) {
            System.out.println("分数: "+score +"修复次数: "+repairCount +" 失败结束");
            return  Map.of(DECISION,REJECT);
        }
        System.out.println("分数: "+score +"修复次数: "+repairCount +" 还要继续修复");
        return  Map.of(DECISION,REPAIR);
    }

    public  Map<String, Object> repair(OverAllState state){
        Integer score = state.value(SCORE, Integer.class).orElseThrow();
        Integer repairCount = state.value(REPAIR_COUNT, Integer.class).orElse(0);
        System.out.println("第 "+(repairCount+1) +" 次修复,修复后分数: "+(score+20));
        return Map.of(SCORE,score+20,REPAIR_COUNT,repairCount+1);
    }

    public  Map<String, Object> finish(OverAllState state){
        String s = state.value(DECISION, String.class).orElseThrow();
        System.out.println("正式结束 状态 "+s);
        if (s.equals(APPROVED)) {
            return Map.of(RESULT,APPROVED);
        }
        return Map.of(RESULT,REJECT);
    }

    public String cd(OverAllState state){
        return state.value(DECISION, String.class).orElseThrow();
    }

    @Test
    void test2() throws GraphStateException {
       int score = -100 ;


        StateGraph stateGraph = new StateGraph("资料审核", this::strategy1);
        stateGraph.addNode(INSPECT_NODE,node_async(this::inspect));
        stateGraph.addNode(FINISH_NODE,node_async(this::finish));
        stateGraph.addNode(REPAIR_NODE,node_async(this::repair));



        stateGraph.addEdge(StateGraph.START,INSPECT_NODE);
        stateGraph.addConditionalEdges(INSPECT_NODE,edge_async(this::cd),Map.of(APPROVED,FINISH_NODE,REJECT,FINISH_NODE,REPAIR,REPAIR_NODE));
        stateGraph.addEdge(REPAIR_NODE,INSPECT_NODE);
        stateGraph.addEdge(FINISH_NODE,StateGraph.END);


        CompiledGraph compile = stateGraph.compile();
        OverAllState allState = compile.invoke(Map.of(SCORE, score)).orElseThrow();



    }





    @Test
    void streamDemo() throws GraphStateException {
        int score = -100 ;


        StateGraph stateGraph = new StateGraph("资料审核", this::strategy1);
        stateGraph.addNode(INSPECT_NODE,node_async(this::inspect));
        stateGraph.addNode(FINISH_NODE,node_async(this::finish));
        stateGraph.addNode(REPAIR_NODE,node_async(this::repair));



        stateGraph.addEdge(StateGraph.START,INSPECT_NODE);
        stateGraph.addConditionalEdges(INSPECT_NODE,edge_async(this::cd),Map.of(APPROVED,FINISH_NODE,REJECT,FINISH_NODE,REPAIR,REPAIR_NODE));
        stateGraph.addEdge(REPAIR_NODE,INSPECT_NODE);
        stateGraph.addEdge(FINISH_NODE,StateGraph.END);


        CompiledGraph compile = stateGraph.compile();
//        OverAllState allState = compile.invoke(Map.of(SCORE, score)).orElseThrow();

//        new Array
        compile.stream(Map.of(SCORE, score))
                .doOnNext(this::consumeer)
                .blockLast();


    }

    public void  consumeer(NodeOutput nodeOutput){
        System.out.println(nodeOutput.node());
        System.out.println(nodeOutput.state().data());

    }







}
