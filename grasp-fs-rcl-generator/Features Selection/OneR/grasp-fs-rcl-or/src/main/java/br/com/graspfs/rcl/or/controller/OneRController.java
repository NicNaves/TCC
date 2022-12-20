package br.com.graspfs.rcl.or.controller;

import br.com.graspfs.rcl.or.dto.DataSolution;
import br.com.graspfs.rcl.or.producer.KafkaSolutionsProducer;
import br.com.graspfs.rcl.or.service.OneRService;
import br.com.graspfs.rcl.or.util.SelectionFeaturesUtils;
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

@Controller
@RequestMapping("/or")
@RequiredArgsConstructor
public class OneRController {
    private final KafkaSolutionsProducer OneRProducer;

    public static BufferedWriter br;
    public boolean firstTime = true;
    private final Logger logg = LoggerFactory.getLogger(OneRController.class);
    @PostMapping
    public ResponseEntity<DataSolution> createMessage() throws Exception {
        DataSolution data = SelectionFeaturesUtils.createData();
        try{
            OneRService oneRService = new OneRService();
            data = oneRService.doOR();
            int k = 0;
            br = new BufferedWriter(new FileWriter("OneR_METRICS_10",true));
            if(firstTime) {
                br.write("solutionFeatures;f1Score;neighborhood;iterationNeighborhood;localSearch;iterationLocalSearch;runnigTime");
                br.newLine();
                firstTime = false;
            }
            while (k <10000) {
                oneRService.GenerationSolutions(data,5, br);
                OneRProducer.send(data);
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
