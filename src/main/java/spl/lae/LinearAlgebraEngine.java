package spl.lae;

import parser.*;
import memory.*;
import scheduling.*;

import java.util.ArrayList;
import java.util.List;

public class LinearAlgebraEngine {

    private SharedMatrix leftMatrix = new SharedMatrix();
    private SharedMatrix rightMatrix = new SharedMatrix();
    private TiredExecutor executor;

    public LinearAlgebraEngine(int numThreads) {
        // create executor with given thread count 
        this.executor = new TiredExecutor(numThreads);
    }

    // helpers
    public void initiateShutdown() {
        try{
            executor.shutdown();
        }
        catch(InterruptedException e){
           throw new IllegalStateException("Executor failed to shutdown");
        }
    }

    public ComputationNode run(ComputationNode computationRoot) {
        // resolve computation tree step by step until final matrix is produced
        computationRoot.associativeNesting(); 
        while (computationRoot.getNodeType() != ComputationNodeType.MATRIX) { 
            ComputationNode activeNode = computationRoot.findResolvable();
            loadAndCompute(activeNode);
        }

        initiateShutdown();

        return computationRoot;
    }

    public void loadAndCompute(ComputationNode node) {
        // load operand matrices
        // create compute tasks & submit tasks to executor
        ComputationNodeType type = node.getNodeType();
        List<ComputationNode> children = node.getChildren();
        List<Runnable> tasks = null;

        if (type == ComputationNodeType.ADD) {
            leftMatrix.loadRowMajor(children.get(0).getMatrix());
            rightMatrix.loadRowMajor(children.get(1).getMatrix());

            if (leftMatrix.length() != rightMatrix.length() || 
                leftMatrix.get(0).length() != rightMatrix.get(0).length()) {
                initiateShutdown();
                throw new IllegalArgumentException("Error: cannot add matrices of different dimensions.");
            }
            
            tasks = createAddTasks();
        } 
        else if (type == ComputationNodeType.MULTIPLY) {
            leftMatrix.loadRowMajor(children.get(0).getMatrix());
            rightMatrix.loadRowMajor(children.get(1).getMatrix());

            if (leftMatrix.get(0).length() != rightMatrix.length()) {
                initiateShutdown();
                throw new IllegalArgumentException("Error: columns of left matrix must match rows of right matrix.");
            }
            tasks = createMultiplyTasks();
        } 
        else if (type == ComputationNodeType.NEGATE) {
            leftMatrix.loadRowMajor(children.get(0).getMatrix());
            tasks = createNegateTasks();
        } 
        else if (type == ComputationNodeType.TRANSPOSE) {
            leftMatrix.loadRowMajor(children.get(0).getMatrix());
            tasks = createTransposeTasks();
        }

        if (tasks != null) {
            executor.submitAll(tasks);
        }

        node.resolve(leftMatrix.readRowMajor());
        }

    public List<Runnable> createAddTasks() {
        // return tasks that perform row-wise addition
        List<Runnable> tasks = new java.util.ArrayList<>();
        for (int i = 0; i < leftMatrix.length(); i++){ 
            final int row = i;
            tasks.add(() -> leftMatrix.get(row).add(rightMatrix.get(row)));
        }
        return tasks;
    }

    public List<Runnable> createMultiplyTasks() {
        // return tasks that perform row × matrix multiplication
        List<Runnable> tasks = new ArrayList<>();
        for(int i=0; i<leftMatrix.length();i++){
            final int row = i;
            tasks.add(()-> leftMatrix.get(row).vecMatMul(rightMatrix));
        }
        return tasks;
    }

    public List<Runnable> createNegateTasks() {
        // return tasks that negate rows
    List<Runnable> tasks = new java.util.ArrayList<>();
    for (int i = 0; i < leftMatrix.length(); i++) {
        final int row = i;
        tasks.add(() -> leftMatrix.get(row).negate());
    }
    return tasks;
    }

    public List<Runnable> createTransposeTasks() {
        // return tasks that transpose rows
        List<Runnable> tasks = new java.util.ArrayList<>();
        for (int i = 0; i < leftMatrix.length(); i++) {
            final int row = i;
            tasks.add(() -> leftMatrix.get(row).transpose());
        }
        return tasks;
    }

    public String getWorkerReport() {
        // return summary of worker activity
        return executor.getWorkerReport();
    }
}
