package br.com.graspfs.rcl.ig.service;

import br.com.graspfs.rcl.ig.dto.DataSolution;
import br.com.graspfs.rcl.ig.dto.FeatureAvaliada;
import br.com.graspfs.rcl.ig.machinelearning.MachineLearning;
import br.com.graspfs.rcl.ig.util.MachineLearningUtils;
import br.com.graspfs.rcl.ig.util.SelectionFeaturesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import weka.core.Instances;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Random;
import static br.com.graspfs.rcl.ig.util.SelectionFeaturesUtils.quickSort;

public class InformationGainService {
    private final Logger logg = LoggerFactory.getLogger(InformationGainService.class);

    public void rankFeatures(DataSolution solution, Instances treino, int pontoCorte) throws Exception {
        var random = new Random();
        ArrayList<Integer> rclfeatures = new ArrayList<Integer>();
        try{
            FeatureAvaliada[] allFeatures = new FeatureAvaliada[treino.numAttributes()];
            for (int i = 0; i < treino.numAttributes(); i++) {
                double peso = SelectionFeaturesUtils.calcularaInfoGain(treino, i);
                allFeatures[i] = new FeatureAvaliada(peso, i + 1);
            }
            quickSort(allFeatures, 0, allFeatures.length - 1);
            FeatureAvaliada[] filter = new FeatureAvaliada[pontoCorte];
            int count = 0;
            // int posRank = 0;
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
    public DataSolution doIG() throws Exception {
        Instances datasetTreinamento = MachineLearningUtils.lerDataset("ereno1ktrain.arff");
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
