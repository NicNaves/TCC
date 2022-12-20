package br.com.graspfs.rcl.gr.controller;

import br.com.graspfs.rcl.gr.dto.DataSolution;
import br.com.graspfs.rcl.gr.producer.KafkaSolutionsProducer;
import br.com.graspfs.rcl.gr.service.GainRationService;
import br.com.graspfs.rcl.gr.util.SelectionFeaturesUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

@Controller
@RequestMapping("/gr")
@RequiredArgsConstructor
public class GainRationController {
        private final KafkaSolutionsProducer GainRationProducer;
    public static BufferedWriter br;
    public boolean firstTime = true;
    private final Logger logg = LoggerFactory.getLogger(GainRationController.class);

    @PostMapping
    public ResponseEntity<DataSolution> createMessage() throws Exception {
        var data = SelectionFeaturesUtils.createData();
        try{
            GainRationService gainRationService = new GainRationService();
            data = gainRationService.doGainRation();
            int k = 0;
            br = new BufferedWriter(new FileWriter("GainRation_METRICS_10",true));
            if(firstTime) {
                br.write("solutionFeatures;f1Score;neighborhood;iterationNeighborhood;localSearch;iterationLocalSearch;runnigTime");
                br.newLine();
                firstTime = false;
            }
            while( k < 10000){
                gainRationService.GenerationSolutions(data, 5, br);
                GainRationProducer.send(data);
                k++;
                logg.info(String.valueOf(k));
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
