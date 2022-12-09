package br.com.graspfs.rcl.su.service;

import br.com.graspfs.rcl.su.dto.DataSolution;
import br.com.graspfs.rcl.su.dto.FeatureAvaliada;
import br.com.graspfs.rcl.su.util.MachineLearningUtils;
import br.com.graspfs.rcl.su.util.SelectionFeaturesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Random;

import static br.com.graspfs.rcl.su.util.SelectionFeaturesUtils.quickSort;

public class SymmetricalUncertaintyService {
    private final Logger logg = LoggerFactory.getLogger(SymmetricalUncertaintyService.class);

    public void rankFeatures(DataSolution solution, Instances treino, int pontoCorte) throws Exception {
        Random random = new Random();
        ArrayList<Integer> rclfeatures = new ArrayList<Integer>();
        try{
            FeatureAvaliada[] allFeatures = new FeatureAvaliada[treino.numAttributes()];
            for (int i = 0; i < treino.numAttributes(); i++) {
                double peso = SelectionFeaturesUtils.calcularaSU(treino, i);
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
    public DataSolution doSU() throws Exception {
        Instances datasetTreinamento = MachineLearningUtils.lerDataset("all_in_one_cicids.arff");
        DataSolution InitialSolution = SelectionFeaturesUtils.createData();
        rankFeatures(InitialSolution, datasetTreinamento, 20);

        logg.info("RCL features: "+ InitialSolution.getRclfeatures());

        return InitialSolution;
    }

    public DataSolution GenerationSolutions(DataSolution rcl, int n){
        Random random = new Random();
        short i = 0;
        ArrayList<Integer> rclfeatures = new ArrayList<>(rcl.getRclfeatures());
        ArrayList<Integer> solutionfeatures = new ArrayList<>();
        do{
            solutionfeatures.add(rclfeatures.remove(random.nextInt(rclfeatures.size()-1)));
            i++;
        }while(i<n);
        rcl.setSolutionFeatures(solutionfeatures);
        logg.info("RCL features: "+ rcl.getRclfeatures() + " Solution Features: " + rcl.getSolutionFeatures());
        return rcl;
    }
}
