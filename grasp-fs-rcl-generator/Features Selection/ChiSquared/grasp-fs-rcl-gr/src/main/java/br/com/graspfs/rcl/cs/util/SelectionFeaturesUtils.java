package br.com.graspfs.rcl.cs.util;

import br.com.graspfs.rcl.cs.dto.DataSolution;
import br.com.graspfs.rcl.cs.dto.FeatureAvaliada;
import weka.attributeSelection.ChiSquaredAttributeEval;
import weka.core.Instances;

import java.io.IOException;
import java.util.ArrayList;

public class SelectionFeaturesUtils {

    public static DataSolution createData()  throws IOException {
        var data = DataSolution.builder()
                .seedId(1L)
                .rclfeatures(new ArrayList<Integer>())
                .solutionFeatures(new ArrayList<Integer>())
                .neighborhood("")
                .f1Score(0.0F)
                .runnigTime(0L)
                .iterationLocalSearch(0)
                .build();
        return data;
    }

    public static double calcularaChiSuared(Instances instances, int featureIndice) throws Exception {
        ChiSquaredAttributeEval ase = new ChiSquaredAttributeEval();
        ase.buildEvaluator(instances);
        return ase.evaluateAttribute(featureIndice);
    }

    public static void quickSort(FeatureAvaliada[] vetor, int inicio, int fim) {
        if (inicio < fim) {
            int posicaoPivo = separar(vetor, inicio, fim);
            quickSort(vetor, inicio, posicaoPivo - 1);
            quickSort(vetor, posicaoPivo + 1, fim);
        }
    }

    private static int separar(FeatureAvaliada[] vetor, int inicio, int fim) {
        FeatureAvaliada pivo = vetor[inicio];
        int i = inicio + 1, f = fim;
        while (i <= f) {
            if (vetor[i].getValorFeature() <= pivo.getValorFeature()) {
                i++;
            } else if (pivo.getValorFeature() < vetor[f].getValorFeature()) {
                f--;
            } else {
                FeatureAvaliada troca = vetor[i];
                vetor[i] = vetor[f];
                vetor[f] = troca;
                i++;
                f--;
            }
        }
        vetor[inicio] = vetor[f];
        vetor[f] = pivo;
        return f;
    }
}
