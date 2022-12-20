package br.com.graspfs.rcl.ig.controller;

import br.com.graspfs.rcl.ig.dto.DataSolution;
import br.com.graspfs.rcl.ig.producer.KafkaSolutionsProducer;
import br.com.graspfs.rcl.ig.service.InformationGainService;
import br.com.graspfs.rcl.ig.util.SelectionFeaturesUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.BufferedWriter;
import java.io.FileWriter;

@Controller
@RequestMapping("/ig")
@RequiredArgsConstructor
public class InformationGainController {
        private final KafkaSolutionsProducer InformationGainProducer;

    public static BufferedWriter br;
    public boolean firstTime = true;
    @PostMapping
    public ResponseEntity<DataSolution> createMessage() throws Exception {
        var data = SelectionFeaturesUtils.createData();
        try{
            InformationGainService informationGainService = new InformationGainService();
            data = informationGainService.doIG();
            int k = 0;
            br = new BufferedWriter(new FileWriter("InfoGain_METRICS_30",true));
            if(firstTime) {
                br.write("solutionFeatures;f1Score;neighborhood;iterationNeighborhood;localSearch;iterationLocalSearch;runnigTime");
                br.newLine();
                firstTime = false;
            }
            while (k <10000){
                informationGainService.GenerationSolutions(data, 5, br);
                InformationGainProducer.send(data);
                k++;
            }
            br.close();
        }catch(IllegalArgumentException ex){
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.status(HttpStatus.OK).body(data);
    }



}
