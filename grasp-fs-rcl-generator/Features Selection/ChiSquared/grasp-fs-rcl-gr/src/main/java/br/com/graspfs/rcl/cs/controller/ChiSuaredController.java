package br.com.graspfs.rcl.cs.controller;

import br.com.graspfs.rcl.cs.dto.DataSolution;
import br.com.graspfs.rcl.cs.producer.KafkaSolutionsProducer;
import br.com.graspfs.rcl.cs.service.ChiSquaredService;
import br.com.graspfs.rcl.cs.util.SelectionFeaturesUtils;
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
@RequestMapping("/gr")
@RequiredArgsConstructor
public class ChiSuaredController {
        private final KafkaSolutionsProducer ChiSquaredProducer;
    public static BufferedWriter br;
    public boolean firstTime = true;
    private final Logger logg = LoggerFactory.getLogger(ChiSuaredController.class);

    @PostMapping
    public ResponseEntity<DataSolution> createMessage() throws Exception {
        var data = SelectionFeaturesUtils.createData();
        try{
            ChiSquaredService ChiSquaredService = new ChiSquaredService();
            data = ChiSquaredService.doChiSquared();
            int k = 0;
            br = new BufferedWriter(new FileWriter("ChiSquared_METRICS_30",true));
            if(firstTime) {
                br.write("solutionFeatures;f1Score;neighborhood;iterationNeighborhood;localSearch;iterationLocalSearch;runnigTime");
                br.newLine();
                firstTime = false;
            }
            while( k < 10000){
                ChiSquaredService.GenerationSolutions(data, 5, br);
                ChiSquaredProducer.send(data);
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
