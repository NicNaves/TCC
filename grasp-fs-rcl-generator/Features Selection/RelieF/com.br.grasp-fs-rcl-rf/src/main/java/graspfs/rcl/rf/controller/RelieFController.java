package graspfs.rcl.rf.controller;

import graspfs.rcl.rf.dto.DataSolution;
import graspfs.rcl.rf.producer.KafkaSolutionsProducer;
import graspfs.rcl.rf.service.RelieFService;
import graspfs.rcl.rf.util.SelectionFeaturesUtils;
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
@RequestMapping("/rf")
@RequiredArgsConstructor
public class RelieFController {
        private final KafkaSolutionsProducer RelieFProducer;

    public static BufferedWriter br;
    public boolean firstTime = true;
    private final Logger logg = LoggerFactory.getLogger(RelieFController.class);
    @PostMapping
    public ResponseEntity<DataSolution> createMessage() throws Exception {
        var data = SelectionFeaturesUtils.createData();
        try{
            RelieFService relieFService = new RelieFService();
            data = relieFService.doRelieF();
            int k = 0;
            br = new BufferedWriter(new FileWriter("RelieF_METRICS_30",true));
            if(firstTime) {
                br.write("solutionFeatures;f1Score;neighborhood;iterationNeighborhood;localSearch;iterationLocalSearch;runnigTime");
                br.newLine();
                firstTime = false;
            }
            while( k < 10000){
                relieFService.GenerationSolutions(data, 5, br);
                RelieFProducer.send(data);
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
