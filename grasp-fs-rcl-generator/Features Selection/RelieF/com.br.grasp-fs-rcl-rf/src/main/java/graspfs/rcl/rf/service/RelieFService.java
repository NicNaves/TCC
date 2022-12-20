package graspfs.rcl.rf.service;

import graspfs.rcl.rf.dto.DataSolution;
import graspfs.rcl.rf.dto.FeatureAvaliada;
import graspfs.rcl.rf.machinelearning.MachineLearning;
import graspfs.rcl.rf.util.MachineLearningUtils;
import graspfs.rcl.rf.util.SelectionFeaturesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.core.Instances;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import static graspfs.rcl.rf.util.SelectionFeaturesUtils.quickSort;


public class RelieFService {
    private final Logger logg = LoggerFactory.getLogger(RelieFService.class);
    public void rankFeatures(DataSolution solution, Instances treino, int pontoCorte) throws Exception {
        var random = new Random();
        ArrayList<Integer> solutionfeatures = new ArrayList<Integer>();
        ArrayList<Integer> rclfeatures = new ArrayList<Integer>();
        try{
            FeatureAvaliada[] allFeatures = new FeatureAvaliada[treino.numAttributes()];
            for (int i = 0; i < treino.numAttributes(); i++) {
                double peso = SelectionFeaturesUtils.calcularaRF(treino, i);
                allFeatures[i] = new FeatureAvaliada(peso, i + 1);
            }
            quickSort(allFeatures, 0, allFeatures.length - 1);
            FeatureAvaliada[] filter = new FeatureAvaliada[pontoCorte];
            int count = 0;

            for (int j = allFeatures.length; j > allFeatures.length - pontoCorte; j--) {
                filter[count++] = allFeatures[j - 1];
                rclfeatures.add(filter[count - 1].indiceFeature);
            }

            solution.setRclfeatures(rclfeatures);
            logg.info("RCL features: "+ solution.getRclfeatures());

        }catch (RuntimeException ex){
            logg.info("ERROR : "+ ex.getMessage());
            throw  new Exception("Erro na logica para o machine learning");
        }
    }
    public DataSolution doRelieF() throws Exception {
        Instances datasetTreinamento = MachineLearningUtils.lerDataset("ereno1ktest.arff");
        DataSolution InitialSolution = SelectionFeaturesUtils.createData();
        rankFeatures(InitialSolution, datasetTreinamento, 30);
        logg.info("RCL features: "+ InitialSolution.getRclfeatures());
        return InitialSolution;

    }

    public DataSolution GenerationSolutions(DataSolution rcl, int n, BufferedWriter br) throws Exception {
        var random = new Random();
        final var time = System.currentTimeMillis();
        float valueOfFeatures;
        short i = 0;
        ArrayList<Integer> rclfeatures = new ArrayList<>(rcl.getRclfeatures());
        ArrayList<Integer> solutionfeatures = new ArrayList<>();
        do{
            solutionfeatures.add(rclfeatures.remove(random.nextInt(rclfeatures.size()-1)));
            i++;
        }while(i<n);
        rcl.setSolutionFeatures(solutionfeatures);
        valueOfFeatures = MachineLearning.evaluateSolution(rcl.getSolutionFeatures());//sumArray(solution.getSolutionFeatures());
        rcl.setF1Score(Float.valueOf(valueOfFeatures));
        rcl.setRunnigTime(time);
        logg.info("RCL features: "+ rcl.getRclfeatures() + " Solution Features: " + rcl.getSolutionFeatures());
        br.write(rcl.getSolutionFeatures()+";"
                +rcl.getF1Score()+";"
                +rcl.getNeighborhood()+";"
                +rcl.getIterationNeighborhood()+";"
                +rcl.getLocalSearch()+";"
                +rcl.getIterationLocalSearch()+";"
                +rcl.getRunnigTime()
        );
        br.newLine();
        return rcl;
    }

}
